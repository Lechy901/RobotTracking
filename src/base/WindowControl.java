package base;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.circle;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.line;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;
import static org.bytedeco.javacpp.opencv_imgproc.warpPerspective;
import static org.bytedeco.javacpp.opencv_videoio.CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.javacpp.opencv_videoio.CAP_PROP_FRAME_WIDTH;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;

import util.MainProgramWindow;
import util.Pair;
import util.RobotPositions;
import util.StaticUtils;
import util.WindowStage;

/**
 * A class which controls the content shown in the main window
 * 
 * @author Adam Lechovsky
 *
 */
public class WindowControl {

    private VideoCapture vc;
    private WindowStage windowStage;
    private RobotTracker rt;
    private Mat transformation;
    private ImageGraph ig;
    private RobotArray ra;
    private double topAndBotLineRatio;
    
    private Mat curFrameLeft, curFrameRight;
    private int xSize, ySize;
    private MainProgramWindow mpw;
    private int videoCapture, lineWidth, robotsNumber, pointGroupDistance;
    private boolean paused;

    /**
     * A constructor, already opens the window
     * @param lineWidth line width
     * @param robotsNumber robots number
     * @param pointGroupDistance point group distance
     */
    public WindowControl(int videoCapture, int lineWidth, int robotsNumber, int pointGroupDistance) {
        this.lineWidth = lineWidth;
        this.robotsNumber = robotsNumber;
        this.pointGroupDistance = pointGroupDistance;
        this.videoCapture = videoCapture;
        ra = null;
        mpw = new MainProgramWindow("test", this, lineWidth, robotsNumber, pointGroupDistance);
        windowStage = WindowStage.NONE;
        rt = new RobotTracker("resources/06/cascade.xml");
        resetRightFrame();
    }

    /**
     * Start the VideoCapture and paper recognition
     */
    public void start() {
        curFrameLeft = new Mat();
        vc = new VideoCapture();
        vc.open(videoCapture);
        if (!vc.isOpened()) {
            System.err.println("VideoCapture is not opened");
            vc.close();
            return;
        }

        paused = false;
        windowStage = WindowStage.PAPER_SEARCH;
        while(true) {
            if (paused) {
                Thread.yield();
                continue;
            }

            vc.read(curFrameLeft);
            if (curFrameLeft.empty()) {
                System.err.println("empty frame grabbed");
                continue;
            }
            
            xSize = curFrameLeft.cols();
            ySize = curFrameLeft.rows();
            
            if(windowStage == WindowStage.PAPER_SEARCH) {
                // find all squares in the image
                List<List<Point>> squares = new ArrayList<List<Point>>();
                StaticUtils.findSquares(curFrameLeft, squares);
                List<Point> square = null;
                try {
                    // find the biggest square
                    square = StaticUtils.getBiggestSquare(squares);
                } catch (IllegalArgumentException ex) {
                    mpw.showImage(curFrameLeft, true, true);
                    continue;
                }

                // draw the square
                for(int i = 0; i < square.size(); i++) {
                    line(curFrameLeft, square.get(i), square.get((i + 1) % square.size()), new Scalar(0, 0, 255, 255), 3, 8, 0);
                }
                mpw.showImage(curFrameLeft, true, true);

                List<Point> clockwise = StaticUtils.sortSquareClockWise(square);
                topAndBotLineRatio = StaticUtils.dist(clockwise.get(0), clockwise.get(1)) / StaticUtils.dist(clockwise.get(2), clockwise.get(3));
            }

            if(windowStage == WindowStage.GRAPH_SEARCH) {
                if (transformation == null) {
                    continue;
                }

                // warp perspective and recognize the graph lines in the image
                Mat warped = new Mat();
                warpPerspective(curFrameLeft, warped, transformation, new Size(curFrameLeft.cols(), curFrameLeft.rows()));
                List<Pair<Point, Point>> lines = StaticUtils.getLines(warped, lineWidth / 2);
                List<Pair<Point, Point>> horizontal = new ArrayList<Pair<Point, Point>>();
                List<Pair<Point, Point>> vertical = new ArrayList<Pair<Point, Point>>();
                StaticUtils.groupLines(lines, horizontal, vertical, 40);
                /*
				for (Pair<Point, Point> line : horizontal) {
					line(warped, line.first, line.second, new Scalar(255, 0, 0, 255), 3, 8, 0);
				}

				for (Pair<Point, Point> line : vertical) {
					line(warped, line.first, line.second, new Scalar(0, 255, 0, 255), 3, 8, 0);
				}
                 */
                ig = new ImageGraph(horizontal, vertical, warped, pointGroupDistance);

                // draw the graph
                for (Point vertex : ig.vertices) {
                    circle(warped, vertex, 8, new Scalar(0, 255, 0, 255));
                }

                for (Pair<Point, Point> edge : ig.edges) {
                    line(warped, edge.first, edge.second, new Scalar(255, 0, 0, 255), 3, 8, 0);
                }

                mpw.showImage(warped, true, true);
            }

            if(windowStage == WindowStage.ROBOT_TRACKING) {
                if (ra == null) {
                    continue;
                }

                // reset the right image and draw the graph again
                curFrameRight = new Mat(ySize, xSize, CV_8UC3, new Scalar(0));
                for (Pair<Point, Point> edge : ig.edges) {
                    line(curFrameRight, edge.first, edge.second, new Scalar(255, 0, 0, 255), 3, 8, 0);
                }

                Mat gray = new Mat(), warped = new Mat();
                warpPerspective(curFrameLeft, warped, transformation, new Size(curFrameLeft.cols(), curFrameLeft.rows()));

                // find robots and draw them into the right image
                cvtColor(warped, gray, CV_BGR2GRAY);
                if (ra == null) {
                    continue;
                }
                RobotPositions rp = ra.findRobotsInImage(gray, topAndBotLineRatio);
                
                if (rp != null) {
                    for(Rect rr : rp.boundaries) {
                        rectangle(warped, rr, new Scalar(0, 0, 255, 255));
                    }
                    for(Point center : rp.centers) {
                        circle(warped, center, 8, new Scalar(255, 0, 0, 255));
                    }
                    for(Point positionInGraph : rp.positionsInGraph) {
                        circle(curFrameRight, positionInGraph, 8, new Scalar(0, 255, 255, 255));
                    }
                    
                    mpw.showImage(warped, true, false);
                    mpw.showImage(curFrameRight, false, true);
                } else {
                    mpw.showImage(warped, true, true);
                }

            }
        }
    }

    /**
     * Advance to the next stage
     */
    public void nextStage() {
        if (windowStage == WindowStage.GRAPH_SEARCH) {
            if (ig == null) {
                resetRightFrame();
                return;
            }

            Mat warped = new Mat();
            warpPerspective(curFrameLeft, warped, transformation, new Size(curFrameLeft.cols(), curFrameLeft.rows()));

            List<Pair<Point, Point>> lines = StaticUtils.getLines(warped, lineWidth / 2);
            List<Pair<Point, Point>> horizontal = new ArrayList<Pair<Point, Point>>();
            List<Pair<Point, Point>> vertical = new ArrayList<Pair<Point, Point>>();
            StaticUtils.groupLines(lines, horizontal, vertical, 40);

            ig = new ImageGraph(horizontal, vertical, warped, pointGroupDistance);
            for (Point vertex : ig.vertices) {
                circle(curFrameRight, vertex, 8, new Scalar(0, 255, 0, 255));
            }

            for (Pair<Point, Point> edge : ig.edges) {
                line(curFrameRight, edge.first, edge.second, new Scalar(255, 0, 0, 255), 3, 8, 0);
            }
            
            ra = new RobotArray(robotsNumber, rt, ig);

            mpw.showImage(curFrameRight, false, true);
        }

        if (windowStage == WindowStage.PAPER_SEARCH) {
            transformation = StaticUtils.getPaperTransformation(curFrameLeft);
            if (transformation == null) {
                return;
            }
            ig = null;
        }
        
        if (windowStage == WindowStage.ROBOT_TRACKING) {
            ra.printAllRobotHistories();
        }

        windowStage = windowStage.next();
    }

    /**
     * Go to previous stage
     */
    public void prevStage() {
        if (windowStage == WindowStage.PAPER_SEARCH) {
            ig = null;
        } else if (windowStage == WindowStage.GRAPH_SEARCH) {
            transformation = null;
        } else if (windowStage == WindowStage.ROBOT_TRACKING) {
            ra = null;
        }
        windowStage = windowStage.prev();
        resetRightFrame();
    }
    
    public void flipPaused() {
        paused = !paused;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setRobotsNumber(int robotsNumber) {
        this.robotsNumber = robotsNumber;
    }

    public void setPointGroupDistance(int pointGroupDistance) {
        this.pointGroupDistance = pointGroupDistance;
    }

    private void resetRightFrame() {
        curFrameRight = new Mat(480, 640, CV_8UC3, new Scalar(0));
        mpw.showImage(curFrameRight, false, true);
    }
}

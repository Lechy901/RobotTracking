package base;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.circle;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.line;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;
import static org.bytedeco.javacpp.opencv_imgproc.warpPerspective;

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
import util.StaticUtils;

/**
 * A class which controls the content shown in the main window
 * 
 * @author ALechovsky
 *
 */
public class WindowControl {
	
	private VideoCapture vc;
	private WindowStage windowStage;
	private RobotTracker rt;
	private Mat transformation;
	private ImageGraph ig;
	private Mat curFrameLeft, curFrameRight;
	private MainProgramWindow mpw;
	private int lineWidth, robotsNumber, pointGroupDistance;
	
	/**
	 * A constructor, already opens the window
	 * @param lineWidth
	 * @param robotsNumber
	 * @param pointGroupDistance
	 */
	public WindowControl(int lineWidth, int robotsNumber, int pointGroupDistance) {
		this.lineWidth = lineWidth;
		this.robotsNumber = robotsNumber;
		this.pointGroupDistance = pointGroupDistance;
		mpw = new MainProgramWindow("test", this, lineWidth, robotsNumber, pointGroupDistance);
		windowStage = WindowStage.NONE;
		rt = new RobotTracker("data/06/cascade.xml");
		resetRightFrame();
	}
	
	/**
	 * Start the VideoCapture and paper recognition
	 */
	public void start() {
		curFrameLeft = new Mat();
		vc = new VideoCapture();
		vc.open(1);
		if (!vc.isOpened()) {
			System.err.println("VideoCapture is not opened");
			vc.close();
			return;
		}
		
		windowStage = WindowStage.PAPER_SEARCH;
		while(true) {
			if(windowStage == WindowStage.PAPER_SEARCH) {
				vc.read(curFrameLeft);
				if (curFrameLeft.empty()) {
					System.out.println("empty frame grabbed wtf");
					continue;
				}
				// find all squares in the image
				List<List<Point>> squares = new ArrayList<List<Point>>();
				StaticUtils.findSquares(curFrameLeft, squares);
				List<Point> square = null;
				try {
					// find the biggest square
					square = StaticUtils.getBiggestSquare(squares);
				} catch (IllegalArgumentException ex) {
					mpw.showImage(curFrameLeft, true);
					continue;
				}
				
				// draw the square
				for(int i = 0; i < square.size(); i++) {
					line(curFrameLeft, square.get(i), square.get((i + 1) % square.size()), new Scalar(0, 0, 255, 255), 3, 8, 0);
				}
				mpw.showImage(curFrameLeft, true);
			}
			
			if(windowStage == WindowStage.GRAPH_SEARCH) {
				vc.read(curFrameLeft);
				if (curFrameLeft.empty()) {
					System.err.println("empty frame grabbed");
					continue;
				}
				if (transformation == null)
					continue;
				
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
				
				mpw.showImage(warped, true);
			}
			
			if(windowStage == WindowStage.ROBOT_TRACKING) {
				vc.read(curFrameLeft);
				if (curFrameLeft.empty()) {
					System.out.println("empty frame grabbed wtf");
					continue;
				}
				
				// reset the right image and draw the graph again
				curFrameRight = new Mat(480, 640, CV_8UC3, new Scalar(0));
				for (Pair<Point, Point> edge : ig.edges) {
					line(curFrameRight, edge.first, edge.second, new Scalar(255, 0, 0, 255), 3, 8, 0);
				}
				
				Mat gray = new Mat(), warped = new Mat();
				warpPerspective(curFrameLeft, warped, transformation, new Size(curFrameLeft.cols(), curFrameLeft.rows()));
				
				// find robots and draw them into the right image
				cvtColor(warped, gray, CV_BGR2GRAY);
				Rect[] r = rt.findRobots(gray);
				for(Rect rr : r) {
					rectangle(warped, rr, new Scalar(0, 0, 255, 255));
					Point center = new Point(rr.x() + rr.width() / 2, rr.y() + rr.height() / 2);
					Point robotPosition = ig.getRobotPositionInGraph(center);
					circle(curFrameRight, robotPosition, 8, new Scalar(0, 255, 255, 255));
				}
				mpw.showImage(warped, true);
				mpw.showImage(curFrameRight, false);
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
			
			mpw.showImage(curFrameRight, false);
		}
		
		if (windowStage == WindowStage.PAPER_SEARCH) {
			transformation = StaticUtils.getPaperTransformation(curFrameLeft);
			if (transformation == null)
				return;
			ig = null;
		}
		
		windowStage = windowStage.next();
	}
	
	/**
	 * Go to previous stage
	 */
	public void prevStage() {
		if (windowStage == WindowStage.PAPER_SEARCH) {
			ig = null;
		}
		if (windowStage == WindowStage.GRAPH_SEARCH) {
			transformation = null;
		}
		windowStage = windowStage.prev();
		resetRightFrame();
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
		mpw.showImage(curFrameRight, false);
	}
}

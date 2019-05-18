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

public class WindowControl {
	
	private VideoCapture vc;
	private WindowStage windowStage;
	private RobotTracker rt;
	private Mat transformation;
	private ImageGraph ig;
	private Mat curFrameLeft, curFrameRight;
	private MainProgramWindow mpw;
	
	public WindowControl() {
		mpw = new MainProgramWindow("test", this);
		windowStage = WindowStage.NONE;
		rt = new RobotTracker("data/06/cascade.xml");
		resetRightFrame();
	}
	
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
				
				List<List<Point>> squares = new ArrayList<List<Point>>();
				StaticUtils.findSquares(curFrameLeft, squares);
				List<Point> square = null;
				try {
					square = StaticUtils.getBiggestSquare(squares);
				} catch (IllegalArgumentException ex) {
					mpw.showImage(curFrameLeft, true);
					continue;
				}
				
				for(int i = 0; i < square.size(); i++) {
					line(curFrameLeft, square.get(i), square.get((i + 1) % square.size()), new Scalar(0, 0, 255, 255), 3, 8, 0);
				}
				mpw.showImage(curFrameLeft, true);
			}
			
			if(windowStage == WindowStage.GRAPH_SEARCH) {
				vc.read(curFrameLeft);
				if (curFrameLeft.empty()) {
					System.out.println("empty frame grabbed");
					continue;
				}
				Mat warped = new Mat();
				warpPerspective(curFrameLeft, warped, transformation, new Size(curFrameLeft.cols(), curFrameLeft.rows()));
				List<Pair<Point, Point>> lines = StaticUtils.getLines(warped);
				List<Pair<Point, Point>> horizontal = new ArrayList<Pair<Point, Point>>();
				List<Pair<Point, Point>> vertical = new ArrayList<Pair<Point, Point>>();
				StaticUtils.groupLines(lines, horizontal, vertical, 40);
				
				ig = new ImageGraph(horizontal, vertical, warped);
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
	
				Mat gray = new Mat(), warped = new Mat();
				warpPerspective(curFrameLeft, warped, transformation, new Size(curFrameLeft.cols(), curFrameLeft.rows()));
				
				cvtColor(warped, gray, CV_BGR2GRAY);
				Rect[] r = rt.findRobots(gray);
				for(Rect rr : r) {
					rectangle(warped, rr, new Scalar(0, 0, 255, 255));
					Point center = new Point(rr.x() + rr.width() / 2, rr.y() + rr.height() / 2);
					Pair<Point, Point> robotVertices = ig.getRobotPositionInGraph(center);
					circle(warped, robotVertices.first, 8, new Scalar(0, 255, 255, 255));
					circle(warped, robotVertices.second, 8, new Scalar(0, 255, 255, 255));
				}
				mpw.showImage(warped, true);
				
			}
		}
	}
	
	public void nextStage() {
		if (windowStage == WindowStage.GRAPH_SEARCH) {
			if (ig == null) {
				resetRightFrame();
				return;
			}
			
			Mat warped = new Mat();
			warpPerspective(curFrameLeft, warped, transformation, new Size(curFrameLeft.cols(), curFrameLeft.rows()));
			
			List<Pair<Point, Point>> lines = StaticUtils.getLines(warped);
			List<Pair<Point, Point>> horizontal = new ArrayList<Pair<Point, Point>>();
			List<Pair<Point, Point>> vertical = new ArrayList<Pair<Point, Point>>();
			StaticUtils.groupLines(lines, horizontal, vertical, 40);

			ig = new ImageGraph(horizontal, vertical, warped);
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
	
	public void prevStage() {
		windowStage = windowStage.prev();
	}
	
	private void resetRightFrame() {
		curFrameRight = new Mat(480, 640, CV_8UC3, new Scalar(0));
		mpw.showImage(curFrameRight, false);
	}
}

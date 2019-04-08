package main;

import base.*;
import training_utils.ImageUtils;
import training_utils.TestImageCapturer;
import util.ImageWindow;
import util.Pair;
import util.StaticUtils;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_videoio.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Size;

public class TrackingMain {

	public static void main(String[] args) {
		boolean old = false;
		
		if(old) {
			oldMain();
			return;
		}
		
		newMain();
		
	}
	
	private static void newMain() {
		
		Mat frame = new Mat();
		VideoCapture vc = new VideoCapture();
		vc.open(0);
		if (!vc.isOpened()) {
			System.err.println("VideoCapture is not opened");
			vc.close();
			return;
		}
		
		ImageWindow d = new ImageWindow("test");
		while(!d.isSpacePressed()) {
			vc.read(frame);
			if (frame.empty()) {
				System.out.println("empty frame grabbed wtf");
				continue;
			}
			
			List<List<Point>> squares = new ArrayList<List<Point>>();
			StaticUtils.findSquares(frame, squares);
			List<Point> square = null;
			try {
				square = StaticUtils.getBiggestSquare(squares);
			} catch (IllegalArgumentException ex) {
				d.showImage(frame);
				continue;
			}
			
			for(int i = 0; i < square.size(); i++) {
				line(frame, square.get(i), square.get((i + 1) % square.size()), new Scalar(0, 0, 255, 255), 3, 8, 0);
			}
			d.showImage(frame);
		}
		
		RobotTracker rt = new RobotTracker("data/06/cascade.xml");
		Mat transformation = StaticUtils.getPaperTransformation(frame);
		ImageGraph ig = null;
		
		long iter = 0;
		while(iter++ < 10 || !d.isSpacePressed()) {
			vc.read(frame);
			if (frame.empty()) {
				System.out.println("empty frame grabbed");
				continue;
			}
			Mat warped = new Mat();
			warpPerspective(frame, warped, transformation, new Size(frame.cols(), frame.rows()));
			List<Pair<Point, Point>> lines = StaticUtils.getLines(warped);
			List<Pair<Point, Point>> horizontal = new ArrayList<Pair<Point, Point>>();
			List<Pair<Point, Point>> vertical = new ArrayList<Pair<Point, Point>>();
			StaticUtils.groupLines(lines, horizontal, vertical, 40);
			for (Pair<Point, Point> line : horizontal) {
				line(warped, line.first, line.second, new Scalar(0, 0, 255, 255), 3, 8, 0);
			}
			for (Pair<Point, Point> line : vertical) {
				line(warped, line.first, line.second, new Scalar(255, 0, 0, 255), 3, 8, 0);
			}
			
			ig = new ImageGraph(horizontal, vertical);
			for (Point vertex : ig.vertices) {
				circle(warped, vertex, 8, new Scalar(0, 255, 0, 255));
			}
			
			d.showImage(warped);
		}		
		
		if (ig == null) {
			vc.close();
			return;
		}
		
		iter = 0;
		while(iter++ < 10 || !d.isSpacePressed()) {
			vc.read(frame);
			if (frame.empty()) {
				System.out.println("empty frame grabbed wtf");
				continue;
			}

			Mat gray = new Mat(), warped = new Mat();
			warpPerspective(frame, warped, transformation, new Size(frame.cols(), frame.rows()));
			
			cvtColor(warped, gray, CV_BGR2GRAY);
			Rect[] r = rt.findRobots(gray);
			for(Rect rr : r) {
				rectangle(warped, rr, new Scalar(0, 0, 255, 255));
				Point center = new Point(rr.x() + rr.width() / 2, rr.y() + rr.height() / 2);
				Pair<Point, Point> robotVertices = ig.getRobotPositionInGraph(center);
				circle(warped, robotVertices.first, 8, new Scalar(0, 255, 255, 255));
				circle(warped, robotVertices.second, 8, new Scalar(0, 255, 255, 255));
			}
			d.showImage(warped);
			
		}
		
	}

	private static void oldMain() {
		boolean captureTrainImages = false;
		boolean generateBgFile = false;
		boolean convertImages = false;
		boolean downloadImages = false;
		boolean trackTest = true;
				
		boolean loadFromStorage = false;
		System.out.println("hello");
		
		if (captureTrainImages) {
			TestImageCapturer tic = new TestImageCapturer("bg");
			tic.start();
			return;
		}
		if (generateBgFile) {
			ImageUtils.generateBgFile("bg", "bg2.txt");
			return;
		}
		if (convertImages) {
			ImageUtils.convertImagesFromStorage("my_img");
			return;
		}
		if (downloadImages) {
			ImageUtils.loadAndConvertImagesFromURLs("paper-images");
			return;
		}
		
		Mat frame = new Mat();
		VideoCapture vc = null;
		
		if (loadFromStorage) {
			frame = imread("testframe.png");
		} else {
			vc = new VideoCapture();
			vc.open(0);
			if (!vc.isOpened()) {
				System.err.println("VideoCapture is not opened");
				vc.close();
				return;
			}
			System.out.println("VideoCapture opened successfully");
			System.out.println("Starting to grab frames");
			ImageWindow d = new ImageWindow("test");
			for (int i = 0; i < 60; i++) {
				vc.read(frame);
				if (frame.empty()) {
					System.out.println("empty frame grabbed wtf");
					continue;
				}
				System.out.println("frame grabbed successfully");
				d.showImage(frame);
				//break;
			}
		}
		
		if (trackTest) {
			RobotTracker rt = new RobotTracker("data/cascade.xml");
			Mat gray = new Mat(), normalized = new Mat();
			normalize(frame, normalized);
			cvtColor(frame, gray, CV_BGR2GRAY);
			StaticUtils.display(gray, "norm");
			Rect[] r = rt.findRobots(gray);
			for(Rect rr : r) {
				rectangle(frame, rr, new Scalar(255, 255, 255, 255));
				System.out.println("start");
				System.out.println(rr.x() + " " + rr.width());
				System.out.println(rr.y() + " " + rr.height());
				System.out.println("end");
			}
			StaticUtils.display(frame, "withRObots");
			return;
		} 
		StaticUtils.display(frame, "new webcam frame");
		
		if (!loadFromStorage)
			vc.close();
	}

}

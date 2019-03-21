package main;

import org.bytedeco.javacv.*;

import base.*;
import util.Utils;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_videoio.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import java.util.ArrayList;
import java.util.List;

public class TrackingMain {

	public static void main(String[] args) {
		
		boolean loadFromStorage = true;
		System.out.println("hello");
		
		Mat frame = new Mat();
		VideoCapture vc = null;
		
		if (loadFromStorage) {
			frame = imread("testframe.png");
		} else {
			vc = new VideoCapture();
			vc.open(1);
			if (!vc.isOpened()) {
				System.err.println("VideoCapture is not opened");
				vc.close();
				return;
			}
			System.out.println("VideoCapture opened successfully");
			System.out.println("Starting to grab frames");
			for (int i = 0; i < 5; i++) {
				vc.read(frame);
				if (frame.empty()) {
					System.out.println("empty frame grabbed wtf");
					continue;
				}
				System.out.println("frame grabbed successfully");
				//break;
			}
		}
		Utils.display(frame, "new webcam frame");
		System.out.println(frame);
		GraphFinder gf = new GraphFinder();
		Mat transformation = gf.findGraph(frame);
		
		if (!loadFromStorage)
			vc.close();
	}

}

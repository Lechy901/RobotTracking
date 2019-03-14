package main;

import org.bytedeco.javacv.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_videoio.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class TrackingMain {

	public static void main(String[] args) {
		
		System.out.println("hello");
		
		Mat frame = new Mat();
		
		VideoCapture vc = new VideoCapture();
		vc.open(0);
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

		GraphFinder gf = new GraphFinder();
		gf.findGraph(frame);
		
		vc.close();
	}

}

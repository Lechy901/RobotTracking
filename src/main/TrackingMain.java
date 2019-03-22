package main;

import base.*;
import training_utils.ImageUtils;
import util.Utils;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_videoio.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import java.util.ArrayList;
import java.util.List;

public class TrackingMain {

	public static void main(String[] args) {
		
		boolean generateBgFile = true;
		boolean convertImages = false;
		boolean downloadImages = false;
		
		boolean loadFromStorage = true;
		System.out.println("hello");
		
		if (generateBgFile) {
			ImageUtils.generateBgFile("img");
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

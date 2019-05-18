package training_utils;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;

import java.io.File;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;

import util.ImageWindow;
import util.MainProgramWindow;
import util.StaticUtils;

public class TestImageCapturer {
	
	private File dir;
	private VideoCapture vc;
	
	public TestImageCapturer(String dirpath) {
		dir = new File(dirpath);
		vc = new VideoCapture();
	}
	
	public void start() {
		ImageWindow iw = new ImageWindow("frames");
		vc.open(1);
		if (!vc.isOpened()) {
			System.err.println("VideoCapture is not opened");
			vc.close();
			return;
		}
		int img_num = 1365;
		for(int i = 1; i < 2520; i++) {
			Mat frame = new Mat();
			vc.read(frame);
			if (frame.empty()) {
				System.err.println("grabbed empty frame!");
				vc.close();
				return;
			}

			iw.showImage(frame);
			if (i % 45 == 0) {
				System.out.println("writing image " + img_num);
				Mat gray = new Mat();
				cvtColor(frame, gray, CV_BGR2GRAY);
				imwrite(dir.getName() + "/" + img_num++ + ".jpg", gray);
			}
		}
		System.out.println("done");
		vc.close();
	}
}

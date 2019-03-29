package training_utils;

import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;

import java.io.File;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;

import util.Utils;

public class TestImageCapturer {
	
	private File dir;
	private VideoCapture vc;
	
	public TestImageCapturer(String dirpath) {
		dir = new File(dirpath);
		vc = new VideoCapture();
	}
	
	public void start() {
		
		vc.open(0);
		if (!vc.isOpened()) {
			System.err.println("VideoCapture is not opened");
			vc.close();
			return;
		}
		int img_num = 530;
		for(int i = 1; i < 660; i++) {
			Mat frame = new Mat();
			vc.read(frame);
			if (frame.empty()) {
				System.err.println("grabbed empty frame!");
				vc.close();
				return;
			}
			
			if (i % 60 == 0) {
				Utils.display(frame, "frame " + i);
				System.out.println("writing image " + img_num);
				Mat gray = new Mat(), resized = new Mat();
				resize(frame, resized, new Size(100, 100));
				cvtColor(resized, gray, CV_BGR2GRAY);
				imwrite(dir.getName() + "/" + img_num++ + ".jpg", gray);
			}
		}
		System.out.println("done");
	}
}

package base;

import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

public class RobotTracker {
	private CascadeClassifier cc;
	
	public RobotTracker(String cascadeFilename) {
		cc = new CascadeClassifier(cascadeFilename);
	}
	
	public Rect[] findRobots(Mat frame) {
		RectVector r = new RectVector();
		cc.detectMultiScale(frame, r);
		return r.get();
	}
}

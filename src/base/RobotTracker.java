package base;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

/**
 * A helper class used to hold the CascadeClassifier
 * 
 * @author Adam Lechovsky
 *
 */
public class RobotTracker {
    private CascadeClassifier cc;

    /**
     * Create a new CascadeClassifier
     * @param cascadeFilename a filename of the cascade
     */
    public RobotTracker(String cascadeFilename) {
        cc = new CascadeClassifier(cascadeFilename);
    }

    /**
     * A helper function to be used when searching for robots in an image.
     * @param frame The image to search.
     * @return An array of Rects representing the location of the robots in the image.
     */
    public Rect[] findRobots(Mat frame) {
        if (frame == null || frame.empty())
            System.out.println("empty frame passed to findRobots");
        RectVector r = new RectVector();
        cc.detectMultiScale(frame, r);
        return r.get();
    }
}

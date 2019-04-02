package util;

import javax.swing.WindowConstants;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class ImageWindow {
	
	OpenCVFrameConverter<Mat> converter;
	CanvasFrame canvas;
	
	public ImageWindow(String caption) {
		canvas = new CanvasFrame(caption, 1.0);

        // Request closing of the application when the image window is closed.
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Convert from OpenCV Mat to Java Buffered image for display
        converter = new OpenCVFrameConverter.ToMat();
        
	}
	
	public void showImage(Mat image) {
		// Show image on window.
		canvas.showImage(converter.convert(image));
	}
}

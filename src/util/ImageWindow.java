package util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.WindowConstants;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

/**
 * A class designed to show a series of images (such as the output of a webcam) in a window.
 * Has added functionality for detecting spacebar status (pressed, not pressed).
 * 
 * @author Adam Lechovský
 *
 */
public class ImageWindow {
	
	private OpenCVFrameConverter<Mat> converter;
	private CanvasFrame canvas;
	private boolean spacePressed = false;
	
	/**
	 * A constructor which opens a new window with the specified caption.
	 * @param caption The title of the window
	 */
	public ImageWindow(String caption) {
		canvas = new CanvasFrame(caption, 1.0);
		
        // Request closing of the application when the image window is closed.
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        // Add a key listener for spacebar
        canvas.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE)
					spacePressed = false;
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE)
					spacePressed = true;		
			}
		});

        // Convert from OpenCV Mat to Java Buffered image for display
        converter = new OpenCVFrameConverter.ToMat();
        
	}
	
	/**
	 * Changes the image shown in the window.
	 * @param image The new image to be shown
	 */
	public void showImage(Mat image) {
		// Show image on window.
		canvas.showImage(converter.convert(image));
	}
	
	/**
	 * Gets the status of the spacebar.
	 * @return A boolean noting whether the spacebar is pressed or not
	 */
	public boolean isSpacePressed() {
		return spacePressed;
	}
}

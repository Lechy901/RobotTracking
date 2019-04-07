package util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.WindowConstants;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class ImageWindow {
	
	OpenCVFrameConverter<Mat> converter;
	CanvasFrame canvas;
	boolean spacePressed = false;
	
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
	
	public void showImage(Mat image) {
		// Show image on window.
		canvas.showImage(converter.convert(image));
	}
	
	public boolean isSpacePressed() {
		return spacePressed;
	}
}

package util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;


/**
 * A class designed to show a series of images (such as the output of a webcam) in a window.
 * Has added functionality for detecting spacebar status (pressed, not pressed).
 * 
 * @author Adam Lechovský
 *
 */
public class ImageWindow {
	
	private JFrame window;
	private JLabel picLabelLeft;
	private JLabel picLabelRight;
	private ImageIcon iconLeft;
	private ImageIcon iconRight;
	private boolean spacePressed = false;
	
	/**
	 * A constructor which opens a new window with the specified caption.
	 * @param caption The title of the window
	 */
	public ImageWindow(String caption) {
		window = new JFrame(caption);
		window.setSize(1500, 900);
		
        // Request closing of the application when the image window is closed.
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        // Add a key listener for spacebar
        window.addKeyListener(new KeyListener() {
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
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        
        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BorderLayout());
        
        JPanel botPanel = new JPanel();
        botPanel.setLayout(new BorderLayout());
        
        iconLeft = new ImageIcon();
        iconLeft.setImage(createAwtImage(new Mat(480, 640, CV_8UC3, new Scalar(0))));
        picLabelLeft = new JLabel(iconLeft);
        topPanel.add(picLabelLeft, "West");
        iconRight = new ImageIcon();
        iconRight.setImage(createAwtImage(new Mat(480, 640, CV_8UC3, new Scalar(0))));
        picLabelRight = new JLabel(iconRight);
        topPanel.add(picLabelRight, "East");
        
        window.getContentPane().add(topPanel, "North");
        
        JTextField text1 = new JTextField();
        midPanel.add(text1, "West");
        
        window.getContentPane().add(midPanel, "Center");
        
        JButton but1 = new JButton("Button1");
        botPanel.add(but1, "South");
        
        window.getContentPane().add(botPanel, "South");
        
        window.setVisible(true);
	}
	
	/**
	 * Changes the image shown in the window.
	 * @param image The new image to be shown
	 * @param left true => show image in the left JLabel, false => show image in the right JLabel
	 */
	public void showImage(Mat image, boolean left) {
		// Show image on window.
		if (left)
			iconLeft.setImage(createAwtImage(image));
		else
			iconRight.setImage(createAwtImage(image));
		window.repaint();
	}
	
	/**
	 * Gets the status of the spacebar.
	 * @return A boolean noting whether the spacebar is pressed or not
	 */
	public boolean isSpacePressed() {
		return spacePressed;
	}
	
	/**
	 * Creates an image that is showable in a JFrame from OpenCV Mat
	 * @param mat OpenCV Mat to convert
	 * @return Awt BufferedImage for showing in JFrame
	 */
	private BufferedImage createAwtImage(Mat mat) {

	    int type = 0;
	    if (mat.channels() == 1) {
	        type = BufferedImage.TYPE_BYTE_GRAY;
	    } else if (mat.channels() == 3) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    } else {
	        return null;
	    }

	    BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
	    WritableRaster raster = image.getRaster();
	    DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
	    byte[] data = dataBuffer.getData();
	    mat.data().get(data);

	    return image;
	}
}

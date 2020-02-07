package util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;

import base.WindowControl;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_imgproc.resize;


/**
 * A class designed to show a series of images (such as the output of a webcam) in a window.
 * Has added functionality for detecting spacebar status (pressed, not pressed).
 * 
 * @author Adam Lechovsky
 *
 */
public class MainProgramWindow {

    private JFrame window;
    private JLabel picLabelLeft, picLabelRight;
    private JLabel label1, label2, label3, textWarning;
    private JTextField text1, text2, text3;
    private ImageIcon iconLeft;
    private ImageIcon iconRight;
    private boolean validFormValues = true;
    private WindowControl windowControl;

    /**
     * A constructor which opens a new window with the specified caption.
     * @param caption The title of the window
     * @param wc The window control to be controled by form buttons
     * @param defaultLineWidth default line width
     * @param defaultRobotsNumber default robots number
     * @param defaultPointGroupDistance default point group distance
     */
    public MainProgramWindow(String caption, WindowControl wc, int defaultLineWidth, int defaultRobotsNumber, int defaultPointGroupDistance) {
        windowControl = wc;

        window = new JFrame(caption);
        window.setSize(1400, 800);

        // close the app when the window is closed
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // set up three panels to hold content
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));

        JPanel botPanel = new JPanel();
        botPanel.setLayout(new BorderLayout());

        // fill the top panel with two picture labels
        iconLeft = new ImageIcon();
        iconLeft.setImage(createAwtImage(new Mat(480, 640, CV_8UC3, new Scalar(0))));
        picLabelLeft = new JLabel(iconLeft);
        topPanel.add(picLabelLeft, "West");

        iconRight = new ImageIcon();
        iconRight.setImage(createAwtImage(new Mat(480, 640, CV_8UC3, new Scalar(0))));
        picLabelRight = new JLabel(iconRight);
        topPanel.add(picLabelRight, "East");

        window.getContentPane().add(topPanel, "North");

        // fill the middle panel with calculation parameters textfields
        
        addLabelAndTextField(midPanel, label1, text1, "Line width:", Integer.toString(defaultLineWidth), windowControl::setLineWidth);
        addLabelAndTextField(midPanel, label2, text2, "Robots number:", Integer.toString(defaultRobotsNumber), windowControl::setRobotsNumber);
        addLabelAndTextField(midPanel, label3, text3, "Point group distance:", Integer.toString(defaultPointGroupDistance), windowControl::setPointGroupDistance);

        textWarning = new JLabel("All fields must be 32 bit integers!");
        textWarning.setForeground(Color.RED);
        textWarning.setVisible(false);
        midPanel.add(textWarning);

        window.getContentPane().add(midPanel, "Center");

        // add two buttons into the bottom panel
        JButton but1 = new JButton("Next Stage");
        but1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!validFormValues) {
                    return;
                }
                windowControl.nextStage();
            }
        });
        botPanel.add(but1, "East");

        JButton but2 = new JButton("Previous Stage");
        but2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!validFormValues) {
                    return;
                }
                windowControl.prevStage();
            }
        });
        botPanel.add(but2, "West");
        
        JButton but3 = new JButton("Pause");
        but3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowControl.flipPaused();
            }
        });
        botPanel.add(but3, "Center");

        window.getContentPane().add(botPanel, "South");

        window.setVisible(true);
    }

    /**
     * Changes the image shown in the window.
     * @param image The new image to be shown
     * @param left true = show image in the left JLabel, false = show image in the right JLabel
     */
    public void showImage(Mat image, boolean left, boolean repaint) {
        if (image.cols() > 640) {
            resize(image, image, new Size(640, 480));
        }
        // Show image on window.
        if (left) {
            iconLeft.setImage(createAwtImage(image));
        } else {
            iconRight.setImage(createAwtImage(image));
        }
        if (repaint) {
            window.repaint();
        }
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

    private void setFormValuesValidity(boolean valid) {
        validFormValues = valid;
        textWarning.setVisible(!valid);
    }
    
    private void addLabelAndTextField(JComponent panel, JLabel label, JTextField textField, String labelText, String defaultTextFieldText, Consumer<Integer> func) {
        label = new JLabel(labelText);
        panel.add(label);

        textField = new JTextField();
        textField.setMaximumSize(new Dimension(300,30));
        textField.setText(defaultTextFieldText);
        
        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField sender = (JTextField)e.getComponent();
                sender.setText(sender.getText().trim());
                try {
                    func.accept(Integer.parseInt(sender.getText()));
                    setFormValuesValidity(true);
                } catch(Exception ex) {
                    setFormValuesValidity(false);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
        panel.add(textField);
    }
}

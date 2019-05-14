package training_utils;

import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.*;

/**
 * A class used to help with generating images for training the CascadeClassifier.
 * Not used during the runtime of the main program.
 * 
 * @author ALechovsky
 *
 */
public class ImageUtils {
	
	/**
	 * Takes a list of URLs containing images, downloads them and converts them into 100x100 grayscale image.
	 * @param urlsFilePath The path to a file containing the URLs of the images. One per line.
	 */
	public static void loadAndConvertImagesFromURLs(String urlsFilePath) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(urlsFilePath));
			int i = 1486;
			for(String line : lines) {
				System.out.println("loading image " + line);
				URL url = new URL(line);
				String filename = "img/" + i + ".jpg";
				BufferedImage image = null;
				try {
					image = ImageIO.read(url);
				} catch(IIOException ex) {
					// failed to connect or other issues
					System.out.println("url failed");
					continue;
				}
				if (image == null) {
					// connection was successful but could not load an image from the server
					System.out.println("null image");
					continue;
				}
				ImageIO.write(image, "jpg", new File(filename));
			
				Mat img = imread(filename, IMREAD_GRAYSCALE);
				Mat resized = new Mat();
				resize(img, resized, new Size(100, 100));
				
				imwrite(filename, resized);
				i++;
				System.out.println("done");
			}
		} catch (IOException e) {
			// failed to open the URLs file
			System.err.println("Failed to open file: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Takes a directory of existing images and converts them to 100x100 grayscale.
	 * @param dirpath The path to the directory containing the images to be converted.
	 */
	public static void convertImagesFromStorage(String dirpath) {
		File dir = new File(dirpath);
		File[] contents = dir.listFiles();
		int i = 1;
		for(File f : contents) {
			System.out.println("processing " + f.getName());
			
			Mat img = imread(dirpath + "/" + f.getName(), IMREAD_GRAYSCALE);
			Mat resized = new Mat();
			resize(img, resized, new Size(100, 100));
			
			imwrite("a" + i + ".jpg", resized);
			i++;
			System.out.println("done");
		}
	}
	
	/**
	 * Takes a directory of images and generates a bg file (a list of filenames in the directory) for OpenCV
	 * @param dirpath A path to the directory of images
	 * @param filename A name of the bg file to be created.
	 */
	public static void generateBgFile(String dirpath, String filename) {
		try {
			File dir = new File(dirpath);
			File[] contents = dir.listFiles();
			BufferedWriter bg = new BufferedWriter(new FileWriter(filename));
			
			for(File f : contents) {
				bg.write(dirpath + "/" + f.getName() + "\n");
			}
			bg.close();
			
		} catch (IOException e) {
			System.err.println("bg generation failed");
			e.printStackTrace();
		}
	}
}

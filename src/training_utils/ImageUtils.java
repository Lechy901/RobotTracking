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

public class ImageUtils {
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
					System.out.println("url failed");
					continue;
				}
				if (image == null) {
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
			System.err.println("Failed to open file: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void convertImagesFromStorage(String dirpath) {
		File dir = new File(dirpath);
		File[] contents = dir.listFiles();
		int i = 2051;
		for(File f : contents) {
			System.out.println("processing " + f.getName());
			
			Mat img = imread(dirpath + "/" + f.getName(), IMREAD_GRAYSCALE);
			Mat resized = new Mat();
			resize(img, resized, new Size(100, 100));
			
			imwrite(i + ".jpg", resized);
			i++;
			System.out.println("done");
		}
	}
	
	public static void generateBgFile(String dirpath) {
		try {
			File dir = new File(dirpath);
			File[] contents = dir.listFiles();
			BufferedWriter bg = new BufferedWriter(new FileWriter("bg.txt"));
			
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

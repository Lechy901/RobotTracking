package base;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacpp.indexer.Indexer;

public class GraphFinder {
	public void findGraph(Mat input) {
		Mat scaled = new Mat();
		Utils.scaleImageToFit(input, scaled);
		
		Mat gray = new Mat();
		cvtColor(scaled, gray, COLOR_BGR2GRAY);
		
		Mat blurred = new Mat();
		GaussianBlur(gray, blurred, new Size(5, 5), 0);
		
		Mat dilated = new Mat();
		Mat kernel = getStructuringElement(MORPH_ELLIPSE, new Size(11, 11));
		dilate(blurred, dilated, kernel, new Point(-1, -1), 1, BORDER_CONSTANT, morphologyDefaultBorderValue());
		
		Mat edges = new Mat();
		Canny(dilated, edges, 50, 150);
		Utils.display(edges, "edges");
		
		int rho = 1;
		double theta = Math.PI / 180;
		int threshold = 10;
		int min_line_len = 250;
		int max_line_gap = 10;
		Mat line_img = new Mat(scaled);
		
		Mat lines = new Mat();
		HoughLinesP(edges, lines, rho, theta,threshold, min_line_len, max_line_gap);
		System.out.println(lines);
		Indexer indexer = lines.createIndexer();
		for(int i = 0; i < indexer.rows(); i++) {
			line(line_img, new Point((int)indexer.getDouble(0, i, 0), (int)indexer.getDouble(0, i, 1)),
					       new Point((int)indexer.getDouble(0, i, 2), (int)indexer.getDouble(0, i, 3)),
					       new Scalar(255, 0, 0, 255));
		}
		
		Utils.display(line_img, "lines");
	}
	
}

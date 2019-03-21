package util;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.swing.WindowConstants;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Point2f;
import org.bytedeco.javacpp.opencv_core.PointVector;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class Utils {
	
	static Random rand = new Random();
	
	public static void display(Mat image, String caption) {
        // Create image window named "My Image".
		
        final CanvasFrame canvas = new CanvasFrame(caption, 1.0);

        // Request closing of the application when the image window is closed.
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Convert from OpenCV Mat to Java Buffered image for display
        final OpenCVFrameConverter converter = new OpenCVFrameConverter.ToMat();
        // Show image on window.
        canvas.showImage(converter.convert(image));
        
    }
	
	public static Mat scaleImageToFit(Mat image, Mat output) {
		List<List<Point>> squares = new ArrayList<List<Point>>();
		findSquares(new Mat(image), squares);
		List<Point> biggestSquare;
		try {
			biggestSquare = getBiggestSquare(squares);
		} catch (IllegalArgumentException ex) {
			System.out.println("no squares");
			return null;
		}
		biggestSquare = sortSquareClockWise(biggestSquare);
	    for (Point p : biggestSquare) {
	    	System.out.println(p.x() + " " + p.y());
	    	//circle(image, p, 6, new Scalar());
	    }
	    System.out.println("done");

	    Mat srcMat = pointsToMat(biggestSquare);
	    List<Point> to = new ArrayList<Point>();
	    to.add(new Point(0, 0));
	    to.add(new Point(image.cols(), 0));
	    to.add(new Point(image.cols(), image.rows()));
	    to.add(new Point(0, image.rows()));
	    Mat toMat = pointsToMat(to);
	    
	    Mat transformation = getPerspectiveTransform(srcMat, toMat);
	    
	    warpPerspective(image, output, transformation, new Size(image.cols(), image.rows()));
	    return transformation;
	}
	
	public static Point lineIntersection(Pair<Point, Point> line1, Pair<Point, Point> line2) throws Exception {
		int x1diff = line1.first.x() - line1.second.x();
		int x2diff = line2.first.x() - line2.second.x();
		int y1diff = line1.first.y() - line1.second.y();
		int y2diff = line2.first.y() - line2.second.y();
		
		int div = det(x1diff, x2diff, y1diff, y2diff);
		if (div == 0)
			throw new Exception("lines do not intersect");
		
		int d1 = det(line1.first.x(), line1.first.y(), line1.second.x(), line1.second.y());
		int d2 = det(line2.first.x(), line2.first.y(), line2.second.x(), line2.second.y());
		
		int x = det(d1, d2, x1diff, x2diff) / div;
		int y = det(d1, d2, y1diff, y2diff) / div;
		
		return new Point(x, y);
	}
	
	public static double getDist(Point p1, Point p2) {
		return Math.sqrt((p1.x() - p2.x()) * (p1.x() - p2.x()) + (p1.y() - p2.y()) * (p1.y() - p2.y()));
	}
	
	private static int det(int x1, int x2, int y1, int y2) {
		return x1 * y2 - x2 * y1;
	}
	
	private static void findSquares(Mat image, List<List<Point>> squares) {
	    // blur will enhance edge detection
	    Mat blurred = new Mat(image);
	    medianBlur(image, blurred, 5);
	    
	    Mat gray0 = new Mat(blurred.size(), CV_8U), gray = new Mat();
	    MatVector contours = new MatVector();

	    // find squares in every color plane of the image
	    for (int c = 0; c < 3; c++)
	    {
	        int ch[] = {c, 0};
	        mixChannels(blurred, 1, gray0, 1, ch, 1);

	        // try several threshold levels
	        final int threshold_level = 1;
	        for (int l = 0; l < threshold_level; l++)
	        {
	            // Use Canny instead of zero threshold level!
	            // Canny helps to catch squares with gradient shading
	            if (l == 0)
	            {
	                Canny(gray0, gray, 10.0, 20.0, 3, false);

	                // Dilate helps to remove potential holes between edge segments
	                dilate(gray, gray, new Mat(), new Point(-1,-1), 1, BORDER_CONSTANT, morphologyDefaultBorderValue());
	                //Utils.display(gray, "square finder gray c = " + c);
	                if (c != 1)
	                	break;
	            }
	            else
	            {
                    //gray = gray0 >= (l+1) * 255 / threshold_level;
            		Mat comparisonMat = new Mat(gray0);
            		for(int i = 0; i < comparisonMat.rows(); i++) {
            			for(int j = 0; j < comparisonMat.cols(); j++) {
            				comparisonMat.ptr(i, j).put((byte)((l+1) * 255 / threshold_level));
            			}
            		}
                    compare(gray0, comparisonMat, gray, CMP_GE);
	            }
	            // Find contours and store them in a list
	            findContours(gray, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
	            // Test contours
	            Mat approx = new Mat();
	            for (int i = 0; i < contours.size(); i++)
	            {
	            	//display(contours.get(i), "xdghfjgfjgh");
                    // approximate contour with accuracy proportional
                    // to the contour perimeter
                    approxPolyDP(new Mat(contours.get(i)), approx, arcLength(new Mat(contours.get(i)), true)*0.02, true);
                    // Note: absolute value of an area is used because
                    // area may be positive or negative - in accordance with the
                    // contour orientation
                    
                    if (approx.rows() == 4 &&
                        Math.abs(contourArea(new Mat(approx))) > 1000 &&
                        isContourConvex(new Mat(approx)))
                    {   
                        double maxCosine = 0;
                        Indexer indexer = approx.createIndexer();
                        	
                        for (int j = 2; j < 5; j++)
                        {
                            double cosine = Math.abs(angle(new Point((int)indexer.getDouble(0, j % 4, 0), (int)indexer.getDouble(0, j % 4, 1)),
                     								new Point((int)indexer.getDouble(0, j - 2, 0), (int)indexer.getDouble(0, j - 2, 1)),
                     								new Point((int)indexer.getDouble(0, j - 1, 0), (int)indexer.getDouble(0, j - 1, 1))));
                            
                            maxCosine = Math.max(maxCosine, cosine);
                        }
                        
                        if (maxCosine < 0.3) {
                        	List<Point> square = new ArrayList<Point>();
                        	square.add(new Point((int)indexer.getDouble(0, 0, 0), (int)indexer.getDouble(0, 0, 1)));
                        	square.add(new Point((int)indexer.getDouble(0, 1, 0), (int)indexer.getDouble(0, 1, 1)));
                        	square.add(new Point((int)indexer.getDouble(0, 2, 0), (int)indexer.getDouble(0, 2, 1)));
                        	square.add(new Point((int)indexer.getDouble(0, 3, 0), (int)indexer.getDouble(0, 3, 1)));
                        	squares.add(square);
                        }
                    }
	                    
	            }
	        }
	    }        
	}
	
	private static List<Point> getBiggestSquare(List<List<Point>> squares) throws IllegalArgumentException {
		double maxArea = -1.0;
		List<Point> maxAreaSquare = null;
		for(List<Point> square : squares) {
			double curSquareArea = getPolygonArea(square);
			if (curSquareArea > maxArea) {
				maxArea = curSquareArea;
				maxAreaSquare = square;
			}
		}
		if (maxAreaSquare == null)
			throw new IllegalArgumentException();
		return maxAreaSquare;
	}
	
	private static double getPolygonArea(List<Point> square) {
		double max = -1.0;
		
		for(int i = 0; i < square.size(); i++) {
			for(int j = 0; j < square.size() && j != i; j++) {
				double curArea = Math.abs(square.get(i).x() - square.get(j).x()) * Math.abs(square.get(i).y() - square.get(j).y());
				if (curArea > max) {
					max = curArea;
				}
			}
		}
		
		return max;
	}
	
	private static List<Point> sortSquareClockWise(List<Point> square) {
		List<Point> r = new ArrayList<Point>(4);
		r.add(square.stream().min((a, b) -> { // upper left
			if (a.x() + a.y() < b.x() + b.y())
				return -1;
			return 1;
		}).get());
		r.add(square.stream().max((a, b) -> { // upper right
			if (a.x() - a.y() < b.x() - b.y())
				return -1;
			return 1;
		}).get());
		r.add(square.stream().max((a, b) -> { // lower right
			if (a.x() + a.y() < b.x() + b.y())
				return -1;
			return 1;
		}).get());
		r.add(square.stream().min((a, b) -> { // lower left
			if (a.x() - a.y() < b.x() - b.y())
				return -1;
			return 1;
		}).get());
		
		return r;
	}
	
	private static double angle( Point pt1, Point pt2, Point pt0 )
	{
	    double dx1 = pt1.x() - pt0.x();
	    double dy1 = pt1.y() - pt0.y();
	    double dx2 = pt2.x() - pt0.x();
	    double dy2 = pt2.y() - pt0.y();
	    return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
	}
	
	private static Mat pointsToMat(List<Point> points) {
		Mat r = new Mat(3, new int[] {1, points.size(), 2}, CV_32F);
		Indexer indexer = r.createIndexer();
		for(int i = 0; i < points.size(); i++) {
	    	indexer.putDouble(new long[] {0, i, 0}, points.get(i).x());
	    	indexer.putDouble(new long[] {0, i, 1}, points.get(i).y());
	    }
		return r;
	}
}

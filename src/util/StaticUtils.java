package util;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
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
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

/**
 * A helper class containing static helper functions.
 * 
 * @author Adam Lechovský
 *
 */
public class StaticUtils {

    /**
     * A program-wide Random instance
     */
    public static Random rand = new Random();

    /**
     * Calls OpenCV HoughLinesP() and all necessary transformations on the source image.
     * @param img The image to call HoughLinesP() on
     * @return The result of HoughLinesP() as ArrayList of lines, where one line is a Pair of Points
     */
    public static List<Pair<Point, Point>> getLines(Mat img, int dilateIterations) {
        Mat gray = new Mat(), dilated = new Mat(), edges = new Mat(), lines = new Mat();
        cvtColor(img, gray, CV_BGR2GRAY);

        if (dilateIterations >= 0) {
            dilate(gray, dilated, new Mat(), new Point(-1,-1), dilateIterations, BORDER_CONSTANT, morphologyDefaultBorderValue());
        } else {
            erode(gray, dilated, new Mat(), new Point(-1, -1), -1 * dilateIterations, BORDER_CONSTANT, morphologyDefaultBorderValue());
        } 
        Canny(dilated, edges, 50, 150, 3, false);
        HoughLinesP(edges, lines, 1, Math.PI / 180, 40, 10, 0);

        List<Pair<Point, Point>> r = new ArrayList<Pair<Point, Point>>();
        if (lines.isNull() || lines.rows() == 0) {
            return r;
        }

        Indexer indexer = lines.createIndexer();		

        for (int i = 0; i < indexer.rows(); i++) {
            r.add(new Pair<Point, Point>(new Point((int)indexer.getDouble(0, i, 0), (int)indexer.getDouble(0, i, 1)),
                    new Point((int)indexer.getDouble(0, i, 2), (int)indexer.getDouble(0, i, 3))));
        }
        return r;
    }

    /**
     * Takes the output of getLines() and groups the lines into vertical and horizontal lines 
     * which are at least threshold apart from each other.
     * @param input The List of lines to group
     * @param horizontal The result horizontal lines will be put into this List - should already be initialized
     * @param vertical The result vertical lines will be put into this List - should already be initialized
     * @param threshold A number denoting how far should the groups of lines be from each other in pixels
     */
    public static void groupLines(List<Pair<Point, Point>> input, List<Pair<Point, Point>> horizontal, List<Pair<Point, Point>> vertical, int threshold) {

        List<Pair<Point, Point>> horizontal_temp = new ArrayList<Pair<Point, Point>>();
        List<Pair<Point, Point>> vertical_temp = new ArrayList<Pair<Point, Point>>();

        for(Pair<Point, Point> line : input) {
            double lineAngle = angle(line.first, line.second, new Point(line.first.x() - 1, line.first.y()));
            if (lineAngle < 0.1) {
                vertical_temp.add(line);
            } else if (lineAngle > 0.9) {
                horizontal_temp.add(line);
            }
        }

        vertical_temp.sort((a, b) -> {
            return a.first.x() - b.first.x();
        });

        int last_x = -1;
        List<Pair<Point, Point>> temp = new ArrayList<Pair<Point, Point>>();

        for (int i = 0; i < vertical_temp.size(); i++) {
            if (last_x == -1) {
                // first vertical line in the first set
                temp.add(vertical_temp.get(i));
            } else {
                if (vertical_temp.get(i).first.x() - last_x < threshold && i != vertical_temp.size() - 1) {
                    // another vertical line in the same set
                    temp.add(vertical_temp.get(i));
                } else {
                    // first vertical line in another set
                    if (temp.size() >= 4) {
                        int min_y = Integer.MAX_VALUE;
                        int min_y_x = 0;
                        for (int j = 0; j < temp.size(); j++) {
                            if (temp.get(j).first.y() < min_y) {
                                min_y = temp.get(j).first.y();
                                min_y_x = temp.get(j).first.x();
                            }
                            if (temp.get(j).second.y() < min_y) {
                                min_y = temp.get(j).second.y();
                                min_y_x = temp.get(j).second.x();
                            }
                        }
                        int max_y = -1;
                        int max_y_x = 0;
                        for (int j = 0; j < temp.size(); j++) {
                            if (temp.get(j).first.y() > max_y) {
                                max_y = temp.get(j).first.y();
                                max_y_x = temp.get(j).first.x();
                            }
                            if (temp.get(j).second.y() > max_y) {
                                max_y = temp.get(j).second.y();
                                max_y_x = temp.get(j).second.x();
                            }
                        }
                        vertical.add(new Pair<Point, Point>(new Point(min_y_x, min_y), new Point(max_y_x, max_y)));
                    }
                    temp.clear();
                    temp.add(vertical_temp.get(i));
                }
            }
            last_x = vertical_temp.get(i).first.x();
        }

        horizontal_temp.sort((a, b) -> {
            return a.first.y() - b.first.y();
        });

        int last_y = -1;
        temp = new ArrayList<Pair<Point, Point>>();

        for (int i = 0; i < horizontal_temp.size(); i++) {
            if (last_y == -1) {
                // first horizontal line in the first set
                temp.add(horizontal_temp.get(i));
            } else {
                if (horizontal_temp.get(i).first.y() - last_y < threshold && i != horizontal_temp.size() - 1) {
                    // another horizontal line in the same set
                    temp.add(horizontal_temp.get(i));
                } else {
                    // first horizontal line in another set
                    if (temp.size() >= 4) {
                        int min_x = Integer.MAX_VALUE;
                        int min_x_y = 0;
                        for (int j = 0; j < temp.size(); j++) {
                            if (temp.get(j).first.x() < min_x) {
                                min_x = temp.get(j).first.x();
                                min_x_y = temp.get(j).first.y();
                            }
                            if (temp.get(j).second.x() < min_x) {
                                min_x = temp.get(j).second.x();
                                min_x_y = temp.get(j).second.y();
                            }
                        }
                        int max_x = -1;
                        int max_x_y = 0;
                        for (int j = 0; j < temp.size(); j++) {
                            if (temp.get(j).first.x() > max_x) {
                                max_x = temp.get(j).first.x();
                                max_x_y = temp.get(j).first.y();
                            }
                            if (temp.get(j).second.x() > max_x) {
                                max_x = temp.get(j).second.x();
                                max_x_y = temp.get(j).second.y();
                            }
                        }
                        horizontal.add(new Pair<Point, Point>(new Point(min_x, min_x_y), new Point(max_x, max_x_y)));
                    }
                    temp.clear();
                    temp.add(horizontal_temp.get(i));
                }
            }
            last_y = horizontal_temp.get(i).first.y();
        }
    }

    /**
     * Takes a List of Points and groups the Points so that the distance between any two groups is at least threshold.
     * Every group then gets transformed into a single Point by averaging the Points in the group.
     * @param points The List of Points to be grouped
     * @param threshold The distance between groups in pixels
     * @return The grouped List of Points
     */
    public static List<Point> groupPoints(List<Point> points, double threshold) {
        List<Point> r = new ArrayList<Point>();

        while(!points.isEmpty()) {
            Point cur = points.get(0);

            List<Point> nearby = points.stream().filter(x -> (dist(cur, x) < threshold)).collect(Collectors.toList());
            points.removeAll(nearby);

            r.add(new Point((int)nearby.stream().mapToInt(a -> a.x()).average().getAsDouble(),
                    (int)nearby.stream().mapToInt(a -> a.y()).average().getAsDouble()));
        }

        return r;
    }

    /**
     * Opens a single window with set image and caption.
     * @param image The image to show in the window
     * @param caption The caption of the window
     */
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

    /**
     * Takes an image, finds a paper in it and returns a transformation that would scale
     * the paper over the whole image.
     * @param image The image to calculate the transformation from
     * @return The transformation required to scale the paper in the image over the whole image
     */
    public static Mat getPaperTransformation(Mat image) {
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

        return getPerspectiveTransform(srcMat, toMat);
    }

    /**
     * Takes two lines written as Pairs of Points and returns the Point of their intersection.
     * @param line1 The first line
     * @param line2 The second line
     * @return The Point of intersection of the two lines
     * @throws NoIntersectionException Thrown when the two lines do not intersect
     */
    public static Point lineIntersection(Pair<Point, Point> line1, Pair<Point, Point> line2) throws NoIntersectionException {
        int x1diff = line1.first.x() - line1.second.x();
        int x2diff = line2.first.x() - line2.second.x();
        int y1diff = line1.first.y() - line1.second.y();
        int y2diff = line2.first.y() - line2.second.y();

        int div = det(x1diff, x2diff, y1diff, y2diff);
        if (div == 0) {
            throw new NoIntersectionException("lines do not intersect");
        }

        int d1 = det(line1.first.x(), line1.first.y(), line1.second.x(), line1.second.y());
        int d2 = det(line2.first.x(), line2.first.y(), line2.second.x(), line2.second.y());

        int x = det(d1, d2, x1diff, x2diff) / div;
        int y = det(d1, d2, y1diff, y2diff) / div;

        return new Point(x, y);
    }

    /**
     * Calculates the distance between two Points.
     * @param p1 The first Point
     * @param p2 The second Point
     * @return The distance between the two Points
     */
    public static double dist(Point p1, Point p2) {
        return Math.sqrt((p1.x() - p2.x()) * (p1.x() - p2.x()) + (p1.y() - p2.y()) * (p1.y() - p2.y()));
    }

    /**
     * Calculates the determinant of a 2x2 matrix.
     * @param x1 Number at index 0,0
     * @param x2 Number at index 0,1
     * @param y1 Number at index 1,0
     * @param y2 Number at index 1,1
     * @return The determinant of the matrix
     */
    private static int det(int x1, int x2, int y1, int y2) {
        return x1 * y2 - x2 * y1;
    }

    /**
     * Takes a List of squares where each square is a List of four Points (doesn't strictly have to be a square)
     * and returns the one with the largest area.
     * @param squares The list of squares
     * @return The largest square by area.
     * @throws IllegalArgumentException Thrown if the list of squares is empty.
     */
    public static List<Point> getBiggestSquare(List<List<Point>> squares) throws IllegalArgumentException {
        double maxArea = -1.0;
        List<Point> maxAreaSquare = null;
        for(List<Point> square : squares) {
            double curSquareArea = getApproximateSquareArea(square);
            if (curSquareArea > maxArea) {
                maxArea = curSquareArea;
                maxAreaSquare = square;
            }
        }
        if (maxAreaSquare == null) {
            throw new IllegalArgumentException();
        }
        return maxAreaSquare;
    }

    /**
     * Takes an image and returns a List of squares (4 sided polygons) as returned by OpenCV approxPolyDP().
     * @param image The image to find squares in
     * @param squares The output List with the squares. Must already be initialized
     */
    public static void findSquares(Mat image, List<List<Point>> squares) {
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
                    if (c != 1) {
                        break;
                    }
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

    /**
     * Calculates the angle between three Points, where pt0 is the center Point.
     * @param pt1 The first side Point
     * @param pt2 The second side Point
     * @param pt0 The center Point
     * @return The cosine of the angle between the three Points
     */
    public static double angle( Point pt1, Point pt2, Point pt0 )
    {
        double dx1 = pt1.x() - pt0.x();
        double dy1 = pt1.y() - pt0.y();
        double dx2 = pt2.x() - pt0.x();
        double dy2 = pt2.y() - pt0.y();
        return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
    }


    /**
     * Takes a square as a List of four Points and calculates its approximate area (enough to get the largest square
     * out of a List of squares).
     * @param square The square to calculate the area of
     * @return The approximate area of the square
     */
    private static double getApproximateSquareArea(List<Point> square) {
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

    /**
     * Takes a square as a List of four Points and sorts its vertices in a clockwise fashion.
     * @param square The square to be sorted
     * @return The sorted square
     */
    private static List<Point> sortSquareClockWise(List<Point> square) {
        List<Point> r = new ArrayList<Point>(4);
        r.add(square.stream().min((a, b) -> { // upper left
            if (a.x() + a.y() < b.x() + b.y()) {
                return -1;
            }
            return 1;
        }).get());
        r.add(square.stream().max((a, b) -> { // upper right
            if (a.x() - a.y() < b.x() - b.y()) {
                return -1;
            }
            return 1;
        }).get());
        r.add(square.stream().max((a, b) -> { // lower right
            if (a.x() + a.y() < b.x() + b.y()) {
                return -1;
            }
            return 1;
        }).get());
        r.add(square.stream().min((a, b) -> { // lower left
            if (a.x() - a.y() < b.x() - b.y()) {
                return -1;
            }	
            return 1;
        }).get());

        return r;
    }

    /**
     * Takes a List of Points and puts them into a Mat to be used in OpenCV functions.
     * @param points The List of Points to be put into a Mat
     * @return The Mat containing the Points
     */
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

package base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.BORDER_CONSTANT;
import static org.bytedeco.javacpp.opencv_imgproc.erode;
import static org.bytedeco.javacpp.opencv_imgproc.morphologyDefaultBorderValue;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.indexer.Indexer;

import util.NoIntersectionException;
import util.Pair;
import util.StaticUtils;

/**
 * A class for representing a graph on an image. A vertex is a Point in the image.
 * @author ALechovsky
 *
 */
public class ImageGraph {

    /**
     * A list of vertices - Points in the image.
     */
    public List<Point> vertices;

    /**
     * A list of edges in the image 
     */
    public List<Pair<Point, Point>> edges;
    
    private int horizontalLinesNum = 0;
    private int verticalLinesNum = 0;

    /**
     * A constructor which takes a list of horizontal lines and a list of vertical lines, calculates intersections of all vertical lines with all horizontal lines,
     * and fills the vertices property accordingly.
     * @param horizontal Horizontal lines in the graph
     * @param vertical Vertical lines in the graph
     * @param image The image over which the graph is created
     * @param groupPointsThreshold The distance two points need to be apart in order to be grouped into one point
     */
    public ImageGraph(List<Pair<Point, Point>> horizontal, List<Pair<Point, Point>> vertical, Mat image, int groupPointsThreshold) {
    	
    	horizontalLinesNum = horizontal.size();
    	verticalLinesNum = vertical.size();
    	
        vertices = new ArrayList<Point>();
        edges = new ArrayList<Pair<Point, Point>>();

        // add intersections of lines as vertices as well as line endpoints
        for(int i = 0; i < vertical.size(); i++) {
            vertices.add(vertical.get(i).first);
        }
        for(int i = 0; i < horizontal.size(); i++) {

            vertices.add(horizontal.get(i).first);

            for(int j = 0; j < vertical.size(); j++) {
                try {
                    Point intersection = StaticUtils.lineIntersection(horizontal.get(i), vertical.get(j));
                    vertices.add(intersection);
                } catch (NoIntersectionException ex) {

                }
            }
            vertices.add(horizontal.get(i).second);
        }
        for(int i = 0; i < vertical.size(); i++) {
            vertices.add(vertical.get(i).second);
        }

        vertices = StaticUtils.groupPoints(vertices, groupPointsThreshold);

        // fill edges - for each vertex, add the two edges that go to the left and up - if there are any
        for(int i = 0; i < vertices.size(); i++) {
            Point current = vertices.get(i);
            Point nearestUp = null;
            Point nearestLeft = null;

            List<Point> left = new ArrayList<Point>();
            List<Point> up = new ArrayList<Point>();
            for(int j = 0; j < i; j++) {
                Point current_cmp = vertices.get(j);
                double angle = StaticUtils.angle(current_cmp, new Point(current.x() + 1, current.y()), current);
                if (angle < -0.97) {
                    // left
                    left.add(current_cmp);
                } else if (angle > -0.15 && angle < 0.15) {
                    // up
                    up.add(current_cmp);
                }
            }

            nearestLeft = getNearest(current, left);
            nearestUp = getNearest(current, up);
            
            Mat eroded = new Mat();
            erode(image, eroded, new Mat(), new Point(-1, -1), 6, BORDER_CONSTANT, morphologyDefaultBorderValue());

            if (nearestUp != null && areConnectedByEdge(current, nearestUp, eroded)) {
                edges.add(new Pair<Point, Point>(current, nearestUp));
            }
            if (nearestLeft != null && areConnectedByEdge(current, nearestLeft, eroded)) {
                edges.add(new Pair<Point, Point>(current, nearestLeft));
            }

        }

    }

    /**
     * A function to determine where is the closest in the graph to a selected Point
     * @param p A Point to be used in the calculation
     * @return The Point which lies on the graph the closest to the given Point
     */
    public Pair<Point, Boolean> getRobotPositionInGraph(Point p, double vertexDist) {
        Point nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        // check vertices - these have priority
        Point nearestVertex = getNearest(p, vertices);
        double distToVertex = StaticUtils.dist(nearestVertex, p);
        if (distToVertex < vertexDist) {
            return new Pair<Point, Boolean>(nearestVertex, true);
        }
        
        // check edges
        for(Pair<Point, Point> line : edges) {
            Point currentNearest = nearestPoint(line, p);
            double dist = StaticUtils.dist(currentNearest, p);
            if (dist < nearestDist) {
                nearest = currentNearest;
                nearestDist = dist;
            }
        }

        return new Pair<Point, Boolean>(nearest, false);
    }
    
    public char[][] getGraphInMapfFormat() {
    	Point[][] points = new Point[verticalLinesNum][horizontalLinesNum];
    	
    	Point[] verticesSortedHorizontal = new Point[vertices.size()];
    	Point[] verticesSortedVertical = new Point[vertices.size()];
    	verticesSortedHorizontal = vertices.toArray(verticesSortedHorizontal);
    	verticesSortedVertical = vertices.toArray(verticesSortedVertical);
    	
    	Arrays.sort(verticesSortedHorizontal, (Point a, Point b) -> {
    		if (a.x() < b.x())
    			return -1;
    		else if (a.x() == b.x())
    			return 0;
    		else
    			return 1;
    	});
    	Arrays.sort(verticesSortedVertical, (Point a, Point b) -> {
    		if (a.y() < b.y())
    			return -1;
    		else if (a.y() == b.y())
    			return 0;
    		else
    			return 1;
    	});
    	
    	List<Point> verticesSortedHorizontalWithoutEdges = new ArrayList<Point>();
    	List<Point> verticesSortedVerticalWithoutEdges = new ArrayList<Point>();
    	
    	for(int i = horizontalLinesNum; i < verticesSortedHorizontal.length - horizontalLinesNum; i++) {
    		verticesSortedHorizontalWithoutEdges.add(verticesSortedHorizontal[i]);
    	}
    	for(int i = verticalLinesNum; i < verticesSortedVertical.length - verticalLinesNum; i++) {
    		verticesSortedVerticalWithoutEdges.add(verticesSortedVertical[i]);
    	}
    	
    	List<Point> innerVertices = new ArrayList<Point>();
    	for(Point p1 : verticesSortedHorizontalWithoutEdges) {
    		boolean same = false;
    		for(Point p2 : verticesSortedVerticalWithoutEdges) {
    			if (p1 == p2) {
    				same = true;
    			}
    		}
    		if (same) {
    			innerVertices.add(p1);
    		}
    	}
    	
    	for(int i = 0; i < verticalLinesNum; i++) {
    		List<Point> curVerticalLinePoints = new ArrayList<Point>();
    		for(int j = i * horizontalLinesNum; j < (i + 1) * horizontalLinesNum; j++) {
    			curVerticalLinePoints.add(innerVertices.get(j));
    		}
    		
    		curVerticalLinePoints.sort((Point a, Point b) -> {
        		if (a.y() < b.y())
        			return -1;
        		else if (a.y() == b.y())
        			return 0;
        		else
        			return 1;
        	});
    		
    		for(int j = 0; j < horizontalLinesNum; j++) {
    			points[i][j] = curVerticalLinePoints.get(j);
    		}
    	}
    	
    	char[][] r = new char[2 * verticalLinesNum - 1][2 * horizontalLinesNum - 1];
    	
    	for(int i = 0; i < r.length; i++) {
    		for(int j = 0; j < r[0].length; j++) {
    			if (i % 2 == 1 && j % 2 == 1) {
    				// space between 4 vertices
    				r[i][j] = '@';
    			} else if (i % 2 == 0 && j % 2 == 0) {
    				// vertex
    				r[i][j] = '.';
    			} else if (i % 2 == 1 && j % 2 == 0) {
    				// horizontal graph edge
    				if (existsEdge(points[(i - 1) / 2][j / 2], points[(i - 1) / 2 + 1][j / 2])) {
    					r[i][j] = '.';
    				} else {
    					r[i][j] = '@';
    				}
    			} else {
    				// vertical graph edge
    				if (existsEdge(points[i / 2][(j - 1) / 2], points[i / 2][(j - 1) / 2 + 1])) {
    					r[i][j] = '.';
    				} else {
    					r[i][j] = '@';
    				}
    			}
    		}
    	}
    	
    	return r;
    }
    
    private boolean existsEdge(Point a, Point b) {
    	for(var edge : edges) {
    		if (edge.first == a && edge.second == b) {
    			return true;
    		}
    		if (edge.first == b && edge.second == a) {
    			return true;
    		}
    	}
    	
    	return false;
    }

    private Point getNearest(Point p, List<Point> l) {
        Point nearest = null;
        double min = Double.MAX_VALUE;
        for(Point ll : l) {
            double dist = StaticUtils.dist(ll, p);
            if (dist < min) {
                min = dist;
                nearest = ll;
            }
        }
        return nearest;
    }

    /**
     * Checks whether two points in an image are connected by black pixels	
     * @param p1 The first point
     * @param p2 The second point
     * @param image The image
     * @return true if connected
     */
    private boolean areConnectedByEdge(Point p1, Point p2, Mat image) {
        double deltaX = p2.x() - p1.x();
        double deltaY = p2.y() - p1.y();

        int numberOfSteps = 0;

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            numberOfSteps = (int)Math.abs(deltaX);
        } else {
            numberOfSteps = (int)Math.abs(deltaY);
        }

        double stepX = deltaX / numberOfSteps;
        double stepY = deltaY / numberOfSteps;

        Indexer indexer = image.createIndexer();

        int numberOfWhitePixels = 0;

        for(int i = 0; i < numberOfSteps; i++) {
            int curX = p1.x() + (int)(stepX * i);
            int curY = p1.y() + (int)(stepY * i);
            if (curY >= image.rows() || curX >= image.cols() || curY < 0 || curX < 0) {
                return false;
            }
            int curR = (int)indexer.getDouble(curY, curX, 0);
            int curG = (int)indexer.getDouble(curY, curX, 1);
            int curB = (int)indexer.getDouble(curY, curX, 2);

            if (curR > 130 || curG > 130 || curB > 130) {
                numberOfWhitePixels++;
            }	
        }

        if (((double)numberOfWhitePixels / (double)numberOfSteps) > 0.3) {
            return false;
        }

        return true;
    }

    /**
     * Takes a line and a point and returns the nearest point on the line to the given point
     * @param line The line to calculate nearest point on
     * @param p The point to calculate nearest point on the line to
     * @return The point on the line closest to the given point
     */
    private Point nearestPoint(Pair<Point, Point> line, Point p) {

        Point ap = new Point(p.x() - line.first.x(), p.y() - line.first.y());
        Point ab = new Point(line.second.x() - line.first.x(), line.second.y() - line.first.y());

        double abDist = StaticUtils.dist(line.first, line.second);
        double abDist2 = abDist * abDist;
        double abapProduct = ap.ddot(ab);
        double distance = abapProduct / abDist2;

        if (distance < 0) {
            return line.first;
        } else if (distance > 1) {
            return line.second;
        } else {
            return new Point((int)(line.first.x() + ab.x() * distance), (int)(line.first.y() + ab.y() * distance));
        }
    }
}
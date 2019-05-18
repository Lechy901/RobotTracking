package base;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;
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
	
	/**
	 * A constructor which takes a list of horizontal lines and a list of vertical lines, calculates intersections of all vertical lines with all horizontal lines,
	 * and fills the vertices property accordingly.
	 * @param horizontal Horizontal lines in the graph
	 * @param vertical Vertical lines in the graph
	 */
	public ImageGraph(List<Pair<Point, Point>> horizontal, List<Pair<Point, Point>> vertical, Mat image) {
		
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
		
		vertices = StaticUtils.groupPoints(vertices, 50);
		
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
			
			if (nearestUp != null && areConnectedByEdge(current, nearestUp, image)) {
				edges.add(new Pair<Point, Point>(current, nearestUp));
			}
			if (nearestLeft != null && areConnectedByEdge(current, nearestLeft, image)) {
				edges.add(new Pair<Point, Point>(current, nearestLeft));
			}
			
		}
		
	}
	
	/**
	 * A function to determine which line in the graph lies the closest to a given Point
	 * @param p A Point to be used in the calculation
	 * @return The line which lies the closest to the given Point
	 */
	public Pair<Point, Point> getRobotPositionInGraph(Point p) {
		Point nearest = null, secondNearest = null;
		double nearestDist = Double.MAX_VALUE, secondNearestDist = Double.MAX_VALUE;
		
		for(Point cur_vertex : vertices) {
			double cur_dist = StaticUtils.dist(p, cur_vertex);
			if (cur_dist < nearestDist) {
				secondNearest = nearest;
				secondNearestDist = nearestDist;
				nearest = cur_vertex;
				nearestDist = cur_dist;
			} else if (cur_dist < secondNearestDist) {
				secondNearest = cur_vertex;
				secondNearestDist = cur_dist;
			}
		}
		
		return new Pair<Point, Point>(nearest, secondNearest);
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
		for(int i = 0; i < numberOfSteps; i++) {
			int curR = (int)indexer.getDouble(p1.y() + (int)(stepY * i), p1.x() + (int)(stepX * i), 0);
			int curG = (int)indexer.getDouble(p1.y() + (int)(stepY * i), p1.x() + (int)(stepX * i), 1);
			int curB = (int)indexer.getDouble(p1.y() + (int)(stepY * i), p1.x() + (int)(stepX * i), 2);
			
			if (curR > 150 || curG > 150 || curB > 150)
				return false;
		}
		
		return true;
	}
}
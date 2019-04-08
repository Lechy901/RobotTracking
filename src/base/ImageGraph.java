package base;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;

import util.NoIntersectionException;
import util.Pair;
import util.StaticUtils;

public class ImageGraph {
	public List<Point> vertices;
	
	public ImageGraph(List<Pair<Point, Point>> horizontal, List<Pair<Point, Point>> vertical) {
		
		vertices = new ArrayList<Point>();
		
		// add all endpoints for all lines
		for(int i = 0; i < horizontal.size(); i++) {
			vertices.add(horizontal.get(i).first);
			vertices.add(horizontal.get(i).second);
		}
		for(int i = 0; i < vertical.size(); i++) {
			vertices.add(vertical.get(i).first);
			vertices.add(vertical.get(i).second);
		}
		
		// add intersections of lines as vertices
		for(int i = 0; i < horizontal.size(); i++) {
			for(int j = 0; j < vertical.size(); j++) {
				try {
					Point intersection = StaticUtils.lineIntersection(horizontal.get(i), vertical.get(j));
					vertices.add(intersection);
				} catch (NoIntersectionException ex) {
					
				}
			}
		}
		
		vertices = StaticUtils.groupPoints(vertices, 50);
	}
	
	public Pair<Point, Point> getRobotPositionInGraph(Point p) {
		Point nearest = null, secondNearest = null;
		double nearestDist = Double.MAX_VALUE, secondNearestDist = Double.MAX_VALUE;
		
		for(Point cur_vertex : vertices) {
			double cur_dist = StaticUtils.getDist(p, cur_vertex);
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
}
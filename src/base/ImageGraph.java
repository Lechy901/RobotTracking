package base;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;

import util.Direction;
import util.Pair;
import util.Utils;

public class ImageGraph {
	private Point[][] vertices;
	
	public ImageGraph(List<EndVertex> endVertices) {
		if (endVertices.size() % 2 != 0) {
			System.err.println("fuck lichy pocet koncu");
			return;
		}
		
		List<Pair<Point, Point>> horizontal = new ArrayList<Pair<Point, Point>>();
		List<Pair<Point, Point>> vertical = new ArrayList<Pair<Point, Point>>();
		
		while(!endVertices.isEmpty()) {
			EndVertex e = endVertices.get(0);
			Direction opposite = Direction.getOpposite(e.getDir());
			
			EndVertex nearestOpposite = endVertices.stream().filter(x -> x.getDir() == opposite).min((a, b) -> {
				double aDist = Utils.getDist(a.getPoint(), e.getPoint());
				double bDist = Utils.getDist(b.getPoint(), e.getPoint());
				if (aDist < bDist)
					return -1;
				if (aDist > bDist)
					return 1;
				return 0;
			}).get();
			
			endVertices.remove(e);
			endVertices.remove(nearestOpposite);
			
			if (e.getDir() == Direction.RIGHT || e.getDir() == Direction.LEFT)
				vertical.add(new Pair<Point, Point>(e.getPoint(), nearestOpposite.getPoint()));
			if (e.getDir() == Direction.UP || e.getDir() == Direction.DOWN)
				horizontal.add(new Pair<Point, Point>(e.getPoint(), nearestOpposite.getPoint()));
		}
		
		vertices = new Point[horizontal.size() + 2][vertical.size() + 2];
		
		for(int i = 0; i < horizontal.size(); i++) {
			vertices[i+1][0] = horizontal.get(i).first;
			vertices[i+1][vertices[i].length - 1] = horizontal.get(i).second;
		}
		
		for(int i = 0; i < vertical.size(); i++) {
			vertices[0][i+1] = vertical.get(i).first;
			vertices[vertices.length - 1][i+1] = vertical.get(i).second;
		}
		try {
			for(int i = 0; i < horizontal.size(); i++) {
				for(int j = 0; j < vertical.size(); j++) {
					vertices[i+1][j+1] = Utils.lineIntersection(horizontal.get(i), vertical.get(j));
				}
			}
		} catch(Exception ex) {
			System.out.println("There are lines which do not intersect");
			ex.printStackTrace();
		}
	}
}
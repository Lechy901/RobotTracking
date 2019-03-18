package base;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;

import util.Direction;
import util.Pair;

public class ImageGraph {
	private Mat image;
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
				double aDist = Math.sqrt((a.getPoint().x() - e.getPoint().x()) * (a.getPoint().x() - e.getPoint().x()) + (a.getPoint().y() - e.getPoint().y()) * (a.getPoint().y() - e.getPoint().y()));
				double bDist = Math.sqrt((b.getPoint().x() - e.getPoint().x()) * (b.getPoint().x() - e.getPoint().x()) + (b.getPoint().y() - e.getPoint().y()) * (b.getPoint().y() - e.getPoint().y()));
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
			for(int j = 0; j < vertical.size(); j++) {
				//TODO
			}
		}
	}
}
package base;

import org.bytedeco.javacpp.opencv_core.Point;

import util.Direction;

public class EndVertex {

	private Point p;
	private Direction dir;
	
	public EndVertex(Point point, Direction direction) {
		this.p = point;
		this.dir = direction;
	}
	
	public Point getPoint() {
		return p;
	}
	public void setPoint(Point point) {
		p = point;
	}
	public Direction getDir() {
		return dir;
	}
	public void setDir(Direction direction) {
		dir = direction;
	}
	
}

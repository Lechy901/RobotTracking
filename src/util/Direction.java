package util;

public enum Direction {
	LEFT, UP, RIGHT, DOWN, NONE;
	
	public static Direction getOpposite(Direction d) {
		switch (d) {
		case LEFT:
			return Direction.RIGHT;
		case UP:
			return Direction.DOWN;
		case RIGHT:
			return Direction.LEFT;
		case DOWN:
			return Direction.UP;
		default:
			return Direction.NONE;
		}
	}
}

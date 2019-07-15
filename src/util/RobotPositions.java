package util;

import java.util.List;

import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;

public class RobotPositions {
    public Rect[] boundaries;
    public List<Point> centers;
    public List<Point> positionsInGraph;
    
    public RobotPositions(Rect[] boundaries, List<Point> centers, List<Point> positionsInGraph) {
        this.boundaries = boundaries;
        this.centers = centers;
        this.positionsInGraph = positionsInGraph;
    }
}

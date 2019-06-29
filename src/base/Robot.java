package base;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Point;

public class Robot {
    private Point lastKnownPosition;
    private Point lastKnownPositionInGraph;
    private List<Point> positionHistory;
    
    public Robot() {
        lastKnownPosition = new Point(-10000, -10000);
        lastKnownPositionInGraph = new Point(-10000, -10000);
        positionHistory = new ArrayList<Point>();
    }

    public Point getLastKnownPosition() {
        return lastKnownPosition;
    }

    public void setLastKnownPosition(Point lastKnownPosition) {
        this.lastKnownPosition = lastKnownPosition;
    }

    public Point getLastKnownIntersectionInGraph() {
        return lastKnownPositionInGraph;
    }

    public void setLastKnownIntersectionInGraph(Point lastKnownIntersectionInGraph) {
        this.lastKnownPositionInGraph = lastKnownIntersectionInGraph;
    }
    
    public void addPointToHistory(Point p) {
        positionHistory.add(p);
    }
}

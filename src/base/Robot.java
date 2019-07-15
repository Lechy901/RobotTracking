package base;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Point;

/**
 * A class representing one robot
 * 
 * @author ALechovsky
 *
 */
public class Robot {
    private Point lastKnownPosition;
    private Point lastKnownPositionInGraph;
    private List<Point> positionHistory;
    
    public Robot() {
        lastKnownPosition = new Point(-10000, -10000);
        lastKnownPositionInGraph = new Point(-10000, -10000);
        positionHistory = new ArrayList<Point>();
    }
    
    public String getHistory() {
        // remove repeated entries
        List<Point> newHistory = new ArrayList<Point>();
        
        for (int i = 0; i < positionHistory.size(); i++) {
            if (i == 0)
                continue;
            if (positionHistory.get(i - 1) != positionHistory.get(i))
                newHistory.add(positionHistory.get(i));
        }
        
        // format for return
        StringBuilder sb = new StringBuilder();
        
        for (Point p : newHistory) {
            sb.append("[" + p.x() + ", " + p.y() + "], ");
        }
        
        return sb.toString();
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
    
    public void clearHistory() {
        positionHistory.clear();
    }
}

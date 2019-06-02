package base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;

public class RobotArray {

    private Robot[] robots;
    
    public RobotArray(int robotsNumber) {
        robots = new Robot[robotsNumber];
        for(int i = 0; i < robots.length; i++) {
            robots[i] = new Robot();
        }
    }
    
    public void updateRobotPositions(Rect[] positions, double topAndBotLineRatio) {
        while (positions.length > robots.length) {
            int max_area = Arrays.stream(positions).mapToInt(Rect::area).max().getAsInt();
            positions = (Rect[]) Arrays.stream(positions).filter(x -> x.area() != max_area).toArray();
        }
        
        List<Point> availablePositions = convertRectsToPoints(positions, topAndBotLineRatio);
        List<Robot> availableRobots = Arrays.asList(robots);
    }
    
    private List<Point> convertRectsToPoints(Rect[] positions, double topAndBotLineRatio) {
        List<Point> r = new ArrayList<Point>();

        topAndBotLineRatio = topAndBotLineRatio * topAndBotLineRatio;
        
        return r;
    }
}

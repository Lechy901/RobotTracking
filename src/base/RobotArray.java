package base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;

import util.Pair;
import util.StaticUtils;

public class RobotArray {

    private Robot[] robots;
    private RobotTracker tracker;
    private ImageGraph graph;
    
    public RobotArray(int robotsNumber, RobotTracker rt, ImageGraph ig) {
        tracker = rt;
        graph = ig;
        
        robots = new Robot[robotsNumber];
        for(int i = 0; i < robots.length; i++) {
            robots[i] = new Robot();
        }
    }
    
    public RobotPositions findRobotsInImage(Mat image, double topAndBotLineRatio) {
        Rect[] robots = tracker.findRobots(image);
        List<Point> robotCenters = convertRectsToPoints(robots, topAndBotLineRatio);
        List<Point> robotsInGraph = updateRobotPositions(robotCenters, topAndBotLineRatio);
        
        printAllRobotPositions();
        return new RobotPositions(robots, robotCenters, robotsInGraph);
    }
    
    public List<Point> updateRobotPositions(List<Point> positions, double topAndBotLineRatio) {
        
        List<Point> availablePositions = positions;
        List<Robot> availableRobots = new LinkedList<Robot>(Arrays.asList(robots));
        List<Point> robotsInGraph = new ArrayList<Point>();
        
        boolean removed = true;
        while (removed) {
            removed = false;
            loop:
            for(int i = 0; i < availablePositions.size(); i++) {
                for(int j = i + 1; j < availablePositions.size(); j++) {
                    if (StaticUtils.dist(availablePositions.get(i), availablePositions.get(j)) < 80) {
                        availablePositions.remove(j);
                        removed = true;
                        break loop;
                    }
                }
            }
        }
        
        for(Point pos : availablePositions) {
            
            Robot nearestRobot = availableRobots.stream().min((a, b) -> {
                double aDist = StaticUtils.dist(a.getLastKnownPosition(), pos);
                double bDist = StaticUtils.dist(b.getLastKnownPosition(), pos);
                
                if (aDist > bDist)
                    return 1;
                else if (aDist < bDist)
                    return -1;
                return 0;
            }).get();
            
            if (nearestRobot == null)
                continue;
            
            availableRobots.remove(nearestRobot);
            Point nearestRobotInGraph = graph.getRobotPositionInGraph(pos);
            nearestRobot.setLastKnownPosition(pos);
            nearestRobot.setLastKnownIntersectionInGraph(nearestRobotInGraph);
            robotsInGraph.add(nearestRobotInGraph);
        }
        
        return robotsInGraph;
    }
    
    private List<Point> convertRectsToPoints(Rect[] positions, double topAndBotLineRatio) {
        
        while (positions.length > robots.length) {
            int max_area = Arrays.stream(positions).mapToInt(Rect::area).max().getAsInt();
            positions = Arrays.stream(positions).filter(x -> x.area() != max_area).toArray(Rect[]::new);
        }
        
        List<Point> r = new ArrayList<Point>();

        double k = 1 - (topAndBotLineRatio * topAndBotLineRatio * topAndBotLineRatio);
        
        for(Rect position : positions) {
            r.add(new Point(position.x() + (position.width() / 2), position.y() + (int)(position.height() / 2) + (int)(position.height() * k / 2)));
        }
        
        return r;
    }
    
    public void printAllRobotPositions() {
        System.out.println("---");
        int i = 0;
        for (Robot r : robots) {
            System.out.println("Robot " + i++);
            Point rPos = r.getLastKnownPosition();
            System.out.println(rPos.x() + " " + rPos.y());
        }
        System.out.println("===");
    }
}

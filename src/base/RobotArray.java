package base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;

import util.Pair;
import util.RobotPositions;
import util.StaticUtils;

/**
 * A class which represents a set of robots 
 * 
 * @author ALechovsky
 *
 */
public class RobotArray {

    private Robot[] robots;
    private RobotTracker tracker;
    private ImageGraph graph;
    
    /**
     * a constructor
     * @param robotsNumber the number of robots to allocate
     * @param rt reference to the RobotTracker instance
     * @param ig reference to the ImageGraph instance from previous stage
     */
    public RobotArray(int robotsNumber, RobotTracker rt, ImageGraph ig) {
        tracker = rt;
        graph = ig;
        
        robots = new Robot[robotsNumber];
        for(int i = 0; i < robots.length; i++) {
            robots[i] = new Robot();
        }
    }
    
    /**
     * finds robots in a frame, updates their positions in the Robot classes and places it on the ImageGraph
     * @param image the image to search for robots in
     * @param topAndBotLineRatio the camera angle used to shift the square center down
     * @return an object containing the results - the Rects of the Robots, their centers shifted downwards and their positions in the ImageGraph
     */
    public RobotPositions findRobotsInImage(Mat image, double topAndBotLineRatio) {
        Rect[] robots = tracker.findRobots(image);
        Rect[] robotsFixed = fixRobotPositions(robots);
        if (robotsFixed == null) {
            return null;
        }
        List<Point> robotCenters = convertRectsToPoints(robots, topAndBotLineRatio);
        List<Point> robotsInGraph = updateRobotPositions(robotCenters, topAndBotLineRatio);
        
        //printAllRobotPositions();
        return new RobotPositions(robots, robotCenters, robotsInGraph);
    }
    
    /**
     * print last known positions of all robots
     */
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
    
    /**
     * print the history of all robots
     */
    public void printAllRobotHistories() {
        System.out.println("---");
        int i = 0;
        for (Robot r : robots) {
            System.out.println("Robot " + i++);
            System.out.println(r.getHistory());
            r.clearHistory();
        }
        System.out.println("===");
    }

    /**
     * a method which connects current positions to the positions in the last frame,
     * places them on the ImageGraph and updates the Robot positions accordingly
     * @param positions robot positions in the image as Points
     * @param topAndBotLineRatio the camera angle
     * @return a List of Points in the ImageGraph
     */
    private List<Point> updateRobotPositions(List<Point> positions, double topAndBotLineRatio) {
        
        List<Point> availablePositions = positions;
        List<Robot> availableRobots = new LinkedList<Robot>(Arrays.asList(robots));
        List<Point> robotsInGraph = new ArrayList<Point>();
        /*
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
        */
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
            Pair<Point, Boolean> nearestRobotInGraph = graph.getRobotPositionInGraph(pos, 30);
            nearestRobot.setLastKnownPosition(pos);
            nearestRobot.setLastKnownIntersectionInGraph(nearestRobotInGraph.first);
            if (nearestRobotInGraph.second) {
                nearestRobot.addPointToHistory(nearestRobotInGraph.first);
            }
            robotsInGraph.add(nearestRobotInGraph.first);
        }
        
        return robotsInGraph;
    }
    
    /**
     * this method filters the frames based on the quality of detection
     * this implementation only filters out frames which do not have the set number of robots
     * @param positions the detected positions of the robots
     * @return fixed positions or null if the frame is to be discarded
     */
    private Rect[] fixRobotPositions(Rect[] positions) {
        if (positions.length == robots.length)
            return positions;
        if (positions.length < robots.length)
            return null;
        return null;
    }
    
    /**
     * converts robot Rects to Points
     * @param positions robot Rects
     * @param topAndBotLineRatio the camera angle used to shift the center of the Rect downwards
     * @return a List of Points, each representing one robot
     */
    private List<Point> convertRectsToPoints(Rect[] positions, double topAndBotLineRatio) {
        
        while (positions.length > robots.length) {
            int max_area = Arrays.stream(positions).mapToInt(Rect::area).max().getAsInt();
            positions = Arrays.stream(positions).filter(x -> x.area() != max_area).toArray(Rect[]::new);
        }
        
        List<Point> r = new ArrayList<Point>();

        double k = 1 - ((topAndBotLineRatio * topAndBotLineRatio) / 2);
        
        for(Rect position : positions) {
            r.add(new Point(position.x() + (position.width() / 2), position.y() + (int)(position.height() * k)));
        }
        
        return r;
    }
    
    
}

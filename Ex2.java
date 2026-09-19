/*
* File: Ex2.java
* Created: 8th December 2025, 19:51
* Author: Szymon Galutowski
*/

/*
 * Preamble:
 * 
 * My implementation saves space because we're only storing one integer, 
 * the one representing the heading, instead of 3; both coordinates plus
 * the heading. Therefore we only use a third of the space for our robot data.
 * Additionally, this implementation requires us to delete junctions after all
 * their branches have been visited in order to backtrack correctly, meaning 
 * even more space is saved.
 * 
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;
import java.util.List;

/**
 * The class containing the all the code associated with controlling the robot required in Exercise 2.
 * @author Szymon Galutowski
 */
public class Ex2 {
    
    /** A private variable that stores the step taken. */
    private int pollRun = 0;
    /** A private variable that defines the exploration mode - either exploring (true), or backtracking (false). */
    private boolean exploreMode;
    /** A private variable that stores a RobotData object. */
    private RobotData robotData;
    /** A private variable that stores the starting X coordinate for the backtracking to not mix it with a dead end.*/
    private int startX;
    /** A private variable that stores the starting Y coordinate for the backtracking to not mix it with a dead end.*/
    private int startY;

    /**
     * The main method of the Ex2 class that is polled by the maze environment. 
     * 
     * <p> It initialises each run by logging the starting coordinates in <code>startX</code> and <code>startY</code>, and resetting the 
     * <code>robotData</code> if this is the first run. 
     * Them, based on the <code>exploreMode</code>, it then calls a specific controller
     * for exploring (<code>exploreControl()</code>) or backtracking (<code>backtrackControl()</code>). 
     * There are some cases where those controllers decide the other controller must be
     * used, and in that case it calls the other controller.
     * 
     * <p>This method also does the final facing of the robot. Since, once this method finishes, the run
     * finishes, <code>pollRun</code> is incremented during this method.
     * @param robot passed so that the method may use environment properties and methods.
     * @see startX
     * @see startY
     * @see robotData
     * @see exploreMode
     * @see exploreControl
     * @see backtrackControl
     * @see pollRun
     */
    public void controlRobot(IRobot robot) {

        if (pollRun == 0) {
            startX = robot.getLocation().x;
            startY = robot.getLocation().y;
            if (robot.getRuns() == 0) {
                robotData = new RobotData();
                exploreMode = true;
            }
        }

        pollRun++;

        // Initialising so that the compiler doesn't cry at me
        int direction = IRobot.CENTRE;
        
        while (direction == IRobot.CENTRE) {
            if (exploreMode) {
                direction = exploreControl(robot);
            } else {
                direction = backtrackControl(robot);
            }
        }

        robot.face(direction);

    }
    /**
     * A controller method that can be selected by the <code>controlRobot()</code> method to choose a direction.
     * 
     * <p>This method is selected when <code>exploreMode</code> is <code>true</code>. It uses the <code>nonwallExits()</code> method to figure
     * out what kind of location the robot is at, and runs the appropriate handler methods (one of <code>deadEnd()</code>, <code>corridor()</code>, 
     * or <code>junction()</code>). There is a case where if the robot is at a dead end, and it is not at the start, it will
     * return a flag (<code>IRobot.CENTRE</code>) signalling this, and change the <code>exploreMode</code> to <code>false</code>.
     * 
     * @param robot passed so that the method may use environment properties and methods.
     * @return an integer that corresponds to a direction the robot can face towards in the maze environment, or a flag.
     * @see exploreMode
     * @see nonwallExits
     * @see deadEnd
     * @see corridor
     * @see junction
     */
    private int exploreControl(IRobot robot){
        int exits;
        exits = nonwallExits(robot);

        switch (exits) {
            case 1: if ((robot.getLocation().x == startX) && (robot.getLocation().y == startY)) {
                    return deadEnd(robot);
                } else {
                    exploreMode = false; 
                    return IRobot.CENTRE;
                }
            case 2: return corridor(robot);
            default: return junction(robot);
        }
    }

    /**
     * A controller method that can be selected by the <code>controlRobot()</code> method to choose a direction.
     * 
     * <p>This method is selected when <code>exploreMode</code> is <code>false</code>. It uses the <code>nonwallExits()</code> method to figure
     * out what kind of location the robot is at, and runs the appropriate handler methods (one of <code>deadEnd()</code> or <code>corridor()</code>. 
     * 
     * <p>It then uses <code>beenbeforeExits</code> to check if the robot has any branches left to visit in this junction. If so, it sets
     * <code>exploreMode</code> to <code>true</code> and returns a flag, as it means the robot still has some exploring to do.
     * 
     * <p>Otherwise, this means the robot must now reverse. The method gets the past heading of the junction, reverses it, and starts
     * backtracking in that direction.
     * 
     * @param robot passed so that the method may use environment properties and methods.
     * @return an integer that corresponds to a direction the robot can face towards in the maze environment, or a flag.
     * @see exploreMode
     * @see nonwallExits
     * @see deadEnd
     * @see corridor
     * @see robotData
     * @see RobotData#getLastJunction()
     * @see RobotData#removeJunction()
     * @see junction
     */
    private int backtrackControl(IRobot robot) {
        int exits;
        exits = nonwallExits(robot);

        switch (exits) {
            case 1: return deadEnd(robot);
            case 2: return corridor(robot);
        }

        // If we have not returned, we are at a junction
        int beenbefore;
        beenbefore = beenbeforeExits(robot);

        // This means the junction is new, and there are passage exits.
        if (beenbefore < exits) {
            exploreMode = true;
            return IRobot.CENTRE;
        }

        int pastHeading;
        int[] reversed = {IRobot.SOUTH, IRobot.WEST, IRobot.NORTH, IRobot.EAST};
        pastHeading = robotData.getLastJunction();

        robot.setHeading(reversed[pastHeading - IRobot.NORTH]); robotData.removeJunction(); return IRobot.AHEAD;

    }

    /**
     * A method that returns the number of exits that are not walls around the robot in the maze environment.
     * 
     * <p>It does this by looping through all the directions, looking in each of them, and incrementing a counter if 
     * it "sees" a tile that isn't a wall.
     * @param robot passed so that the method may use environment properties and methods.
     * @return an integer specifying the amount of non wall exits around the robot in the maze environment.
     */
    private int nonwallExits (IRobot robot) {
        
        int nonwallCount = 0;
        for (int i=0; i<4; i++) {
            if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
                nonwallCount++;
            }
        }

        return nonwallCount;
    };

    /**
     * A method that handles the robot in the event that it is in a dead end in the maze environment.
     * 
     * <p>It does so by looping through all directions for the only possible valid direction, 
     * and returning this direction.
     * @param robot passed so that the method may use environment properties and methods.
     * @return an integer that corresponds to a direction the robot can face towards in the maze environment.
     */
    private int deadEnd(IRobot robot) {

        for (int i=0; i<4; i++) {
            if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
                return IRobot.AHEAD + i;
            }
        }
        return 0; // This is in place so the compiler doesn't get angry, this will never return
    };

    /**
     * A method that handles the robot in the event that it is in a corridor or corner in the maze environment.
     * 
     * <p>It does so by searching for the only valid direction (aka non wall) that is not behind it. 
     * It checks the front, right, left, and returns the direction that is not a wall.
     * @param robot passed so that the method may use environment properties and methods.
     * @return an integer that corresponds to a direction the robot can face towards in the maze environment.
     */
    private int corridor(IRobot robot) {

        if (robot.look(IRobot.AHEAD) == IRobot.WALL) {
            if (robot.look(IRobot.RIGHT) != IRobot.WALL) {
                return IRobot.RIGHT;
            } else {
                return IRobot.LEFT;
            }
        } else {
            return IRobot.AHEAD;
        }
    };

    /**
     * A method that handles the robot in the event that it is in a junction in the maze environment.
     * 
     * <p>It does so by first logging the junction in <code>robotData</code> if this is the first time visiting this 
     * specific junction. It does so by using the <code>robotData.recordJunction()</code> method.
     * 
     * <p>Then, it compiles ArrayLists for all unvisited directions and allowed directions by looping through all possible
     * directions and looking if they are walls or beenbefore tiles, and addding those directions to a corresponding ArrayList
     * if the conditions are matched. It then checks how many directions are now in those ArrayLists, and based on the amount
     * it (randomly) returns a direction such that is unvisited if possible, and simply valid if not.
     * @param robot passed so that the method may use environment properties and methods.
     * @return an integer that corresponds to a direction the robot can face towards in the maze environment.
     * @see robotData
     * @see RobotData#recordJunction(int)
     */
    private int junction(IRobot robot) {

        // checking if the junction is unvisited - this will be the case if only 1 exit is beenbefore
        if (beenbeforeExits(robot) == 1) {
            robotData.recordJunction(robot.getHeading());
        }

        int direction;
        int size;
        double randno;

        ArrayList<Integer> allowedDirections = new ArrayList<Integer>();

        // Checks all directions, finds valid and unvisited directions and stores them
        for (int i=0; i<4; i++){
            direction = IRobot.AHEAD + i;
            if (robot.look(direction) == IRobot.PASSAGE) {
                allowedDirections.add(direction);
            }
        }

        // We choose an allowed direction.
        size = allowedDirections.size();
        switch(size) {
            case 0: exploreMode = false; return IRobot.BEHIND;
            case 1: return allowedDirections.get(0);
            default: randno = Math.floor(Math.random()*size);
                return allowedDirections.get((int)randno);
        }

    };

    /**
     * A function that returns the amount of beenbefore tiles around the robot in the maze environment.
     * 
     * <p>It calculates this by looping through all the directions, looking in each of them,
     * and incrementing a counter when it "sees" a beenbefore tile.
     * @param robot passed so that the method may use environment properties and methods.
     * @return the amount of beenbefore tiles around the robot in the maze environment.
     */
    private int beenbeforeExits(IRobot robot) {

        int count = 0;
        for (int i=0; i<4; i++) {
            if (robot.look(IRobot.AHEAD + i) == IRobot.BEENBEFORE) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * A function that is called by the maze environment whenever the Reset button is clicked.
     * 
     * <p>When this happens, it calls <code>robotData.resetJunctionCounter()</code> to reset the counter,
     * and also resets <code>pollRun</code> to 0.
     * 
     * @see RobotData#resetJunctionCounter()
     * @see pollRun
     */
    public void reset() {
        robotData.resetJunctionCounter();
        pollRun = 0;
    }

}

/**
 * The class that is used to store robot data, and manipulate this data.
 * @author Szymon Galutowski
 */
class RobotData {

    /** An ArrayList of <code>JunctionRecorder</code> objects - this is where everything is stored. */
    private ArrayList<JunctionRecorder> junctionRecorders = new ArrayList<>();
    /** A integer that stores the amount of junctions found in the maze environment. It is equivalent to the amount of <code>JunctionRecorder</code> objects in the <code>junctionRecorders</code> ArrayList.*/
    private int junctionCounter;

    /**
     * A constructor method such that whenver an object of <code>RobotData</code> is created, <code>resetJunctionCounter()</code> is called.
     * 
     * @see resetJunctionCounter
     */
    public RobotData() {
        resetJunctionCounter();
    }

    /**
     * A method that resets the <code>junctionCounter</code> attribute to 0.
     * 
     * @see junctionCounter
     */
    public void resetJunctionCounter() {
        junctionCounter = 0;
    };

    /**
     * A method that increments the <code>junctionCounter</code> attribute by 1.
     * 
     * @see junctionCounter
     */
    public void incrementJunctionCounter() {
        junctionCounter++;
    };

    /**
     * A method that decrements the <code>junctionCounter</code> attribute by 1.
     * 
     * @see junctionCounter
     */
    public void decrementJunctionCounter() {
        junctionCounter--;
    };

    /**
     * A method that returns the current value of <code>junctionCounter</code>.
     * 
     * @return The current integer value of <code>junctionCounter</code>.
     * @see junctionCounter
     */
    public int getJunctionCounter() {
        return junctionCounter;
    }

    /**
     * A method that records the junction based on the argument provided.
     * 
     * <p>First, <code>incrementJunctionCounter()</code> is called. Then, the method takes the inputHeading 
     * argument in order to construct a <code>JunctionRecorder</code> object, and add it to the end of the
     * <code>junctionRecorders</code> ArrayList.
     * 
     * @param inputHeading the heading of the robot at the time of visiting the junction.
     * @see incrementJunctionCounter
     * @see JunctionRecorder
     * @see junctionRecorders
     */
    public void recordJunction(int inputHeading) {
        incrementJunctionCounter();
        JunctionRecorder recorder = new JunctionRecorder(inputHeading);
        junctionRecorders.add(recorder);
    }

    /**
     * A method that removes the most recent <code>JunctionRecorder</code> object added.
     * 
     * <p>First, <code>decrementJunctionCounter()</code> is called. Then we remove the junction from the ArrayList,
     * using <code>junctionCounter</code> as the index - since it was just decremented, it will match the index of the
     * last element.
     * @see decrementJunctionCounter
     * @see junctionRecorders
     */
    public void removeJunction() {
        decrementJunctionCounter();
        junctionRecorders.removeLast();

    }

    /**
     * A method that returns the heading attribute of a <code>JunctionRecorder</code> object at the given index in the ArrayList.
     * 
     * <p>It does this by getting the last <code>JunctionRecorder</code> object added, and returning the heading contained within it.
     * @return the heading stored in the <code>JunctionRecorder</code> object, or a flag
     */
    public int getLastJunction() {
        JunctionRecorder recorder;
        recorder = junctionRecorders.getLast();
        return recorder.getHeading();
    }

}


/**
 * The class that is used to store individual junction data.
 * @author Szymon Galutowski
 */
class JunctionRecorder {

    /**The attribute that stores the heading of the robot when it first visited the junction. */
    private int heading; 

    /**
     * The constructor method that creates the <code>JunctionRecorder</code> object.
     * 
     * @param inputHeading The input heading relating to the <code>heading</code> attribute.
     * @see heading
     */
    public JunctionRecorder(int inputHeading) {
        heading = inputHeading;
    };

    /**
     * A getter method that returns the value of the <code>heading</code> attribute.
     * @return <code>heading</code>
     */
    public int getHeading() {
        return heading;
    }
    
}
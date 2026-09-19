/*
* File: Ex1.java
* Created: 7th December 2025, 18:47
* Author: Szymon Galutowski
*/

/*
 * Preamble:
 * 
 * For passageExits and nonwallExits I looped through all possible directions and kept a count
 * which was incremented if the specific tile was looked at. I only had 3 controller methods,
 * since I combined crossroads and junctions into one junction method since they did the same
 * thing - I implemented it by adding unvisited and valid directions into ArrayLists and then
 * picking a random one using Math.random() and Math.floor together for accuracy. This was more 
 * space and time efficient than how I did this in Coursework 1 where I used Arrays. As for the
 * deadEnd controller I looped until I found a non-wall exit, and for the corridor controller
 * I chose to go ahead, unless that pathway was blocked - otherwise i'd check if I could go
 * left or right, and pick the one that was a valid exit. I ensured efficiency by reusing code
 * where I could, and not using recursion or nested loops, and using ArrayLists for easier data
 * structure manipulation rather than painstakingly using static Arrays.
 * 
 * My chosen implementation of the RobotData class was for it to contain a private variable,
 * which was an ArrayList of JunctionRecorder objects. Each JunctionRecorder object in turn 
 * stored the relevant information and had getter methods - the setting of the variables is 
 * done in the constructor. The RobotData methods were mostly simple. searchJunction loops 
 * through the ArrayList for a junction at specific coordinates, from the most recent junction 
 * for faster lookup times, i.e. efficiency.
 * 
 * The worst case scenario is that the robot visits every single dead end in a Prim generated
 * maze, before it reaches the end of the maze. This would cause it to have the most unecessary
 * steps taken before it reaches the end. However, if we consider other mazes, there is technically
 * a worst case scenario such that it never reaches the end - if we use a loopy maze, and the start
 * is a dead end, and the exit out of that dead end is a junction - then, if the robot visits all other
 * exits to the junction, then the next time it vists it, it will be directed to head towards the dead
 * end, and then will get stuck in an infinite loop of traversing between the dead end and the junction.
 * 
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList; // Finally gave in
import java.util.List;

/**
 * The class containing the all the code associated with controlling the robot required in Exercise 1.
 * @author Szymon Galutowski
 */
public class Ex1 {
    
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
     * The main method of the Ex1 class that is polled by the maze environment. 
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
        int direction = 0;
        
        if (!exploreMode) {
            direction = backtrackControl(robot);
            if (direction == IRobot.CENTRE) {
                direction = exploreControl(robot);
            }
        } else {
            direction = exploreControl(robot);
            if (direction == IRobot.CENTRE) {
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
     * There is a case where if none of those handler methods are used, then it uses <code>robotData.searchJunction</code> to search for the junction, and the 
     * heading of the robot at that time - 
     * if <code>robotData</code> returns a flag (<code>IRobot.CENTRE</code>) then it sets <code>exploreMode</code> to true, and returns the flag.
     * 
     * <p>Else, it checks for the amount of <code>IRobot.PASSAGE</code> exits by looping through all directions and adding
     * to an ArrayList. If the size of the ArrayList is 0, it returns the reverse of the past heading. Else, it follows the same logic
     * that <code>junction()</code> does.
     * 
     * @param robot passed so that the method may use environment properties and methods.
     * @return an integer that corresponds to a direction the robot can face towards in the maze environment, or a flag.
     * @see exploreMode
     * @see nonwallExits
     * @see deadEnd
     * @see corridor
     * @see robotData
     * @see RobotData#searchJunction(IRobot, int, int)
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
        int pastHeading;
        pastHeading = robotData.searchJunction(robot, robot.getLocation().x, robot.getLocation().y);

        // This means the junction is new, and there are passage exits.
        if (pastHeading == IRobot.CENTRE) {
            exploreMode = true;
            return IRobot.CENTRE;
        }

        // If we haven't returned, there still might be passage exits.
        int direction;
        int size;
        double randno;
        int[] reversed = {IRobot.SOUTH, IRobot.WEST, IRobot.NORTH, IRobot.EAST};

        ArrayList<Integer> unvisitedDirections = new ArrayList<Integer>();

        // Checks all directions, finds valid and unvisited directions and stores them
        for (int i=0; i<4; i++){
            direction = IRobot.AHEAD + i;
            if (robot.look(direction) == IRobot.PASSAGE) {
                unvisitedDirections.add(direction);
            }
        }

        // If there are no passage exits, we reverse, otherwise act as normal
        size = unvisitedDirections.size();
        switch(size) {
            case 0: robot.setHeading(reversed[pastHeading - IRobot.NORTH]); return IRobot.AHEAD;
            case 1: exploreMode = true; return unvisitedDirections.get(0);
            default: exploreMode = true; 
                randno = Math.floor(Math.random()*size);
                return unvisitedDirections.get((int)randno);
        }
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
     * specific junction. It does so by using the <code>robotData.recordJunction()</code> method. It then immediately 
     * outputs this new junction to the terminal for testing/logging purposes, using <code>robotData.printJunction()</code>.
     * 
     * <p>Then, it compiles ArrayLists for all unvisited directions and allowed directions by looping through all possible
     * directions and looking if they are walls or beenbefore tiles, and addding those directions to a corresponding ArrayList
     * if the conditions are matched. It then checks how many directions are now in those ArrayLists, and based on the amount
     * it (randomly) returns a direction such that is unvisited if possible, and simply valid if not.
     * @param robot passed so that the method may use environment properties and methods.
     * @return an integer that corresponds to a direction the robot can face towards in the maze environment.
     * @see robotData
     * @see RobotData#recordJunction(int, int, int)
     * @see RobotData#printJunction(IRobot)
     */
    private int junction(IRobot robot) {

        // checking if the junction is unvisited - this will be the case if only 1 exit is beenbefore
        if (beenbeforeExits(robot) == 1) {
            robotData.recordJunction(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
            robotData.printJunction(robot);
        }

        int direction;
        int size;
        double randno;

        ArrayList<Integer> allowedDirections = new ArrayList<Integer>();
        ArrayList<Integer> unvisitedDirections = new ArrayList<Integer>();

        // Checks all directions, finds valid and unvisited directions and stores them
        for (int i=0; i<4; i++){
            direction = IRobot.AHEAD + i;
            if (robot.look(direction) != IRobot.WALL) {
                allowedDirections.add(direction);
                if (robot.look(direction) != IRobot.BEENBEFORE) {
                    unvisitedDirections.add(direction);
                }
            }
        }

        // Returns a random direction if there are more than 1, does as required for other cases
        size = unvisitedDirections.size();
        switch(size) {
            case 1: return unvisitedDirections.get(0);
            case 0: break;
            default: randno = Math.floor(Math.random()*size);
                return unvisitedDirections.get((int)randno);
        }

        // If we have not returned yet, we must do the same for an allowed direciton.
        size = allowedDirections.size();
        switch(size) {
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
     * A method that returns the current value of <code>junctionCounter</code>.
     * 
     * @return The current integer value of <code>junctionCounter</code>.
     * @see junctionCounter
     */
    public int getJunctionCounter() {
        return junctionCounter;
    }

    /**
     * A method that records the junction based on the arguments provided.
     * 
     * <p>First, <code>incrementJunctionCounter()</code> is called. Then, the method takes the X, Y, and inputHeading 
     * arguments in order to construct a <code>JunctionRecorder</code> object, and add it to the 
     * <code>junctionRecorders</code> ArrayList.
     * 
     * @param X the X coordinate of the junction.
     * @param Y the Y coordinate of the junction.
     * @param inputHeading the heading of the robot at the time of visiting the junction.
     * @see incrementJunctionCounter
     * @see JunctionRecorder
     * @see junctionRecorders
     */
    public void recordJunction(int X, int Y, int inputHeading) {
        incrementJunctionCounter();
        JunctionRecorder recorder = new JunctionRecorder(X, Y, inputHeading);
        junctionRecorders.add(recorder);
    }

    /**
     * A method that prints the information about the most recent junction added.
     * 
     * <p>It first gets the most recent junction by subtracting 1 from <code>junctionCounter</code>
     * since ArrayLists are 0 indexed. It then uses the <code>JunctionRecorder</code> getter methods,
     * <code>JunctionRecorder.getHeading()</code>, <code>JunctionRecorder.getX()</code>, 
     * and <code>JunctionRecorder.getY()</code>. It then translates the heading from an integer into a string,
     * and prints one message containing the relevant information in a readable format to the terminal.
     * @param robot passed so that the method may use environment properties and methods.
     * @see junctionCounter
     * @see JunctionRecorder
     * @see JunctionRecorder#getHeading()
     * @see JunctionRecorder#getX()
     * @see JunctionRecorder#getY()
     */
    public void printJunction(IRobot robot) {

        int robotHeading;
        // Initialising so that the compiler doesn't cry at me
        String heading = "";
        JunctionRecorder recorder;

        recorder = junctionRecorders.get(junctionCounter-1);

        robotHeading = recorder.getHeading();

        switch(robotHeading) {
            case IRobot.NORTH: heading = "NORTH"; break;
            case IRobot.WEST: heading = "WEST"; break;
            case IRobot.SOUTH: heading = "SOUTH"; break;
            case IRobot.EAST: heading = "EAST"; 
        }

        System.out.println("Junction "+junctionCounter+" (x="+recorder.getX()+",y="+recorder.getY()+") heading "+heading);

    }

    /**
     * A method that returns the heading attribute of a <code>JunctionRecorder</code> object with coordinates that match with the parameters.
     * 
     * <p>It does this by iterating backwards for greater efficiency, since the junction is likely to be added recently.
     * Once it finds a match, it returns the heading stored in the object. All the attributes of the object are retrieved
     * using the getter methods: <code>JunctionRecorder.getX()</code>, <code>JunctionRecorder.getY()</code>, and <code>JunctionRecorder.getHeading()</code>.
     * @param robot passed so that the method may use environment properties and methods.
     * @param currentX the current X coordinate of the robot
     * @param currentY the current Y coordinate of the robot
     * @return the heading stored in the <code>JunctionRecorder</code> object, or a flag
     */
    public int searchJunction(IRobot robot, int currentX, int currentY) {
        JunctionRecorder recorder;

        // Iterating backwards to find the junction with greater efficiency
        for (int i = junctionRecorders.size()-1; i >=0 ; i--) {
            recorder = junctionRecorders.get(i);
            if (recorder.getX() == currentX && recorder.getY() == currentY) {
                return recorder.getHeading();
            }
        }
        // In case the junction is new
        return IRobot.CENTRE;
    }

}


/**
 * The class that is used to store individual junction data.
 * @author Szymon Galutowski
 */
class JunctionRecorder {

    /**The attribute that stores the X coordinate of the junction. */
    private int X;
    /**The attribute that stores the Y coordinate of the junction. */
    private int Y;
    /**The attribute that stores the heading of the robot when it first visited the junction. */
    private int heading; 

    /**
     * The constructor method that creates the <code>JunctionRecorder</code> object.
     * 
     * @param inputX The input coordinate relating to the <code>X</code> attribute.
     * @param inputY The input coordinate relating to the <code>Y</code> attribute.
     * @param inputHeading The input heading relating to the <code>heading</code> attribute.
     * @see X
     * @see Y
     * @see heading
     */
    public JunctionRecorder(int inputX, int inputY, int inputHeading) {
        X = inputX;
        Y = inputY;
        heading = inputHeading;
    };

    /**
     * A getter method that returns the value of the <code>X</code> attribute.
     * @return <code>X</code>
     */
    public int getX() {
        return X;
    }

    /**
     * A getter method that returns the value of the <code>Y</code> attribute.
     * @return <code>Y</code>
     */
    public int getY() {
        return Y;
    }

    /**
     * A getter method that returns the value of the <code>heading</code> attribute.
     * @return <code>heading</code>
     */
    public int getHeading() {
        return heading;
    }
    
}
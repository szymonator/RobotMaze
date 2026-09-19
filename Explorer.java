import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList; // Finally gave in
import java.util.List;

public class Explorer {
    
    private int pollRun = 0;
    private int exploreMode;
    private RobotData robotData;
    private int startX;
    private int startY;

    public void controlRobot(IRobot robot) {

        if (pollRun == 0) {
            startX = robot.getLocation().x;
            startY = robot.getLocation().y;
            if (robot.getRuns() == 0) {
                robotData = new RobotData();
                exploreMode = 1;
            }
        }

        pollRun++;

        // Initialising so that the compiler doesn't cry at me
        int direction = 0;
        
        switch(exploreMode) {
            case 0: 
                direction = backtrackControl(robot);
                if (direction == IRobot.CENTRE) {
                    direction = exploreControl(robot);
                }
                break;

            case 1: 
                direction = exploreControl(robot);
                if (direction == IRobot.CENTRE) {
                    direction = backtrackControl(robot);
                }
                break;
        }

        
        robot.face(direction);

    }

    private int exploreControl(IRobot robot){
        int exits;
        exits = nonwallExits(robot);

        switch (exits) {
            case 1: if ((robot.getLocation().x == startX) && (robot.getLocation().y == startY)) {
                    return deadEnd(robot);
                } else {
                    exploreMode = 0; 
                    return IRobot.CENTRE;
                }
            case 2: return corridor(robot);
            default: return junction(robot);
        }
    }

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
            exploreMode = 1;
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
            case 1: exploreMode = 1; return unvisitedDirections.get(0);
            default: exploreMode = 1; 
                randno = Math.floor(Math.random()*size);
                return unvisitedDirections.get((int)randno);
        }
    }

    private int nonwallExits (IRobot robot) {
        
        int nonwallCount = 0;
        for (int i=0; i<4; i++) {
            if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
                nonwallCount++;
            }
        }

        return nonwallCount;
    };

    private int deadEnd(IRobot robot) {

        for (int i=0; i<4; i++) {
            if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
                return IRobot.AHEAD + i;
            }
        }
        return 0; // This is in place so the compiler doesn't get angry, this will never return
    };

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

    private int beenbeforeExits(IRobot robot) {

        int count = 0;
        for (int i=0; i<4; i++) {
            if (robot.look(IRobot.AHEAD + i) == IRobot.BEENBEFORE) {
                count++;
            }
        }
        return count;
    }

    public void reset() {
        robotData.resetJunctionCounter();
        pollRun = 0;
    }

}

class RobotData {

    private ArrayList<JunctionRecorder> junctionRecorders = new ArrayList<>();
    private int junctionCounter;

    public RobotData() {
        resetJunctionCounter();
    }

    public void resetJunctionCounter() {
        junctionCounter = 0;
    };

    public void incrementJunctionCounter() {
        junctionCounter++;
    };

    public int getJunctionCounter() {
        return junctionCounter;
    }

    public void recordJunction(int X, int Y, int inputHeading) {
        incrementJunctionCounter();
        JunctionRecorder recorder = new JunctionRecorder(X, Y, inputHeading);
        junctionRecorders.add(recorder);
    }

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

class JunctionRecorder {

    private int X;
    private int Y;
    private int heading; 

    public JunctionRecorder(int inputX, int inputY, int inputHeading) {
        X = inputX;
        Y = inputY;
        heading = inputHeading;
    };

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public int getHeading() {
        return heading;
    }
    
}
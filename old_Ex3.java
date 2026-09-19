/*
 * File:    Broken	.java
 * Created: 7 September 2001
 * Author:  Stephen Jarvis
 */

/*
 * Preamble:
 * 
 * I chose my design by looking at the requirements and thinking in terms of steps.
 * I decided it would be best to find preferred directions, then validate them and
 * all other possible directions. Then the process begun of choosing them randomly
 * or returning them based on the validity, as the customer asked for.
 * 
 * My solution ensures the robot will always move towards a direction where possible,
 * and it meets all the customer requirements. However, the requirements are such that
 * the robot will not always find the target, since it will get stuck in a loop of going
 * back and forth at dead ends that face a preferred direction. I'd suggest using 
 * IRobot.BEENBEFORE to prioritise directions which have not been visited yet, which
 * would solve this issue.
 * 
 * NOTE: There are methods in this class which should be private, but have been left
 *       public so that they show in the JavaDoc Doc Comment page.
 */


import uk.ac.warwick.dcs.maze.logic.IRobot;

/**
 * The class containing all the code required in Exercise 3.
 * @author Szymon Galutowski
 */
public class Ex3 
{
   /**
     * Does not return anything. Utilises robot methods and properties.
     * 
     * <p>
     * Serves as a main method from which the testing handler is called, and 
     * from which the <code>headingController()</code> method is called, which
     * contains all the logic.
     * 
     * It is also where the heading of the robot is set.
     * 
     * @param robot passed so that the method may use environment properties and methods
     * @see headingController
     */
     public void controlRobot(IRobot robot) {

          int heading;

          heading = headingController(robot);
          ControlTest.test(heading, robot);
          robot.setHeading(heading);

     }

   /**
     * A method to discern if the Target is North or South, or along the same latitude.
     * This is done relative to the position of the robot.
     *
     * <p>
     * This method along with <code>isTargetEast()</code> are used in <code>headingController()</code>.
     * 
     * @param robot passed so that the method may use environment properties and methods
     * @return An integer specifying the heading of the target relative to the robot.
     * @see isTargetEast
     * @see headingController
     */
     public static int isTargetNorth( IRobot robot ) {
          if ( robot.getLocation().y < robot.getTargetLocation().y ) {
               return -1;
          } else if ( robot.getLocation().y > robot.getTargetLocation().y ) {
               return 1;
          } else {
               return 0;
          }
     }
     
   /**
     * A method to discern if the Target is East or West, or along the same longitude.
     * This is done relative to the position of the robot.
     *
     * <p>
     * This method along with <code>isTargetNorth()</code> are used in <code>headingController()</code>.
     *
     * @param robot passed so that the method may use environment properties and methods
     * @return An integer specifying the heading of the target relative to the robot.
     * @see isTargetNorth
     * @see headingController
     */
     public static int isTargetEast( IRobot robot ) {

          if ( robot.getLocation().x < robot.getTargetLocation().x ) {
               return 1;
          } else if ( robot.getLocation().x > robot.getTargetLocation().x ) {
               return -1;
          } else {
               return 0;
          }
     }

   /**
     * A method that takes in a target heading in the form of a cardinal direction, 
     * and checks what kind of tile is in that direction.
     * 
     * <p>
     * The cardinal directions can be referenced with <code>IRobot.NORTH</code>, and
     * the tile can be referenced in the same way, e.g. <code>IRobot.WALL</code>.
     * The actual values being held in these properties are integers.
     * 
     * <p>
     * This method takes advantage of the fact that the directions are integers separated
     * by 1, and increments/decrements them in order to face the robot where needed, and use
     * the provided robot.look() method which only works for relative directions, e.g.
     * <code>IRobot.RIGHT</code>.
     * 
     * Used in <code>headingController()</code>.
     * 
     * @param robot passed so that the method may use environment properties and methods
     * @param targetHeading an integer which represents a cardinal direction in IRobot
     * @return an integer which represents a tile type in IRobot
     * @see headingController
     */
     public static int lookHeading( IRobot robot, int targetHeading ) {
          
          int currentHeading = robot.getHeading();
          int output;
          
          // Useful for the code will be checking an invalid direction
          if (targetHeading == IRobot.CENTRE) {
               return IRobot.WALL;
          }

          if (targetHeading == currentHeading) {

               output = robot.look(IRobot.AHEAD);

          } else if (targetHeading < currentHeading) {

               // "Decreases" the required amount of anticlockwise turns
               robot.face(IRobot.LEFT-(currentHeading-targetHeading-1));
               output =  robot.look(IRobot.AHEAD);
               robot.face(IRobot.RIGHT+(currentHeading-targetHeading-1));

          } else {

               // "Increases" the required amount of clockwise turns
               robot.face(IRobot.RIGHT+(targetHeading-currentHeading-1));
               output =  robot.look(IRobot.AHEAD);
               robot.face(IRobot.LEFT-(targetHeading-currentHeading-1));

          };

          return output;
     }

   /**
     * A method that contains the main logic for deciding the direction of the robot.
     * 
     * <p>
     * It firsts uses the <code>isTargetNorth()</code> and <code>isTargetEast()</code>
     * methods to choose the preferred directions. It then uses <code>lookHeading()</code>
     * to check all valid directions, since if <code>lookHeading()</code> returns <code>IRobot.WALL</code>
     * then the direction is invalid.
     * 
     * <p>
     * Using these methods the preferred directions are found, and if there are more than one,
     * the direction to be returned is chosen randomly from the array. If there is only one 
     * preferred direction, it is returned immediately. Otherwise, the same logic is then applied
     * to the rest of the valid directions, which there will often be at least one of.
     * 
     * <p>
     * The cardinal directions can be referenced with <code>IRobot.NORTH</code>, and
     * the tile can be referenced in the same way, e.g. <code>IRobot.WALL</code>.
     * The actual values being held in these properties are integers.
     * 
     * @param robot passed so that the method may use environment properties and methods
     * @return an integer that represents a cardinal direction
     */
     public static int headingController( IRobot robot ) {
          
          /* 
           *  This section creates a preferedDirections array, and fills it with the preferred
           * direction to go in. I don't care about the preferred direction if we are on the 
           * same latitude/longitude. We also have allowed directions, and cardinal directions
           * for looping through.
           */

          int[] preferredDirections = {IRobot.CENTRE, IRobot.CENTRE};
          int[] allowedDirections = {IRobot.CENTRE, IRobot.CENTRE, IRobot.CENTRE, IRobot.CENTRE};
          int[] cardinalDirections = {IRobot.NORTH, IRobot.EAST, IRobot.SOUTH, IRobot.WEST};

          // These count variables are for keeping track of directions in the above arrays.

          int allowedCount;
          int preferredCount;

          double randno;
          int trueIndex;

          // Here we fill our preferredDirections array. At least one value will be input, probably 2.

          preferredCount = 0;
          switch (isTargetNorth(robot)) {
               case 1:
                    preferredDirections[0] = IRobot.NORTH;
                    preferredCount++;
                    break;
               case -1:
                    preferredDirections[0] = IRobot.SOUTH;
                    preferredCount++;
                    break;
          }

          switch (isTargetEast(robot)) {
               case 1:
                    preferredDirections[1] = IRobot.EAST;
                    preferredCount++;
                    break;
               case -1:
                    preferredDirections[1] = IRobot.WEST;
                    preferredCount++;
                    break;
          }

          // Then we check if those, and all, directions are valid, and filter them out.

          allowedCount = 0;
          for (int direction : cardinalDirections) {
			if (lookHeading(robot, direction) != IRobot.WALL) {
                    allowedDirections[direction - IRobot.NORTH] = direction;
                    allowedCount++;
               } else if (preferredDirections[0] == direction) {
                    preferredDirections[0] = IRobot.CENTRE;
                    preferredCount--;
               } else if (preferredDirections[1] == direction) {
                    preferredDirections[1] = IRobot.CENTRE;
                    preferredCount--;
               };
		};
          
          /* 
           * Now we are left with an array of either 2 preferred directions, or 1, or none.
           *  We also have an array filled with allowed directions - there should be at least 1.
           */

          if (preferredCount == 2) {

               // We have to choose randomly between the 2 preferred directions.
               randno = Math.floor(Math.random()*2);
               return preferredDirections[(int) randno];

          } else if (preferredCount == 1) {

               // Our job is easy - we just have to find the only preferred direction, and return it.
               for (int i=0; i<2; i++) {
                    if (preferredDirections[i] != IRobot.CENTRE) {
                         return preferredDirections[i];
                    }
               }
          } 

          /* 
           * Since we have gotten to this part of the program, it means we have no preferred directions.
           * We now essentially repeat what we just did, but for allowed directions.
           */

          if (allowedCount > 1) {

               randno = (int) Math.floor(Math.random()*allowedCount);
               // Has to start at -1 because arrays are 0-indexed
               trueIndex = -1;
               for (int i=0; i<4; i++) {
                    if (allowedDirections[i] != IRobot.CENTRE) {
                         trueIndex++;
                    }
                    if (trueIndex == randno) {
                         return allowedDirections[i];
                    }
               }

          } else if (allowedCount == 1) {

               for (int i=0; i<4; i++) {
                    if (allowedDirections[i] != IRobot.CENTRE) {
                         return allowedDirections[i];
                    }
               }
          };

          return 1;
     }
     
   /**
     * A method that is called in <code>robotController</code> to test the correctness of the solution.
     */
     public void reset() {                                                                                                                                                     
          ControlTest.printResults();
     }
}

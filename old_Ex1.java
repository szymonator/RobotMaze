/*
* File: Ex1.java
* Created: 30 October 2025, 18:42
* Author: Szymon Galutowski
*/

/*
 * Preamble:
 * 
 * I designed my code so that it loops constantly, generating a random direction until
 * it finds one that doesn't face a wall. When it does, it exits the loop, outputs 
 * the current state, and makes the robot face said direction. I designed it this way
 * so that it would be short and clear to read, and hopefully efficient.
 * 
 * NOTE: There are methods in this class which should be private, but have been left
 *       public so that they show in the JavaDoc Doc Comment page.
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;

/**
 * The class containing all the code required in Exercise 1.
 * @author Szymon Galutowski
 */
public class Ex1
{

	/**
	 * Does not return anything. Utilises robot methods and properties.
	 * 
	 * <p>
	 * This method selects a pseudorandom direction, which will be reflected in the maze environment.
	 * This direction must not be facing a wall, in order for it to be chosen.
	 * 
	 * <p>
	 * There is an output to the terminal stating the direction and location type of the robot for 
	 * testing/logging purposes. In order to fulfill its purpose, it uses the <code>locationType()</code> 
	 * method.
	 * 
	 * @param robot passed so that the method may use environment properties and methods
	 * @see locationType
	 */

	public void controlRobot(IRobot robot) {

		int randno;
		int direction = IRobot.CENTRE;
		int[] possibleDirections = {IRobot.AHEAD, IRobot.RIGHT, IRobot.BEHIND, IRobot.LEFT};
		String[] wordedDirections = {"forward ", "right ", "backwards ", "left "};

		do {
			randno = (int) Math.round(Math.random()*3);
			if (robot.look(possibleDirections[randno]) != IRobot.WALL) {
				direction = possibleDirections[randno];
			}
		} while (direction == IRobot.CENTRE);

		System.out.println("I'm going "+wordedDirections[randno]+ locationType(robot) + "!");
		robot.face(direction); /* Face the robot in this direction */ 
	}
	/**
	 * Returns a string in order to help with the logging output in the <code>controlRobot()</code> method
	 * 
	 * 
	 * @param robot passed so that the method may use environment properties and methods
	 * @return      a string specifying the type of location that the robot is in
	 * @see controlRobot
	 */
	public String locationType(IRobot robot) {

		int wallCount = 0;
		String[] locationTypes = {"at a crossroads", "at a junction", "down a corridor", "at a dead end"};

		for (int i=0; i<4; i++) {
			if (robot.look(IRobot.AHEAD + i) == IRobot.WALL) {
				wallCount++;
			};
		};

		return locationTypes[wallCount];

	}

}




















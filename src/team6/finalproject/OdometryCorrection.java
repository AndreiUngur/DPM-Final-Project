package team6.finalproject;

/**
 * Odometer correction class.
 * 
 * Corrects odometer values while the robot is traveling to waypoints (i.e. moving in a straight line).
 * Works by snapping the odometer's position to a grid line's X or Y position when passing over it.
 * 
 * @author erick
 * @version 0.1
 */
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class OdometryCorrection implements TimerListener {
	
	//variables
	private Timer timer;
	private Odometer odometer;
	
	private static final int REFRESH_RATE = 10;
	private static final double SENSOR_TO_CENTRE = 4.5;
	private static final double GRID_WIDTH = 30.48;
	private static final double ODOMETER_ERROR_THRESHOLD = 0.5;

	/**
	 * Constructor for the odometer correction
	 * @param odometer	The odometer object running on a separate Timer that is to be corrected
	 */
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		this.timer = new Timer(REFRESH_RATE,this);
	}

	/**
	 * Checks if odometer reading can be corrected every Timer loop and executes the correction
	 * whenever it is needed.
	 */
	@Override
	public void timedOut() {
		if(LightPoller.blackLine()){
			correctOdometerPosition();
		}
	}

	/**
	 * Corrects the odometer's reading whenever a black line is detected.
	 * 
	 * This works by snapping the odometer to a value of the grid when it passes over a black line.
	 * The odometer reading snaps to the value of the nearest black grid line.
	 */
	private void correctOdometerPosition(){
		
		double positionX = odometer.getX();
		double positionY = odometer.getY();
		
		if (isRobotNearGridLine(positionX)) {
			double actualPosition = getNearestGridLine(positionX)+getHorizontalSensorToCentreDistance();
			double[] position = {actualPosition,0,0};
			boolean[] update =  {true,false,false};
			
			odometer.setPosition(position,update);
		}
		
		if (isRobotNearGridLine(positionY)) {
			double actualPosition = getNearestGridLine(positionY)+getVerticalSensorToCentreDistance();
			double[] position = {0,actualPosition,0};
			boolean[] update = {false,true,false};
			
			odometer.setPosition(position,update);
		}
		
	}
	
	/**
	 * Determines if the odometer reading one one of the two axes is close enough to a grid line
	 * to snap to it.
	 * @param position	The x or y position of the robot according to the odometer
	 * @return			The boolean value indicating if the odometer should snap to this grid line.
	 */
	private boolean isRobotNearGridLine(double position) {
		
		double distanceFromLine = GRID_WIDTH-position%GRID_WIDTH;
		
		if (Math.abs(distanceFromLine) < ODOMETER_ERROR_THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Gets the position of the nearest grid line in either the x or y direction (depending on which axis'
	 * position is passed as a parameter).
	 * 
	 * This works by dividing the current position by the grid width to see which grid line it's closest to,
	 * and then multiplying that number by the grid width again to get that line's distance from the origin.
	 * 
	 * @param position	The x or y position of the robot according to the odometer
	 * @return			The corresponding position of the nearest grid line
	 */
	private double getNearestGridLine(double position) {
		return (int)((position+ODOMETER_ERROR_THRESHOLD)/GRID_WIDTH)*GRID_WIDTH;
	}
	
	/**
	 * Gets the horizontal distance between the sensor and the centre of rotation.
	 * @return		A double representing the horizontal distance in cm
	 */
	private double getHorizontalSensorToCentreDistance() {
		return Math.cos(angleToRadians(odometer.getAng()))*SENSOR_TO_CENTRE;
	}
	
	/**
	 * Gets the vertical distance between the sensor and the centre of rotation.
	 * @return		A double representing the vertical distance in cm
	 */
	private double getVerticalSensorToCentreDistance() {
		return Math.sin(angleToRadians(odometer.getAng()))*SENSOR_TO_CENTRE;
	}
	
	/**
	 * Converts an angle from degrees to radians.
	 * @param angle	A double representing an angle in degrees
	 * @return		The equivalent angle in radians
	 */
	private double angleToRadians(double angle) {
		return 2*Math.PI*angle/360;
	}
	
	public void stop() {
		if (this.timer != null)
			this.timer.stop();
	}
	public void start() {
		if (this.timer != null)
			this.timer.start();
	}
	
}
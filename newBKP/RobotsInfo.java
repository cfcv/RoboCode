import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;


public class RobotsInfo {
	
	public class Enemy {
		public String name;
		public double bearing, heading, speed, x, y, distance, changehead, energy;
		public long ctime; 		//
		public boolean live;
		public boolean readed;
		public Point2D.Double guessPosition(long when) {
			double diff = when - ctime;
			double newY = y + Math.cos(heading) * speed * diff;
			double newX = x + Math.sin(heading) * speed * diff;
			
			return new Point2D.Double(newX, newY);
		}
	}
	
	private Map<String, Enemy> mapaNomes;
	private Enemy closestRobot;
	private AdvancedRobot self;
	public int readedRobots = 0;

	
	RobotsInfo(AdvancedRobot me) {
		self = me;
		mapaNomes = new HashMap<String, Enemy>();
		closestRobot = null;
		this.readedRobots = 0;
	}
	
	public int GetMapSize() {
		return mapaNomes.size();
	}
	
	public Enemy getRobot(String name) {
		return mapaNomes.get(name);
	}
	
	public Enemy getClosestRobot() {
		return closestRobot;			
	}
	
	public Iterator<Enemy> getMapIterator() {
		return mapaNomes.values().iterator();
	}
	
	public void addRobotEvent(ScannedRobotEvent event) {
		// Add new robot or update existing entry
		String robotName = event.getName();
		Enemy en = new Enemy();
		
		if (mapaNomes.get(robotName) != null) {
			en = mapaNomes.get(robotName);
			if(!en.readed) {
				this.readedRobots += 1;
				en.readed = true;
			}
		}
		
		double absbearing_rad = (self.getHeadingRadians()+event.getBearingRadians())%(2*RoboUtils.PI);
		//this section sets all the information about our target
		en.name = event.getName();
		double h = RoboUtils.normaliseBearing(event.getHeadingRadians() - en.heading);
		h = h/(self.getTime() - en.ctime);
		en.changehead = h;
		en.x = self.getX()+Math.sin(absbearing_rad)*event.getDistance(); //works out the x coordinate of where the target is
		en.y = self.getY()+Math.cos(absbearing_rad)*event.getDistance(); //works out the y coordinate of where the target is
		en.bearing = event.getBearingRadians();
		en.heading = event.getHeadingRadians();
		en.ctime = self.getTime();				//game time at which this scan was produced
		en.speed = event.getVelocity();
		en.distance = event.getDistance();	
		en.energy = event.getEnergy();
		en.live = true;

		mapaNomes.put(robotName, en);
		
		if (closestRobot == null)
			closestRobot = en;
		else {
			// Recalcula o closestRobot
			closestRobot = recalculateClosestRobot();
		}
	}
	
	private Enemy recalculateClosestRobot() {
		if (mapaNomes.size() == 0)
			return null;
		Collection<Enemy> values = mapaNomes.values();
		Iterator<Enemy> it = values.iterator();
		Enemy minTemp = new Enemy();
		minTemp.name = "NullTarget";
		minTemp.distance = Double.MAX_VALUE;
		while (it.hasNext()) {
			Enemy robot = it.next();
			if (robot.live && robot.distance < minTemp.distance)
				minTemp = robot;
		}
		return minTemp;
	}
	
	public void clearHash() {
		this.readedRobots = 0;
		Collection<Enemy> values = mapaNomes.values();
		Iterator<Enemy> it = values.iterator();
		System.out.println("clear hash");
		while(it.hasNext()) {
			Enemy aux = it.next();
			aux.readed = false;
		}	
	}
	
	public void updateRobotDeath(RobotDeathEvent event) {
		Enemy en = getRobot(event.getName());
		en.live = false;
		System.out.println("robot " + event.getName() + " morreu e foi atualizado na hash.");
		
		if (closestRobot.name == en.name) {
			System.out.println("closestRobot: " + closestRobot.name);
			closestRobot.live = false;
			closestRobot = recalculateClosestRobot();
			System.out.println("recalculated closestRobot: " + closestRobot.name);
		}
	}

}

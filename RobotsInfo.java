import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;


public class RobotsInfo {
	
	public class Enemy {
		public String name;
		public double bearing,heading,speed,x,y,distance,changehead;
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
		return this.mapaNomes.size();
	}
	public Enemy getRobot(String name) {
		return mapaNomes.get(name);
	}
	
	public Enemy getClosestRobot() {
		if (closestRobot == null)
			return null;
		return closestRobot;
	}
	
	public Iterator<Enemy> getMapIterator() {
		return mapaNomes.values().iterator();
	}
	
	public void printHash() {
		Collection<Enemy> values = mapaNomes.values();
		Iterator<Enemy> it = values.iterator();
		while(it.hasNext()) {
			Enemy aux = it.next();
			System.out.print(aux.readed + " ");
			//aux.readed = false;
		}
		System.out.println();
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
	
	public void addRobotEvent(ScannedRobotEvent event) {
		// Add new robot or update existing entry
		Enemy en = new Enemy();
		//en.readed = false;
		String robotName = event.getName();
		if (mapaNomes.get(robotName) != null) {
			en = mapaNomes.get(robotName);
			if(!en.readed) {
				this.readedRobots += 1;
				en.readed = true;
			}
		}
		
		System.out.println("Others: " + this.self.getOthers());
		System.out.println("ReadedRobots: " + this.readedRobots);
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
		en.live = true;
		mapaNomes.put(robotName, en);
		
		if (closestRobot == null)
			closestRobot = en;
		else if (closestRobot.name == event.getName()) {
			// Recalcula o closestRobot
			closestRobot = recalculateClosestRobot();
		}
		else if (en.distance < closestRobot.distance) {
			// Update the closestRobot to the new one
			closestRobot = en;			
		}
	}
	
	private Enemy recalculateClosestRobot() {
		if (mapaNomes.size() == 0)
			return null;
		Collection<Enemy> values = mapaNomes.values();
		Iterator<Enemy> it = values.iterator();
		Enemy minTemp = it.next();
		while (it.hasNext()) {
			Enemy robot = it.next();
			if (robot.distance < minTemp.distance)
				minTemp = robot;
		}
		return minTemp;
	}
}

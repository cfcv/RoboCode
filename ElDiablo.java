import java.awt.geom.Point2D;
import java.util.*;

import RobotsInfo.Enemy;
import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

public class ElDiablo extends TeamRobot {
//	private RobotsInfo enemiesInfo;
//	private RobotsInfo friendsInfo;
	Hashtable targets;
	Enemy target;
	GravPoint antiGPoints[];
	int midGravTurnCount;
	public void run () {
		midGravTurnCount = 0;
		while(true) {
			targets = new Hashtable();
			target = new Enemy();
			doMovement();
			scanField();
//			chooseEnemyToFire();
			execute();
		}
	}

	
	class GravPoint {
		double posX, posY, mass;
		public GravPoint (double px, double py, double force) {
			this.posX = px;
			this.posY = py;
			this.mass = force;
		}
	}
	
	private void scanField () {
		setTurnRadarLeftRadians(2 * Math.PI);
	}
	
/*	private void chooseEnemyToFire() {
		ScannedRobotEvent closestEnemy = enemiesInfo.getClosestRobot();
		if (closestEnemy == null)
			this.setTurnRadarLeft(45);
		else {
		
		if (getGunHeat() == 0) {
		       double firePower = Math.min(500 / closestEnemy.getDistance(), 3);
		       // calculate speed of bullet
		       double bulletSpeed = 20 - firePower * 3;
		       // distance = rate * time, solved for time
		       long time = (long)(closestEnemy.getDistance() / bulletSpeed);
		       
		      /* double futureX = closestEnemy.getFutureX(time);
		       double futureY = closestEnemy.getFutureY(time);
		       double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
		       // turn the gun to the predicted x,y location
		       setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));*/
/*		       
		       Bullet bullet = this.setFireBullet(firePower);//set(Rules.MAX_BULLET_POWER);
		       System.out.println("atirou");


		       // Get the velocity of the bullet
		       if (bullet != null) {
		           double bulletVelocity = bullet.getVelocity();
		       }
		   }
		}
		
	}
	
	public void OnScannedRobot (ScannedRobotEvent event) {
		if (this.isTeammate(event.getName())) {
			friendsInfo.addRobotEvent(event);
		} else {
			enemiesInfo.addRobotEvent(event);
		}
	}
*/	 
	
	private void doMovement () {
		antiGravMove();
	}
	
	double midpointMass = 0;
	private void antiGravMove() {
   		double xforce = 0;
	    double yforce = 0;
	    double force;
	    double ang;
	    
	    GravPoint p;
		Enemy en;
    	Enumeration e = targets.elements();
	    //cycle through all the enemies.  If they are alive, they are repulsive.  Calculate the force on us
		while (e.hasMoreElements()) {
    	    en = (Enemy)e.nextElement();
			if (en.live) {
				p = new GravPoint(en.x,en.y, -1000);
		        force = p.mass/Math.pow(RoboUtils.getRange(getX(),getY(),p.posX,p.posY),2);
		        //Find the bearing from the point to us
		        ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.posY, getX() - p.posX)); 
		        //Add the components of this force to the total force in their respective directions
		        xforce += Math.sin(ang) * force;
		        yforce += Math.cos(ang) * force;
			}
	    }
	    
		/**The next section adds a middle point with a random (positive or negative) strength.
		The strength changes every 5 turns, and goes between -1000 and 1000.  This gives a better
		overall movement.**/
		midGravTurnCount++;
		if (midGravTurnCount > 5) {
			out.println("change center mass");
			midGravTurnCount = 0;
			midpointMass = (Math.random() * 2000) - 1000;
		}
		p = new GravPoint(getBattleFieldWidth()/2, getBattleFieldHeight()/2, midpointMass);
		force = p.mass/Math.pow(RoboUtils.getRange(getX(),getY(),p.posX,p.posY),1.5);
	    ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.posY, getX() - p.posX)); 
	    xforce += Math.sin(ang) * force;
	    yforce += Math.cos(ang) * force;
	   
	    /**The following four lines add wall avoidance.  They will only affect us if the bot is close 
	    to the walls due to the force from the walls decreasing at a power 3.**/
	    xforce += 5000/Math.pow(RoboUtils.getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
	    xforce -= 5000/Math.pow(RoboUtils.getRange(getX(), getY(), 0, getY()), 3);
	    yforce += 5000/Math.pow(RoboUtils.getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
	    yforce -= 5000/Math.pow(RoboUtils.getRange(getX(), getY(), getX(), 0), 3);
	    
	    //Move in the direction of our resolved force.
	    goTo(getX()-xforce,getY()-yforce);
	}

	private int turnTo(double angle) {
	    double ang;
    	int dir;
	    ang = RoboUtils.normaliseBearing(getHeading() - angle);
	    if (ang > 90) {
	        ang -= 180;
	        dir = -1;
	    }
	    else if (ang < -90) {
	        ang += 180;
	        dir = -1;
	    }
	    else {
	        dir = 1;
	    }
	    setTurnLeft(ang);
	    return dir;
	}

	private void goTo(double x, double y) {
	    double dist = 20; 
	    double angle = Math.toDegrees(RoboUtils.absbearing(getX(),getY(),x,y));
	    double r = turnTo(angle);
	    setAhead(dist * r);
	}
	
	
}


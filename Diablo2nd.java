import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import robocode.*;

public class Diablo2nd extends TeamRobot {
	private RobotsInfo enemiesInfo;
	private RobotsInfo friendsInfo;
	private double PI = Math.PI;
	private int radarDirection=1;

	public void run () {
		setAdjustRadarForGunTurn(true);
		//setTurnRadarRightRadians(2*PI);
		enemiesInfo = new RobotsInfo(this);
		friendsInfo = new RobotsInfo(this);
		//addCustomEvent(new RadarTurnCompleteCondition(this));
		while(true) {
			//setTurnRadarLeftRadians(2*PI);
			sweep();
			antiGravMove();
			//chooseEnemyToFire();
			//setAhead(10);
			execute();
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e){
		out.println("OnScannedRobot");
		
		enemiesInfo.addRobotEvent(e);
		out.println("scanned enemy");
	}
	
	private void chooseEnemyToFire() {
		this.setTurnRadarLeftRadians(PI/4);
		RobotsInfo.Enemy closestEnemy;

		closestEnemy = enemiesInfo.getClosestRobot();
		if (closestEnemy == null)
			return;

		System.out.println("closest enemy found: " + closestEnemy.name);
		
		if (getGunHeat() == 0) {
		       double firePower = Math.min(500 / closestEnemy.distance, 3);
		       // calculate speed of bullet
		       double bulletSpeed = 20 - firePower * 3;
		       // distance = rate * time, solved for time
		       long time = (long)(closestEnemy.distance / bulletSpeed);
		       
		      /* double futureX = closestEnemy.getFutureX(time);
		       double futureY = closestEnemy.getFutureY(time);
		       double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
		       // turn the gun to the predicted x,y location
		       setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));*/
		       
		       Bullet bullet = this.setFireBullet(2);//set(Rules.MAX_BULLET_POWER);
		       System.out.println("atirou");


		       // Get the velocity of the bullet
		       if (bullet != null) {
		           double bulletVelocity = bullet.getVelocity();
		       }
		   }
		}
		
	
	private void processScannedRobots() {
		Vector<ScannedRobotEvent> events = this.getScannedRobotEvents();
		System.out.println(events.size());
		Iterator<ScannedRobotEvent> it = events.iterator();
		//System.out.println("processing scanned robots");
		while(it.hasNext()) {
			System.out.println("next iteration");
			ScannedRobotEvent event = it.next();

			//enemiesInfo.addRobotEvent(event);
			System.out.println("scanned enemy");
			
		}
	}
	
	private int midpointcount = 0;			//Number of turns since that strength was changed.
	private double midpointstrength = 0;	//The strength of the gravity point in the middle of the field
	
	void antiGravMove() {
   		double xforce = 0;
	    double yforce = 0;
	    double force;
	    double ang;
	    GravPoint p;
	    RobotsInfo.Enemy en;
    // AQUI TEM QUE PEGAR O DADO DO INIMIGO
    	Iterator<RobotsInfo.Enemy> e = enemiesInfo.getMapIterator();
	    //cycle through all the enemies.  If they are alive, they are repulsive.  Calculate the force on us
		while (e.hasNext()) {
    	    en = (RobotsInfo.Enemy)e.next();
//			if (en.live) {
    	    double enX, enY;
    	    double absbearing_rad = (getHeadingRadians()+en.bearing)%(2*PI);
    	    enX = getX()+Math.sin(absbearing_rad)*en.distance;
    	    enY = getY()+Math.cos(absbearing_rad)*en.distance;
				p = new GravPoint(enX,enY, -1000);
		        force = p.power/Math.pow(RoboUtils.getRange(getX(),getY(),p.x,p.y),2);
		        //Find the bearing from the point to us
		        ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.y, getX() - p.x)); 
		        //Add the components of this force to the total force in their respective directions
		        xforce += Math.sin(ang) * force;
		        yforce += Math.cos(ang) * force;
//			}
	    }
	    
		/**The next section adds a middle point with a random (positive or negative) strength.
		The strength changes every 5 turns, and goes between -1000 and 1000.  This gives a better
		overall movement.**/
		midpointcount++;
		if (midpointcount > 5) {
			midpointcount = 0;
			midpointstrength = (Math.random() * 2000) - 1000;
		}
		p = new GravPoint(getBattleFieldWidth()/2, getBattleFieldHeight()/2, midpointstrength);
		force = p.power/Math.pow(RoboUtils.getRange(getX(),getY(),p.x,p.y),1.5);
	    ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.y, getX() - p.x)); 
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
	
	/**Move towards an x and y coordinate**/
	void goTo(double x, double y) {
	    double dist = 20; 
	    double angle = Math.toDegrees(RoboUtils.absbearing(getX(),getY(),x,y));
	    double r = turnTo(angle);
	    setAhead(dist * r);
	}


	/**Turns the shortest angle possible to come to a heading, then returns the direction the
	the bot needs to move in.**/
	int turnTo(double angle) {
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
	
	 
	private void sweep() {
	  enemiesInfo.printHash();
	  int number_of_robots = getOthers(); 
	 
	  if(enemiesInfo.readedRobots >= number_of_robots) {
		  enemiesInfo.clearHash();
		  radarDirection = (radarDirection == 1) ? 0 : 1;
	  }
	  if(radarDirection == 1) {
		  this.setTurnRadarRight(20);
	  }
	  else {
		  this.setTurnRadarLeft(20);
	  } 
	}


}
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import robocode.*;

public class Diablo extends TeamRobot 
{
	private RobotsInfo enemiesInfo = new RobotsInfo(this);
	private RobotsInfo friendsInfo = new RobotsInfo(this);
	private BulletsInfo bulletsInfo = new BulletsInfo(this);
	private double PI = Math.PI;
	private int radarDirection = 1;
	
	public void run () {
		this.setBodyColor(java.awt.Color.GREEN);
		setAdjustRadarForGunTurn(true);
		//turnRadarRightRadians(2*PI);
		enemiesInfo = new RobotsInfo(this);
		friendsInfo = new RobotsInfo(this);
		while(true) {
			//setTurnRadarLeftRadians(2*PI);
			bulletsInfo.updateBullets();
			sweep();
			antiGravMove();
			//chooseEnemyToFire();
			//setAhead(10);
			execute();
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e){
		if (this.isTeammate(e.getName()))
			friendsInfo.addRobotEvent(e);
		else	{
			if (enemiesInfo.getRobot(e.getName()) != null) {
				RobotsInfo.Enemy en = enemiesInfo.getRobot(e.getName());
				double energyDiff = e.getEnergy() - en.energy;
				if (energyDiff > 0.0 && energyDiff <= 3.0) { 
					bulletsInfo.addBullet(e, energyDiff);
					System.out.println("Enemy " + e.getName() + " shot");
				}
			}
			enemiesInfo.addRobotEvent(e);
			System.out.println("Enemy " + e.getName() + " scanned");
		}
	}
	
	public void onRobotDeath(RobotDeathEvent e) {
		if (this.isTeammate(e.getName()))
			friendsInfo.updateRobotDeath(e);
		else
			enemiesInfo.updateRobotDeath(e);
	}
	
	private void chooseEnemyToFire() {
		RobotsInfo.Enemy closestEnemy;
		//this.setTurnRadarRightRadians(PI/4);
		closestEnemy = enemiesInfo.getClosestRobot();
		
		if (closestEnemy == null || !closestEnemy.live) {
			if (closestEnemy != null)
				System.out.println("closestEnemy " + closestEnemy.name + " is dead");
			return;
		}
		//System.out.println("closest enemy is " + closestEnemy.name);
		CircularIntercept interception = new CircularIntercept(); 
		
		interception.calculate(getX(), getY(), closestEnemy.x, closestEnemy.y,
				Math.toDegrees(closestEnemy.heading), closestEnemy.speed, 2, Math.toDegrees(closestEnemy.changehead));
		
		double gun_bearing = robocode.util.Utils.normalRelativeAngleDegrees(interception.bulletHeading_deg - this.getGunHeading());// Math.toDegrees(RoboUtils.normaliseBearing(Math.toRadians()));
		
		this.setTurnGunRight(gun_bearing);

		//System.out.println("closest enemy found: " + closestEnemy.name);
		//System.out.println("closest impact point: " + interception.impactPoint.x + ", " + interception.impactPoint.y);
		
		if (getGunHeat() == 0) {
		       double firePower = Math.min(500 / closestEnemy.distance, 3);
		       /*// calculate speed of bullet
		       double bulletSpeed = 20 - firePower * 3;
		       // distance = rate * time, solved for time
		       long time = (long)(closestEnemy.distance / bulletSpeed);*/
			System.out.println("GunHeat = 0");
			System.out.println("Gun_bearing = " + gun_bearing);
		       if (Math.abs(gun_bearing) <= interception.angleThreshold) {
		    	   System.out.println("gun_bearing < threshold");
		    	   if (	 (interception.impactPoint.x > 0) &&
		    			 (interception.impactPoint.x < getBattleFieldWidth()) &&
		    			 (interception.impactPoint.y > 0) &&
		    			 (interception.impactPoint.y < getBattleFieldHeight())
		    		  ) {
		    			    // Ensure that the predicted impact point is within 
		    			    // the battlefield
		    		   		Bullet bullet = this.setFireBullet(firePower);
		    		   		System.out.println("atirou");
		    			  }
		       }
		   }
		}
	
	static int rounds;
	static int notChangedDirection;
	
	private void sweep() {
	     //enemiesInfo.printHash();
	     int number_of_robots = getOthers();
	     
	     
	     if (number_of_robots > 1){    
	        if(enemiesInfo.readedRobots < number_of_robots - notChangedDirection) {
	            rounds++;
	        }else {
	            
	            enemiesInfo.clearHash();
	            notChangedDirection = 0;
	            if (number_of_robots > 2 && rounds * 20 < 270)
	                radarDirection = (radarDirection == 1) ? 0 : 1;     
	            else if (number_of_robots == 2 && rounds * 20 < 180)
	                radarDirection = (radarDirection == 1) ? 0 : 1;
	            else
	            	notChangedDirection = 1;
	            
	        
	            rounds = 0;
	        }
	     }
	     else {
	    	 if (enemiesInfo.readedRobots  == 1) {
	    		 radarDirection = (radarDirection == 1) ? 0 : 1;
	    		 notChangedDirection = 0;
	    		 enemiesInfo.clearHash();
	    	 }
	     }
	     //sniper_code() // <------------ pega aquele codigo
	     if(radarDirection == 1) {
             this.setTurnRadarRight(45);
         }
         else {
             this.setTurnRadarLeft(45);
         }
	    }
	
	private int midpointcount = 0;			//Number of turns since that strength was changed.
	private double midpointstrength = 0;	//The strength of the gravity point in the middle of the field
	
	void antiGravMove() {
   		double xforce = 0;
	    double yforce = 0;
	    double force;
	    double ang;
	    RoboUtils.GravPoint p;
	    RobotsInfo.Enemy en;
    // AQUI TEM QUE PEGAR O DADO DO INIMIGO
    	Iterator<RobotsInfo.Enemy> e = enemiesInfo.getMapIterator();
	    //cycle through all the enemies.  If they are alive, they are repulsive.  Calculate the force on us
		while (e.hasNext()) {
    	    en = (RobotsInfo.Enemy)e.next();
			if (en.live) {
	    	    double enX, enY;
	    	    double absbearing_rad = (getHeadingRadians()+en.bearing)%(2*PI);
	    	    enX = getX()+Math.sin(absbearing_rad)*en.distance;
	    	    enY = getY()+Math.cos(absbearing_rad)*en.distance;
				p = new RoboUtils.GravPoint(enX,enY, -1000);
		        force = p.power/Math.pow(RoboUtils.getRange(getX(),getY(),p.x,p.y),2);
		        //Find the bearing from the point to us
		        ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.y, getX() - p.x)); 
		        //Add the components of this force to the total force in their respective directions
		        xforce += Math.sin(ang) * force;
		        yforce += Math.cos(ang) * force;
			}
	    }
		
    	Iterator<RobotsInfo.Enemy> f = friendsInfo.getMapIterator();
    	while (f.hasNext()) {
    	    en = (RobotsInfo.Enemy)f.next();
			if (en.live) {
    	    double enX, enY;
    	    double absbearing_rad = (getHeadingRadians()+en.bearing)%(2*PI);
    	    enX = getX()+Math.sin(absbearing_rad)*en.distance;
    	    enY = getY()+Math.cos(absbearing_rad)*en.distance;
				p = new RoboUtils.GravPoint(enX,enY, -1000);
		        force = p.power/Math.pow(RoboUtils.getRange(getX(),getY(),p.x,p.y),2);
		        //Find the bearing from the point to us
		        ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.y, getX() - p.x)); 
		        //Add the components of this force to the total force in their respective directions
		        xforce += Math.sin(ang) * force;
		        yforce += Math.cos(ang) * force;
			}
	    }
    	
    	/** Creating a line o gravity points representing the bullets detected by the robot **/
    	Object[] bforces = bulletsInfo.getForce();
    	xforce += (double) bforces[0];
    	yforce += (double) bforces[1];
		
	    
		/**The next section adds a middle point with a random (positive or negative) strength.
		The strength changes every 5 turns, and goes between -1000 and 1000.  This gives a better
		overall movement.**/
		midpointcount++;
		if (midpointcount > 5) {
			midpointcount = 0;
			midpointstrength = (Math.random() * 2000) - 1000;
		}
		p = new RoboUtils.GravPoint(getBattleFieldWidth()/2, getBattleFieldHeight()/2, midpointstrength);
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
	
}

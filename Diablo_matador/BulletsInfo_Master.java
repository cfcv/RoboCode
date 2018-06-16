package Diablo_matador;
import java.util.Iterator;
import java.util.Vector;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class BulletsInfo_Master {

	
	private Vector<BulletsInfo.Bullet> bullets;
	private AdvancedRobot self;
	
	BulletsInfo_Master(AdvancedRobot me) {
		self = me;
		bullets = new Vector<BulletsInfo.Bullet>();
	}
	
	public Object[] getForce() {
		double Xforce = 0.0;
		double Yforce = 0.0;
		Iterator<BulletsInfo.Bullet> it = bullets.iterator();
		while (it.hasNext()) {
			BulletsInfo.Bullet b = it.next();
			RoboUtils.Point intersec_p = b.closestPointToLine(self);
			double force = -800/Math.pow(RoboUtils.getRange(self.getX(), self.getY(), intersec_p.x, intersec_p.y), 2);
			double ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(self.getY() - intersec_p.y, self.getX() - intersec_p.x));
			Xforce += Math.sin(ang) * force;
			Yforce += Math.cos(ang) * force;
		}
		return new Object[] {Xforce, Yforce} ;
	}

	void updateBullets() {
		Iterator<BulletsInfo.Bullet> it = bullets.iterator();
		while (it.hasNext()) {
			BulletsInfo.Bullet b = it.next();
			b.turnsAlive -= 1;
			if (b.turnsAlive == 0) {
				System.out.println("bullet removed");
				it.remove();
				
			}
		}
	}
	
	public void addBullet(RobotsInfo.Enemy en, double diff) {
		BulletsInfo.Bullet b = new BulletsInfo.Bullet();
		b.owner = en.name;
		double absbearing_rad = (self.getHeadingRadians()+en.bearing)%(2*RoboUtils.PI);
		b.x = self.getX()+Math.sin(absbearing_rad)*en.distance;
		b.y = self.getY()+Math.cos(absbearing_rad)*en.distance; 
		
		CircularIntercept interception = new CircularIntercept();
		
		interception.calculate(b.x, b.y, self.getX(), self.getY(), Math.toDegrees(self.getHeading()), self.getVelocity(),
				diff, Math.toDegrees(((Diablo_Master)self).angular_velocity));
		
		b.direction = Math.toRadians(interception.bulletHeading_deg);
		b.firepower = diff;
		b.speed = 20.0 - 3.0*b.firepower;
		double width = self.getBattleFieldWidth();
		double height = self.getBattleFieldHeight();
		// Calcula  o ponto em que a bala irá colidir com a borda.
		b.impact_point = checkIntersectionPointWithBorder(b);
		
		double dist = RoboUtils.getRange(b.impact_point.x, b.impact_point.y, b.x, b.y);
		
		b.turnsAlive = (int) Math.floor(dist/b.speed);
				
		bullets.add(b);
	}
	
	public void addBullet(ScannedRobotEvent event, double diff) {
		BulletsInfo.Bullet b = new BulletsInfo.Bullet();
		b.owner = event.getName();
		double absbearing_rad = (self.getHeadingRadians()+event.getBearingRadians())%(2*RoboUtils.PI);
		b.x = self.getX()+Math.sin(absbearing_rad)*event.getDistance();
		b.y = self.getY()+Math.cos(absbearing_rad)*event.getDistance(); 
		
		Intercept interception = new Intercept();
		interception.calculate(b.x, b.y, self.getX(), self.getY(), self.getHeading(), self.getVelocity(), diff, 0);
		
		b.direction = Math.toRadians(interception.bulletHeading_deg);
		b.firepower = diff;
		b.speed = 20.0 - 3.0*b.firepower;
		double width = self.getBattleFieldWidth();
		double height = self.getBattleFieldHeight();
		// Calcula  o ponto em que a bala irá colidir com a borda.
		b.impact_point = checkIntersectionPointWithBorder(b);
		
		double dist = RoboUtils.getRange(b.impact_point.x, b.impact_point.y, b.x, b.y);
		
		b.turnsAlive = (int) Math.floor(dist/b.speed);
				
		bullets.add(b);
	}
	
	private RoboUtils.Point checkIntersectionPointWithBorder(BulletsInfo.Bullet bul) {
		RoboUtils.Point p = new RoboUtils.Point();
		if (bul.direction == 0.0) {
			p.x = bul.x;
			p.y = self.getBattleFieldHeight();
		}
		else if (bul.direction == Math.PI/4.0) {
			p.x = self.getBattleFieldWidth();
			p.y = bul.y;
		} 
		else if (bul.direction == Math.PI) {
			p.x = bul.x;
			p.y = 0;
		}
		else if (bul.direction == Math.PI*3.0/2.0) {
			p.x = 0;
			p.y = bul.y;
		} else {
			// coeficientes para eq. da reta y = ax + b
			double a1 = 1/Math.tan(bul.direction);
			double c1 = bul.y - a1*bul.x;
			// coeficientes para a eq. da reta x = ay + b
			double a2 = Math.tan(bul.direction);
			double c2 = bul.x - a2*bul.y;
			if (bul.direction >= Math.PI/4.0 && bul.direction < Math.PI/2.0 + Math.PI/4.0) {
				// Vertical direita
				p.x = self.getBattleFieldWidth();
				p.y = (self.getBattleFieldWidth() - c2)/a2;
			}
			else if (bul.direction >= Math.PI/2.0 + Math.PI/4.0 && bul.direction < Math.PI + Math.PI/4.0) {
				// Horizontal inferior
				p.x = (-c1)/a1;
				p.y = 0;
			}
			else if (bul.direction >= Math.PI + Math.PI/4.0 && bul.direction < Math.PI*3.0/2.0 + Math.PI/4.0){
				// Vertical esquerda
				p.x = 0;
				p.y = (-c2)/a2;
			}
			else {
				// Horizontal superior
				p.x = (self.getBattleFieldHeight() - c1)/a1;
				p.y = self.getBattleFieldHeight();	
			}
			
		}
		return p;		
	}
}

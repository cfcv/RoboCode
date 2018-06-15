import java.util.Iterator;
import java.util.Vector;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class BulletsInfo {
	
	public class Bullet {
		String owner;
		public double x, y, direction, firepower, speed;
		//double coef, constant;
		public int turnsAlive;
		public Vector<RoboUtils.GravPoint> gravPoints;
		public RoboUtils.Point impact_point;
		
		Bullet() {
			gravPoints = new Vector<RoboUtils.GravPoint>();
		}
		
		private RoboUtils.Point closestPointToLine() {
			double[] P1P2 = {impact_point.x - x, impact_point.y - y};
			double[] P1V = {self.getX() - x, self.getY() - y};
			
			double sqrP1P2 = Math.pow(P1P2[0], 2) + Math.pow(P1P2[1], 2);
			double P1P2_dot_P1V = P1P2[0]*P1V[0] + P1P2[1]*P1V[1];
			double d = P1P2_dot_P1V/sqrP1P2;
			
			return new RoboUtils.Point(x + P1P2[0]*d, y + P1P2[1]*d );
		}
	}
	
	private Vector<Bullet> bullets;
	private AdvancedRobot self;
	
	BulletsInfo(AdvancedRobot me) {
		self = me;
		bullets = new Vector<Bullet>();
	}
	
	public Object[] getForce() {
		double Xforce = 0.0;
		double Yforce = 0.0;
		Iterator<Bullet> it = bullets.iterator();
		while (it.hasNext()) {
			Bullet b = it.next();
			RoboUtils.Point intersec_p = b.closestPointToLine();
			double force = -1000/Math.pow(RoboUtils.getRange(self.getX(), self.getY(), intersec_p.x, intersec_p.y), 2);
			double ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(self.getY() - intersec_p.y, self.getX() - intersec_p.x));
			Xforce += Math.sin(ang) * force;
			Yforce += Math.cos(ang) * force;
			/*Iterator<RoboUtils.GravPoint> g_it = b.gravPoints.iterator();
			while (g_it.hasNext()) {
				RoboUtils.GravPoint gp = g_it.next();
				
				double force = gp.power/Math.pow(RoboUtils.getRange(self.getX(), self.getY(), gp.x, gp.y), 2);
				//double ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(self.getY() - gp.y, self.getX() - gp.x));
				
				Xforce += Math.sin(ang) * force;
				Yforce += Math.cos(ang) * force;
			}*/
		}
		return new Object[] {Xforce, Yforce} ;
	}

	void updateBullets() {
		Iterator<Bullet> it = bullets.iterator();
		while (it.hasNext()) {
			Bullet b = it.next();
			b.turnsAlive -= 1;
			if (b.turnsAlive == 0)
				it.remove();
		}
	}
	
	public void addBullet(ScannedRobotEvent event, double diff) {
		Bullet b = new Bullet();
		b.owner = event.getName();
		double absbearing_rad = (self.getHeadingRadians()+event.getBearingRadians())%(2*RoboUtils.PI);
		b.x = self.getX()+Math.sin(absbearing_rad)*event.getDistance();
		b.y = self.getY()+Math.cos(absbearing_rad)*event.getDistance(); 
		b.direction = RoboUtils.absbearing(b.x, b.y, self.getX(), self.getY());
		b.firepower = diff;
		b.speed = 20.0 - 3.0*b.firepower;
		double width = self.getBattleFieldWidth();
		double height = self.getBattleFieldHeight();
		// Falta definir o ponto de intersecção da bala com a borda do mapa, e em quantos turnos isso irá ocorrer
		// Define a equação da reta prevista como rota da bala
		
		b.impact_point = checkIntersectionPointWithBorder(b);
		System.out.println("shot direction: " + Math.toDegrees(b.direction));
		System.out.println("bullet will colide in point: (" + b.impact_point.x + ", " + b.impact_point.y + ")");
		
		double dist = RoboUtils.getRange(b.impact_point.x, b.impact_point.y, b.x, b.y);
		
		b.turnsAlive = (int) Math.floor(dist/b.speed);
				
		bullets.add(b);
	}
	
	private RoboUtils.Point checkIntersectionPointWithBorder(Bullet bul) {
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

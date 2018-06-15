import java.util.Iterator;
import java.util.Vector;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class BulletsInfo {
	
	public class Bullet {
		String owner;
		public double x, y, direction, firepower, speed;
		public int turnsAlive;
		public Vector<RoboUtils.GravPoint> gravPoints;
		
		Bullet() {
			gravPoints = new Vector<RoboUtils.GravPoint>();
		}
		
		public void addGravPoints() {
			// Adiciona uma "linha"de gravpoints seguindo a trajetória prevista da bala
			for (int i = 1; i < turnsAlive ; i++) {
				RoboUtils.GravPoint g = new RoboUtils.GravPoint((x + i*Math.sin(direction)*speed), (y + i*Math.cos(direction)*speed), -500);
				gravPoints.addElement(g);
			}
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
			Iterator<RoboUtils.GravPoint> g_it = b.gravPoints.iterator();
			while (g_it.hasNext()) {
				RoboUtils.GravPoint gp = g_it.next();
				double force = gp.power/Math.pow(RoboUtils.getRange(self.getX(), self.getY(), gp.x, gp.y), 2);
				double ang = RoboUtils.normaliseBearing(Math.PI/2 - Math.atan2(self.getY() - gp.y, self.getX() - gp.x));
				
				Xforce += Math.sin(ang) * force;
				Yforce += Math.cos(ang) * force;
			}
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
			else {
				b.gravPoints.remove(0);
			}
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
		
		RoboUtils.Point p = checkIntersectionPointWithBorder(b);
		
		double dist = RoboUtils.getRange(p.x, p.y, b.x, b.y);
		
		b.turnsAlive = (int) Math.floor(dist/b.speed);
		
		b.addGravPoints();
		
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
			// Se a direção está entre 45 e 225 graus, então só é necessário checar intersecção com a borda inferior e a borda direita
			if (bul.direction >= Math.PI/4.0 && bul.direction < Math.PI + Math.PI/4.0) {
				// Horizontal inferior
				double hX = (-c1)/a1;
				double hY = 0;
				
				// Vertical direita
				double vX = self.getBattleFieldWidth();
				double vY = (self.getBattleFieldWidth() - c2)/a2;
				// Pega a colisão válida.
				if (hX <= self.getBattleFieldWidth()) {
					p.x = hX;
					p.y = hY;
				} else {
					p.x = vX;
					p.y = vY;
				}
			} // Se não, basta checar intersecção com a borda superior e a borda esquerda
			else {
				// Horizontal superior
				double hX = (self.getBattleFieldHeight() - c1)/a1;
				double hY = self.getBattleFieldHeight();
				
				// Vertical esquerda
				double vX = 0;
				double vY = (-c2)/a2;
				if (hX >= 0) {
					p.x = hX;
					p.y = hY;
				} else {
					p.x = vX;
					p.y = vY;
				}
				
			}
			
		}
		return p;		
	}
}

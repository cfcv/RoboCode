import java.util.Vector;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class BulletsInfo {
	
	public class Bullet {
		String owner;
		double x, y, direction, firepower, speed, turnsAlive;
		Vector<GravPoint> gravPoints;
		
		Bullet() {
			gravPoints = new Vector<GravPoint>();
		}
	}
	
	private Vector<Bullet> bullets;
	private AdvancedRobot self;
	
	BulletsInfo(AdvancedRobot me) {
		self = me;
		bullets = new Vector<Bullet>();
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
		
		double turnsAlive = 0;
		
		bullets.add(b);
	}
	
	private void addGravPoints(Bullet b) {
		// Adiciona uma "linha"de gravpoints seguindo a trajetória prevista da bala
	}
}

package Diablo_matador;
import robocode.*;

public class Intercept {
	 public Coordinate impactPoint = new Coordinate(0,0);
	 public double bulletHeading_deg;
	 
	 protected Coordinate bulletStartingPoint = new Coordinate();
	 protected Coordinate targetStartingPoint = new Coordinate();
	 public double targetHeading;
	 public double targetVelocity;
	 public double bulletPower;
	 public double angleThreshold;
	 public double distance;
	 
	 protected double impactTime;
	 protected double angularVelocity_rad_per_sec;
 
 public void calculate (
 
 // Initial bullet position x coordinate 
 double xb, 
 // Initial bullet position y coordinate
 double yb, 
 // Initial target position x coordinate
 double xt, 
 // Initial target position y coordinate
 double yt, 
 // Target heading
 double tHeading, 
 // Target velocity
 double vt, 
 // Power of the bullet that we will be firing
 double bPower, 
 // Angular velocity of the target
 double angularVelocity_deg_per_sec 
)
{
	angularVelocity_rad_per_sec = 
	 Math.toRadians(angularVelocity_deg_per_sec);
	 
	bulletStartingPoint.set(xb, yb);
	targetStartingPoint.set(xt, yt);
	 
	targetHeading = tHeading;
	targetVelocity = vt;
	bulletPower = bPower;
	double vb = 20-3*bulletPower;
	 
	double dX,dY;
	 
	// Start with initial guesses at 10 and 20 ticks
	impactTime = getImpactTime(10, 20, 0.01); 
	impactPoint = getEstimatedPosition(impactTime);
	 
	dX = (impactPoint.x - bulletStartingPoint.x);
	dY = (impactPoint.y - bulletStartingPoint.y);
	 
	distance = Math.sqrt(dX*dX+dY*dY);
	 
	bulletHeading_deg = Math.toDegrees(Math.atan2(dX,dY));
	angleThreshold = Math.toDegrees
	 (Math.atan((36)/distance));
}
 
protected Coordinate getEstimatedPosition(double time) {
 
	double x = targetStartingPoint.x + 
	   targetVelocity * time * Math.sin(Math.toRadians(targetHeading));
	double y = targetStartingPoint.y + 
	   targetVelocity * time * Math.cos(Math.toRadians(targetHeading));
	return new Coordinate(x,y);
}
 
private double f(double time) {
 
	double vb = 20-3*bulletPower;
	 
	Coordinate targetPosition = getEstimatedPosition(time);
	double dX = (targetPosition.x - bulletStartingPoint.x);
	double dY = (targetPosition.y - bulletStartingPoint.y);
	 
	return Math.sqrt(dX*dX + dY*dY) - vb * time;
}
 
private double getImpactTime(double t0, 
  double t1, double accuracy) {
 
	double X = t1;
	double lastX = t0;
	int iterationCount = 0;
	double lastfX = f(lastX);
	 
	while ((Math.abs(X - lastX) >= accuracy) && 
	  (iterationCount < 15)) {
	 
	iterationCount++;
	double fX = f(X);
	 
	if ((fX-lastfX) == 0.0) break;
	 
	double nextX = X - fX*(X-lastX)/(fX-lastfX);
	lastX = X;
	X = nextX;
	lastfX = fX;
	}
	 
	return X;
}

public class Coordinate {
	public double x, y;
	
	public Coordinate() {
		x = 0;
		y = 0;
	}
	
	public Coordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
}
}
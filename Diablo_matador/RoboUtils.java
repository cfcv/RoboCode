package Diablo_matador;
import java.util.Enumeration;

public class RoboUtils {
	
	static public class Point {
		public double x, y;
		Point() {
			x = y = 0;
		}
		
		Point(double X, double Y) {
			this.x = X;
			this.y = Y;
		}
	}
	
	/**Holds the x, y, and strength info of a gravity point**/
	static public class GravPoint {
	    public double x,y,power;
	    public GravPoint(double pX,double pY,double pPower) {
	        x = pX;
	        y = pY;
	        power = pPower;
	    }
	}


	static final double PI = Math.PI;

	//if a bearing is not within the -pi to pi range, alters it to provide the shortest angle
	static public double normaliseBearing(double ang) {
		if (ang > PI)
			ang -= 2*PI;
		if (ang < -PI)
			ang += 2*PI;
		return ang;
	}
	
	//if a heading is not within the 0 to 2pi range, alters it to provide the shortest angle
	static public double normaliseHeading(double ang) {
		if (ang > 2*PI)
			ang -= 2*PI;
		if (ang < 0)
			ang += 2*PI;
		return ang;
	}
	
	//returns the distance between two x,y coordinates
	static public double getRange( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = Math.sqrt( xo*xo + yo*yo );
		return h;	
	}
	
	//gets the absolute bearing between two x,y coordinates
	static public double absbearing( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = getRange( x1,y1, x2,y2 );
		if( xo > 0 && yo > 0 )
		{
			return Math.asin( xo / h );
		}
		if( xo > 0 && yo < 0 )
		{
			return Math.PI - Math.asin( xo / h );
		}
		if( xo < 0 && yo < 0 )
		{
			return Math.PI + Math.asin( -xo / h );
		}
		if( xo < 0 && yo > 0 )
		{
			return 2.0*Math.PI - Math.asin( -xo / h );
		}
		return 0;
	}
	
	Point closestPointToLine(Point P1, Point P2, Point V) {
		double[] P1P2 = {P1.x - P2.x, P1.y - P2.y};
		double[] P1V = {P1.x - V.x, P1.y - V.y};
		
		double sqrP1P2 = Math.pow(P1P2[0], 2) + Math.pow(P1P2[1], 2);
		double P1P2_dot_P1V = P1P2[0]*P1V[0] + P1P2[1]*P1V[1];
		//Normalized distance (0..1) of P1 to the closest point
		double d = P1P2_dot_P1V/sqrP1P2;
		
		return new RoboUtils.Point(P1.x + P1P2[0]*d, P1.y + P1P2[1]*d );
	}
	


}

package morphy.game;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class RatingTest {
	
	public static final int defaultRating = 1720;
	public static final double defaultRD = 350.0;
	public static Map<String,Integer> ratings = new HashMap<String,Integer>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// create initial ratings, etc
		ratings.put("PlayerA",defaultRating);
		ratings.put("PlayerB",defaultRating);
		ratings.put("PlayerC",defaultRating);
		ratings.put("PlayerD",defaultRating);
		
		// create some test matches
		
		calculateChange(1965,53.3,defaultRating,defaultRD);
		calculateChange(defaultRating,defaultRD,1965,53.3);
	}
	
	public static void calculateChange(int myrating,double myrd,int opprating,double opprd) {
		double RD = myrd;
		final double p = (3*Math.pow(Math.log(10),2)) / (Math.pow(Math.PI,2)*Math.pow(400,2));
		//System.out.println(p);
		double f = 1/Math.sqrt(1+p*Math.pow(RD,2)); // normal chess 
		//System.out.println(f);
		
		int r1 = myrating;
		int r2 = opprating;
		double E = 1/(1+ Math.pow(10,-(r1-r2)*f/400));
		//System.out.println(E);
		final double q = Math.log(10)/400;
		double K = q*f / (1/Math.pow(RD,2) + Math.pow(q,2) * Math.pow(f,2) * E * (1-E));
		if (K < 16) K = 16;
		
		double wr = r1 + K * (1-E);
		double dr = r1 + K * (0.5-E);
		double lr = r1 + K * (0-E);
		double newRD = 1 / (Math.sqrt(1/Math.pow(RD,2)+Math.pow(q,2)*Math.pow(f,2)*E*(1-E)));
		
		System.out.println((int)round(wr,0) + " " + (int)round(dr,0) + " " + (int)round(lr,0) + " " + round(newRD,1));
	}
	
	public static double round(double x,int scale) {
		return new BigDecimal(x).setScale(scale,BigDecimal.ROUND_HALF_UP).doubleValue();
	}

}

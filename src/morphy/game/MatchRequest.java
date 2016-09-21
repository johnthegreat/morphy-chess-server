package morphy.game;

import morphy.user.UserSession;

public class MatchRequest implements Offer {
	UserSession white;
	UserSession black;
	int time;
	int increment;
	boolean isRated;
	String variant;
	String color;
	
	public UserSession getUserSession() {
		return color.equals("white")?white:black;
	}
	
	public UserSession getOpponent() {
		return color.equals("white")?black:white;
	}
	
	private MatchRequest() {
		
	}
	
	public MatchRequest(UserSession userSession,UserSession opponent,int time,int increment,boolean rated,String variant,String color) {
		this();
		
		if (color == null || color.equals("white")) { 
			setWhite(userSession);
			setBlack(opponent);
		} else if (color.equals("black")) {
			setWhite(opponent);
			setBlack(userSession);
		}
		
		setTime(time);
		setIncrement(increment);
		setRated(rated);
		setVariant(variant);
		setColor(color);
	}

	public UserSession getWhite() {
		return white;
	}

	protected void setWhite(UserSession white) {
		this.white = white;
	}

	public UserSession getBlack() {
		return black;
	}

	protected void setBlack(UserSession black) {
		this.black = black;
	}

	public int getTime() {
		return time;
	}

	protected void setTime(int time) {
		this.time = time;
	}

	public int getIncrement() {
		return increment;
	}

	protected void setIncrement(int increment) {
		this.increment = increment;
	}

	public boolean isRated() {
		return isRated;
	}

	protected void setRated(boolean isRated) {
		this.isRated = isRated;
	}

	public String getVariant() {
		return variant;
	}

	protected void setVariant(String variant) {
		this.variant = variant;
	}

	public String getColor() {
		return color;
	}

	protected void setColor(String color) {
		this.color = color;
	}
}

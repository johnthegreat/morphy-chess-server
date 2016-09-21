package morphy.service;

import morphy.user.UserSession;

// http://pueblo.sourceforge.net/doc/manual/ansi_color_codes.html
public class HighlightService implements Service {
	
	
	private HighlightService() {
		
	}
	
	private static HighlightService singletonInstance = new HighlightService();
	public static HighlightService getSingletonInstance() {
		return singletonInstance;
	}
	

	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	public String before(UserSession sess) {
		String str = ""+((char)27);
		
		int type = Integer.parseInt(sess.getUser().getUserVars().getVariables().get("highlight"));
		
		if (type == 0) { return ""; }
		if (type == 1) { str += "[7m"; }
		if (type == 2) { }
		if (type == 3) { }
		if (type == 4) { }
		if (type == 5) { }
		if (type == 6) { }
		if (type == 7) { }
		if (type == 8) { }
		if (type == 9) { }
		if (type == 10) { }
		if (type == 11) { }
		if (type == 12) { }
		if (type == 13) { }
		if (type == 14) { }
		if (type == 15) { }
		
		return str;
	}
	
	public String after() {
		return (((char)27)+"[0m");
	}

}

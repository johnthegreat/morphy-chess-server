package morphy.game;

import morphy.game.params.SeekParams;
import morphy.user.UserSession;

/**
 * Created by John on 09/23/2016.
 */
public class Seek {
	private int seekIndex;
	
	private UserSession userSession;
	
//	private int seekTimeMinutes;
//	private int seekIncSeconds;
//	private boolean rated;
//	private Variant variant;
	
	private SeekParams seekParams;
	private boolean useManual;
	private boolean useFormula;
	
	public Seek() {
		seekParams = new SeekParams();
	}
	
	
	//
	// SETTERS
	//

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}
	
	public void setSeekIndex(int seekIndex) {
		this.seekIndex = seekIndex;
	}
	
	public void setUseManual(boolean useManual) {
		this.useManual = useManual;
	}

	public void setUseFormula(boolean useFormula) {
		this.useFormula = useFormula;
	}
	
	//
	// GETTERS
	//
	
	public UserSession getUserSession() {
		return userSession;
	}
	
	public int getSeekIndex() {
		return seekIndex;
	}
	
	public boolean isUseManual() {
		return useManual;
	}

	public boolean isUseFormula() {
		return useFormula;
	}
	
	public SeekParams getSeekParams() {
		return seekParams;
	}
}

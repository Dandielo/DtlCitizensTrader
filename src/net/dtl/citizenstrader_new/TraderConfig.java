package net.dtl.citizenstrader;

public class TraderConfig {
	//MySQL section
	private String mode = "secure";
	
	public void setTraderConfig(String arg0) {
		if ( arg0 != null )
			mode = arg0;
	}

	public String getMode() {
		return mode;
	}
	public void setMode(String m) {
		mode = m;
	}
	
}

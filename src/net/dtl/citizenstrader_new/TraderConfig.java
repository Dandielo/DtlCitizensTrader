package net.dtl.citizenstrader_new;

import net.milkbowl.vault.economy.Economy;

public class TraderConfig {
	//MySQL section
	private String mode = "secure";
	private Economy econ;
	
	public void setTraderConfig(String arg0) {
		if ( arg0 != null )
			mode = arg0;
	}

	public Economy getEcon() {
		return econ;
	}
	public void setEcon(Economy e) {
		econ = e;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String m) {
		mode = m;
	}
	
}

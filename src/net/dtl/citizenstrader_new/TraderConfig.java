package net.dtl.citizenstrader_new;

import java.security.Permissions;

import net.milkbowl.vault.economy.Economy;

public class TraderConfig {
	/* *
	 * Trader mode, indicates which mode to use (secure mode isn't working)
	 * default = simple
	 * 
	 */
	private String mode = "simple";
	
	/* *
	 * Economy plugin (going to delete this)
	 * 
	 */
	private Economy econ;
	/* *
	 * Permissions plugin (going to delete this)
	 * future preparation
	 * 
	 */
	private Permissions perm;
	
	/* *
	 * Constructor
	 * 
	 */
	public void setTraderConfig(String arg0) {
		if ( arg0 != null )
			mode = arg0;
	}

	/* *
	 * Functions 
	 * 
	 */
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

package net.dtl.citizenstrader_new;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import net.milkbowl.vault.economy.Economy;

public class TraderConfig {

	//configuration
	//global trader mode (default simple)
	private String globalMode;
	private String listingFormat;
	private String traderDefault;
	private String walletDefault;
	
	
	//economy plugin (via vault)
	private Economy econ;
	

	//functions
	public TraderConfig(ConfigurationSection config) {
		globalMode = config.getString("general.global-mode","simple");
		listingFormat = config.getString("general.listingFormat","- " + ChatColor.RED + "<in> " + ChatColor.WHITE + " <a> <p> " + ChatColor.YELLOW + " [<s>]");
		traderDefault = config.getString("general.trader-default","player-trader");
		walletDefault = config.getString("general.wallet-default","npc-wallet");
	}
	public Economy getEcon() {
		return econ;
	}
	public void setEcon(Economy e) {
		econ = e;
	}
	public String getMode() {
		return globalMode;
	}
	public void setMode(String m) {
		globalMode = m;
	}
	public String getListingFormat() {
		return this.listingFormat;
	}
	
}

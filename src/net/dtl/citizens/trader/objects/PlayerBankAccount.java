package net.dtl.citizens.trader.objects;

import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.traders.Banker.BankTabType;

//import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class PlayerBankAccount extends BankAccount {
	//super
	//private FileConfiguration config = CitizensTrader.getInstance().getConfig();
	
	public PlayerBankAccount(String accountName, boolean save) {
		//super
		super();
		
		
		owner = accountName;
		availableTabs = CitizensTrader.getInstance().getConfig().getConfigurationSection("bank").getInt("max-tabs");
		
		if ( save )
			backend.newAccount(accountName);
		
		bankTabs.clear();
		bankTabs.put(BankTabType.Tab1, new BankTab(new ItemStack(35,1), "tab1", CitizensTrader.getInstance().getConfig().getConfigurationSection("bank").getInt("tab-size")));
	}
}

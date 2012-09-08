package net.dtl.citizenstrader_new.containers;

import static net.dtl.citizenstrader_new.backends.file.FileBackend.buildPath;
import net.dtl.citizenstrader_new.CitizensTrader;
import net.dtl.citizenstrader_new.traders.Banker.BankTabType;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class PlayerBankAccount extends BankAccount {
	//super
	private FileConfiguration config = CitizensTrader.getInstance().getConfig();
	
	public PlayerBankAccount(String accountName) {
		//super
		super();
		
		
		owner = accountName;
		availableTabs = CitizensTrader.getInstance().getConfig().getConfigurationSection("bank").getInt("default-max-tabs");
		
		backend.newAccount(accountName);
		bankTabs.clear();
		bankTabs.put(BankTabType.Tab1, new BankTab(new ItemStack(35,1), "tab1", CitizensTrader.getInstance().getConfig().getConfigurationSection("bank").getInt("tab-size")));
	}
}

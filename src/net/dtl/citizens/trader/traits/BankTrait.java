package net.dtl.citizens.trader.traits;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.objects.StockItem;

public class BankTrait {
	//config variable
	private static FileConfiguration config;
	
	//deposit fee
	private double depositFee;
	private double withdrawFee;
	private boolean settings;
	
	//max tabs to show
	
	
	public BankTrait()
	{
		config = CitizensTrader.getInstance().getConfig();
	}
	
	public void load(DataKey data) throws NPCLoadException {
		depositFee = data.getDouble("deposit-fee", config.getDouble("bank.default-deposit-fee", 0.0));
		depositFee = data.getDouble("withdraw-fee", config.getDouble("bank.default-withdraw-fee", 0.0));
		settings = data.getBoolean("settings-available", true);
	}

	public void save(DataKey data) {
		data.setDouble("deposit-fee", depositFee);
		data.setDouble("withdraw-fee", withdrawFee);
		data.setBoolean("settings-available", settings);
	}
	
	public boolean hasSettingsPage()
	{
		return settings;
	}

	public double getDepositFee()
	{
		return this.depositFee;
	}
	
	public double getWithdrawFee()
	{
		return this.withdrawFee;
	}
	
	
}
;
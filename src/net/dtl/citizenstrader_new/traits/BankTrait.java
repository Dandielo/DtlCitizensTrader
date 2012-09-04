package net.dtl.citizenstrader_new.traits;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizenstrader_new.CitizensTrader;

public class BankTrait implements InventoryHolder {
	//config variable
	private static FileConfiguration config;
	
	//deposit fee
	private double depositFee;
	private double withdrawFee;
	private boolean settings;
	
	//max tabs to show
	private int maxTabs;
	private int tabSize;
	
	//money to "item" converter
//	private boolean moneyConverter;
	
	public BankTrait()
	{
		this(54);
		
		config = CitizensTrader.getInstance().getConfig();
	}
	
	public BankTrait(int size)
	{
        if( size <= 0 || size > 54 ){
        	throw new IllegalArgumentException("Size must be between 1 and 54");}
		tabSize = size;
	}
	
	public void load(DataKey data) throws NPCLoadException {
		depositFee = data.getDouble("deposit-fee", config.getDouble("bank.default-deposit-fee", 0.0));
		depositFee = data.getDouble("withdraw-fee", config.getDouble("bank.default-withdraw-fee", 0.0));
		maxTabs = data.getInt("max-tabs", config.getInt("bank.default-max-tabs", 9));
		settings = data.getBoolean("settings-available", true);
		tabSize = config.getInt("bank.tab-size", 54);
	}

	public void save(DataKey data) {
		data.setDouble("deposit-fee", depositFee);
		data.setDouble("withdraw-fee", withdrawFee);
		data.setInt("max-tabs", maxTabs);
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
	
	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, tabSize, "Banker");
		
		return inv;
	}
	
	
	
}
;
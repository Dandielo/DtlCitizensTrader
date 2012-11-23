package net.dtl.citizens.trader.traits;

import org.bukkit.configuration.file.FileConfiguration;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.CitizensTrader;

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
		depositFee = data.getDouble("deposit-fee", config.getDouble("bank.deposit-fee", 0.0));
		withdrawFee = data.getDouble("withdraw-fee", config.getDouble("bank.withdraw-fee", 0.0));
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

	public void toggleSettingsPage()
	{
		settings = !settings;
	}
	
	public double getDepositFee()
	{
		return this.depositFee;
	}
	public void setDepositFee(double fee)
	{
		this.depositFee = fee;
	}
	
	public double getWithdrawFee()
	{
		return this.withdrawFee;
	}
	public void setWithdrawFee(double fee)
	{
		this.withdrawFee = fee;
	}
	
	
}
;
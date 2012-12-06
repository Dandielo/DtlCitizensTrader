package net.dtl.citizens.trader.parts;

import org.bukkit.configuration.file.FileConfiguration;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.CitizensTrader;

public class BankerPart {
	//config variable
	private static FileConfiguration config;
	
	//deposit fee
	private double deposit;
	private double withdraw;
	private boolean feeDefaults;
	
	private boolean settings;
	
	//max tabs to show	
	public BankerPart()
	{
		config = CitizensTrader.getInstance().getConfig();
	}
	
	public void load(DataKey data) throws NPCLoadException 
	{
		feeDefaults = data.getBoolean("banker.feeDefaults", true);
		if ( feeDefaults )
		{
			withdraw = config.getDouble("banker.withdraw-fee", 0.0);
			deposit = config.getDouble("banker.deposit-fee", 0.0);
		}
		else
		{
			withdraw = data.getDouble("banker.withdraw-fee", 0.0);
			deposit = data.getDouble("banker.deposit-fee", 0.0);
		}
			
		settings = data.getBoolean("banker.settings-enabled", true);
	}

	public void save(DataKey data) 
	{
		if ( !feeDefaults )
		{
			data.setBoolean("banker.defaults", feeDefaults);
			data.setDouble("banker.withdraw-fee", withdraw);
			data.setDouble("banker.deposit-fee", withdraw);
			data.setBoolean("banker.settings-enabled", settings);
		}
		else
		{
			data.setBoolean("banker.defaults", feeDefaults);
			data.setBoolean("banker.settings-enabled", settings);
		}
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
		return this.deposit;
	}
	public double getWithdrawFee()
	{
		return this.withdraw;
	}
	
	public void setDepositFee(double fee)
	{
		this.deposit = fee;
	}
	public void setWithdrawFee(double fee)
	{
		this.withdraw = fee;
	}
	
	
}
;
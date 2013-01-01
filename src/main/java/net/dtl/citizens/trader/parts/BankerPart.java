package net.dtl.citizens.trader.parts;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.dandielo.citizens.wallets.AbstractWallet;
import net.dandielo.citizens.wallets.Wallets;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.objects.Wallet;
import net.dtl.citizens.trader.objects.Wallet.WalletType;

public class BankerPart {
	
	//config variable
	private static FileConfiguration config;
	
	//deposit fee
	private Wallet wallet;
	private AbstractWallet dtlWallet;
	
	private double deposit;
	private double withdraw;
	private boolean feeDefaults;
	
	private boolean settings;
	
	//max tabs to show	
	public BankerPart()
	{
		config = CitizensTrader.getInstance().getConfig();
		wallet = new Wallet(WalletType.OWNER);
		

		for ( String key : config.getConfigurationSection("bank.tab-prices").getKeys(false) )
		{
			tabPrices.add(config.getDouble("bank.tab-prices."+key));
		}
		
	}
	
	public void loadDtlWallet(NPC npc)
	{
		dtlWallet = Wallets.getWallet(npc);
	}
	
	public void load(DataKey data) throws NPCLoadException 
	{
		feeDefaults = data.getBoolean("settings.feeDefaults", true);
		if ( feeDefaults )
		{
			withdraw = config.getDouble("bank.withdraw-fee", 0.0);
			deposit = config.getDouble("bank.deposit-fee", 0.0);
		}
		else
		{
			withdraw = data.getDouble("settings.withdraw-fee", 0.0);
			deposit = data.getDouble("settings.deposit-fee", 0.0);
		}
			
		settings = data.getBoolean("settings.settings-enabled", true);
	}

	public void save(DataKey data) 
	{
		if ( !feeDefaults )
		{
			data.setBoolean("settings.defaults", feeDefaults);
			data.setDouble("settings.withdraw-fee", withdraw);
			data.setDouble("settings.deposit-fee", withdraw);
			data.setBoolean("settings.settings-enabled", settings);
		}
		else
		{
			data.setBoolean("settings.defaults", feeDefaults);
			data.setBoolean("settings.settings-enabled", settings);
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
	
	public boolean withdraw(String player, double money)
	{
		if ( dtlWallet != null )
			dtlWallet.deposit(money);
		return wallet.withdraw(player, money);
	}
/*	public Wallet getWallet()
	{
		return wallet;
	}*/
	
	//global static settings
	protected static List<Double> tabPrices = new ArrayList<Double>(); 
	
	public static double getTabPrice(int tab)
	{
		return tabPrices.size() > tab - 1 ? tabPrices.get(tab) : 0;
	}
	
}
;
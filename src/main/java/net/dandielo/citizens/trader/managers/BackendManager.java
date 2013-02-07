package net.dandielo.citizens.trader.managers;

import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.backends.Backend;
import net.dandielo.citizens.trader.backends.file.FileBackend;
import net.dandielo.citizens.trader.objects.BankAccount;

public class BackendManager {
	//PluginConfig
	private FileConfiguration config;
	
	//Backend instance
	private Backend players;

	
	public BackendManager()
	{
		config = CitizensTrader.getInstance().getConfig();
		players = new FileBackend(config, "player-accounts");
	}
	
	public Backend getBackend()
	{
		return players;
	}
	
	public Map<String, BankAccount> getBankAccounts()
	{
		return players.getAccounts();
	}
	
}

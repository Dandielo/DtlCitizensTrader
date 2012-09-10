package net.dtl.citizens.trader;

import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import net.dtl.citizens.trader.backends.Backend;
import net.dtl.citizens.trader.backends.file.FileBackend;
import net.dtl.citizens.trader.containers.BankAccount;

public class BackendManager {
	//PluginConfig
	private FileConfiguration config;
	
	//Backend instance
	private Backend backend;

	
	public BackendManager()
	{
		config = CitizensTrader.getInstance().getConfig();
		backend = new FileBackend(config);
	}
	
	public Backend getBackend()
	{
		return backend;
	}
	
	public Map<String, BankAccount> getBankAccounts()
	{
		return backend.getAccounts();
	}
	
}

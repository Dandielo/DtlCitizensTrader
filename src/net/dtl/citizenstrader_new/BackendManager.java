package net.dtl.citizenstrader_new;

import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import net.dtl.citizenstrader_new.backends.Backend;
import net.dtl.citizenstrader_new.backends.file.FileBackend;
import net.dtl.citizenstrader_new.containers.BankAccount;

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
	
	public Map<String, BankAccount> getBankAccounts()
	{
		return backend.getAccounts();
	}
	
}

package net.dtl.citizenstrader_new;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import net.dtl.citizenstrader_new.backends.Backend;
import net.dtl.citizenstrader_new.backends.file.FileBackend;
import net.dtl.citizenstrader_new.containers.BankAccount;

public class BackendManager {
	private Backend backend;

	public BackendManager(ConfigurationSection config)
	{
		backend = new FileBackend(config);
	}
	
	public Map<String, BankAccount> getBankAccounts()
	{
		System.out.print("add");
		return backend.getAccounts();
	}
}

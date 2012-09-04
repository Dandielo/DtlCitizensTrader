package net.dtl.citizenstrader_new.containers;

import static net.dtl.citizenstrader_new.backends.file.FileBackend.buildPath;

import java.util.ArrayList;
import java.util.List;

import net.dtl.citizenstrader_new.traders.Banker.BankTab;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class PlayerBankAccount extends BankAccount {
	//super
	public PlayerBankAccount(String accountName) {
		//super
		super();
		
		
		owner = accountName;
		
		backend.newAccount(accountName);
		storedItems.clear();
		
		storedItems.put(BankTab.Tab1, new ArrayList<BankItem>());
	}
}

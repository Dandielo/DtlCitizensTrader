package net.dtl.citizenstrader_new.backends;

import java.io.File;
import java.util.Map;

import net.dtl.citizenstrader_new.containers.BankAccount;
import net.dtl.citizenstrader_new.containers.BankItem;
import net.dtl.citizenstrader_new.traders.Banker.BankTab;

public abstract class Backend {
	protected String saveTrigger;
	
	public Backend() {
		saveTrigger = "item";
	}
	
	abstract public Map<String, BankAccount> getAccounts();
	
	abstract public void addItem(String player, BankTab tab, BankItem item);
	abstract public void removeItem(String player, BankTab tab, BankItem item);
	abstract public BankAccount newAccount(String player);
	
}

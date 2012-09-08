package net.dtl.citizenstrader_new.backends;

import java.io.File;
import java.util.Map;

import net.dtl.citizenstrader_new.containers.BankAccount;
import net.dtl.citizenstrader_new.containers.BankItem;
import net.dtl.citizenstrader_new.traders.Banker.BankTabType;

public abstract class Backend {
	protected String saveTrigger;
	
	public Backend() {
		saveTrigger = "item";
	}
	
	abstract public Map<String, BankAccount> getAccounts();
	
	abstract public void addItem(String player, BankTabType tab, BankItem item);
	abstract public void removeItem(String player, BankTabType tab, BankItem item);
	
	abstract public void addBankTab(String player, BankTabType tab);
	abstract public void setBankTabItem(String player, BankTabType tab, BankItem item);
	
	abstract public BankAccount newAccount(String player);

	abstract public void increaseTabSize(String player, BankTabType tabType, int tabSize);
	
}

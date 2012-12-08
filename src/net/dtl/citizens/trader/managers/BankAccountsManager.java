package net.dtl.citizens.trader.managers;

import java.util.Map;

import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.objects.BankAccount;

public class BankAccountsManager {
	protected static Map<String, BankAccount> accounts;
	protected static BackendManager backendManager = CitizensTrader.getBackendManager();
	
	
	public void loadAccounts() 
	{
		accounts = backendManager.getBankAccounts();
	}
	
	public void saveAccounts()
	{
		backendManager.getBackend().save();
	}
	
	public BankAccount getAccount(String owner)
	{
		return accounts.get(owner);
	}
	
	public void removeAccount(String owner)
	{
		accounts.remove(owner);
		backendManager.getBackend().removeAccount(owner);
	}
	
	public boolean accountExists(String owner)
	{
		return accounts.containsKey(owner);
	}
	
	public void addAccount(BankAccount account)
	{
		accounts.put(account.getOwner(), account);
		backendManager.getBackend().newAccount(account.getOwner());
	}
}

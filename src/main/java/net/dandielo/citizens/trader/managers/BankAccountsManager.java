package net.dandielo.citizens.trader.managers;

import java.util.Map;

import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.objects.BankAccount;
import net.dandielo.citizens.trader.objects.PlayerBankAccount;

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
		if ( !accountExists(owner) )
			addAccount(owner);
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
	
	public void addAccount(String owner)
	{
		accounts.put(owner, new PlayerBankAccount(owner));
		backendManager.getBackend().newAccount(owner);
	}
}

package net.dandielo.citizens.trader.backends;

import java.util.Map;

import net.dandielo.citizens.trader.objects.BankAccount;
import net.dandielo.citizens.trader.objects.BankItem;
import net.dandielo.citizens.trader.objects.BankTab;

public abstract class Backend {
	protected SaveTrigger trigger;
	
	public Backend(SaveTrigger trigger) {
		this.trigger = trigger;
	}
	
	abstract public Map<String, BankAccount> getAccounts();
	
	abstract public void addItem(String owner, int tab, BankItem item);
	abstract public void updateItem(String owner, int tab, BankItem oldItem, BankItem newItem);
	abstract public void removeItem(String owner, int tab, BankItem item);
	
	abstract public void addBankTab(String owner, BankTab tab);
	abstract public void setBankTabItem(String owner, int tab, BankItem item);
	abstract public void setTabSize(String owner, int tab, int tabSize);
	
	abstract public void removeAccount(String owner);
	abstract public BankAccount newAccount(String owner);

	//backend methods
	abstract public void reload();
	abstract public void save();
	
	public enum SaveTrigger  {
		ITEM, TAB, ACCOUNT, RELOAD;
		
		public boolean itemSaving()
		{
			return this.equals(ITEM);
		}
		
		public boolean tabSaving()
		{
			return this.equals(ITEM) || this.equals(TAB);
		}
		
		public boolean accountSaving()
		{
			return tabSaving() || this.equals(ACCOUNT);
		}
		
		@Override
		public String toString()
		{
			switch( this )
			{
			case ITEM: return "item";
			case TAB: return "tab";
			case ACCOUNT: return "account";
			case RELOAD: return "reload";
			}
			return "";
		}
	}
}

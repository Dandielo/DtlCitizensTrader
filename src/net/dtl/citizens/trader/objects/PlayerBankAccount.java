package net.dtl.citizens.trader.objects;

import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.backends.Backend;

import org.bukkit.inventory.Inventory;
public class PlayerBankAccount extends BankAccount 
{	
	private Backend players = CitizensTrader.getBackendManager().getBackend();
	
	public PlayerBankAccount(String owner, boolean save) {
		//super
		super(owner);
	}
	
	public PlayerBankAccount(BankAccount account)
	{
		super(account.getOwner());
	}

	//Bank tab methods
	@Override
	public BankTab getBankTab(String tab)
	{
		return bankTabs.get(tab);
	}

	@Override
	public boolean maxed()
	{
		return bankTabs.size() >= 9;
	}

	@Override
	public String nextTabName()
	{
		return "tab" + (bankTabs.size() + 1);
	}

	@Override
	public boolean addBankTab()
	{
		if ( maxed() )
			return false;
		
		String tabName = "tab" + (bankTabs.size() + 1 );
		BankTab tab = new BankTab(new BankItem("35:" + bankTabs.size() + " a:1"), tabName, config.getInt("bank.tab-size"));
		
		bankTabs.put(tabName, tab);
		players.addBankTab(owner, tab);
		
		return true;
	}
	
	//Item management methods
	@Override
	public void addItem(String tab, BankItem item)
	{
		bankTabs.get(tab).addItem(item);
		players.addItem(owner, tab, item);
	}

	@Override
	public void updateItem(String tab, BankItem oldItem, BankItem newItem) 
	{
		bankTabs.get(tab).removeItem(oldItem);
		bankTabs.get(tab).addItem(newItem);
		
		players.updateItem(owner, tab, oldItem, newItem);
	}

	@Override
	public void removeItem(String tab, BankItem item)
	{
		bankTabs.get(tab).removeItem(item);
		players.removeItem(owner, tab, item);
	}

	@Override
	public BankItem getItem(String tab, int slot) 
	{
		return bankTabs.get(tab).getBankItem(slot);
	}
	
	@Override
	public Inventory getInventory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccountType getType() {
		return AccountType.PLAYER;
	}
}

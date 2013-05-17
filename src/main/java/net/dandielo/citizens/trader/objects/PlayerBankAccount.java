package net.dandielo.citizens.trader.objects;

import net.dandielo.citizens.trader.DtlTraders;
import net.dandielo.citizens.trader.backends.Backend;

import org.bukkit.inventory.Inventory;
public class PlayerBankAccount extends BankAccount 
{	
	private Backend players = DtlTraders.getBackendManager().getBackend();
	
	public PlayerBankAccount(String owner) {
		//super
		super(owner);
		bankTabs.put(0, new BankTab(new BankItem("35:0 a:1 n:First tab"), 0, "First tab", config.getInt("bank.tab-size")));
	}
	
	public PlayerBankAccount(BankAccount account)
	{
		super(account.getOwner());
		bankTabs = account.bankTabs;
	}

	//Bank tab methods
	@Override
	public BankTab getBankTab(int tab)
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
		
		String tabName = "Tab " + (bankTabs.size() + 1 );
		BankTab tab = new BankTab(new BankItem("35:" + bankTabs.size() + " a:1 n:"+tabName), bankTabs.size(), tabName, config.getInt("bank.tab-size"));
		
		bankTabs.put(tab.getId(), tab);
		players.addBankTab(owner, tab);
		
		return true;
	}
	
	//Item management methods
	@Override
	public void addItem(int tab, BankItem item)
	{
		bankTabs.get(tab).addItem(item);
		players.addItem(owner, tab, item);
	}

	@Override
	public void updateItem(int tab, BankItem oldItem, BankItem newItem) 
	{
		bankTabs.get(tab).removeItem(oldItem);
		bankTabs.get(tab).addItem(newItem);
		
		players.updateItem(owner, tab, oldItem, newItem);
	}

	@Override
	public void removeItem(int tab, BankItem item)
	{
		bankTabs.get(tab).removeItem(item);
		players.removeItem(owner, tab, item);
	}

	@Override
	public BankItem getItem(int tab, int slot) 
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

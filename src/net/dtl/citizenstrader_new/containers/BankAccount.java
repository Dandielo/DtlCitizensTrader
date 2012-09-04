package net.dtl.citizenstrader_new.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.Inventory;

import net.dtl.citizenstrader_new.CitizensTrader;
import net.dtl.citizenstrader_new.backends.Backend;
import net.dtl.citizenstrader_new.traders.Banker.BankTab;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;

abstract public class BankAccount {
	//
	protected static Backend backend = CitizensTrader.getBackendManager().getBackend();
	
	//Stored items
	protected Map<BankTab, List<BankItem>> storedItems;	
	
	//bank account owner
	protected String owner;
	
	//Constructor
	public BankAccount()
	{
		owner = "";
		storedItems = new HashMap<BankTab, List<BankItem>>();
	}
	
	public List<BankItem> getBankTab(BankTab key)
	{
		return storedItems.get(key);
	}
	
	public BankTab addBankTab()
	{
		if ( storedItems.containsKey(BankTab.Tab9) )
			return null;
		
		final String bankTabName = "tab";
		
		for ( int i = 0 ; i < 9 ; ++ i )
		{
			BankTab tab = BankTab.getTabByName(bankTabName+(i+1));
			
			if ( !storedItems.containsKey(tab) )
			{
				storedItems.put(tab, new ArrayList<BankItem>());
				return tab;
			}
		}
		
		return null;
	}

	public void inventoryView(Inventory inventory)
	{
		inventory.clear();
		
		for ( BankItem item : storedItems.get(BankTab.Tab1) )
			inventory.setItem(item.getSlot(), item.getItemStack());
	}
	
	public void addItem(BankTab tab, BankItem item)
	{
		List<BankItem> items = storedItems.get(tab);
		
		if ( items == null )
			return;
		
		items.add(item);
		backend.addItem(owner, tab, item);
	}

	public void updateItem(BankTab tab, BankItem oldItem, BankItem newItem) {
		removeItem(tab, oldItem);
		addItem(tab, newItem);
		/*List<BankItem> items = storedItems.get(tab);
		
		if ( items == null )
			return;
		
		items.add(item);
		backend.addItem(owner, tab, item);*/
	}
	
	public boolean removeItem(BankTab tab, BankItem item)
	{
		List<BankItem> items = storedItems.get(tab);
		
		if ( items == null )
			return false;
		
		items.remove(item);
		backend.removeItem(owner, tab, item);
		
		return true;
	}

	public BankItem getItem(int slot, BankTab tab) {
		for ( BankItem item : storedItems.get(tab) )
		{
			if ( item.getSlot() == slot )
				return item;
		}
		return null;
	}
	
}

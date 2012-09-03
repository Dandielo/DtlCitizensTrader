package net.dtl.citizenstrader_new.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.Inventory;

import net.dtl.citizenstrader_new.traders.Banker.BankTab;

abstract public class BankAccount {
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
		int i = 0;
		for ( BankItem item : storedItems.get(BankTab.Tab1) )
		{
			inventory.setItem(i++, item.getItemStack());
		}
	}
	
	public void addItem(BankTab tab, BankItem item)
	{
		List<BankItem> items = storedItems.get(tab);
		
		if ( items == null )
			return;
		
		items.add(item);
	}
	
	public boolean removeItem(BankTab tab, BankItem item)
	{
		List<BankItem> items = storedItems.get(tab);
		
		if ( items == null )
			return false;
		
		return items.remove(item);
	}
	
}

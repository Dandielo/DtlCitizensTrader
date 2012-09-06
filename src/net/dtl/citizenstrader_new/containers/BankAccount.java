package net.dtl.citizenstrader_new.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory; 
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.dtl.citizenstrader_new.CitizensTrader;
import net.dtl.citizenstrader_new.backends.Backend;
import net.dtl.citizenstrader_new.traders.Banker.BankTab;

abstract public class BankAccount implements InventoryHolder  {
	//
	protected static Backend backend = CitizensTrader.getBackendManager().getBackend();
	
	//Stored items
	protected Map<BankTab, List<BankItem>> storedItems;	
	protected Map<BankTab, ItemStack> tabItems;
	
	//bank account owner
	protected String owner;
	
	//Constructor
	public BankAccount()
	{
		owner = "";
		storedItems = new HashMap<BankTab, List<BankItem>>();
		tabItems = new HashMap<BankTab, ItemStack>();
	}
	
	public List<BankItem> getBankTab(BankTab key)
	{
		return storedItems.get(key);
	}
	
	public Inventory inventoryView(int size, String name) {
		Inventory view = Bukkit.createInventory(this, size, name);
		
		for( BankItem item : storedItems.get(BankTab.Tab1) ) {

	        ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	        chk.addEnchantments(item.getItemStack().getEnchantments());
	
	        if ( item.getSlot() < 0 )
        		item.setSlot(view.firstEmpty());
	        view.setItem(item.getSlot(),chk);
        }

		tabSelectionView(view);
		return view;
	}
	
	public Inventory cleanInventory(int size, String name) {
		Inventory view = Bukkit.createInventory(this, size, name);
		
		return view;
	}
	
	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, "Banker");
		
		return inv;
	}
	
	public boolean hasAllTabs()
	{
		if ( storedItems.containsKey(BankTab.Tab9) )
			return true;
		return false;
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
				tabItems.put(tab, new ItemStack(35,1));
				backend.addBankTab(owner, tab);
				return tab;
			}
		}
		
		return null;
	}

	public void inventoryView(Inventory inventory, BankTab tab)
	{
		
		for ( BankItem item : storedItems.get(tab) )
			inventory.setItem(item.getSlot(), item.getItemStack());
		
		tabSelectionView(inventory);
	}
	
	public void tabSelectionView(Inventory inventory)
	{
		int lastRow = ( inventory.getSize() / 9 ) - 1;
		//System.out.print("a");
		int i = lastRow * 9;
		
		for ( BankTab tab : storedItems.keySet() )
		{

			inventory.setItem(i + Integer.parseInt(tab.toString().substring(3))-1, tabItems.get(tab));
		//	++i;
		}
	//	for ( BankItem item : storedItems.get(tab) )
	//		inventory.setItem(item.getSlot(), item.getItemStack());
	}
	
	public void settingsView(Inventory inventory, BankTab btab)
	{
		int lastRow = ( inventory.getSize() / 9 ) - 1;
		int i = lastRow * 9;
		
		//tabs
		//buy new tab
		inventory.setItem(0, new ItemStack(35,1,(short)0,(byte)1));
		
		//set tab item
		inventory.setItem(1, new ItemStack(35,1,(short)0,(byte)2));
		
		for ( int j = 0 ; j < 9 ; ++j )
		{
			inventory.setItem(((lastRow-1)*9)+j, tabItems.get(btab) );
		}
		
		for ( BankTab tab : storedItems.keySet() )
		{
			/*if ( tab.equals(btab) )
				for ( int j = 0 ; j < 9 ; ++j )
				{
					inventory.setItem(((lastRow-1)*9)+j, tabItems.get(tab) );
				}*/
			
			inventory.setItem((lastRow*9) + Integer.parseInt(tab.toString().substring(3))-1, tabItems.get(tab) );
			++i;
		}
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
	
	public void setBankTabItem(BankTab tab, BankItem item)
	{
		tabItems.put(tab, item.getItemStack());
		backend.setBankTabItem(owner, tab, item);
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

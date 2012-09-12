package net.dtl.citizens.trader.objects;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory; 
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.backends.Backend;
import net.dtl.citizens.trader.traders.Banker.BankTabType;

abstract public class BankAccount implements InventoryHolder  {
	//
	protected static Backend backend = CitizensTrader.getBackendManager().getBackend();
	
	//Stored items
	protected Map<BankTabType, BankTab> bankTabs;	
	//protected Map<BankTab, ItemStack> tabItems;
	
	//bank account owner
	protected String owner;
	protected int availableTabs;
	
	//Constructor
	public BankAccount()
	{
		owner = "";
		bankTabs = new HashMap<BankTabType, BankTab>();
		
		//storedItems = new HashMap<BankTab, List<BankItem>>();
		//tabItems = new HashMap<BankTab, ItemStack>();
	}
	
	public BankTab getBankTab(BankTabType key)
	{
		return bankTabs.get(key);
	}
	
	public Inventory inventoryView(int size, String name) {
		Inventory view = Bukkit.createInventory(this, size, name);
		
		for( BankItem item : bankTabs.get(BankTabType.Tab1).getTabItems() ) {

	        ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	        chk.addEnchantments(item.getItemStack().getEnchantments());
	
	        if ( item.getSlot() < 0 )
        		item.setSlot(view.firstEmpty());
	        view.setItem(item.getSlot(),chk);
        }

		tabSelectionView(view);
		return view;
	}
	
	public Inventory inventoryTabView(BankTabType type) {
		BankTab tab = bankTabs.get(type);
		
		Inventory view = Bukkit.createInventory(this, ( tab.getTabSize() + 1) * 9, tab.getTabName());
		
		for( BankItem item : bankTabs.get(BankTabType.Tab1).getTabItems() ) {

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
		if ( bankTabs.containsKey(BankTabType.Tab9) )
			return true;
		return false;
	}
	
	public BankTabType nextTab()
	{
		if ( bankTabs.containsKey(BankTabType.Tab9) )
			return null;
		
		final String bankTabName = "tab";
		for ( int i = 0 ; i < 9 ; ++ i )
		{
			BankTabType tab = BankTabType.getTabByName(bankTabName+(i+1));
			
			if ( !bankTabs.containsKey(tab) )
				return tab;
		}
		
		return null;
	}
	
	public BankTabType addBankTab()
	{
		if ( bankTabs.containsKey(BankTabType.Tab9) )
			return null;
		
		final String bankTabName = "tab";
		
		for ( int i = 0 ; i < 9 ; ++ i )
		{
			BankTabType tab = BankTabType.getTabByName(bankTabName+(i+1));
			
			if ( !bankTabs.containsKey(tab) )
			{
			//	storedItems.put(tab, new ArrayList<BankItem>());
			//	tabItems.put(tab, new ItemStack(35,1));
				bankTabs.put(tab, new BankTab(new ItemStack(35,1), tab.toString(), CitizensTrader.getInstance().getConfig().getConfigurationSection("bank").getInt("tab-size")));
				backend.addBankTab(owner, tab);
				return tab;
			}
		}
		
		return null;
	}
	
	public void increaseTabSize(BankTabType tabType)
	{
		BankTab tab = bankTabs.get(tabType);
		
		if ( tab.getTabSize() < 5 )
		{
			tab.setTabSize(tab.getTabSize()+1);
		
			backend.increaseTabSize(owner, tabType, tab.getTabSize());
		}
	}

	public void inventoryView(Inventory inventory, BankTabType tab)
	{
		
		for ( BankItem item : bankTabs.get(tab).getTabItems() )
			inventory.setItem(item.getSlot(), item.getItemStack());
		
		tabSelectionView(inventory);
	}
	
	public void tabSelectionView(Inventory inventory)
	{
		int lastRow = ( inventory.getSize() / 9 ) - 1;
		//System.out.print("a");
		int i = lastRow * 9;
		
		for ( BankTabType tab : bankTabs.keySet() )
		{

			inventory.setItem(i + Integer.parseInt(tab.toString().substring(3))-1, bankTabs.get(tab).getTabItem());
		//	++i;
		}
	//	for ( BankItem item : storedItems.get(tab) )
	//		inventory.setItem(item.getSlot(), item.getItemStack());
	}
	
	public void settingsView(Inventory inventory, BankTabType btab)
	{
		int lastRow = ( inventory.getSize() / 9 ) - 1;
		int i = lastRow * 9;
		
		//tabs
		//buy new tab
	//	inventory.setItem(0, new ItemStack(35,1,(short)0,(byte)1));
		
		//set tab item
	//	inventory.setItem(1, new ItemStack(35,1,(short)0,(byte)2));
		
		BankTab bankTab = bankTabs.get(btab);
		
		for ( int j = 0 ; j < 9 ; ++j )
		{
		//	if ( j < bankTab.getTabSize() || j > 4 )
				inventory.setItem(((lastRow-1)*9)+j, bankTab.getTabItem() );
	//		if ( bankTab.getTabSize() < 5 )
	//			inventory.setItem(((lastRow-1)*9)+bankTab.getTabSize(), new ItemStack(35,1) );
				
		}
		
		for ( BankTabType tab : bankTabs.keySet() )
		{
			/*if ( tab.equals(btab) )
				for ( int j = 0 ; j < 9 ; ++j )
				{
					inventory.setItem(((lastRow-1)*9)+j, tabItems.get(tab) );
				}*/
			
			inventory.setItem((lastRow*9) + Integer.parseInt(tab.toString().substring(3))-1, bankTabs.get(tab).getTabItem() );
			++i;
		}
		if ( i < 53 && availableTabs > i % 45 )
			inventory.setItem(i, new ItemStack(35,1) );
	}
	
	public void addItem(BankTabType tabType, BankItem item)
	{
		BankTab tab = bankTabs.get(tabType);//.getTabItems();
		
		if ( tab == null )
			return;
		
		tab.addItem(item);
		backend.addItem(owner, tabType, item);
	}

	public void updateItem(BankTabType tab, BankItem oldItem, BankItem newItem) {
		removeItem(tab, oldItem);
		addItem(tab, newItem);
		/*List<BankItem> items = storedItems.get(tab);
		
		if ( items == null )
			return;
		
		items.add(item);
		backend.addItem(owner, tab, item);*/
	}
	
	public void setBankTabItem(BankTabType tab, BankItem item)
	{
		bankTabs.get(tab).setTabItem(item.getItemStack());
		backend.setBankTabItem(owner, tab, item);
	}
	
	public boolean removeItem(BankTabType tabType, BankItem item)
	{
		BankTab tab = bankTabs.get(tabType);
		
		if ( tab == null )
			return false;
		
		tab.removeItem(item);
		backend.removeItem(owner, tabType, item);
		
		return true;
	}

	public BankItem getItem(int slot, BankTabType tab) {
		for ( BankItem item : bankTabs.get(tab).getTabItems() )
		{
			if ( item.getSlot() == slot )
				return item;
		}
		return null;
	}
	
}

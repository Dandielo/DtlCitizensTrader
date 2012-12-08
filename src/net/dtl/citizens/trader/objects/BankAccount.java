package net.dtl.citizens.trader.objects;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.InventoryHolder;

import net.dtl.citizens.trader.CitizensTrader;

abstract public class BankAccount implements InventoryHolder  {
	//
	//protected static Backend backend = CitizensTrader.getBackendManager().getBackend();
	protected static ConfigurationSection config = CitizensTrader.getInstance().getConfig();
	
	//Stored items
	protected Map<String, BankTab> bankTabs;	
	
	//bank account owner
	protected String owner;
	protected int availableTabs;
	protected AccountType type;
	
	//Constructor
	public BankAccount(String owner)
	{
		this.owner = owner;
		bankTabs = new HashMap<String, BankTab>();
	}

	public String getOwner()
	{
		return owner;
	}
	
	public abstract AccountType getType();
	
	//Bank tab methods
	public abstract BankTab getBankTab(String tab);
	
	public abstract boolean maxed();
	
	public abstract String nextTabName();
	
	public abstract boolean addBankTab();
	
	//Item management methods
	public abstract void addItem(String tab, BankItem item);

	public abstract void updateItem(String tab, BankItem oldItem, BankItem newItem);
	
	public abstract void removeItem(String tab, BankItem item);

	public abstract BankItem getItem(String tab, int slot);
	
/*	public Inventory inventoryView(int size, String name) {
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
			//if ( tab.equals(btab) )
			//	for ( int j = 0 ; j < 9 ; ++j )
			//	{
			//		inventory.setItem(((lastRow-1)*9)+j, tabItems.get(tab) );
			//	}
			
			inventory.setItem((lastRow*9) + Integer.parseInt(tab.toString().substring(3))-1, bankTabs.get(tab).getTabItem() );
			++i;
		}
		if ( i < 54 && availableTabs > i % 45 )
			inventory.setItem(i, new ItemStack(35,1) );
	}*/
	
	
	public enum AccountType {
		PLAYER, GUILD, ABSTRACT;
		
		public boolean isPlayer()
		{
			return this.equals(PLAYER);
		}
		
		public boolean isGuild()
		{
			return this.equals(GUILD);
		}
		
		public boolean isAbstract()
		{
			return this.equals(ABSTRACT);
		}
		
		@Override
		public String toString()
		{
			return ( this.equals(PLAYER) ? "player" : "guild" );
		}
	}
}

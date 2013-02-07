package net.dandielo.citizens.trader.objects;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.dandielo.citizens.trader.CitizensTrader;

abstract public class BankAccount implements InventoryHolder  {
	//
	//protected static Backend backend = CitizensTrader.getBackendManager().getBackend();
	protected static ConfigurationSection config = CitizensTrader.getInstance().getConfig();
	
	//Stored items
	protected Map<Integer, BankTab> bankTabs;	
	
	//bank account owner
	protected String owner;
	protected int availableTabs = config.getInt("bank.max-tabs", 9);
	protected AccountType type;
	
	//Constructor
	public BankAccount(String owner)
	{
		this.owner = owner;
		bankTabs = new HashMap<Integer, BankTab>();
	}

	public String getOwner()
	{
		return owner;
	}
	
	public abstract AccountType getType();
	
	//Bank tab methods
	public int tabs()
	{
		return bankTabs.size();
	}
	
	public abstract BankTab getBankTab(int tab);
	
	public abstract boolean maxed();
	
	public abstract String nextTabName();
	
	public abstract boolean addBankTab();
	
	//Item management methods
	public abstract void addItem(int tab, BankItem item);

	public abstract void updateItem(int tab, BankItem oldItem, BankItem newItem);
	
	public abstract void removeItem(int tab, BankItem item);

	public abstract BankItem getItem(int tab, int slot);
	
	public Inventory exchangeInventory(int size, String name)
	{
		return Bukkit.createInventory(this, size, name);
	}
	public Inventory inventoryView(String name) {
		Inventory view = Bukkit.createInventory(this, (bankTabs.get(0).getTabSize()+1)*9, name);
		for( BankItem item : bankTabs.get(0).getTabItems() ) 
		{

	        ItemStack chk = item.getItemStack().clone();//new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	     //   chk.addEnchantments(item.getItemStack().getEnchantments());
	
	        if ( item.getSlot() < 0 )
        		item.setSlot(view.firstEmpty());
	        view.setItem(item.getSlot(),chk);
        }

		tabSelectionView(view);
		return view;
	}
	/*
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
*/
	public void inventoryView(Inventory inventory, int tab)
	{
		
		for ( BankItem item : bankTabs.get(tab).getTabItems() )
			inventory.setItem(item.getSlot(), item.getItemStack());
		
		tabSelectionView(inventory);
	}
	
	public void tabSelectionView(Inventory inventory)
	{
		int lastRow = ( inventory.getSize() / 9 ) - 1;
		int i = lastRow * 9;
		
		for ( BankTab tab : bankTabs.values() )
			inventory.setItem(i++, tab.getTabItem().getItemStack());
	}
	
	public void settingsView(Inventory inventory, int tab)
	{
		int lastRow = ( inventory.getSize() / 9 ) - 1;
		int i = lastRow * 9;
		
		BankTab bankTab = bankTabs.get(tab);
		
		for ( int j = 0 ; j < 9 ; ++j )
		{
			inventory.setItem(((lastRow-1)*9)+j, bankTab.getTabItem().getItemStack() );
		}
		
		for ( Map.Entry<Integer, BankTab> mTab : bankTabs.entrySet() )
		{
			inventory.setItem((lastRow*9) + mTab.getKey(), mTab.getValue().getTabItem().getItemStack() );
			++i;
		}
		if ( i < inventory.getSize() && availableTabs > i % ( inventory.getSize() - 9 ) )
			inventory.setItem(i, new ItemStack(35,1) );
	}
	
	
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

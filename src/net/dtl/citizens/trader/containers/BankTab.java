package net.dtl.citizens.trader.containers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class BankTab {
	private String tabName;
	private int tabSize;
	private ItemStack tabItem;
	private List<BankItem> items;
	
	public BankTab(ItemStack item, String name, int size)
	{
		items = new ArrayList<BankItem>();
		tabItem = item;
		tabName = name;
		tabSize = size;
	}
	
	public void setTabName(String name)
	{
		tabName = name;
	}
	
	public String getTabName()
	{
		return tabName;
	}
	
	public void setTabItem(ItemStack item)
	{
		tabItem = item;
	}
	
	public ItemStack getTabItem()
	{
		return tabItem;
	}
	
	public void setTabSize(int size)
	{
		tabSize = size;
	}
	
	public int getTabSize()
	{
		return tabSize;
	}
	
	public List<BankItem> getTabItems()
	{
		return items;
	}
	
	public void setTabItems(List<BankItem> items)
	{
		if ( items == null )
			return;
		
		this.items = items;
	}

	public void addItem(BankItem item)
	{
		items.add(item);
	}
	
	public void removeItem(BankItem item)
	{
		items.remove(item);
	}
}

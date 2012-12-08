package net.dtl.citizens.trader.objects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class BankTab {
	private String tabName;
	private int tabSize;
	private BankItem tabItem;
	
	//item list
	private List<BankItem> items;
	
	public BankTab(BankItem item, String name, int size)
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
	
	public void setTabItem(BankItem item)
	{
		tabItem = item;
	}
	
	public BankItem getTabItem()
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
	
	public void setTabItems(List<BankItem> items)
	{
		if ( items == null )
			return;
		
		this.items = items;
	}
	
	public List<BankItem> getTabItems()
	{
		return items;
	}

	//TODO shoudl i do it in the "right" way?
	public BankItem getBankItem(int slot)
	{
		for ( BankItem item : items )
			if ( item.getSlot() == slot )
				return item;
		return null;
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

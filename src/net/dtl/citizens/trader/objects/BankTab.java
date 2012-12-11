package net.dtl.citizens.trader.objects;

import java.util.ArrayList;
import java.util.List;

public class BankTab {
	private final int id;
	private String tabName;
	private int tabSize;
	private BankItem tabItem;
	
	//item list
	private List<BankItem> items;
	
	public BankTab(BankItem item, int tid, String name, int size)
	{
		id = tid;
		items = new ArrayList<BankItem>();
		tabItem = item;
		tabName = name;
		tabSize = size;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setName(String name)
	{
		tabName = name;
	}
	
	public String getName()
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
	
	@Override
	public boolean equals(Object obj)
	{
		return id == ((BankTab)obj).getId();
	}
}

package net.dtl.citizens.trader.objects;

import java.util.Date;

public class MarketItem extends StockItem {
	protected String itemOwner;
	protected Date time;
	
	public MarketItem(String data) {
		super(data);
	}
	
	public String getItemOwner()
	{
		return itemOwner;
	}
	
	public void setItemOwner(String owner)
	{
		itemOwner = owner;
	}
}

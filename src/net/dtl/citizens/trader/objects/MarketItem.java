package net.dtl.citizens.trader.objects;

public class MarketItem extends StockItem {
	protected String itemOwner;
	
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

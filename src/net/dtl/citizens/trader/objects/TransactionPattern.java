package net.dtl.citizens.trader.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.dtl.citizens.trader.traders.Trader.TraderStatus;

import org.bukkit.configuration.ConfigurationSection;

public class TransactionPattern {

	HashMap<String, List<StockItem>> patternItems;
	HashMap<String, HashMap<String, Double>> patternPrices;
	
	public TransactionPattern()
	{
		patternItems = new HashMap<String, List<StockItem>>();
		patternPrices = new HashMap<String, HashMap<String,Double>>();
	}
	
	public void loadPrices(ConfigurationSection prices)
	{
		HashMap<String,Double> sell = new HashMap<String,Double>();
		HashMap<String,Double> buy = new HashMap<String,Double>();
		
		for ( String transaction : prices.getKeys(false) )
		{
			if ( transaction.equals("all") )
			{
				for ( String item : prices.getConfigurationSection("all").getKeys(false) )
				{
					sell.put(item, prices.getDouble(transaction+"/"+item) );
					buy.put(item, prices.getDouble(transaction+"/"+item) );
				}
			}
			else
			if ( transaction.equals("sell") )
			{
				for ( String item : prices.getConfigurationSection("sell").getKeys(false) )
				{
					sell.put(item, prices.getDouble(transaction+"/"+item) );
				}
			}
			else
			if ( transaction.equals("buy") )
			{
				for ( String item : prices.getConfigurationSection("buy").getKeys(false) )
				{
					buy.put(item, prices.getDouble(transaction+"/"+item) );
				}
			}
		}
	}
	
	public void loadItems(ConfigurationSection items)
	{
		List<StockItem> sell = new ArrayList<StockItem>();
		List<StockItem> buy = new ArrayList<StockItem>();
		
		for ( String transaction : items.getKeys(false) )
		{
			if ( transaction.equals("all") )
			{
				for ( String item : items.getStringList("all") )
				{
					StockItem stockItem = new StockItem(item);
					stockItem.setAsPetternItem(true);
					sell.add(stockItem);
					buy.add(stockItem);
				}
			}
			else
			if ( transaction.equals("sell") )
			{
				for ( String item : items.getStringList("sell") )
				{
					StockItem stockItem = new StockItem(item);
					stockItem.setAsPetternItem(true);
					sell.add(stockItem);
				}
			}
			else
			if ( transaction.equals("buy") )
			{
				for ( String item : items.getStringList("buy") )
				{
					StockItem stockItem = new StockItem(item);
					stockItem.setAsPetternItem(true);
					buy.add(stockItem);
				}
			}
		}
		patternItems.put("sell", sell);
		patternItems.put("buy", buy);
	}
	
	public List<StockItem> getStockItems(TraderStatus status)
	{
		if ( status.equals(TraderStatus.SELL)
				|| status.equals(TraderStatus.MANAGE_SELL) )
		{
			return patternItems.get("sell");
		}
		if ( status.equals(TraderStatus.BUY) 
				|| status.equals(TraderStatus.MANAGE_BUY) )
		{
			return patternItems.get("buy");
		}
		return null;
	}
	
	
	
}

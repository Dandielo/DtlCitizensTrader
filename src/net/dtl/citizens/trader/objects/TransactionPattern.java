package net.dtl.citizens.trader.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		patternPrices.put("sell", sell);
		patternPrices.put("buy", buy);
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
					stockItem.setAsPatternItem(true);
					if ( stockItem.getSlot() < 0 )
						sell.add(stockItem);
					else
						sell.add(0, stockItem);
					
					stockItem = new StockItem(item);
					stockItem.setAsPatternItem(true);
					if ( stockItem.getSlot() < 0 )
						buy.add(stockItem);
					else
						buy.add(0, stockItem);
				}
			}
			else
			if ( transaction.equals("sell") )
			{
				for ( String item : items.getStringList("sell") )
				{
					StockItem stockItem = new StockItem(item);
					stockItem.setAsPatternItem(true);
					if ( stockItem.getSlot() < 0 )
						sell.add(stockItem);
					else
						sell.add(0, stockItem);
				}
			}
			else
			if ( transaction.equals("buy") )
			{
				for ( String item : items.getStringList("buy") )
				{
					StockItem stockItem = new StockItem(item);
					stockItem.setAsPatternItem(true);
					if ( stockItem.getSlot() < 0 )
						buy.add(stockItem);
					else
						buy.add(0, stockItem);
				}
			}
		}
		patternItems.put("sell", sell);
		patternItems.put("buy", buy);
	}
	
	public List<StockItem> getStockItems(String transation)
	{
		return patternItems.get(transation);
	}

	public boolean getItemPrice(StockItem item, String transation) {
		if ( patternPrices.get(transation) == null )
			return false;
		if ( patternPrices.get(transation).containsKey(item.getIdAndData()) )
		{
			item.setRawPrice(patternPrices.get(transation).get(item.getIdAndData()));
			return true;
		}
			
		for ( Map.Entry<String, Double> entry : patternPrices.get(transation).entrySet() )
		{
			if ( item.getIdAndData().split(":")[0].equals(entry.getKey()) )
			{
				item.setRawPrice(entry.getValue());
				return true;
			}
		}
		return false;
	}
	
	
	
}

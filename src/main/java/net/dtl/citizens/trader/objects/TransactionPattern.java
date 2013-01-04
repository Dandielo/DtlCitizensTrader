package net.dtl.citizens.trader.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.managers.PatternsManager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class TransactionPattern {
	private static PatternsManager patternsManager;
	
	private final String name;
	private final boolean tier;
	
	HashMap<String, List<StockItem>> patternItems;
	HashMap<String, HashMap<String, Double>> patternPrices;
	TreeMap<String, TransactionPattern> patternInherits;
	TreeMap<String, TransactionPattern> patternTiers;
	HashMap<String, Double> multiplier;
	
	public TransactionPattern(String name, PatternsManager manager)
	{
		this(name, false);
		manager = patternsManager;
	}
	
	public TransactionPattern(String name, boolean tier)
	{
		//teh patterns name
		this.name = name;
		//is this pattern a tier?
		this.tier = tier;
		
		//pattern items, needs to add both buy and sell when "items" section not in a pattern to avoid NPE
		patternItems = new HashMap<String, List<StockItem>>();
		patternItems.put("sell", new ArrayList<StockItem>());
		patternItems.put("buy", new ArrayList<StockItem>());
		
		//pattern prices
		patternPrices = new HashMap<String, HashMap<String,Double>>();
		
		//pattenr triers and inherits
		patternTiers = new TreeMap<String, TransactionPattern>();
		patternInherits = new TreeMap<String, TransactionPattern>();
		
		//multipliers
		multiplier = new HashMap<String, Double>();
		multiplier.put("sell", 1.00);
		multiplier.put("buy", 1.00);
	}
	
	public String getName()
	{
		return name;
	}
	
	public void prepareInherits(ConfigurationSection inherits)
	{
		for ( String inherit : inherits.getStringList("inherits") )
		{
			TransactionPattern pattern = patternsManager.getPattern(inherit);
			if ( pattern != null )
				patternInherits.put(inherit, pattern);
			else
				CitizensTrader.warning("Could not load inherited pattern, does is it defined before this one?");
		}
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
					if ( item.equals("multiplier") )
					{
						multiplier.put("sell", prices.getDouble(transaction+"/"+item));
						multiplier.put("buy", prices.getDouble(transaction+"/"+item));
					}
					else
					{
						sell.put(item, prices.getDouble(transaction+"/"+item) );
						buy.put(item, prices.getDouble(transaction+"/"+item) );
					}
				}
			}
			else
			if ( transaction.equals("sell") )
			{
				for ( String item : prices.getConfigurationSection("sell").getKeys(false) )
				{
					if ( item.equals("multiplier") )
					{
						multiplier.put("sell", prices.getDouble(transaction+"/"+item));
					}
					else
						sell.put(item, prices.getDouble(transaction+"/"+item) );
				}
			}
			else
			if ( transaction.equals("buy") )
			{
				for ( String item : prices.getConfigurationSection("buy").getKeys(false) )
				{
					if ( item.equals("multiplier") )
					{
						multiplier.put("buy", prices.getDouble(transaction+"/"+item));
					}
					else
						buy.put(item, prices.getDouble(transaction+"/"+item) );
				}
			}
			else
			if ( transaction.startsWith("tier") )
			{
				TransactionPattern tier = patternTiers.get(transaction);
				if ( tier == null )
				{
					tier = new TransactionPattern(name+":"+transaction, true);
					patternTiers.put(transaction, tier);
				}
				
				tier.loadPrices( prices.getConfigurationSection(transaction) );
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
			/*else
			if ( transaction.startsWith("tier") )
			{
				TransactionPattern tier = patternTiers.get(transaction);
				if ( tier == null )
				{
					tier = new TransactionPattern();
					patternTiers.put(transaction, tier);
				}
				
				tier.loadItems( items.getConfigurationSection(transaction) );
			}*/
		}
		patternItems.put("sell", sell);
		patternItems.put("buy", buy);
	}
	
	public List<StockItem> getStockItems(String transation)
	{
		return patternItems.get(transation);
	}
	
	public double getMultiplier(String transaction)
	{
		return multiplier.get(transaction);
	}

	public double getItemPrice(Player player, StockItem item, String transation, int slot, double nprice) 
	{
		double price = nprice;
		double m = multiplier.get(transation);

		if ( item.isPatternListening() )
		{
			for ( Map.Entry<String, TransactionPattern> inherit : patternInherits.entrySet() )
			{
				if ( inherit.getValue() != null )
				{
					price = inherit.getValue().getItemPrice(player, item, transation, slot, price);
					m = inherit.getValue().getMultiplier(transation);
				}
			}
			
			if ( patternPrices.containsKey(transation) )
				if ( patternPrices.get(transation).containsKey(item.getIdAndData()) )
					price = patternPrices.get(transation).get(item.getIdAndData());
				else
					for ( Map.Entry<String, Double> entry : patternPrices.get(transation).entrySet() )
						if ( item.getIdAndData().split(":")[0].equals(entry.getKey()) )
							price = entry.getValue();

			for ( Map.Entry<String, TransactionPattern> tier : patternTiers.entrySet() )
				if ( CitizensTrader.getPermissionsManager().has(player, "dtl.trader.tiers." + tier.getKey()) )
				{
					price = tier.getValue().getItemPrice(player, item, transation, slot, price);
					m = tier.getValue().getMultiplier(transation);
				}
			
			if ( !tier )
				price *= m;
		}
		else
			price = item.getRawPrice();

		if ( !item.hasStackPrice() && nprice == 0.0 && !tier )
			price *= item.getAmount(slot);
		
		return price;
	}
	
	
	
}

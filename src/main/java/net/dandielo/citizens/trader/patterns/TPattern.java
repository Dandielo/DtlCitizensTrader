package net.dandielo.citizens.trader.patterns;

import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.managers.PermissionsManager;

import org.bukkit.configuration.ConfigurationSection;

public abstract class TPattern {	
	// permissions manager
	protected static final PermissionsManager perms = CitizensTrader.getPermissionsManager();
	
	// pattern fields 
	protected final String name;
	protected final String type;
	
	public TPattern(String name, String type, boolean tier)
	{
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public abstract void load(ConfigurationSection data);
	
	public abstract <T> T getData(String data, Class<T> type);
//		/*
//		
//		//pattern items, needs to add both buy and sell when "items" section not in a pattern to avoid NPE
//		patternItems = new HashMap<String, List<StockItem>>();
//		patternItems.put("sell", new ArrayList<StockItem>());
//		patternItems.put("buy", new ArrayList<StockItem>());
//		
//		//pattern prices
//		patternPrices = new HashMap<String, HashMap<String,Double>>();
//		
//		//pattenr triers and inherits
//		patternTiers = new TreeMap<String, TPattern>();
//		patternInherits = new TreeMap<String, TPattern>();
//		
//		//multipliers
//		multiplier = new HashMap<String, Double>();
//		multiplier.put("sell", 1.00);
//		multiplier.put("buy", 1.00);
//	}
//	
////	HashMap<String, List<StockItem>> i
///*	
//	HashMap<String, List<StockItem>> patternItems;
//	HashMap<String, HashMap<String, Double>> patternPrices;
//	TreeMap<String, TPattern> patternInherits;
//	TreeMap<String, TPattern> patternTiers;
//	HashMap<String, Double> multiplier;
//	
//	public TPattern(String name, PatternsManager manager)
//	{
//		this(name, "master");
//		patternsManager = manager;
//	
//	
//	public void prepareInherits(ConfigurationSection inherits)
//	{
//		for ( String inherit : inherits.getStringList("inherits") )
//		{
//			TPattern pattern = patternsManager.getPattern(inherit);
//			if ( pattern != null )
//				patternInherits.put(inherit, pattern);
//			else
//				CitizensTrader.warning("Could not load inherited pattern, does is it defined before this one?");
//		}
//	}*/
	
//	public void loadPrices(ConfigurationSection prices)
//	{
//		HashMap<String,Double> sell = new HashMap<String,Double>();
//		HashMap<String,Double> buy = new HashMap<String,Double>();
//		
//		for ( String transaction : prices.getKeys(false) )
//		{
//			if ( transaction.equals("all") )
//			{
//				for ( String item : prices.getConfigurationSection("all").getKeys(false) )
//				{
//					if ( item.equals("multiplier") )
//					{
//						multiplier.put("sell", prices.getDouble(transaction+"/"+item));
//						multiplier.put("buy", prices.getDouble(transaction+"/"+item));
//					}
//					else
//					{
//						sell.put(item, prices.getDouble(transaction+"/"+item) );
//						buy.put(item, prices.getDouble(transaction+"/"+item) );
//					}
//				}
//			}
//			else
//			if ( transaction.equals("sell") )
//			{
//				for ( String item : prices.getConfigurationSection("sell").getKeys(false) )
//				{
//					if ( item.equals("multiplier") )
//					{
//						multiplier.put("sell", prices.getDouble(transaction+"/"+item));
//					}
//					else
//						sell.put(item, prices.getDouble(transaction+"/"+item) );
//				}
//			}
//			else
//			if ( transaction.equals("buy") )
//			{
//				for ( String item : prices.getConfigurationSection("buy").getKeys(false) )
//				{
//					if ( item.equals("multiplier") )
//					{
//						multiplier.put("buy", prices.getDouble(transaction+"/"+item));
//					}
//					else
//						buy.put(item, prices.getDouble(transaction+"/"+item) );
//				}
//			}
//			else
//			if ( transaction.startsWith("tier") )
//			{
//				TPattern tier = patternTiers.get(transaction);
//				if ( tier == null )
//				{
//					//TODO new pattern tierring
//					tier = new TPattern(name+":"+transaction, true);
//					patternTiers.put(transaction, tier);
//				}
//				
//				tier.loadPrices( prices.getConfigurationSection(transaction) );
//			}
//		}
//		patternPrices.put("sell", sell);
//		patternPrices.put("buy", buy);
//	}
//	
//	public void loadItems(ConfigurationSection items)
//	{
//		List<StockItem> sell = new ArrayList<StockItem>();
//		List<StockItem> buy = new ArrayList<StockItem>();
//		
//		for ( String transaction : items.getKeys(false) )
//		{
//			if ( transaction.equals("all") )
//			{
//				for ( String item : items.getStringList("all") )
//				{
//					StockItem stockItem = new StockItem(item);
//					stockItem.setAsPatternItem(true);
//					if ( stockItem.getSlot() < 0 )
//						sell.add(stockItem);
//					else
//						sell.add(0, stockItem);
//					
//					stockItem = new StockItem(item);
//					stockItem.setAsPatternItem(true);
//					if ( stockItem.getSlot() < 0 )
//						buy.add(stockItem);
//					else
//						buy.add(0, stockItem);
//				}
//			}
//			else
//			if ( transaction.equals("sell") )
//			{
//				for ( String item : items.getStringList("sell") )
//				{
//					StockItem stockItem = new StockItem(item);
//					stockItem.setAsPatternItem(true);
//					if ( stockItem.getSlot() < 0 )
//						sell.add(stockItem);
//					else
//						sell.add(0, stockItem);
//				}
//			}
//			else
//			if ( transaction.equals("buy") )
//			{
//				for ( String item : items.getStringList("buy") )
//				{
//					StockItem stockItem = new StockItem(item);
//					stockItem.setAsPatternItem(true);
//					if ( stockItem.getSlot() < 0 )
//						buy.add(stockItem);
//					else
//						buy.add(0, stockItem);
//				}
//			}
//			/*else
//			if ( transaction.startsWith("tier") )
//			{
//				TransactionPattern tier = patternTiers.get(transaction);
//				if ( tier == null )
//				{
//					tier = new TransactionPattern();
//					patternTiers.put(transaction, tier);
//				}
//				
//				tier.loadItems( items.getConfigurationSection(transaction) );
//			}*/
//		}
//		patternItems.put("sell", sell);
//		patternItems.put("buy", buy);
//	}
//	
//	public List<StockItem> getStockItems(String transaction)
//	{
//		return patternItems.get(transaction);
//	}
//	
//	public double getMultiplier(String transaction)
//	{
//		return multiplier.get(transaction);
//	}
//
//	public double getItemPrice(Player player, StockItem item, String transaction, int slot, double nprice)
//	{
//		return this.getItemPrice(player, item, transaction, slot, nprice, true);			
//	}
//	public double getItemPrice(Player player, StockItem item, String transaction, int slot, double nprice, boolean mp) 
//	{
//		double price = nprice;
//
//		if ( item.isPatternListening() )
//		{
//			double m = 1.0;
//			
//			for ( Map.Entry<String, TPattern> inherit : patternInherits.entrySet() )
//			{
//				if ( inherit.getValue() != null )
//				{
//					price = inherit.getValue().getItemPrice(player, item, transaction, slot, price, false);
//					m = inherit.getValue().getMultiplier(transaction);
//				}
//			}
//			
//			if ( patternPrices.containsKey(transaction) )
//				if ( patternPrices.get(transaction).containsKey(item.getIdAndData()) )
//					price = patternPrices.get(transaction).get(item.getIdAndData());
//				else
//					for ( Map.Entry<String, Double> entry : patternPrices.get(transaction).entrySet() )
//						if ( item.getIdAndData().split(":")[0].equals(entry.getKey()) )
//							price = entry.getValue();
//
//			for ( Map.Entry<String, TPattern> tier : patternTiers.entrySet() )
//				if ( CitizensTrader.getPermissionsManager().has(player, "dtl.trader.tiers." + tier.getKey()) )
//				{
//					price = tier.getValue().getItemPrice(player, item, transaction, slot, price, false);
//					m = tier.getValue().getMultiplier(transaction);
//				}
//			
//			if ( multiplier.get(transaction) != 1.0 )
//				m = multiplier.get(transaction);
//			
//			if ( !tier && mp )
//				price *= m;
//		}
//		else
//			price = item.getRawPrice();
//
//		if ( !item.hasStackPrice() && nprice == 0.0 && !tier )
//			price *= item.getAmount(slot);
//		
//		return price;
//	}
	
	
	
}

package net.dandielo.citizens.trader.patterns.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.patterns.TPattern;

public class PricePattern extends TPattern {

	public PricePattern(String name, String type, boolean tier) {
		super(name, type, tier);
		prices = new HashMap<String, List<StockItem>>();
		inherits = new HashMap<String, PricePattern>();
		tiers = new HashMap<String, PricePattern>();
	}
	
	private int priority;
	
	private Map<String, List<StockItem>> prices; 
	private Map<String, PricePattern> inherits;
	private Map<String, PricePattern> tiers;

	@Override
	public void load(ConfigurationSection data)
	{
		List<StockItem> sell = new ArrayList<StockItem>();
		List<StockItem> buy = new ArrayList<StockItem>();
		
		priority = data.getInt("priority", 0);
		
		for ( String key : data.getKeys(false) )
		{
			if ( key.equals("all") )
			{
				for ( Object object : data.getList(key) )
				{
					StockItem item = StockItem.loadItem(object);
					item.setAsPatternItem(true);
					sell.add(item);
					buy.add(item);
				}
			}
			if ( key.equals("sell") )
			{
				for ( Object object : data.getList(key) )
				{
					StockItem item = StockItem.loadItem(object);
					item.setAsPatternItem(true);
					sell.add(item);
				}
			}
			if ( key.equals("buy") )
			{

				for ( Object object : data.getList(key) )
				{
					StockItem item = StockItem.loadItem(object);
					item.setAsPatternItem(true);
					buy.add(item);
				}
			}
			else
			if ( key.equals("inherits") )
			{
				for ( String pat : data.getStringList(key) )
					inherits.put(pat, null);
			}
			else
			{
				PricePattern pattern = new PricePattern(name + "." + key, "price", true);
				pattern.load(data.getConfigurationSection(key));
				tiers.put(key, pattern);
			}
		}
		this.prices.put("sell", sell);
		this.prices.put("buy", buy);
	}
	
	public double getEndPrice(StockItem item, Player player, String stock)
	{
		return getPrice(item, player, stock, false).endPrice();
	}
	
	public double getEndPrice(StockItem item, Player player, String stock, boolean unit)
	{
		return getPrice(item, player, stock, unit).endPrice();
	}
	
	public Price getPrice(StockItem item, Player player, String stock)
	{
		return getPrice(item, player, stock, false);
	}
	
	public Price getPrice(StockItem item, Player player, String stock, boolean unit)
	{
		if ( !item.patternPrice() )
			return new Price(item.stackPrice() ? ( unit ? item.getRawPrice() / item.getAmount() : item.getRawPrice() ) : item.getPrice(0)); 

		Price price = new Price(0.0);
		for ( Entry<String, PricePattern> pat : inherits.entrySet() )
		{
			if ( pat.getValue() == null )
				continue;
			
			if ( perms.has(player, "") )
			{
				price.merge(pat.getValue().getPrice(item, player, stock));
			}
		}
		
		for ( StockItem match : prices.get(stock) )
		{
			if ( item.matches(match) )
			{
				if ( !match.patternPrice() && price.priority[0] <= match.getMatchPriority() )
				{
					price.price(match.getRawPrice(), match.getMatchPriority());
				}
				if ( match.hasMupltiplier() && price.priority[1] <= match.getMatchPriority() )
				{
					price.multipler(match.getMultiplier(), match.getMatchPriority());
				}
			}
		}
		for ( PricePattern pattern : tiers.values() )
		{
			if ( perms.has(player, "") )
			{
				price.merge(pattern.getPrice(item, player, stock));
			}
		}
		
		price.priority[0] += (priority * 1000);
		price.priority[1] += (priority * 1000);
		return price;
	}
	
	public static class Price
	{
		int[] priority = {-1, -1};
		double price;
		Double multiply;
		
		public Price(double p)
		{
			price = p;
			multiply = null;
		}
		
		public void merge(Price p)
		{
			if ( p.priority[0] >= priority[0] )
			{
				priority[0] = p.priority[0];
				price = p.price;
			}
			if ( p.priority[1] >= priority[1] )
			{
				priority[1] = p.priority[1];
				multiply = p.multiply;
			}
		}

		public void price(double rawPrice, int matchPriority) {
			priority[0] = matchPriority;
			price = rawPrice;
		}

		public void multipler(double multiplier, int matchPriority) {
			priority[1] = matchPriority;
			multiply = new Double(multiplier);
		}
		
		public boolean hasPrice()
		{
			return priority[0] > -1;
		}
		
		public boolean hasMultiplier()
		{
			return multiply != null;
		}
		
		public double getMultiplier()
		{
			return multiply != null ? multiply : 1.0;
		}
		
		public double endPrice()
		{
			return price *= getMultiplier();
		}
	}
}

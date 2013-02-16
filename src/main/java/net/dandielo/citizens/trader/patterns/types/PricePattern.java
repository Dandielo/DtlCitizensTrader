package net.dandielo.citizens.trader.patterns.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.patterns.TPattern;

public class PricePattern extends TPattern {

	public PricePattern(String name, String type, boolean tier) {
		super(name, type, tier);
		prices = new HashMap<String, List<StockItem>>();
		tiers = new HashMap<String, PricePattern>();
	}
	
	private Map<String, List<StockItem>> prices; 
	private Map<String, PricePattern> tiers;

	@Override
	public void load(ConfigurationSection data)
	{
		List<StockItem> sell = new ArrayList<StockItem>();
		List<StockItem> buy = new ArrayList<StockItem>();
		
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
			{
				PricePattern pattern = new PricePattern(name + "." + key, "price", true);
				pattern.load(data.getConfigurationSection(key));
				tiers.put(key, pattern);
			}
		}
		this.prices.put("sell", sell);
		this.prices.put("buy", buy);
	}
	
	public double getPrice(StockItem item, Player player, String stock)
	{
		return getPrice(item, player, stock, false);
	}
	
	public double getPrice(StockItem item, Player player, String stock, boolean unit)
	{
		if ( !item.patternPrice() )
			return item.stackPrice() ? ( unit ? item.getRawPrice() / item.getAmount() : item.getRawPrice() ) : item.getPrice(0); 
		
		Price price = new Price(0, -1); 
		
		
		return price.price;
	}
	
	@Override
//	@SuppressWarnings("unchecked")
	public <T> T getData(String data, Class<T> type) {
	//	if ( !items.get("sell").getClass().equals(type) )
			return null;
	//	return (T) items.get("sell");
	}
	
	public static class Price
	{
		int priority;
		double price;
		
		public Price(double pri, int prio)
		{
			price = pri;
			priority = prio;
		}
	}
}

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

public class ItemPattern extends TPattern {

	public ItemPattern(String name, String type, boolean tier) {
		super(name, type, tier);
		items = new HashMap<String, List<StockItem>>();
		inherits = new HashMap<String, ItemPattern>();
		tiers = new HashMap<String, ItemPattern>();
	}

	//should tiers override items?
	private boolean override;
	
	private Map<String, List<StockItem>> items;
	private Map<String, ItemPattern> inherits;
	private Map<String, ItemPattern> tiers;
	
	public void load(ConfigurationSection data)
	{
		List<StockItem> sell = new ArrayList<StockItem>();
		List<StockItem> buy = new ArrayList<StockItem>();
		
		override = data.getBoolean("override", false);
		
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
			else
			if ( key.equals("sell") )
			{
				for ( Object object : data.getList(key) )
				{
					StockItem item = StockItem.loadItem(object);
					item.setAsPatternItem(true);
					sell.add(item);
				}
			}
			else
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
				ItemPattern pattern = new ItemPattern(name + "." + key, "item", true);
				pattern.load(data.getConfigurationSection(key));
				tiers.put(key, pattern);
			}
		}
		this.items.put("sell", sell);
		this.items.put("buy", buy);
	}
	
	public List<StockItem> getStock(Player player, String stock)
	{
		List<StockItem> ret = new ArrayList<StockItem>();
		
		for ( Entry<String, ItemPattern> pat : inherits.entrySet() )
		{
			if ( pat.getValue() == null )
				continue;
			
			if ( perms.has(player, "") )
			{
				if ( override )
				{
					for ( StockItem item : pat.getValue().getStock(player, stock) )
					{
						ret.remove(item);
						ret.add(item);
					}
				}
				else
					ret.addAll(pat.getValue().getStock(player, stock));
			}
		}
		
		if ( override )
		{
			for ( StockItem item :items.get(stock) )
			{
				ret.remove(item);
				ret.add(item);
			}
		}
		else
			ret.addAll(items.get(stock)); 
		
		for ( Entry<String, ItemPattern> tier : tiers.entrySet() )
		{
			if ( perms.has(player, "") )
			{
				if ( override )
				{
					for ( StockItem item : tier.getValue().getStock(player, stock) )
					{
						ret.remove(item);
						ret.add(item);
					}
				}
				else
					ret.addAll(tier.getValue().getStock(player, stock));
			}
		}
		return items.get(stock);
	}
}

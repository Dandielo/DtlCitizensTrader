package net.dandielo.citizens.trader.limits;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.dandielo.citizens.trader.DtlTraders;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.types.Trader;

public class LimitManager {

	public LimitManager()
	{
		
	}
	
	protected final static char PATH_SEPARATOR = '/';
	
	protected FileConfiguration data;
	protected File file;
	
	public void loadFile()
	{
		ConfigurationSection config = DtlTraders.getInstance().getConfig();
		
		String name = config.getString("locale.file");
		if ( name == null ) 
		{
			name = "locale.en";
			config.set("locale.file", name);
			DtlTraders.getInstance().saveConfig();
		}
		
		String path = config.getString("locale.path", "plugins/DtlCitizensTrader/locale");
		if ( path.contains("\\") && !"\\".equals(File.separator) ) 
		{
			path = path.replace("\\", File.separator);
		}
		
		File baseDirectory = new File(path);
		if ( !baseDirectory.exists() ) 
			baseDirectory.mkdirs();

		
		file = new File(path, name);
		
		if ( !file.exists() )
		{
			try 
			{
				file.createNewFile();
				
				// Look for defaults in the jar
			    InputStream stream = DtlTraders.getInstance().getResource("locale.en");
			    
			    if (stream != null)
			    {
			        YamlConfiguration yconfig = YamlConfiguration.loadConfiguration(stream);
					data = new YamlConfiguration();
			        data.setDefaults(yconfig);
			        data.options().copyDefaults(true);
			    }
				
			    save();
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		load();
	}
	
	public void load()
	{
		data = new YamlConfiguration();
		data.options().pathSeparator(PATH_SEPARATOR);
				
		try 
		{
			data.load(file);

			for ( String key : data.getKeys(false) )
			{
				ConfigurationSection con = data.getConfigurationSection(key);
				List<LimitEntry> entries = new ArrayList<LimitEntry>();
				
				for ( String target : con.getKeys(false) )
				{
					LimitEntry e = new LimitEntry(target.equals("global limit") ? "global" : "player", target);
					for ( String item : con.getConfigurationSection(target).getKeys(false) )
					{
						e.addItem(item, con.getString(buildPath(target, item)) );
					}
					entries.add(e);
				}
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void save()
	{
		try {
			data.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, List<LimitEntry>> limits = new HashMap<String, List<LimitEntry>>();
	
	public StockItem getLimit(Trader trader, String target, StockItem item)
	{
		if ( limits.get(trader.getNpc().getName()) == null )
			return null;
		for ( LimitEntry entry : limits.get(trader.getNpc().getName()) )
			if ( entry.getTarget().equals(target) )
				return entry.getItem(item);
		return null;
	}
	
	public boolean checkLimit(Trader trader, String target, StockItem item, int amount)
	{
		if ( limits.get(trader.getNpc().getName()) == null )
			return true;
		for ( LimitEntry entry : limits.get(trader.getNpc().getName()) )
			if ( entry.getTarget().equals(target) )
				return entry.check(item, amount);
		return true;
	}
	
	public void updateLimit(Trader trader, String target, StockItem item, int amount) throws ParseException
	{
		List<LimitEntry> entries = limits.get(trader.getNpc().getName());
		if ( entries == null )
			 entries = new ArrayList<LimitEntry>();
		
		for ( LimitEntry entry : entries )
		{
			if ( entry.getTarget().equals(target) )
			{
				StockItem i = entry.getItem(item);
				if ( i == null )
					entry.addItem(item.toString(), new Date().toString());
				else
					i.setAmount(i.getAmount() + amount);
				updateLinked(trader, target, item, amount);
				return;
			}
		}
		
		if ( item.hasLimit(target) )
		{	
			LimitEntry newEntry = new LimitEntry(target.equals("global limit") ? "global" : "player", target);
			newEntry.addItem(item.toString(), new SimpleDateFormat("dd-MM-yy").format(new Date()) );
			newEntry.getItem(item).setAmount(amount);
			entries.add(newEntry);
			
			limits.put(trader.getNpc().getName(), entries);
		}
		
	}
	
	protected void updateLinked(Trader trader, String target, StockItem item, int amount) throws ParseException
	{
		StockItem linked = item.getLimits().getLinked();
		if ( linked != null )
			updateLimit(trader, target, linked, amount);
			//linked.getLimitSystem().limit.amount -= thisItem.getAmount(slot)*scale;
		//limit.changeAmount(thisItem.getAmount(slot)*scale);
	}
	
	public void scheduleCheck()
	{//TODO
		
	}
	
	public static class LimitEntry
	{
		private String limit;
		private String target;
		private Map<StockItem, Date> items;
		
		public LimitEntry(String limit, String player)
		{
			this.limit = limit;
			this.target = player;
			items = new HashMap<StockItem, Date>();
		}
		
		public void addItem(String itemData, String date) throws ParseException
		{
			items.put(StockItem.loadItem(itemData), new SimpleDateFormat("dd-MM-yy").parse(date));
		}
		
		public void removeItem(StockItem item)
		{
			items.remove(item);
		}
		
		public String getTarget()
		{
			return target;
		}
		
		public StockItem getItem(StockItem item)
		{
			StockItem matched = null;
			for ( StockItem itm : items.keySet() )
				if ( item.matches(itm, false) )
					if ( item == null || itm.getMatchPriority() >= matched.getMatchPriority() )
						matched = itm;
			return matched;
		}
		
		public boolean check(StockItem item, int amount)
		{
			StockItem matched = null;
			for ( StockItem itm : items.keySet() )
				if ( item.matches(itm, false) )
					if ( item == null || itm.getMatchPriority() >= matched.getMatchPriority() )
						matched = itm;
			return matched == null ? true : matched.getAmount() + amount >= matched.getLimits().get(limit).getLimit();					
		}
		
		@Override
		public boolean equals(Object o)
		{
			return target.equals(((LimitEntry)o).target);
		}
	}
	
	// helper tools
	public static String buildPath(String... path) 
	{
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		char separator = PATH_SEPARATOR; 

		for ( String node : path ) 
		{
			if ( !first ) 
			{
				builder.append(separator);
			}

			builder.append(node);

			first = false;
		}

		return builder.toString();
	}
}

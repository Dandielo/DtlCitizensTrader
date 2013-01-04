package net.dtl.citizens.trader.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.objects.TransactionPattern;

public class PatternsManager {

	HashMap<String, TransactionPattern> patterns = new HashMap<String, TransactionPattern>(); 
	
	private final static char PATH_SEPARATOR = '/';
	protected boolean separateFiles;

	protected FileConfiguration patternsConfig;
	protected File patternsFile;
	
	public PatternsManager() {		
		ConfigurationSection config = CitizensTrader.getInstance().getConfig();
		
		String accountsFilename = config.getString("trader.patterns.file");

		// Default settings
		if ( accountsFilename == null ) 
		{
			accountsFilename = "patterns.yml";
			config.set("trader.patterns.file", "patterns.yml");
		}

		String baseDir = config.getString("trader.patterns.basedir", "plugins/DtlCitizensTrader" );// "plugins/PermissionsEx");

		if ( baseDir.contains("\\") && !"\\".equals(File.separator) ) 
		{
			baseDir = baseDir.replace("\\", File.separator);
		}

		File baseDirectory = new File(baseDir);
		if ( !baseDirectory.exists() ) 
		{
			baseDirectory.mkdirs();
		}

		this.patternsFile = new File(baseDir, accountsFilename);

		this.reload();

		if ( !patternsFile.exists() )
		{
			try 
			{
				patternsFile.createNewFile();
				// Load default permissions
				
				this.save();
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public void reload() {
		patternsConfig = new YamlConfiguration();
		patternsConfig.options().pathSeparator(PATH_SEPARATOR);
				
		try 
		{
			patternsConfig.load(patternsFile);
			

			for ( String patternName : patternsConfig.getKeys(false) )
			{
				TransactionPattern pattern = new TransactionPattern(patternName);
				
				for ( String section : patternsConfig.getConfigurationSection(patternName).getKeys(false) )
				{	
					if ( section.equals("prices") )
					{
						pattern.loadPrices(patternsConfig.getConfigurationSection(buildPath(patternName, section)));
					}
					else 
					if ( section.equals("items") )
					{
						pattern.loadItems(patternsConfig.getConfigurationSection(buildPath(patternName, section)));
					}
					else 
					if ( section.equals("inherits") )
					{
						pattern.prepareInherits(patternsConfig.getConfigurationSection(buildPath(patternName)));
					}
					
				}

				patterns.put(patternName.toLowerCase(), pattern);
			}
			
		} 
		catch (FileNotFoundException e)
		{
		//	severe(e.getMessage());
		} 
		catch (Throwable e)
		{
			throw new IllegalStateException("Error loading patterns file", e);
		}
	}
	
	public void setFromList(String name, List<StockItem> sellList, List<StockItem> buyList, String mode)
	{		
		if ( mode.equals("prices") || mode.equals("all") )
		{
			
			for ( StockItem item : sellList )
			{
				patternsConfig.set(buildPath(name, "prices", "sell"), item.getRawPrice());
			}
			for ( StockItem item : buyList )
			{
				patternsConfig.set(buildPath(name, "prices", "buy"), item.getRawPrice());
			}
		}
		
		if ( mode.equals("items") || mode.equals("all") )
		{
			for ( StockItem item : sellList )
			{
				patternsConfig.set(buildPath(name, "items", "sell"), item.toString());
			}
			for ( StockItem item : buyList )
			{
				patternsConfig.set(buildPath(name, "items", "buy"), item.toString());
			}
		}
		save();
	}

	public void save() {
		try 
		{
			this.patternsConfig.save(patternsFile);
		} 
		catch (IOException e) 
		{
		//	severe("Error during saving warps file: " + e.getMessage());
		}
	}
	
	public static String buildPath(String... path) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		char separator = PATH_SEPARATOR; //permissions.options().pathSeparator();

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
	
	public TransactionPattern getPattern(String pattern)
	{
		return patterns.get(pattern.toLowerCase());
	}
	
}

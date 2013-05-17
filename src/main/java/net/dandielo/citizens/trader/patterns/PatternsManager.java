package net.dandielo.citizens.trader.patterns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.dandielo.citizens.trader.DtlTraders;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.patterns.types.ItemPattern;
import net.dandielo.citizens.trader.patterns.types.PricePattern;

public class PatternsManager {

	HashMap<String, TPattern> patterns = new HashMap<String, TPattern>(); 
	
	private final static char PATH_SEPARATOR = '/';
	protected boolean separateFiles;

	protected FileConfiguration patternsConfig;
	protected File patternsFile;
	
	public PatternsManager() {		
		ConfigurationSection config = DtlTraders.getInstance().getConfig();
		
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
				TPattern pattern = createPattern(patternName, patternsConfig.getString(buildPath(patternName,"type")));
				
				pattern.load(patternsConfig.getConfigurationSection(patternName));

				patterns.put(patternName.toLowerCase(), pattern);
			}
			
			System.out.print(patterns);
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
				patternsConfig.set(buildPath(name, "prices", "sell", item.getIdAndData()), item.getRawPrice());
			}
			for ( StockItem item : buyList )
			{
				patternsConfig.set(buildPath(name, "prices", "buy", item.getIdAndData()), item.getRawPrice());
			}
		}
		
		if ( mode.equals("items") || mode.equals("all") )
		{
			List<String> stringSell = new ArrayList<String>();
			List<String> stringBuy = new ArrayList<String>();
			
			for ( StockItem item : sellList )
			{
				stringSell.add(item.toString());
			}
			for ( StockItem item : buyList )
			{
				stringBuy.add(item.toString());
			}
			patternsConfig.set(buildPath(name, "items", "sell"), stringSell);
			patternsConfig.set(buildPath(name, "items", "buy"), stringBuy);
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
	
	public static TPattern createPattern(String name, String type)
	{
		if ( type.equals("price") )
			return new PricePattern(name, type, false);
		if ( type.equals("item") )
			return new ItemPattern(name, type, false);
		return null;
	}
	
	public TPattern getPattern(String pattern)
	{
		return patterns.get(pattern.toLowerCase());
	}
	
}

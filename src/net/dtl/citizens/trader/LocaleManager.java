package net.dtl.citizens.trader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocaleManager {
	//config
	protected ConfigurationSection config;

	//yaml path separator
	protected final static char PATH_SEPARATOR = '/';
	
	//locale file configuration
	protected FileConfiguration locale;
	protected File localeFile;
	
	//localeCache
	protected Map<String,String> stringsCache;
	protected Map<String,String> keywordsCache;
	
	public LocaleManager() {
		//Loca config
		config = CitizensTrader.getInstance().getConfig();
		
		//initialize cache holder
		this.stringsCache = new HashMap<String, String>();
		this.keywordsCache = new HashMap<String, String>();
		
		//initialize the locale
		initialize();
	}

	public void initialize() {
		String localeFilename = config.getString("locale.file");

		// Default settings
		if ( localeFilename == null ) 
		{
			//creating a config default
			localeFilename = "english.loc";
			config.set("locale.file", "english.loc");
			
			//saving the new config
			CitizensTrader.getInstance().saveConfig();
		}
		
		//getting the base dir
		String baseDir = config.getString("locale.basedir", "plugins/DtlCitizensTrader/locale" );

		if ( baseDir.contains("\\") && !"\\".equals(File.separator) ) 
		{
			baseDir = baseDir.replace("\\", File.separator);
		}

		File baseDirectory = new File(baseDir);
		if ( !baseDirectory.exists() ) 
		{
			baseDirectory.mkdirs();
		}

		this.localeFile = new File(baseDir, localeFilename);

		this.reload();
		
		if ( !localeFile.exists() )
		{
			
			try 
			{
				
				localeFile.createNewFile();
				
				// Look for defaults in the jar
			    InputStream defConfigStream = CitizensTrader.getInstance().getResource("english.loc");
			    
			    
			    if (defConfigStream != null) {
			        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			        locale.setDefaults(defConfig);
			        locale.options().copyDefaults(true);
			    }
				
				//save and reload
				this.save();
				this.reload();
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

	}
	
	//build yaml path
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

	public void reload() {
		locale = new YamlConfiguration();
		locale.options().pathSeparator(PATH_SEPARATOR);
				
		try 
		{
			locale.load(localeFile);
			
			
			stringsCache.clear();
			keywordsCache.clear();

			
			for ( String key : locale.getConfigurationSection("messages").getKeys(false) )
			{
				stringsCache.put(key, locale.getString(buildPath("messages",key)).replace('^', '§') );
			}
			
			for ( String keyword : locale.getConfigurationSection("keywords").getKeys(false) )
			{
				for ( String value : locale.getConfigurationSection(buildPath("keywords",keyword)).getKeys(false) )
				{
					keywordsCache.put(keyword+":"+value, locale.getString(buildPath("keywords",keyword,value)).replace('^', '§') );
				}
			}
			
		} 
		catch (FileNotFoundException e)
		{
			CitizensTrader.severe(e.getMessage());
		} 
		catch (Throwable e)
		{
			throw new IllegalStateException("Error loading permissions file", e);
		}
	}

	public String getLocaleString(String messageType)
	{
		if ( stringsCache.containsKey(messageType) )
			return stringsCache.get(messageType);
		
		return ChatColor.RED + "ERROR! Reset the locale file!";
	}
	
	public String getLocaleString(String messageType, String keyword1)
	{
		if ( keyword1.isEmpty() )
			return this.getLocaleString(messageType);
		return this.getLocaleString(messageType).replaceFirst("\\{"+keyword1.split(":")[0]+"\\}", ( keyword1.split(":")[1].startsWith("{") ? keyword1.split(":")[1] : keywordsCache.get(keyword1) ) );
	}
	
	public String getLocaleString(String messageType, String keyword1, String keyword2)
	{
		return this.getLocaleString(messageType, keyword1).replaceFirst("\\{"+keyword2.split(":")[0]+"\\}", ( keyword2.split(":")[1].startsWith("{") ? keyword2.split(":")[1] : keywordsCache.get(keyword2).toLowerCase() )  );
	}
	
	public String getLocaleString(String messageType, String keyword1, String keyword2, String keyword3)
	{
		return this.getLocaleString(messageType, keyword1, keyword2).replaceFirst("\\{"+keyword3.split(":")[0]+"\\}", ( keyword3.split(":")[1].startsWith("{") ? keyword3.split(":")[1] : keywordsCache.get(keyword3) ) );
	}
	
	
	
	
	public void save() {
		try 
		{
			this.locale.save(localeFile);
		} 
		catch (IOException e) 
		{
			CitizensTrader.severe("Error during saving permissions file: " + e.getMessage());
		}
	}

}

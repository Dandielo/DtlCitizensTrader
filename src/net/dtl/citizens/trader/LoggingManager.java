package net.dtl.citizens.trader;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LoggingManager {
	//config file separator
	private final static char PATH_SEPARATOR = '/';
	
	//settings and files
	protected boolean separateFiles;
	protected File generalLogFile;
	protected boolean generalLoggingEnabled; 
	
	protected File playerLogFile;
	protected FileConfiguration playerLog;
	protected boolean playerLoggingEnabled; 
	
	//constructor
	public LoggingManager()
	{	
		this.initializePlayerLogs();
		this.initializeGeneralLogg();
	}
	
	public void initializePlayerLogs()
	{
		FileConfiguration config = CitizensTrader.getInstance().getConfig();
		playerLoggingEnabled = config.getBoolean("logging.player-trader.enable", true);
		
		//filename
		String fileName = null;
		
		fileName = config.getString("logging.player-trader.file"); 
		// Default settings
		if ( fileName == null ) 
		{
			fileName = "player_trader_logs.yml";
			config.set("logging.player-trader.file", fileName);
		}

		String baseDir = config.getString("logging.player-trader.basedir", "plugins\\DtlCitizensTrader\\logs");
		if ( baseDir.contains("\\") && !"\\".equals(File.separator) ) 
		{
			baseDir = baseDir.replace("\\", File.separator);
		}

		
		File baseDirectory = new File(baseDir);
		if ( !baseDirectory.exists() ) 
		{
			baseDirectory.mkdirs();
		}
		
		this.playerLogFile = new File(baseDir, fileName);
		this.reload();
		
		if ( !playerLogFile.exists() )
		{
			try 
			{
				playerLogFile.createNewFile();
				this.save();
				this.reload();
				
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
	}
	
	public void initializeGeneralLogg()
	{
		//config file
		FileConfiguration config = CitizensTrader.getInstance().getConfig();
		generalLoggingEnabled = config.getBoolean("logging.general.enable", true);
		
		
		//filename
		String fileName = null;
		
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("dd_MM_yyyy");
		
		// Default settings
		if ( fileName == null ) 
		{
			fileName = df.format(date) + "_log.txt";
		}

		String baseDir = config.getString("logging.general.basedir", "plugins\\DtlCitizensTrader\\logs");
		if ( baseDir.contains("\\") && !"\\".equals(File.separator) ) 
		{
			baseDir = baseDir.replace("\\", File.separator);
		}

		
		File baseDirectory = new File(baseDir);
		if ( !baseDirectory.exists() ) 
		{
			baseDirectory.mkdirs();
		}

		this.generalLogFile = new File(baseDir, fileName);

		if ( !generalLogFile.exists() )
		{
			try 
			{
				generalLogFile.createNewFile();

			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
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
	
	public List<String> getPlayerLogs(String player, String trader)
	{
		if ( trader.isEmpty() )
		{
			List<String> fullLog = new ArrayList<String>();
			if ( !playerLog.contains(player) )
				return new ArrayList<String>();
			
			for ( String traderKey : playerLog.getConfigurationSection(player).getKeys(false) )
			{
				for ( String log : playerLog.getStringList(player+PATH_SEPARATOR+traderKey) )
					fullLog.add(0, ChatColor.AQUA + traderKey + ChatColor.YELLOW + " : " + ChatColor.GOLD + log); 
			}
			return fullLog;
		}
		return playerLog.getStringList(player+PATH_SEPARATOR+trader);
	}
	
	public void clearPlayerLogs(String player, String trader)
	{
		if ( trader.isEmpty() )
		{
			playerLog.set(player, null);
			return;
		}
		playerLog.set(player+PATH_SEPARATOR+trader, new String[0]);
		
		this.save();
	}
	
	public void playerLog(String player, String trader, String logString)
	{
		if ( !this.playerLoggingEnabled )
			return;
		
		List<String> list = playerLog.getStringList(player+PATH_SEPARATOR+trader);
		
		if ( list == null )
			list = new ArrayList<String>();
		
		list.add(0, logString);
		playerLog.set(player+PATH_SEPARATOR+trader, list);
		
		this.save();
	}
	
	public void save()
	{
		try {
			
			playerLog.save(playerLogFile);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reload()
	{
		
		playerLog = new YamlConfiguration();
		playerLog.options().pathSeparator(PATH_SEPARATOR);

		if ( !playerLogFile.exists() )
			return;
		
		try {
			playerLog.load(playerLogFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void log(String logString)
	{
		if ( !this.generalLoggingEnabled )
			return;
		
		FileWriter writer = null;
		try {
			if ( generalLogFile != null )
			{
				writer = new FileWriter(generalLogFile,true);
	
				writer.append(logString+"\n");
				writer.flush();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
	
}

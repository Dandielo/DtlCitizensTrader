package net.dtl.citizenstrader_new;

import java.util.logging.Logger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.Towny;

public class CitizensTrader extends JavaPlugin {
	//citizens trader logger
	protected final static Logger logger = Logger.getLogger("Minecraft");
	
	//plugin instance
	private static CitizensTrader instance;
	private static SimpleClans clans;
	private static Towny towny;
	
	//CitizensTrader Managers
	private static PermissionsManager permsManager;
	private static BackendManager backendManager;
	private static NpcEcoManager npcEcoManager;
	private static LocaleManager localeManager;
	private static LoggingManager logManager;
	
	//Trader configuration
	private static ItemsConfig itemConfig;
	private static FileConfiguration stdConfig;
	
	//Economy plugin
	private Economy economy;
	
	
	//On plugin load
	@Override
	public void onLoad()
	{
		//info("Loading v" + getDescription().getVersion());
		
		//loading the stdConfig
		saveDefaultConfig();
		stdConfig = getConfig();
		
		//loading itemConfig
		itemConfig = new ItemsConfig(stdConfig);
		
		//setting the plugins instance
		instance = this;
		
		//sucessfully loaded
	//	info("Loaded v" + getDescription().getVersion());
	}
	
	@Override
	public void onEnable() {
		//plugin description variable
		PluginDescriptionFile pdfFile = getDescription();
		
		
		//initializing permissions support
		permsManager = new PermissionsManager();
		
		//initializing all managers
		info("Loading bank accounts");
		backendManager = new BackendManager();
		
		
		info("Loading locale");
		localeManager = new LocaleManager();
		
		
		npcEcoManager = new NpcEcoManager();
		logManager = new LoggingManager();
		
		
		//initializing vault plugin
		if ( getServer().getPluginManager().getPlugin("Vault") == null ) 
		{
			info("Vault plugin not found! Disabling plugin");
			this.setEnabled(false);
			this.getPluginLoader().disablePlugin(this);
			return;
		}
			
        RegisteredServiceProvider<Economy> rspEcon = getServer().getServicesManager().getRegistration(Economy.class);
       
        //check if there is an economy plugin
        if ( rspEcon != null ) 
        {
        	//economy exists, plugin enabled
        	economy = rspEcon.getProvider();
			info("Using " + economy.getName() + " plugin");
        } 
        else 
        {
        	//no economy plugin found disable the plugin
        	info("Economy plugin not found! Disabling plugin");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		
        initializeSoftDependPlugins();
        
        //register the DtlTraderTrait
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TraderCharacterTrait.class).withName("trader"));
		
		//register events
		getServer().getPluginManager().registerEvents(npcEcoManager, this);
		
		//register command executor
		getCommand("trader").setExecutor(new TraderCommandExecutor(this));
		
		//plugin enabled
		logger.info("["+ pdfFile.getName() + "] v" + pdfFile.getVersion() + " enabled.");

	} 
	
	//on plugin disable
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();

		logger.info("["+ pdfFile.getName() + "] v" + pdfFile.getVersion() + " disabled.");
	}
	
	//Hooking into clans and towny bank account
	public void initializeSoftDependPlugins()
	{
		clans = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
		if ( clans != null )
		{
			info("Hooked into " + clans.getDescription().getFullName());
		}
		towny = (Towny) Bukkit.getPluginManager().getPlugin("Towny");
		if ( towny != null )
		{
		//	towny.getTownyUniverse().getTownsMap().get("").get
			info("Hooked into " + towny.getDescription().getFullName());
		}
	}
	
	public static SimpleClans getSimpleClans()
	{
		return clans;
	}
	
	//static functions
	public static PermissionsManager getPermissionsManager()
	{
		return permsManager;
	}
	
	public static LocaleManager getLocaleManager()
	{
		return localeManager;
	}
	
	public static LoggingManager getLoggingManager()
	{
		return logManager;
	}
	
	public static BackendManager getBackendManager()
	{
		return backendManager;
	}
	
	public static NpcEcoManager getNpcEcoManager()
	{
		return npcEcoManager;
	}
	
	public Economy getEconomy()
	{
		return economy;
	}
	
	//get configs	
	public ItemsConfig getItemConfig()
	{
		return itemConfig;
	}
	
	//logger info
	public static void info(String message)
	{
		logger.info("["+getInstance().getDescription().getFullName()+"] " + message);
	}
	//logger warning
	public static void warning(String message)
	{
		logger.warning("["+getInstance().getDescription().getFullName()+"] " + message);
	}
	//logger severe
	public static void severe(String message)
	{
		logger.severe("["+getInstance().getDescription().getFullName()+"] " + message);
	}
	
	//plugin instance
	public static CitizensTrader getInstance()
	{
		return instance;
	}
	
	
}

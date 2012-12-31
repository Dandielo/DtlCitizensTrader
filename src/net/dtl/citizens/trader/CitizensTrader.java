package net.dtl.citizens.trader;

import java.util.logging.Logger;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.dtl.citizens.trader.denizen.AbstractDenizenCommand;
import net.dtl.citizens.trader.denizen.AbstractDenizenTrigger;
import net.dtl.citizens.trader.denizen.triggers.TransactionTrigger;
import net.dtl.citizens.trader.managers.BackendManager;
import net.dtl.citizens.trader.managers.BankAccountsManager;
import net.dtl.citizens.trader.managers.LocaleManager;
import net.dtl.citizens.trader.managers.LoggingManager;
import net.dtl.citizens.trader.managers.PatternsManager;
import net.dtl.citizens.trader.managers.PermissionsManager;
import net.dtl.citizens.wallets.Wallets;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_4_6.NBTTagCompound;
import net.minecraft.server.v1_4_6.NBTTagList;
import net.minecraft.server.v1_4_6.NBTTagString;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.P;
import com.palmergames.bukkit.towny.Towny;


public class CitizensTrader extends JavaPlugin {
	//citizens trader logger
	protected final static Logger logger = Logger.getLogger("Minecraft");
	protected static CommandSender sender;
	
	//plugin instance
	private static CitizensTrader instance;
	private static SimpleClans clans;
	private static Towny towny;
	private static P factions;
	private static Denizen denizen;
	private static Wallets wallets;
	
	//CitizensTrader Managers
	private static PermissionsManager permsManager;
	private static BackendManager backendManager;
	private static NpcEcoManager npcEcoManager;
	private static LocaleManager localeManager;
	private static LoggingManager logManager;
	private static PatternsManager patternsManager;
	private static BankAccountsManager accountsManager;
	
	//Trader configuration
	private static ItemsConfig itemConfig;
	private static FileConfiguration stdConfig;
	
	//Economy plugin
	private static Economy economy;
	
	
	//On plugin load
	@Override
	public void onLoad()
	{
		this.setEnabled(false);
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
		//loading sender
		sender = Bukkit.getServer().getConsoleSender();
		
		//plugin description variable
		PluginDescriptionFile pdfFile = getDescription();
		
		//initializing permissions support
		permsManager = new PermissionsManager();
		
		//initializing all managers
		info("Loading bank accounts");
		backendManager = new BackendManager();
		
		
		info("Loading locale");
		localeManager = new LocaleManager();
		
		info("loading patterns");
		patternsManager = new PatternsManager();
		
		npcEcoManager = new NpcEcoManager();
		logManager = new LoggingManager();
		
		accountsManager = new BankAccountsManager();
		accountsManager.loadAccounts();
		
		
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
		getCommand("trader").setExecutor(new TraderCommandExecutor());
		getCommand("banker").setExecutor(new BankerCommandExecutor());
		

		//loading accounts
		//TODO loading accounts
		info("Loaded bank accounts");
		
		//Denizen commands
		//initializeDenizenCommands();
		//initializeDenizenTriggers();
		
		//plugin enabled
		info("v" + pdfFile.getVersion() + " enabled.");

	} 
	
	//on plugin disable
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();

		info("v" + pdfFile.getVersion() + " disabled.");
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
			info("Hooked into " + towny.getDescription().getFullName());
		}
		factions = (P) Bukkit.getPluginManager().getPlugin("Factions");
		if ( factions != null )
		{
			info("Hooked into " + factions.getDescription().getFullName());
		}
		
		wallets = (Wallets) Bukkit.getPluginManager().getPlugin("dtlWallets");
		if ( wallets != null )
		{
			info("Hooked into " + wallets.getDescription().getFullName());
		}
	}
	
	public void initializeDenizenCommands()
	{
		denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		if ( denizen != null )
		{
			AbstractDenizenCommand.initializeDenizenCommands(denizen);
		}
	}
	
	public void initializeDenizenTriggers()
	{
		denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		if ( denizen != null )
		{
			info("Registering Denizen triggers... ");
			AbstractDenizenTrigger.registerTriggers();
		}
	}
	
	public static Denizen getDenizen()
	{
		return denizen;
	}
	
	public static Towny getTowny() {
		return towny;
	}
	
	public static P getFactions() {
		return factions;
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
	
	public static PatternsManager getPatternsManager()
	{
		return patternsManager;
	}
	
	public static BankAccountsManager getAccountsManager()
	{
		return accountsManager;
	}
	
	public static Economy getEconomy()
	{
		return economy;
	}

	public static Wallets getDtlWallets()
	{
		return wallets;
	}
	
	public static boolean dtlWalletsEnabled()
	{
		return wallets != null;
	}
	
	//get configs	
	public ItemsConfig getItemConfig()
	{
		return itemConfig;
	}
	
	//logger info
	public static void info(String message)
	{
		sender.sendMessage("["+getInstance().getDescription().getName()+"] " + ChatColor.WHITE + message);
	}
	//logger warning
	public static void warning(String message)
	{
		logger.warning("["+getInstance().getDescription().getName()+"] " + message);
	}
	//logger severe
	public static void severe(String message)
	{
		logger.severe("["+getInstance().getDescription().getName()+"] " + message);
	}
	
	//plugin instance
	public static CitizensTrader getInstance()
	{
		return instance;
	}
	
	/*
	 * 
taxes:
  enabled: false
  #when the prices are with taxes or they should be reacalculated with the tax system (netto/brutto)
  prices: netto
  #the player or bank or npc wallet where the taxes should be deposited
  tax-deposit: bank
  #tax percent 
  tax: 22%
custom: 
  permission.name:
    taxes: 
      tax: 11%
      enabled: true
    
	 */
	
}

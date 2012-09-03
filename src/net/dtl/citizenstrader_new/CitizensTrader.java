package net.dtl.citizenstrader_new;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

public class CitizensTrader extends JavaPlugin {
	//citizens trader logger
	public final Logger logger = Logger.getLogger("Minecraft");
	
	//plugin instance
	private static CitizensTrader instance;
	
	private static TraderConfig config;

	private Economy economy;
	private Permission permission;
	private static TraderManager traderManager;
	private static PermissionsManager permsManager;
	private static LogManager logManager;
	private static LocaleManager locale;
	private static BackendManager backends;
	
	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		
		if ( getServer().getPluginManager().getPlugin("Vault") != null ) {
	        RegisteredServiceProvider<Economy> rspEcon = getServer().getServicesManager().getRegistration(Economy.class);
	        if ( rspEcon != null ) {
	        	economy = rspEcon.getProvider();
				this.logger.info("["+ pdfFile.getName() + "] Economy enabled.");
	        } else {
				this.logger.info("Economy plugin not found. Disabling plugin");
				this.setEnabled(false);
				this.getPluginLoader().disablePlugin(this);
				return;
			}
	        RegisteredServiceProvider<Permission> rspPerm = getServer().getServicesManager().getRegistration(Permission.class);
	        if ( rspPerm != null ) {
		        permission = rspPerm.getProvider();
				this.logger.info("["+ pdfFile.getName() + "] Permissions enabled.");
	        } else {
				this.logger.info("["+ pdfFile.getName() + "] Permissions not found!"); 
	        }
			
		//	if ( getConfig().contains("trader") )
		//	{
		//		getConfig().options().copyDefaults(true);
		//		getConfig().addDefaults(getConfig().options().configuration().getDefaults());
		//		System.out.print(getConfig().options().configuration().getDefaults().getKeys(true));
				saveDefaultConfig();
		//		saveConfig();
			//	traderSection = CitizensTrader.plugin.getConfig().getConfigurationSection("trader");
		//	}
			
			config = new TraderConfig(getConfig());

			plugin = this;
			backends = new BackendManager(getConfig());
			
			locale = new LocaleManager(getConfig());
			permsManager = new PermissionsManager();
			traderManager = new TraderManager(getConfig());
			logManager = new LogManager(getConfig());
						
			//if ( CitizensAPI.hasImplementation()  )
			//	CitizensAPI.getCharacterManager().registerCharacter(new CharacterFactory(TraderCharacter.class).withName("trader"));
			//if ( CitizensAPI.getTraitManager() != null ) {
			CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TraderCharacterTrait.class).withName("trader"));
			//	CitizensAPI.getTraitManager().registerTrait(new TraitFactory(TraderTrait.class).withName("trader").withPlugin(this));
			//}
			getServer().getPluginManager().registerEvents(traderManager, this);
			getCommand("trader").setExecutor(new TraderCommandExecutor(this));
			
			//((TraderCharacter) CitizensAPI.getCharacterManager().getCharacter("trader")).setConfig(config);
			
			this.logger.info("["+ pdfFile.getName() + "]  Plugin version " + pdfFile.getVersion() + " is now enabled.");
		} else {
			this.logger.info("Vault plugin not found. Disabling plugin");
			this.setEnabled(false);
			this.getPluginLoader().disablePlugin(this);
			return;
		}
	} 
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();

		this.logger.info("["+ pdfFile.getName() + "] saving config.");
		this.logger.info("["+ pdfFile.getName() + "] Plugin version " + pdfFile.getVersion() + " is now disabled.");
	}
	
	public static TraderManager getTraderManager() {
		return traderManager;
	}
	
	public static PermissionsManager getPermissionsManager() {
		return permsManager;
	}
	
	public static LogManager getLogManager() {
		return logManager;
	}
	
	public static LocaleManager getLocale()
	{
		return locale;
	}
	
	public static BackendManager getBackendManager()
	{
		return backends;
	}
	
	public static TraderConfig getTraderConfig() {
		return config;
	}
	
	public Economy getEconomy() {
		return economy;
	}
	
}

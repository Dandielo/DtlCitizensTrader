package net.dtl.citizenstrader_new;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.trait.TraitFactory;
import net.dtl.citizenstrader_new.traits.InventoryTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

public class CitizensTrader extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public static CitizensTrader plugin;
	
	public TraderConfig config;

	private Economy economy;
	private Permission permission;
	
	
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
			
			config = new TraderConfig();
			loadConfig();
			config.setEcon(economy);
			
			if ( CitizensAPI.getCharacterManager() != null )
				CitizensAPI.getCharacterManager().registerCharacter(new CharacterFactory(TraderCharacter.class).withName("trader"));
			if ( CitizensAPI.getTraitManager() != null ) {
				CitizensAPI.getTraitManager().registerTrait(new TraitFactory(InventoryTrait.class).withName("inv").withPlugin(this));
				CitizensAPI.getTraitManager().registerTrait(new TraitFactory(TraderTrait.class).withName("trader").withPlugin(this));
			}
			getServer().getPluginManager().registerEvents((Listener) CitizensAPI.getCharacterManager().getCharacter("trader"), this);
			getCommand("trader").setExecutor(new TraderCommandExecutor(this));
			((TraderCharacter) CitizensAPI.getCharacterManager().getCharacter("trader")).setConfig(config);
			
			plugin = this;
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
		try {
			this.logger.info("["+ pdfFile.getName() + "] saving config.");
			this.logger.info("["+ pdfFile.getName() + "]  Plugin version " + pdfFile.getVersion() + " is now disabled.");
			createConfig();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean loadConfig() {
		File file = new File("plugins/DtlCitizensTrader/config.yml");
		PluginDescriptionFile pdfFile = getDescription();
		
		if ( !file.exists() || file.length() <= 0 ) {
			logger.info("["+ pdfFile.getName() + "] Generating config file!");
			try {
				this.createConfig();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			Yaml yaml = new Yaml();
			HashMap<String,Object> config = (HashMap<String,Object>) yaml.load(inputStream);
			
			//QuestName
			if ( config.containsKey("trader") ) {
				HashMap<String,Object> trader = (HashMap<String, Object>) config.get("trader");
				this.config.setTraderConfig((String)trader.get("mode"));
			}
				
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean createConfig() throws IOException {
		File file = new File("plugins/DtlCitizensTrader/config.yml");
		
		if ( !file.exists() ) {
			new File("plugins/DtlCitizensTrader").mkdirs();
			file.createNewFile();
		}
		
	    FileWriter out = new FileWriter(file);
	    
    	out.write("trader:\n"); 
    	out.write("  mode: " + config.getMode() + "\n");
    	
    	out.flush();
    	
		return true;
	}
	
	public Economy getEconomy() {
		return economy;
	}
	
}

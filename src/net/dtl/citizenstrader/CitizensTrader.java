package net.dtl.citizenstrader;

import java.util.logging.Logger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.trait.TraitFactory;
import net.dtl.citizenstrader.traits.InventoryTrait;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CitizensTrader extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public static CitizensTrader plugin;
	
	private CharacterFactory cf;

	private Economy economy;
	
	
	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		
		if ( getServer().getPluginManager().getPlugin("Vault") != null ) {
	        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	        if ( rsp != null ) {
	        	economy = rsp.getProvider();
				this.logger.info("["+ pdfFile.getName() + "]  Plugin version " + pdfFile.getVersion() + " is now enabled.");
	        } else {
				this.logger.info("Vault plugin not found. Disabling plugin");
				this.setEnabled(false);
				this.getPluginLoader().disablePlugin(this);
				return;
			}
		}
		
		cf = new CharacterFactory(TraderNpc.class);

		cf.withName("trader");

		
		if ( CitizensAPI.getCharacterManager() != null )
			CitizensAPI.getCharacterManager().registerCharacter(cf);
		if ( CitizensAPI.getTraitManager() != null )
			CitizensAPI.getTraitManager().registerTrait(new TraitFactory(InventoryTrait.class).withName("inv").withPlugin(this));
		
		getServer().getPluginManager().registerEvents((Listener) CitizensAPI.getCharacterManager().getCharacter("trader"), this);
		getCommand("trader").setExecutor(new TraderCommandExecutor());
		((TraderNpc) CitizensAPI.getCharacterManager().getCharacter("trader")).setEcon(economy);
		
		plugin = this;
	}
	
	@Override
	public void onDisable() {
		
	}
	
	
}

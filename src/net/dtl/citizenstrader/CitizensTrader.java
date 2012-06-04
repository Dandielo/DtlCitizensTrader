package net.dtl.citizenstrader;

import java.util.logging.Logger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.dtl.DtlProject;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CitizensTrader extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public static CitizensTrader plugin;
	
	public DtlProject dtlProject;
	private CharacterFactory cf;
	private int selected;
	
	
	@Override
	public void onEnable() {
		
		PluginManager pm = this.getServer().getPluginManager();
		PluginDescriptionFile pdfFile = this.getDescription();
		
		
		dtlProject = (DtlProject) pm.getPlugin("DtlProject");
		if ( dtlProject != null ) {
			this.logger.info("[" + dtlProject.getDescription().getName() + "]["+ pdfFile.getName() + "]  Plugin version " + pdfFile.getVersion() + " is now enabled.");
		} else {
			this.logger.info("DtlProject plugin not found. Disabling plugin");
			this.setEnabled(false);
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		
		cf = new CharacterFactory(TraderNpc.class);

		cf.withName("trader");
		//cf.create();
		
		
		if ( CitizensAPI.getCharacterManager() != null )
			CitizensAPI.getCharacterManager().registerCharacter(cf);
		
		getServer().getPluginManager().registerEvents(new TraderListener(dtlProject,this), this);
		getCommand("trader").setExecutor(new TraderCommandExecutor(this));
		
		plugin = this;
	}
	
	@Override
	public void onDisable() {
	}
	
	
	public void setSelected(int s) {
		selected = s;
	}
	
	public int getSelected() {
		return selected;
	}
	
	
}

package net.dtl.trader;

import java.util.logging.Logger;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.dtl.DtlProject;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Trader extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	
	//DtlProjeckt
	public DtlProject dtlProject;
	private int selected = -1;
	
	private CharacterFactory cf;
	
	@Override
	public void onEnable() {
		
	//	npc.setName("trader");
	//	npc.setValidTypes(EntityType.PLAYER);
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
		
		getServer().getPluginManager().registerEvents(new NpcListener(dtlProject,this), this);
		getCommand("trader").setExecutor(new TraderCommands(this));
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

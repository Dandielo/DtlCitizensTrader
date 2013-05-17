package net.dandielo.citizens.trader.commands;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.DtlTraders;
import net.dandielo.citizens.trader.CommandManager;
import net.dandielo.citizens.trader.NpcManager;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.types.tNPC;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradersExecutor implements CommandExecutor {
	public static CommandManager cManager;
	public static Citizens citizens;
	
	public TradersExecutor(CommandManager manager)
	{
		cManager = manager;
		citizens = (Citizens) CitizensAPI.getPlugin();
	}
	
	private static NpcManager traders = DtlTraders.getNpcEcoManager();
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		
		if ( sender instanceof Player )
		{
			tNPC npc = traders.tNPC(sender);
			return cManager.execute(name, sender, npc, args);
		}
		return true;
	}
   
}

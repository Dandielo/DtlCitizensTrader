package net.dtl.citizens.trader.commands;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.CommandManager;
import net.dtl.citizens.trader.NpcManager;
import net.dtl.citizens.trader.TraderTrait;
import net.dtl.citizens.trader.types.tNPC;

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
	
	private static NpcManager traders = CitizensTrader.getNpcEcoManager();
	
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

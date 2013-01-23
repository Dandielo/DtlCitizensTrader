package net.dtl.citizens.trader.denizen;

import org.bukkit.ChatColor;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.NpcEcoManager;
import net.dtl.citizens.trader.denizen.commands.TraderCreateCommand;
import net.dtl.citizens.trader.denizen.commands.TransactionCommand;

abstract public class AbstractDenizenCommand extends AbstractCommand {
	
	protected static NpcEcoManager npcManager;
	
	public AbstractDenizenCommand()
	{
		npcManager = CitizensTrader.getNpcEcoManager();
	}
	
	public static void initializeDenizenCommands(Denizen denizen)
	{
		if ( denizen != null )
		{
			CitizensTrader.info("Hooked into " + denizen.getDescription().getFullName());
			CitizensTrader.info("Registering commands... ");
			
			new TransactionCommand();
			new TraderCreateCommand();

		}
	}
	
}

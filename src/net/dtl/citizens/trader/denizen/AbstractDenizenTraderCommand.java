package net.dtl.citizens.trader.denizen;

import org.bukkit.Bukkit;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.commands.AbstractCommand;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.NpcEcoManager;
import net.dtl.citizens.trader.denizen.commands.DenizenCommandTraderPattern;
import net.dtl.citizens.trader.denizen.commands.DenizenCommandTraderTransaction;

abstract public class AbstractDenizenTraderCommand extends AbstractCommand {
	
	protected static NpcEcoManager npcManager;
	
	public AbstractDenizenTraderCommand()
	{
		npcManager = CitizensTrader.getNpcEcoManager();
	}
	
	public static void initializeDenizenCommands(Denizen denizen)
	{
		if ( denizen != null )
		{
			CitizensTrader.info("Hooked into " + denizen.getDescription().getFullName());
			CitizensTrader.info("Registering commands... ");
			denizen.getCommandRegistry().registerCommand("TRADER_TRANSACTION", new DenizenCommandTraderTransaction());
			denizen.getCommandRegistry().registerCommand("TRADER_PATTERN", new DenizenCommandTraderPattern());
		}
	}
	
}

package net.dtl.citizens.trader.denizen;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.NpcEcoManager;

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
		}
	}
	
}

package net.dtl.citizens.trader.denizen;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.NpcEcoManager;

abstract public class AbstractDenizenTraderCommand extends AbstractCommand {
	
	protected static NpcEcoManager npcManager;
	
	public AbstractDenizenTraderCommand()
	{
		npcManager = CitizensTrader.getNpcEcoManager();
	}
	
}

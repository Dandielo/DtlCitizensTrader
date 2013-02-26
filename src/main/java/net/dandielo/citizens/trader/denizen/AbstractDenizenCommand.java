package net.dandielo.citizens.trader.denizen;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.NpcManager;
import net.dandielo.citizens.trader.denizen.commands.TraderCommand;
import net.dandielo.citizens.trader.denizen.commands.TraderCreateCommand;
import net.dandielo.citizens.trader.denizen.commands.TransactionCommand;

abstract public class AbstractDenizenCommand extends AbstractCommand {
	
	protected static NpcManager npcManager;
	
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
			
			new TransactionCommand().activate().as("TRANSACTION").withOptions("({SELL}|BUY) [ITEM:#(:#)] (QTY:#)", 1);
			new TraderCommand().activate().as("TRADER").withOptions("({OPEN}|CLOSE|PATTERN|WALLET) (ACTION:action) (PATTERN:pattern_name)",0);
			new TraderCreateCommand().activate().as("TRADERCREATE").withOptions("({SERVER}|MARKET|PLAYER) [NAME:trader_name] (WALLET:SERVER|{NPC}|OWNER) (PATTERN:pattern_name) (LOC:location) (OWNER:owner) (ENTITY:entity)",1);

		}
	}
	
}

package net.dandielo.citizens.trader.denizen.commands;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.DtlTraders;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.denizen.AbstractDenizenCommand;
import net.dandielo.citizens.trader.objects.MetaTools;
import net.dandielo.citizens.trader.types.MarketTrader;
import net.dandielo.citizens.trader.types.PlayerTrader;
import net.dandielo.citizens.trader.types.ServerTrader;
import net.dandielo.citizens.trader.types.Trader;

public class TraderCommand extends AbstractDenizenCommand {

	/**
	 * <p>Sends a block animation sequence using .schematic files and an 'ANIMATION script'</p>
	 * 
	 * 
	 * <br><b>dScript Usage:</b><br>
	 * <pre>TRADER ({OPEN}|CLOSE|PATTERN|WALLET) (ACTION:action) (PATTERN:pattern_name)</pre>
	 * 
	 * SET/REMOVE/OPEN/CLOSE/DEPOSIT/WITHDRAW
	 * 
	 * <ol><tt>Arguments: [] - Required, () - Optional, {} - Default</ol></tt>
	 * 
	 * <ol><tt>[START|STOP]</tt><br> 
	 *         Opens or closes the traders inventory.</ol>
	 * 
	 * 
	 * @author Dandielo
	 *
	 */


	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException 
	{
		// Initialize fields used
		String action = "open";

		// Iterate through arguments
		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesArg("OPEN, CLOSE, PATTERN", arg)) {
				action = aH.getStringFrom(arg).toLowerCase();
				dB.echoDebug("...set Action: '%s'", action);
				continue;
				// Unknown argument should be caught to avoid unwanted behavior.
			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);

		}
		
		// Stash objects in scriptEntry for use in execute()
		scriptEntry.addObject("action", action.toLowerCase());
	}
	
	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException 
	{
		NPC npc = scriptEntry.getNPC().getCitizen();
		
		if ( !npc.hasTrait(TraderTrait.class) )
			return;
		
		if ( scriptEntry.getObject("action").equals("open") )
		{
			TraderTrait trait = npc.getTrait(TraderTrait.class);
			Trader trader = null;
			Player player = scriptEntry.getPlayer();
			
			switch(trait.getType())
			{
			case SERVER_TRADER:
				trader = new ServerTrader(trait, npc, player);
				break;
			case PLAYER_TRADER:
				trader = new PlayerTrader(trait, npc, player);
				break;
			case MARKET_TRADER:
				trader = new MarketTrader(trait, npc, player);
				break;
			default:
				break;
			}
			
			DtlTraders.getNpcEcoManager().addInteractionNpc(player.getName(), trader);
	
			MetaTools.removeDescriptions(player.getInventory());
			trader.loadDescriptions(player, player.getInventory());
			
			player.openInventory(trader.getInventory());
		}
	}

}

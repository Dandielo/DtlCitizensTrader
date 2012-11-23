package net.dtl.citizens.trader.denizen.commands;

import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.CommandException;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.denizen.AbstractDenizenTraderCommand;
import net.dtl.citizens.trader.traders.EconomyNpc;
import net.dtl.citizens.trader.traders.ServerTrader;
import net.dtl.citizens.trader.traders.Trader;

public class DenizenCommandTraderPattern extends AbstractDenizenTraderCommand {

	enum Action { SET, REMOVE }
	
	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {
		

		String patternName = null;
		boolean isTraderNpc = false;
		Action action = null;
		
		NPC traderNpc = theEntry.getDenizen().getCitizensEntity();
		
		/* Match arguments to expected variables */
		if ( theEntry.arguments() == null )
			throw new CommandException("...Usage: TRADER_PATTERN [SET/REMOVE] (PAT:PATTERN_NAME)");
		
		
		/* Match arguments to expected variables */
		for ( String thisArg : theEntry.arguments() )
		{
			//if the arguments starts a transaction
			if ( thisArg.toUpperCase().contains("SET") )
			{
				action = Action.SET;
			}
			
			//if the argument cancels a transaction
			if ( thisArg.toUpperCase().contains("REMOVE") )
			{
				action = Action.REMOVE;
			}

			//if the argument opens the sell tab first
			if ( thisArg.toUpperCase().contains("PAT:") )
			{
				patternName = aH.getStringModifier(thisArg);
			}

		}
		
		isTraderNpc = npcManager.isEconomyNpc(traderNpc);
		
		
		/*Execute the command if all args are set*/
		if ( isTraderNpc )
		{
	
			switch( action )
			{
			
				case SET:
				{
					EconomyNpc economyNpc = npcManager.getInteractionNpc(theEntry.getPlayer().getName());// new ServerTrader(traderNpc, traderNpc.getTrait(TraderCharacterTrait.class).getTraderTrait());
					
					
					if ( economyNpc == null )
						economyNpc = new ServerTrader(traderNpc, traderNpc.getTrait(TraderCharacterTrait.class).getTraderTrait());
					
					if ( !( economyNpc instanceof Trader ) )
						return false;
					
					Trader trader = (Trader) economyNpc;
					
					if ( patternName != null )
					{
						if ( trader.getTraderStock().setPattern(patternName) )
						{
							trader.getTraderConfig().setPattern(patternName);
							trader.switchInventory(trader.getTraderStatus());
							
							
						}
						else 
							return false;
					}
					else
						throw new CommandException("...Usage: TRADER_PATTERN SET PAT:PATTERN_NAME");
						
					break;
				}
				case REMOVE:
				{
					EconomyNpc economyNpc = npcManager.getInteractionNpc(theEntry.getPlayer().getName());// new ServerTrader(traderNpc, traderNpc.getTrait(TraderCharacterTrait.class).getTraderTrait());
					
					
					if ( economyNpc == null )
						economyNpc = new ServerTrader(traderNpc, traderNpc.getTrait(TraderCharacterTrait.class).getTraderTrait());
					
					if ( !( economyNpc instanceof Trader ) )
						return false;
					
					Trader trader = (Trader) economyNpc;// new ServerTrader(traderNpc, traderNpc.getTrait(TraderCharacterTrait.class).getTraderTrait());
					
					trader.getTraderConfig().setPattern("");
					trader.getTraderStock().removePattern();
					trader.switchInventory(trader.getTraderStatus());
					
				//	Packet103SetSlot packet = new Packet103SetSlot();
				//	packet.a = 0;
				//	((CraftPlayer)theEntry.getPlayer()).getHandle().netServerHandler.sendPacket(packet);//.getServerConfigurationManager().;//sendPacketNearby(loc.getX(),loc.getY(),loc.getZ(),64,((CraftWorld)loc.getWorld()).getHandle().dimension, new Packet60Explosion(loc.getX(),loc.getY(),loc.getZ(),f,blocks));
				//	CraftChicken chicken = ((CraftChicken)theEntry.getEntity());
			//		chicken;
					
					break;
				}
				
			}
			
			return true;
		}	
		
		return false;
	}
	
}

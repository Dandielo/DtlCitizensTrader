package net.dtl.citizens.trader.denizen.commands;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.CommandException;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.denizen.AbstractDenizenTraderCommand;
import net.dtl.citizens.trader.types.EconomyNpc;
import net.dtl.citizens.trader.types.ServerTrader;
import net.dtl.citizens.trader.types.Trader;

public class DenizenCommandTraderPattern extends AbstractDenizenTraderCommand {

	enum Action { SET, REMOVE }
	
	
	//TODO fix server traders without players
	/*@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {
		

		String patternName = null;
		boolean isTraderNpc = false;
		Action action = null;
		
		NPC traderNpc = theEntry.getDenizen().getCitizensEntity();
		
		if ( theEntry.arguments() == null )
			throw new CommandException("...Usage: TRADER_PATTERN [SET/REMOVE] (PAT:PATTERN_NAME)");
		
		
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
		
		
		if ( isTraderNpc )
		{
	
			switch( action )
			{
			
				case SET:
				{
					EconomyNpc economyNpc = npcManager.getInteractionNpc(theEntry.getPlayer().getName());// new ServerTrader(traderNpc, traderNpc.getTrait(TraderCharacterTrait.class).getTraderTrait());
					
					
					if ( economyNpc == null )
						economyNpc = null ;//new ServerTrader(traderNpc.getTrait(TraderCharacterTrait.class), traderNpc, player);
					
					if ( !( economyNpc instanceof Trader ) )
						return false;
					
					Trader trader = (Trader) economyNpc;
					
					if ( patternName != null )
					{
						if ( trader.getStock().setPattern(patternName) )
						{
							//trader.getConfig().setPattern(patternName);
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
						economyNpc = null;//new ServerTrader(traderNpc, traderNpc.getTrait(TraderCharacterTrait.class).getTraderTrait());
					
					if ( !( economyNpc instanceof Trader ) )
						return false;
					
					Trader trader = (Trader) economyNpc;// new ServerTrader(traderNpc, traderNpc.getTrait(TraderCharacterTrait.class).getTraderTrait());
					
				//	trader.getConfig().setPattern("");
					trader.getStock().removePattern();
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
	}*/


	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void execute(String arg0) throws CommandExecutionException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void parseArgs(ScriptEntry arg0) throws InvalidArgumentsException {
		// TODO Auto-generated method stub
		
	}
	
}

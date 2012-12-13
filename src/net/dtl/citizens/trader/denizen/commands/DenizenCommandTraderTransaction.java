package net.dtl.citizens.trader.denizen.commands;


import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.CommandException;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.denizen.AbstractDenizenTraderCommand;
import net.dtl.citizens.trader.types.ServerTrader;
import net.dtl.citizens.trader.types.Trader;
import net.dtl.citizens.trader.types.Trader.TraderStatus;

public class DenizenCommandTraderTransaction extends AbstractDenizenTraderCommand {

	enum Action { START , CANCEL }

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
		
	};
	
//	@SuppressWarnings("incomplete-switch")
//	@Override
//	public boolean execute(ScriptEntry theEntry) throws CommandException {
//
//		TraderStatus status = null;
//		boolean isTraderNpc = false;
//		Action action = null;
//		
//		NPC traderNpc = theEntry.getDenizen().getCitizensEntity();
//		
//		/* Match arguments to expected variables */
//		if ( theEntry.arguments() == null )
//			throw new CommandException("...Usage: TRADER_TRANSACTION [START/CANCEL] [SELL/BUY]");
//		
//		/* Match arguments to expected variables */
//		for ( String thisArg : theEntry.arguments() )
//		{
//			//if the arguments starts a transaction
//			if ( thisArg.toUpperCase().contains("START") )
//			{
//				action = Action.START;
//			}
//			
//			//if the argument cancels a transaction
//			if ( thisArg.toUpperCase().contains("CANCEL") )
//			{
//				action = Action.CANCEL;
//			}
//
//			//if the argument opens the sell tab first
//			if ( thisArg.toUpperCase().contains("SELL") )
//			{
//				status = TraderStatus.SELL;
//			}
//
//			//if the argument opens the buy tab first
//			if ( thisArg.toUpperCase().contains("BUY") )
//			{
//				status = TraderStatus.BUY;
//			}
//			
//		}
//		
//		isTraderNpc = npcManager.isEconomyNpc(traderNpc);
//		
//		
//		/*Execute the command if all args are set*/
//		if ( isTraderNpc )
//		{
//	
//			switch( action )
//			{
//			
//				case START:
//				{
//					if ( status == null )
//						throw new CommandException("...Usage: TRADER_TRANSACTION [START/CANCEL] [SELL/BUY]");
//						
//					Trader trader = null;//new ServerTrader(traderNpc, traderNpc.getTrait(TraderCharacterTrait.class).getTraderTrait());
//					npcManager.addInteractionNpc(theEntry.getPlayer().getName(), trader);
//					
//					switch( status )
//					{
//					case SELL:
//						trader.switchInventory(status);
//						break;
//					case BUY:
//						trader.switchInventory(status);
//						break;
//					}
//
//					theEntry.getPlayer().openInventory(trader.getInventory());
//					break;
//				}
//				case CANCEL:
//				{
//					theEntry.getPlayer().closeInventory();
//					break;
//				}
//				
//			}
//			
//			return true;
//		}	
//				
//		return false;
//	}

}

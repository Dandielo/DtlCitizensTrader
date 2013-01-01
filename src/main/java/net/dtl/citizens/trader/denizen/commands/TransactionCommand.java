package net.dtl.citizens.trader.denizen.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.denizen.AbstractDenizenCommand;
import net.dtl.citizens.trader.events.TraderTransactionEvent;
import net.dtl.citizens.trader.events.TraderTransactionEvent.TransactionResult;
import net.dtl.citizens.trader.types.ServerTrader;
import net.dtl.citizens.trader.types.Trader;
import net.dtl.citizens.trader.types.Trader.TraderStatus;

public class TransactionCommand extends AbstractDenizenCommand {

	@Override
	public void onEnable() {
		// nothing to do here
	}

	/* TRANSACTION [ACTION:OPEN/CLOSE] 
	 * OR
	 * TRANSACTION [ACTION:SELL/BUY] [ITEM:#(:#)] [QTY:#] */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [POTION_EFFECT] Uses bukkit enum for specifying the potion effect to use.
	 *   
	 * Example Usage:
	 * 
	 */

	// Initialize variables 
	
	private Trader trader;
	
	private Player target;
	private String action;
	private ItemStack item;
	private int qty;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Default target as Player, if no Player, default target to NPC
		if (scriptEntry.getPlayer() != null) target = scriptEntry.getPlayer();
		
		Trader trader = new ServerTrader(scriptEntry.getNPC().getCitizen().getTrait(TraderCharacterTrait.class), scriptEntry.getNPC().getCitizen(), target);
		CitizensTrader.getNpcEcoManager().addInteractionNpc(target.getName(), trader);
		this.trader = trader;

		for (String arg : scriptEntry.getArguments())
		{
            if (aH.matchesItem(arg)) {
            	item = aH.getItemFrom(arg);
                continue;
             
            }   else if (aH.matchesValueArg("ACTION", arg, ArgumentType.Custom)) {
            	action = aH.getStringFrom(arg);
				continue;

            }	else if (aH.matchesQuantity(arg)) {
            	qty = aH.getIntegerFrom(arg);
				continue;

			}   else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

	}


	@Override
	public void execute(ScriptEntry arg0) throws CommandExecutionException {
		if ( action.equals("SELL") )
		{
			trader.selectItem(item, TraderStatus.SELL, false, false);
			
			if ( !trader.hasSelectedItem() )
			{
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), target, trader.getTraderStatus(), Trader.toStockItem(item), -1.0, TransactionResult.FAIL_ITEM));
				return;
			}
			
			double price = trader.getPrice(target, "sell")*qty;
			
			if ( trader.getSelectedItem().getLimitSystem().checkLimit(target.getName(), 0, qty) )// !trader.checkLimits() )
			{
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), target, trader.getTraderStatus(), trader.getSelectedItem(), price, TransactionResult.FAIL_LIMIT));
			//	target.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
			}
			else
			if ( !trader.inventoryHasPlaceAmount(qty) )
			{
			//	target.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), target, trader.getTraderStatus(), trader.getSelectedItem(), price, TransactionResult.FAIL_SPACE));
			}
			else
			if ( !trader.buyTransaction(price) )
			{
			//	target.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), target, trader.getTraderStatus(), trader.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
			}
			else
			{ 
				//TODO add debug mode
			//	target.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + trader.getSelectedItem().getAmount() ).replace("{price}", f.format(price) ) );

				trader.addSelectedToInventory(0);

				trader.updateBuyLimits(qty);
				
				//call event Denizen Transaction Trigger
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), target, trader.getTraderStatus(), trader.getSelectedItem(), price, TransactionResult.SUCCESS_SELL));
				
				//logging
				trader.log("buy", 
					trader.getSelectedItem().getItemStack().getTypeId(),
					trader.getSelectedItem().getItemStack().getData().getData(), 
					trader.getSelectedItem().getAmount()*qty, 
					price );
				
			}
		}
		else
		if ( action.equals("BUY") )
		{
			
		}
		else
		if ( action.equals("OPEN") )
		{
			
		}
		else
		if ( action.equals("CLOSE") )
		{
			
		}
	}	

}

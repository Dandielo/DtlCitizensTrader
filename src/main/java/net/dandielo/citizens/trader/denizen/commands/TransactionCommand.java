package net.dandielo.citizens.trader.denizen.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.dandielo.citizens.trader.DtlTraders;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.denizen.AbstractDenizenCommand;
import net.dandielo.citizens.trader.events.TraderTransactionEvent;
import net.dandielo.citizens.trader.events.TraderTransactionEvent.TransactionResult;
import net.dandielo.citizens.trader.limits.LimitManager;
import net.dandielo.citizens.trader.locale.LocaleManager;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.types.ServerTrader;
import net.dandielo.citizens.trader.types.Trader;
import net.dandielo.citizens.trader.types.Trader.TraderStatus;

public class TransactionCommand extends AbstractDenizenCommand {

	/** 
	 * TRANSACTION ({SELL}|BUY) [ITEM:#(:#)] (QTY:#) 
	 * 
	 * 
	 * Arguments: [] - Required, () - Optional 
	 * [POTION_EFFECT] Uses bukkit enum for specifying the potion effect to use.
	 *   
	 * Example Usage:
	 * 
	 */

	// Initialize variables 
	
	LocaleManager locale = DtlTraders.getLocaleManager();
	
	public TransactionCommand()
	{
		this.activate().as("TRANSACTION").withOptions("({SELL}|BUY) [ITEM:item_name(:#)] (QTY:#)", 1);
		DtlTraders.info("Registered denizen " + ChatColor.YELLOW + TransactionCommand.class.getSimpleName());
	}

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		Trader trader = null;
		ItemStack item = null;
		String action = "SELL";
		int qty = 1;
		
		trader = new ServerTrader(scriptEntry.getNPC().getCitizen().getTrait(TraderTrait.class), scriptEntry.getNPC().getCitizen(), scriptEntry.getPlayer());
		DtlTraders.getNpcEcoManager().addInteractionNpc(scriptEntry.getPlayer().getName(), trader);

		for (String arg : scriptEntry.getArguments())
		{
            if (aH.matchesItem(arg)) {

            	item = aH.getItemFrom(arg).getItemStack();
				dB.echoDebug("...set ITEM: '%s'", item.getType().name());
                continue;
             
            }   else if (aH.matchesArg("SELL, BUY", arg)) {
            	action = aH.getStringFrom(arg);
				dB.echoDebug("...set ACTION: '%s'", action);
				continue;

            }	else if (aH.matchesQuantity(arg)) {
            	qty = aH.getIntegerFrom(arg);
				dB.echoDebug("...set QTY: '%s'", String.valueOf(qty));
				continue;

			}   else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

		// Check for null fields from 'required' arguments
		if ( item == null ) 
			throw new InvalidArgumentsException("Must specify a valid 'Item'.");
		
		scriptEntry.addObject("item", item);
		scriptEntry.addObject("trader", trader);
		scriptEntry.addObject("action", action);
		scriptEntry.addObject("qty", qty);
	}


	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		
		String action = (String) scriptEntry.getObject("action");
		Trader trader = (Trader) scriptEntry.getObject("trader");
		ItemStack item = (ItemStack) scriptEntry.getObject("item");
		if ( item == null ) return;
		
		int qty = (Integer) scriptEntry.getObject("qty");
		Player player = scriptEntry.getPlayer();
		
		if ( action.equals("SELL") )
		{
			trader.selectItem(item, TraderStatus.SELL, false, false);
			
			if ( !trader.hasSelectedItem() )
			{
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), Trader.toStockItem(item), -1.0, qty, TransactionResult.FAIL_ITEM));
				return;
			}
			
			double price = trader.getPrice(player, "sell")*qty;
			
			if ( !checkLimits(trader, trader.getSelectedItem(), player) )//trader.getSelectedItem().getLimits().checkLimit(player.getName(), 0, qty) )// !trader.checkLimits() )
			{
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, qty, TransactionResult.FAIL_LIMIT));
				locale.sendMessage(player, "trader-transaction-failed-limit");
			}
			else
			if ( !trader.inventoryHasPlaceAmount(qty) )
			{
					locale.sendMessage(player, "trader-transaction-failed-inventory");
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, qty, TransactionResult.FAIL_SPACE));
			}
			else
			if ( !trader.buyTransaction(price) )
			{
					locale.sendMessage(player, "trader-transaction-failed-money");
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, qty, TransactionResult.FAIL_MONEY));
			}
			else
			{ 
			//	locale.sendMessage(player, "trader-transaction-success", "action", "#bought", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
				
				trader.addAmountToInventory(qty);//.addSelectedToInventory(0);

				trader.updateBuyLimits(qty);
				
				//call event Denizen Transaction Trigger
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, qty, TransactionResult.SUCCESS_SELL));
				
				//logging
				trader.log("buy", 
					trader.getSelectedItem().getItemStack().getTypeId(),
					trader.getSelectedItem().getItemStack().getData().getData(), 
					trader.getSelectedItem().getAmount()*qty, 
					price );
				
			}
		}
	/*	else
		if ( action.equals("BUY") )
		{
			trader.selectItem(item, TraderStatus.BUY, StockItem.hasDurability(item), false);
			
			if ( !trader.hasSelectedItem() )
			{
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), Trader.toStockItem(item), -1.0, qty, TransactionResult.FAIL_ITEM));
				return;
			}
			
			double price = trader.getPrice(player, "sell")*qty;
			
			if ( !trader.getSelectedItem().getLimitSystem().checkLimit(player.getName(), 0, qty) )// !trader.checkLimits() )
			{
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, qty, TransactionResult.FAIL_LIMIT));
				locale.sendMessage(player, "trader-transaction-failed-limit");
			}
			else
			if ( !trader.sellTransaction(price) )
			{
				locale.sendMessage(player, "trader-transaction-failed-money");
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, qty, TransactionResult.FAIL_MONEY));
			}
			else
			{ 
				locale.sendMessage(player, "trader-transaction-success");
			
				//TODO Inventory removement
			//	trader.removeFromInventory(item);
				
				trader.updateBuyLimits(qty);
				
				//call event Denizen Transaction Trigger
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, qty, TransactionResult.SUCCESS_SELL));
				
				//logging
				trader.log("buy", 
					trader.getSelectedItem().getItemStack().getTypeId(),
					trader.getSelectedItem().getItemStack().getData().getData(), 
					trader.getSelectedItem().getAmount()*qty, 
					price );
				
			}
		}*/
	}	
	
	protected static LimitManager limits = DtlTraders.getLimitsManager();
	
	public boolean checkLimits(Trader trader, StockItem selectedItem, Player player) {
		return limits.checkLimit(trader, player.getName(), selectedItem, selectedItem.getAmount()) &&
				limits.checkLimit(trader, "global limit", selectedItem, selectedItem.getAmount());
		//return selectedItem.getLimitSystem().checkLimit(player.getName(),0);
	}

}

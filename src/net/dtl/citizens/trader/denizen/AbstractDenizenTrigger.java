package net.dtl.citizens.trader.denizen;

import org.bukkit.ChatColor;

import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.denizen.triggers.TransactionTrigger;

public abstract class AbstractDenizenTrigger extends AbstractTrigger {

	public static void registerTriggers() {
		TransactionTrigger transaction = new TransactionTrigger();
		
		if ( CitizensTrader.getDenizen().getTriggerRegistry().register("transaction", transaction) )
			CitizensTrader.info("Registered denizen " + ChatColor.YELLOW + transaction.getClass().getSimpleName());
	}

}

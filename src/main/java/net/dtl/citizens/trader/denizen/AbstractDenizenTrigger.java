package net.dtl.citizens.trader.denizen;

import org.bukkit.ChatColor;

import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry.CooldownType;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.denizen.triggers.TraderClickTrigger;

public abstract class AbstractDenizenTrigger extends AbstractTrigger {

	public static void registerTriggers() {
		new TraderClickTrigger().activate().as("Trader").withOptions(false, 2.0, CooldownType.PLAYER);
		CitizensTrader.info("Registered" + ChatColor.YELLOW + " TraderClick trigger");
	}

}

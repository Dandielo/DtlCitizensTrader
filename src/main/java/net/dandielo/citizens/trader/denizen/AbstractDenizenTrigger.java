package net.dandielo.citizens.trader.denizen;

import org.bukkit.ChatColor;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry.CooldownType;
import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.denizen.triggers.TraderClickTrigger;

public abstract class AbstractDenizenTrigger extends AbstractTrigger {

	public static void registerTriggers() {
		new TraderClickTrigger().activate().as("Trader");//.withOptions(false, Settings.TriggerDefaultCooldown("Trader"), CooldownType.PLAYER);
		CitizensTrader.info("Registered" + ChatColor.YELLOW + " TraderClick trigger");
	}

}

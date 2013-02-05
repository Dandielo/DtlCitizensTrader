package net.dtl.citizens.trader.denizen.triggers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.NpcManager;
import net.dtl.citizens.trader.TraderTrait;
import net.dtl.citizens.trader.denizen.AbstractDenizenTrigger;

public class TraderClickTrigger extends AbstractDenizenTrigger implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void clickTrigger(NPCRightClickEvent event) {

        // Check if NPC has triggers.
        if (!event.getNPC().hasTrait(TriggerTrait.class)) return;
        // Check if NPC is trader 
        if (!event.getNPC().hasTrait(TraderTrait.class)) return;
        // Check if trigger is enabled.
        if (!event.getNPC().getTrait(TriggerTrait.class).isEnabled(name)) return;

        event.setCancelled(true);
        // If engaged or not cool, calls On Unavailable, if cool, calls On Click
        // If available (not engaged, and cool) sets cool down and returns true. 
        if (!event.getNPC().getTrait(TriggerTrait.class).trigger(this, event.getClicker())) return;

        // Get Interact Script for Player/NPC
        String script = sH.getInteractScript(event.getNPC(), event.getClicker(), this.getClass());

        // Parse Click Trigger, if unable to parse call No Click Trigger action
        if (!parse(denizen.getNPCRegistry().getDenizen(event.getNPC()), event.getClicker(), script))
            denizen.getNPCRegistry().getDenizen(event.getNPC()).action("no trader click trigger", event.getClicker());
    }

	@Override
    public void onEnable() {
		CitizensTrader.getInstance().getServer().getPluginManager().registerEvents(this, CitizensTrader.getInstance());
    }

}

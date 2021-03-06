package net.dandielo.citizens.trader.denizen.triggers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.dandielo.citizens.trader.DtlTraders;
import net.dandielo.citizens.trader.NpcManager;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.denizen.AbstractDenizenTrigger;

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
        
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());

        // Get Interact Script for Player/NPC
        InteractScriptContainer script = npc.getInteractScript(event.getClicker(), this.getClass());

        // Parse Click Trigger, if unable to parse call No Click Trigger action
        if (!parse(denizen.getNPCRegistry().getDenizen(event.getNPC()), event.getClicker(), script))
            denizen.getNPCRegistry().getDenizen(event.getNPC()).action("no trader click trigger", event.getClicker());
    }

	@Override
    public void onEnable() {
		DtlTraders.getInstance().getServer().getPluginManager().registerEvents(this, DtlTraders.getInstance());
    }

}

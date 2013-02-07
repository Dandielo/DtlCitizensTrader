package net.dandielo.citizens.trader.denizen.triggers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;
import net.dandielo.citizens.trader.denizen.AbstractDenizenTrigger;
import net.dandielo.citizens.trader.events.TraderTransactionEvent;

public class TransactionTrigger extends AbstractDenizenTrigger implements Listener
{
	protected Map<String, TraderTransactionEvent> data = new HashMap<String, TraderTransactionEvent>();
	
	@EventHandler
    public void clickTrigger(TraderTransactionEvent event) {
        // Check if NPC has triggers.
        if (!event.getNPC().hasTrait(TriggerTrait.class)) return;
        // Check if trigger is enabled.
        if (!event.getNPC().getTrait(TriggerTrait.class).isEnabled(name)) return;

        // If engaged or not cool, calls On Unavailable, if cool, calls On Click
        // If available (not engaged, and cool) sets cool down and returns true. 
        if (!event.getNPC().getTrait(TriggerTrait.class).trigger(this, event.getParticipant())) return;

        // Get Interact Script for Player/NPC
        String script = sH.getInteractScript(event.getNPC(), event.getParticipant(), this.getClass());
        
        // Parse Click Trigger, if unable to parse call No Click Trigger action
        if (!parse(denizen.getNPCRegistry().getDenizen(event.getNPC()), event.getParticipant(), script))
        {
        	data.put(event.getParticipant().getName(), event);
            denizen.getNPCRegistry().getDenizen(event.getNPC()).action("no transaction trigger", event.getParticipant());
        }
    }

    @Override
    public boolean parse(dNPC npc, Player player, String script) {
        if (script == null) return false;

        TraderTransactionEvent event = data.get(player.getName());
        
        dB.echoDebug(DebugElement.Header, "Parsing transaction trigger: " + npc.getName() + "/" + player.getName());

        dB.echoDebug("Getting current step:");
        String theStep = sH.getCurrentStep(player, script);

        // Gets entries from the script
        List<String> theScript = sH.getScriptContents(sH.getTriggerScriptPath(script, theStep, name) + ScriptHelper.scriptKey);

        theScript.add(0, "FLAG 'T_RESULT:" + event.getResult().stringResult() + "'");
        theScript.add(0, "FLAG 'T_PRICE:" + event.getEndPrice() + "'");
        theScript.add(0, "FLAG 'T_INFO:" + event.getResult().stringResultInfo() + "'");
        theScript.add(0, "FLAG 'T_ITEM:" + event.getItem().getItemStack().getType().name() + "'");
        
        // Build scriptEntries from the script and queue them up
        sB.queueScriptEntries(player, sB.buildScriptEntries(player, npc, theScript, script, theStep), QueueType.PLAYER);

        return true;
    }

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }
}

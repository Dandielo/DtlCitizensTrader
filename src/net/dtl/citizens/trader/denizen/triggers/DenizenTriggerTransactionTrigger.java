package net.dtl.citizens.trader.denizen.triggers;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.dtl.citizens.trader.denizen.AbstractDenizenTraderTrigger;
import net.dtl.citizens.trader.events.TraderTransactionEvent;
import net.dtl.citizens.trader.events.TraderTransactionEvent.TransactionResult;
import net.dtl.citizens.trader.objects.StockItem;

public class DenizenTriggerTransactionTrigger extends AbstractDenizenTraderTrigger implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTraderTransaction(TraderTransactionEvent event)
	{
		if (!plugin.getDenizenNPCRegistry().isDenizenNPC(event.getNpc()))
			return;

		/* Shortcut to the ScriptHelper */
		ScriptHelper sE = plugin.getScriptEngine().helper;

		DenizenNPC theDenizen = plugin.getDenizenNPCRegistry().getDenizen(event.getNpc());
		Player player = (Player) event.getParticipant();
		
		/* Show NPC info if sneaking and right clicking */
		if (player.isSneaking() 
				&& player.isOp()
				&& plugin.settings.RightClickAndSneakInfoModeEnabled()) { 
			theDenizen.showInfo(player);
			return;
		}

		if (!theDenizen.hasTrigger(triggerName)) {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...click trigger not enabled for this Denizen.");
			return;
		}

		// If Denizen is not interactable (ie. Denizen is toggled off, engaged or not cooled down)
		if (!theDenizen.isInteractable(triggerName, player)) {
			theDenizen.talk(TalkType.CHAT_PLAYERONLY, player, Reason.DenizenIsUnavailable);
			return;
		}

		if ( !event.getResult().equals(TransactionResult.SUCCESS_SELL)
				&& !event.getResult().equals(TransactionResult.SUCCESS_BUY) )
		{
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...transaction not succeded.");
			return;
		}
		
		// Cool! Parse the Trigger...
		// Apply default cool-down to avoid click-spam, then send to parser. */
	//	sE.setCooldown(theDenizen, DenizenTriggerTransactionTrigger.class, plugin.settings.DefaultClickCooldown());

		if (!parseClickTrigger(theDenizen, player, event.getItem())) {
			theDenizen.talk(TalkType.CHAT_PLAYERONLY, player, Reason.NoMatchingClickTrigger);
			return;
		}

		// Success!
	}

	private boolean parseClickTrigger(DenizenNPC theDenizen, Player thePlayer, StockItem item) {
		
		
		CommandSender cs = Bukkit.getConsoleSender();
		ScriptHelper sE = plugin.getScriptEngine().helper;

		/* Check for Quick Click Script */
		if (!plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Interact Scripts")) {
			if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Quick Scripts.Click")) {

				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing QUICK CLICK script: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");

				/* Get the contents of the Script. */
				List<String> theScript = plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Quick Scripts.Click");

				if (theScript.isEmpty()) return false;

				/* Build scriptEntries from theScript and add it into the queue */
				sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, "Quick Click", 1), QueueType.TASK);

				return true;

			}
		}

		/* Get Interact Script, if any. */
		String theScriptName = theDenizen.getInteractScript(thePlayer, this.getClass());

		if (theScriptName == null) return false;

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing click trigger: " + theDenizen.getName() + "/" + thePlayer.getName() + " -+");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Getting current step:");
		/* Get Player's current step */
		Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

		/* Get the contents of the Script. */
		List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName) + sE.scriptString);

		if (theScript.isEmpty()) return false;
		theScript.add(0, "^FLAG TransactionItem:"+item.getItemStack().getType().name());
		
		
		/* Build scriptEntries from theScript and add it into the queue */
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);

		return true;
		
	}
}

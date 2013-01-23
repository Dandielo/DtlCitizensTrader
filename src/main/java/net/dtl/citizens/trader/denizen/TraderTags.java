package net.dtl.citizens.trader.denizen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.NpcEcoManager;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.denizen.commands.TransactionCommand;
import net.dtl.citizens.trader.events.TraderTransactionEvent;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.types.ServerTrader;
import net.dtl.citizens.trader.types.Trader;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class TraderTags implements Listener {
	
	private static NpcEcoManager manager = CitizensTrader.getNpcEcoManager();
	private static TraderTags tTags;
	
    private Map<String, List<String>> playerChatHistory = new ConcurrentHashMap<String, List<String>>();
    private Map<String, Pattern> playerChatPattern = new ConcurrentHashMap<String, Pattern>();
    private Map<String, String> transactionFailed = new ConcurrentHashMap<String, String>();
	
	
	public TraderTags() {
    }
	
	public static void main(String[] a)
	{

		Pattern p = Pattern.compile("\\b(?i:STONE|GRASS|DIRT)\\b");
		Matcher m = p.matcher("what a strange stone it is");
		m.find();
		System.out.println(m.group());
		
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
    public void addMessage(AsyncPlayerChatEvent event) {
            List<String> history = new ArrayList<String>();
            if (playerChatHistory.containsKey(event.getPlayer().getName())) {
                history = playerChatHistory.get(event.getPlayer().getName());
            }

            if (history.size() > 10) history.remove(9);
            history.add(0, event.getMessage());

            playerChatHistory.put(event.getPlayer().getName(), history);
    }
	
	@EventHandler
	public void transactionResult(TraderTransactionEvent e)
	{
		DenizenNPC denizen = DenizenAPI.getDenizenNPC(e.getNPC());
		Player p = e.getParticipant();
		
		if ( e.getResult().succeeded() )
		{
			transactionFailed.put(p.getName(), "");
			denizen.action("Transaction Success", e.getParticipant());
		}
		else
		{
			switch(e.getResult())
			{
			case FAIL_MONEY:
				transactionFailed.put(p.getName(), "money");
				break;
			case FAIL_LIMIT:
				transactionFailed.put(p.getName(), "limit");
				break;
			case FAIL_SPACE:
				transactionFailed.put(p.getName(), "inventory");
			}
			denizen.action("Transaction Failure", e.getParticipant());
		}
	}
	
	@EventHandler
	public void traderTags(ReplaceableTagEvent e)
	{
        Player p = e.getPlayer();
        if (p == null) return;
        
        String tag = e.getName().toUpperCase();
        String subtag = e.getType().toLowerCase();
        
		if ( tag.equals("TRADER") )
		{	
			if ( subtag.equals("selling") )
			{
				if ( manager.isEconomyNpc(e.getNPC().getCitizen()) )
				{
			        Trader trader = new ServerTrader(e.getNPC().getCitizen().getTrait(TraderCharacterTrait.class), e.getNPC().getCitizen(), p);//(Trader) manager.getInteractionNpc(p.getName());
			        
			        String replaceString = "";
			        List<StockItem> stock = trader.getStock().getStock(subtag);
			        for ( StockItem item : stock )
		        		replaceString += "|" + item.getItemStack().getType().name();
			        
			        String rep = replaceString.substring(1);
			        replaceString = "REGEX:\\b(?i:" + rep + ")\\b";
			        playerChatPattern.put(p.getName(), Pattern.compile("\\b(?i:" + rep + ")\\b"));
			        
			        e.setReplaced(replaceString);
				}
			}
			if ( subtag.equals("item") )
			{
				if ( playerChatHistory.containsKey(p.getName()) )
				{
					String last = playerChatHistory.get(p.getName()).get(0);
					Matcher m = playerChatPattern.get(p.getName()).matcher(last);
					if (m.find())
						e.setReplaced(m.group());
				}
			}
			if ( subtag.equals("failure") )
			{
				e.setReplaced(transactionFailed.get(p.getName()));
			}
		}
	}
	
	public static void initializeDenizenTags(Denizen denizen)
	{
		if ( denizen != null )
		{
			tTags = new TraderTags();
			denizen.getServer().getPluginManager().registerEvents(tTags, denizen);
			CitizensTrader.info("Registered denizen " + ChatColor.YELLOW + "Replacement Tags");
		}
	}
	
}

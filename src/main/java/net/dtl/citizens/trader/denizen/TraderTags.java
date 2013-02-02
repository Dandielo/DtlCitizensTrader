package net.dtl.citizens.trader.denizen;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.NpcEcoManager;
import net.dtl.citizens.trader.TraderCharacterTrait;
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
	
    /** TraderTags
     * <trader.stock.sell.trigger>
     * <trader.stock.sell.hint>
     * <trader.stock.buy.trigger>
     * <trader.stock.buy.hint>
     * <trader.pattern>
     * <trader.owner>
     * <trader.wallet>
     * <trader.wallet.balance>
     * <transaction.result>
     * <transaction.item>
     * <transaction.item.price>
     * <transaction.item.instock>
     * <transaction.failure>
     */
	
	public TraderTags() {
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
		dNPC denizen = DenizenAPI.getDenizenNPC(e.getNPC());
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
				break;
			default: break;
			}
			denizen.action("Transaction Failure", e.getParticipant());
		}
	}
	
	@EventHandler
	public void traderTags(ReplaceableTagEvent e)
	{
        Player p = e.getPlayer();
        if (p == null) return;

        NPC npc = e.getNPC().getCitizen();
        
        String name = e.getName().toLowerCase();
        String tag = e.getType().toLowerCase();
        String subtag = e.getSubType().toLowerCase();
        
        if ( name.equals("trader") )
        {
        	if ( !manager.isEconomyNpc(npc) )
        		return;
        	
        	Trader trader = new ServerTrader(npc.getTrait(TraderCharacterTrait.class), npc, p);
        	if ( tag.equals("pattern") )
        	{
        		 e.setReplaced(trader.getStock().getPattern().getName());
        	}
        	else
        	if ( tag.equals("wallet") )
        	{
        		if ( subtag.equals("balance") )
        			e.setReplaced(new DecimalFormat("#.##").format(trader.getConfig().getWallet().getMoney()));
        		else 
        			e.setReplaced(trader.getConfig().getWallet().getType().toString());
        	}
        	else
        	if ( tag.equals("stock") )
        	{
        		if ( !subtag.contains(".") )
        			return;
        		String[] stock = subtag.split(".");
        		
        		if ( stock[1].equals("trigger") )
        			e.setReplaced(this.regexTrigger(p, trader, stock[0]));
        		else
        		if ( stock[1].equals("hint") ) 
        			e.setReplaced(this.hint(p, trader, stock[0]));
        	}
        	else
        	if ( tag.equals("owner") );
        	{
        		e.setReplaced(trader.getConfig().getOwner());
        	}
        }
        else
        if ( name.equals("transaction") )
        {
        	if ( tag.equals("item") )
        	{
        		
        	}
        	if ( tag.equals("result") )
        	{
        		
        	}
        	if ( tag.equals("faliure") )
        	{
        		
        	}
        }
        
        /*
        
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
		}*/
	}
	
	private String hint(Player p, Trader trader, String string) {
		// TODO Auto-generated method stub
		return "";
	}

	public String regexTrigger(Player p, Trader trader, String st)
	{
		String replaceString = "";
        List<StockItem> stock = trader.getStock().getStock(st);
        for ( StockItem item : stock )
    		replaceString += "|" + item.getItemStack().getType().name();
        
        String rep = replaceString.substring(1);
        replaceString = "REGEX:\\b(?i:" + rep + ")\\b";
        playerChatPattern.put(p.getName(), Pattern.compile("\\b(?i:" + rep + ")\\b"));
        return replaceString;
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

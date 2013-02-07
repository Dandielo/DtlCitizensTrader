package net.dandielo.citizens.trader.denizen;

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
import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.NpcManager;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.events.TraderTransactionEvent;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.objects.TransactionPattern;
import net.dandielo.citizens.trader.types.ServerTrader;
import net.dandielo.citizens.trader.types.Trader;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class TraderTags implements Listener {
	
	private static NpcManager manager = CitizensTrader.getNpcEcoManager();
	private static TraderTags tTags;
	
    private Map<String, List<String>> playerChatHistory = new ConcurrentHashMap<String, List<String>>();
    private Map<String, Pattern> playerChatPattern = new ConcurrentHashMap<String, Pattern>();
    
    private Map<String, TransactionInfo> transactions = new ConcurrentHashMap<String, TransactionInfo>();
    //private Map<String, String> transactionFailed = new ConcurrentHashMap<String, String>();
	
    /** TraderTags
     * <trader.stock.sell/trigger>
     * <trader.stock.sell/hint>
     * <trader.stock.buy/trigger>
     * <trader.stock.buy/hint>
     * <trader.pattern>
     * <trader.owner>
     * <trader.wallet>
     * <trader.wallet.balance>
     * <transaction.item>
     * <transaction.item.price>
     * <transaction.item.amount>
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
		String player = e.getParticipant().getName();
		
		if ( e.getResult().succeeded() )
		{
			transactions.put(player, new TransactionInfo(e.getItem().getName(), e.getEndPrice(), e.getAmount(), e.getLeft()));
			denizen.action("Transaction Success", e.getParticipant());
		}
		else
		{
			switch(e.getResult())
			{
			case FAIL_MONEY:
				transactions.put(player, new TransactionInfo(e.getItem().getName(), e.getEndPrice(), e.getAmount(), e.getLeft(), "money"));
				break;
			case FAIL_LIMIT:
				transactions.put(player, new TransactionInfo(e.getItem().getName(), e.getEndPrice(), e.getAmount(), e.getLeft(), "limit"));
				break;
			case FAIL_SPACE:
				transactions.put(player, new TransactionInfo(e.getItem().getName(), e.getEndPrice(), e.getAmount(), e.getLeft(), "inventory"));
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
        String player = p.getName();

        if ( e.getNPC() == null ) return;
        NPC npc = e.getNPC().getCitizen();
        
        String name = e.getName().toLowerCase();
        String tag = e.getType().toLowerCase();
        String subtag = e.getSubType() == null ? "" : e.getSubType().toLowerCase();
        
        if ( name.equals("trader") )
        {
        	if ( !manager.isEconomyNpc(npc) )
        		return;
        	
        	Trader trader = new ServerTrader(npc.getTrait(TraderTrait.class), npc, p);
        	if ( tag.equals("pattern") )
        	{
        		TransactionPattern pat = trader.getStock().getPattern();
        		 e.setReplaced(pat == null ? "none" : pat.getName());
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
        		if ( !subtag.contains("-") )
        			return;
        		String[] stock = subtag.split("-", 2);

        		if ( stock[1].equals("trigger") )
        			e.setReplaced(regexTrigger(p, trader, stock[0]));
        		else
        		if ( stock[1].equals("hint") ) 
        			e.setReplaced(hint(p, trader, stock[0]));
        	}
        	else
        	if ( tag.equals("owner") )
        	{
        		e.setReplaced(trader.getConfig().getOwner());
        	}
        }
        else
        if ( name.equals("transaction") )
        {
        	if ( tag.equals("result") )
        	{
        		if ( subtag.equals("price") )
        		{
        			e.setReplaced(new DecimalFormat("#.##").format(transactions.get(player).price));
        		}
        		else
            	if ( subtag.equals("amount") )
            	{
            		e.setReplaced(String.valueOf(transactions.get(player).amount));
            	}
            	else
                if ( subtag.equals("item") )
            	{
            		e.setReplaced(transactions.get(player).item);
            	}
            	else
                if ( subtag.equals("instock") )
            	{
            		e.setReplaced(String.valueOf(transactions.get(player).left));
            	}
        	}
        	if ( tag.equals("failure") )
        	{
        		e.setReplaced(transactions.get(player).failure);
        	}
        	if ( tag.equals("trigger") )
        	{
        		if ( playerChatHistory.containsKey(player) )
				{
					String last = playerChatHistory.get(player).get(0);

					Matcher m = playerChatPattern.get(player).matcher(last);
					if (m.find())
						e.setReplaced(m.group().toUpperCase());
				}
        	}
        }
	}
	
	private String hint(Player p, Trader trader, String st) {
		String hint = "";
		String hintItem = CitizensTrader.getLocaleManager().message("denizen-hint-item");

        List<StockItem> stock = trader.getStock().getStock(st);
        for ( StockItem item : stock )
        {
        	String price = new DecimalFormat("#.##").format(item.calcPrice(p, trader.getStock().getPattern(), st));
        	hint += " | " + hintItem.replaceAll("\\{price\\}", price).replaceAll("\\{name\\}", item.name());
        }
		return hint.isEmpty() ? "empty stock" : hint.substring(3);
	}

	public String regexTrigger(Player p, Trader trader, String st)
	{
		String replaceString = "";
        List<StockItem> stock = trader.getStock().getStock(st);
        for ( StockItem item : stock )
    		replaceString += "|" + item.name();
        
        if ( replaceString.isEmpty() ) return "REGEX:";
        
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
	
	static class TransactionInfo
	{		
		String item;
		double price;
		int amount;
		int left;
		
		String failure;

		TransactionInfo(String i, double p, int a)
		{
			this(i, p, a, 0);
		}
		TransactionInfo(String i, double p, int a, String f)
		{
			this(i, p, a, 0, f);
		}
		TransactionInfo(String i, double p, int a, int l)
		{
			this(i, p, a, l, "");
		}
		TransactionInfo(String i, double p, int a, int l, String f)
		{
			item = i;
			price = p;
			amount = a;
			failure = f;
			left = l;
		}
	}
}

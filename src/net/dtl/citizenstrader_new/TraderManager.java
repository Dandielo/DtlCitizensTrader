package net.dtl.citizenstrader_new;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.traders.PlayerTrader;
import net.dtl.citizenstrader_new.traders.ServerTrader;
import net.dtl.citizenstrader_new.traders.Trader;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.TraderTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait.TraderType;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class TraderManager implements Listener {
	//private static TraderConfig config;
	private PermissionsManager permManager = CitizensTrader.getPermissionsManager();
	
	//for laggy servers...
	private List<String> playersAntiDclick;
	private Timer antiDClickTimer;
	private long timerDelay;
	
	private HashMap<String,Trader> ongoingTrades = new HashMap<String,Trader>();	
	private List<NPC> isTraderNpc;
	
	public TraderManager(ConfigurationSection config) {
		this.isTraderNpc = new ArrayList<NPC>();
		playersAntiDclick = new ArrayList<String>();
		antiDClickTimer = new Timer("anti-dclick");
		
		
		if ( !config.contains("trader.rclick-interval") )
		{
			config.set("trader.rclick-interval", 1000);
			CitizensTrader.plugin.saveConfig();
		}
		else
			timerDelay = config.getLong("trader.rclick-interval");
		//config = CitizensTrader.getTraderConfig();
	}
	
	public Trader getOngoingTrades(String player) {
		if ( ongoingTrades.containsKey(player) )
			return ongoingTrades.get(player);
		return null;
	}
	
	public boolean isTraderNpc(NPC npc) {
		return this.isTraderNpc.contains(npc);
	}
	
	protected void addTraderNpc(NPC npc) {
		if ( !isTraderNpc(npc) ) {
			this.isTraderNpc.add(npc);
		}
	}	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if ( !( event.getWhoClicked() instanceof Player ) )
			return;
		
		Player p = (Player) event.getWhoClicked();

		if ( ongoingTrades.containsKey(p.getName()) ) {
			
			if ( event.getRawSlot() < 0 ) {
				event.setCancelled(true);
				return;
			}

			if ( TraderStatus.hasManageMode(ongoingTrades.get(p.getName()).getTraderStatus()) ) {
				
				
				if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.MANAGE) )
					ongoingTrades.get(p.getName()).setTraderStatus(TraderStatus.MANAGE_SELL);
				
				
				ongoingTrades.get(p.getName()).managerMode(event);

			}
			else 
			{
				
				ongoingTrades.get(p.getName()).simpleMode(event);
				
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		
		
		//get the trader we are trading with
		Trader trader = ongoingTrades.get(player.getName());
		
		
		//ups, no transaction sorry :<
		if( trader == null )
		{
			return;
		}
		
		
		//are we managing?
		if ( TraderStatus.hasManageMode(trader.getTraderStatus()) )
		{
			
			//have we managed amounts?
			if (  trader.equalsTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT) ) 
			{
				//save amounts and set the basic managing page
				trader.saveManagedAmouts();
				trader.switchInventory(TraderStatus.MANAGE_SELL);
			}
			
			//reset the traders status
			trader.reset(TraderStatus.MANAGE);
		}
		else
		//no managers here, what a pity 
		{
			
			
			//lets end this transaction
			ongoingTrades.remove(player.getName());
		}
			
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		
		//come on... you thought i've forgot to prevent item dropping ;P
		Player player = (Player) event.getPlayer();
		
		//get the trader we are trading with
		Trader trader = ongoingTrades.get(player.getName());
		
		
		//ups, no transaction sorry :<
		if( trader == null )
		{
			return;
		}
				
		//are we managing?
		if ( this.permManager.has(player, "dtl.trader.bypass.drop-item") )
			return;
		
		//sorry we can't allow drop you that item :P
		event.setCancelled(true);
		
	}
	
	@EventHandler
	public void onNPCDespawn(NPCDespawnEvent event) {
		
		//despawning an npc? DESTROY IT Buahahahah! xD
		if ( this.isTraderNpc.contains(event.getNPC()) )
			this.isTraderNpc.remove(event.getNPC());
		
		
	}
	
	@EventHandler
	public void onNPCRightCLick(NPCRightClickEvent event) {		
		//bad touch hurts forever...
		if ( !this.isTraderNpc.contains(event.getNPC()) ) 
			return;

		
		final String playerName = event.getClicker().getName();
		
		if ( playersAntiDclick.contains(playerName) )
			return;
		
		playersAntiDclick.add(playerName);
		
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				playersAntiDclick.remove(playerName);
			}
		};
		
		antiDClickTimer.schedule(task, timerDelay);
		
		//get the touched and the toucher
		NPC npc = event.getNPC();
		Player player = event.getClicker();
		
		//get the desired trait 
		TraderTrait trait = npc.getTrait(TraderCharacterTrait.class).getTraderTrait();
		

		//get the current assigned manager :P (exists one?)
		Trader trader =  ongoingTrades.get(player.getName());
		
		
		//if we got the magic stick!
		if ( player.getItemInHand().getTypeId() == 280 ) 
		{
			if ( ( permManager.has(player, "dtl.trader.options.manager-mode") 
					&& trait.getOwner().equals(player.getName()) )
					|| player.isOp()
					|| permManager.has(player, "dtl.trader.bypass.manager-mode") )
			{
				
				//sth is already managed!
				if ( trader != null )
				{
					//its the same! lets kill it ;>
					if ( trader.getNpcId() == npc.getId() )
					{

						//remove the old one
						ongoingTrades.remove(player.getName());
						
						player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
						return;
					}
					
					if ( !permManager.has(player, "dtl.trader.options.type." + trait.getTraderType().toString()) )
					{
						player.sendMessage("!NO PERMISSIONS, CANT MANAGE THIS TRADERs!");
						return;
					}
					
					//is it a server trader?
					if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
					{
						
						ongoingTrades.put(player.getName(), new ServerTrader(npc,trait));
					}
					//nah it's a player trader
					else 
					if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) 
					{
						
						if ( player.getGameMode().equals(GameMode.CREATIVE) 
								&& !permManager.has(player, "dtl.trader.bypass.creative") )
						{
							player.sendMessage("!NO PERMISSIONS, CREATIVE!");
							return;
						}
						
						ongoingTrades.put(player.getName(), new PlayerTrader(npc,trait));
					}

					
					//we are managing!
					ongoingTrades.get(player.getName()).setTraderStatus(TraderStatus.MANAGE);
					player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
					
					//we are done ;)
					return;
				}
				else
				//nothing exists
				{
					if ( !permManager.has(player, "dtl.trader.options.type." + trait.getTraderType().toString()) )
					{
						player.sendMessage("!NO PERMISSIONS, CANT MANAGE THIS TRADER!");
						return;
					}
					
					//is it a server trader?
					if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
						ongoingTrades.put(player.getName(), new ServerTrader(npc,trait));
					//nah it's a player trader
					else if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) {
						ongoingTrades.put(player.getName(), new PlayerTrader(npc,trait));
					}
					
					//we are managing!
					ongoingTrades.get(player.getName()).setTraderStatus(TraderStatus.MANAGE);
					player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
					
					//return lets end this! :D
					return;
				}
				
				
			}
			else
			{
				player.sendMessage(ChatColor.RED + "!CAN'T MANAGE TRADERS!");
			}
			//nothing to do...
			return;
		}
		else
		//no stick? :< 
		{
			
			//is some1 managing?
			if ( trader != null 
					&& TraderStatus.hasManageMode(trader.getTraderStatus()) ) 
			{ 
				ongoingTrades.get(player.getName()).switchInventory(TraderStatus.MANAGE_SELL);
				
			} 
			else
			//only void ;<
			{
				if ( !permManager.has(player, "dtl.trader.options.simple-mode") )
				{
					player.sendMessage(ChatColor.RED + "!CAN'T USE ALL TRADERS!");
					return;
				}
				
				if ( !permManager.has(player, "dtl.trader.options.type." + trait.getTraderType().toString()) )
				{
					player.sendMessage("!NO PERMISSIONS, CAN USE THIS TRADER!");
					return;
				}
				
				if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
					ongoingTrades.put(player.getName(), new ServerTrader(npc,trait));
				else 
				if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) )
					ongoingTrades.put(player.getName(), new PlayerTrader(npc,trait));
		
				//	((CraftServer)Bukkit.getServer()).getServer().serverConfigurationManager.sendAll(new Packet15Place()); 
			}
			player.openInventory(ongoingTrades.get(player.getName()).getInventory());
			
		}
			
		
	}
	
}

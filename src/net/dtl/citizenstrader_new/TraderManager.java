package net.dtl.citizenstrader_new;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class TraderManager implements Listener {
	//private static TraderConfig config;
	private PermissionsManager permManager = CitizensTrader.getPermissionsManager();
	
	private HashMap<String,Trader> ongoingTrades = new HashMap<String,Trader>();	
	private List<NPC> isTraderNpc;
	
	public TraderManager() {
		this.isTraderNpc = new ArrayList<NPC>();
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
				
				
				if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_MANAGE) )
					ongoingTrades.get(p.getName()).setTraderStatus(TraderStatus.PLAYER_MANAGE_SELL);
				
				
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
			if (  trader.equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL_AMOUNT) ) 
			{
				//save amounts and set the basic managing page
				trader.saveManagedAmouts();
				trader.switchInventory(TraderStatus.PLAYER_MANAGE_SELL);
			}
			
			//reset the traders status
			trader.reset(TraderStatus.PLAYER_MANAGE);
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
		if ( !TraderStatus.hasManageMode(trader.getTraderStatus()) )
		{
			//sorry we can't allow drop you that item :P
			event.setCancelled(true);
		}
		
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
			if ( permManager.has(player, "dtl.trader.options.manager-mode")
					|| trait.getOwner().equals(player.getName())
					|| player.isOp() )
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
					
					
					//is it a server trader?
					if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
						ongoingTrades.put(player.getName(), new ServerTrader(npc,trait));
					//nah it's a player trader
					else if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) {
						ongoingTrades.put(player.getName(), new PlayerTrader(npc,trait));
					}

					
					//we are managing!
					ongoingTrades.get(player.getName()).setTraderStatus(TraderStatus.PLAYER_MANAGE);
					player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
					
					//we are done ;)
					return;
				}
				else
				//nothing exists
				{
					//is it a server trader?
					if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
						ongoingTrades.put(player.getName(), new ServerTrader(npc,trait));
					//nah it's a player trader
					else if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) {
						ongoingTrades.put(player.getName(), new PlayerTrader(npc,trait));
					}
					
					//we are managing!
					ongoingTrades.get(player.getName()).setTraderStatus(TraderStatus.PLAYER_MANAGE);
					player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
					
					//return lets end this! :D
					return;
				}
				
				
			}
			//nothing to do...
			return;
		}
		else
		//no stick? :< 
		{
			
			//is some1 managing?
			if ( trader != null ) 
			{ 
				ongoingTrades.get(player.getName()).switchInventory(TraderStatus.PLAYER_MANAGE_SELL);
				
			} 
			else
			//only void ;<
			{
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

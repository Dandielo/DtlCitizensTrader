package net.dtl.citizenstrader_new;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class TraderManager implements Listener {
	private static TraderConfig config;
	private HashMap<String,Trader> ongoingTrades = new HashMap<String,Trader>();	
	private List<NPC> isTraderNpc;
	
	public TraderManager() {
		this.isTraderNpc = new ArrayList<NPC>();
		config = CitizensTrader.getTraderConfig();
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

		//needs to be canceled cose traders for players are much more templates that they can fill up with their stuff 
		//event.setCancelled(true);

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
			else {
				
				/*
				 * Secure mode handling
				 * 
				 */
				if ( config.getMode().equals("secure") ) 
					ongoingTrades.get(p.getName()).secureMode(event);
				
				
				/*
				 * Simple mode handling
				 * 
				 */
				if ( config.getMode().equals("simple") ) 
					ongoingTrades.get(p.getName()).simpleMode(event);
				
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player p = (Player) event.getPlayer();
		if( ongoingTrades.containsKey(p.getName()) ){
			if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_SELL_AMOUNT) ||
				 ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_SELL) ||
				 ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_BUY) ) 
				ongoingTrades.remove(p.getName());
			else {
				//raderStatus trader = state.get(event.getPlayer().getName());
				//InventoryTrait sr = trader.getTrader().getTrait(InventoryTrait.class);
				if (  ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL_AMOUNT) ) {
					 ongoingTrades.get(p.getName()).saveManagedAmouts();
					 ongoingTrades.get(p.getName()).switchInventory(TraderStatus.PLAYER_MANAGE_SELL);
				}
				ongoingTrades.get(p.getName()).reset(TraderStatus.PLAYER_MANAGE);
			}
	    }
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player p = (Player) event.getPlayer();
		if( ongoingTrades.containsKey(p.getName()) ){
			if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_SELL_AMOUNT) ||
				 ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_SELL) ||
				 ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_BUY) ) 
				event.setCancelled(true);
			
	    }
	}
	
	@EventHandler
	public void onNPCDespawn(NPCDespawnEvent event) {
		if ( this.isTraderNpc.contains(event.getNPC()) )
			this.isTraderNpc.remove(event.getNPC());
	}
	
	@EventHandler
	public void onNPCRightCLick(NPCRightClickEvent event) {		
		if ( !this.isTraderNpc.contains(event.getNPC()) ) 
			return;
		
		NPC npc = event.getNPC();
		Player p = event.getClicker();
		
		TraderTrait trait = npc.getTrait(TraderCharacterTrait.class).getTraderTrait();
		//	System.out.print(trait.getName());

			if ( p.getItemInHand().getTypeId() != 280 ) {
				if ( ongoingTrades.containsKey(p.getName()) ) { 
					if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_MANAGE) ) {
						//
						ongoingTrades.get(p.getName()).switchInventory(TraderStatus.PLAYER_MANAGE_SELL);
						ongoingTrades.get(p.getName()).reset(TraderStatus.PLAYER_MANAGE);
					} else 
						return;
				} else {
					if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
						ongoingTrades.put(p.getName(), new ServerTrader(npc,trait));
					else if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) {
						ongoingTrades.put(p.getName(), new PlayerTrader(npc,trait));
					}
				//	ongoingTrades.get(p.getName()).switchInventory(TraderStatus.PLAYER_SELL);
					/* else if ( trait.getTraderType().equals(TraderType.AUCTIONHUSE) ) {
						
					} else if ( trait.getTraderType().equals(TraderType.BANK) ) {
						
					} *//*
					Packet15Place pa = new Packet15Place();
					pa.a = p.getLocation().getBlockX() + 1;
					pa.b = p.getLocation().getBlockX() + 1;
					pa.c = p.getLocation().getBlockX() + 1;
					((CraftServer)Bukkit.getServer()).getServer().serverConfigurationManager.sendAll(new Packet15Place()); */
				}
				p.openInventory(ongoingTrades.get(p.getName()).getInventory());
			} else {
				if ( p.isOp() || npc.getTrait(net.citizensnpcs.api.trait.trait.Owner.class).isOwnedBy(p.getName()) ) {
					if ( ongoingTrades.containsKey(p.getName()) ) {
						if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_MANAGE) ) {
							ongoingTrades.remove(p.getName());
							p.sendMessage(ChatColor.RED + npc.getFullName() +": user mode!");
						}
					} else {
						if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
							ongoingTrades.put(p.getName(), new ServerTrader(npc,trait));
						else if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) {
							ongoingTrades.put(p.getName(), new PlayerTrader(npc,trait));
						}
						ongoingTrades.get(p.getName()).setTraderStatus(TraderStatus.PLAYER_MANAGE);
						p.sendMessage(ChatColor.RED + npc.getFullName() +": manager mode!");
					}	
				}
			}
		
	}
	
	
	
	
	/*
	@Override
	public void onRightClick(NPC npc, Player p) {
		
	}*/
}

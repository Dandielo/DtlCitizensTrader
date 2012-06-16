package net.dtl.citizenstrader_new;

import java.security.acl.Owner;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizenstrader_new.traits.InventoryTrait;
import net.dtl.citizenstrader_new.traders.PlayerTrader;
import net.dtl.citizenstrader_new.traders.ServerTrader;
import net.dtl.citizenstrader_new.traders.Trader;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.TraderTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait.TraderType;

public class TraderCharacter extends Character implements Listener {
	private static TraderConfig config;
	
	private HashMap<String,Trader> ongoingTrades = new HashMap<String,Trader>();	
	
	public void setConfig(TraderConfig c) {
		config = c;
	}
	
	public Trader getOngoingTrades(String player) {
		if ( ongoingTrades.containsKey(player) )
			return ongoingTrades.get(player);
		return null;
	}
	
	@Override
	public void load(DataKey arg0) throws NPCLoadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save(DataKey arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    public void onSet(NPC npc) {
        if( !npc.hasTrait(InventoryTrait.class) ) 
            npc.addTrait(InventoryTrait.class);
        if ( !npc.hasTrait(TraderTrait.class) ) {
            npc.addTrait(TraderTrait.class);
        	npc.getTrait(TraderTrait.class).getWallet().setEconomy(config.getEcon());
        }
    }
	
	@Override
	public void onRightClick(NPC npc, Player p) {
		TraderTrait trait = npc.getTrait(TraderTrait.class);
	//	System.out.print(trait.getName());

		if ( p.getItemInHand().getTypeId() != 280 ) {
			if ( ongoingTrades.containsKey(p.getName()) ) { 
				if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_MANAGE) ) {
					ongoingTrades.get(p.getName()).switchInventory(TraderStatus.PLAYER_MANAGE_SELL);
					ongoingTrades.get(p.getName()).reset(TraderStatus.PLAYER_MANAGE);
				} else 
					return;
			} else {
				if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
					ongoingTrades.put(p.getName(), new ServerTrader(npc,npc.getTrait(TraderTrait.class)));
				else if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) {
					ongoingTrades.put(p.getName(), new PlayerTrader(npc,npc.getTrait(TraderTrait.class)));
				}
			//	ongoingTrades.get(p.getName()).switchInventory(TraderStatus.PLAYER_SELL);
				/* else if ( trait.getTraderType().equals(TraderType.AUCTIONHUSE) ) {
					
				} else if ( trait.getTraderType().equals(TraderType.BANK) ) {
					
				} *//*
				Packet15Place pa = new Packet15Place();
				pa.a = p.getLocation().getBlockX() + 1;
				pa.b = p.getLocation().getBlockX() + 1;
				pa.c = p.getLocation().getBlockX() + 1;
				((CraftServer)Bukkit.getServer()).getServer().serverConfigurationManager.sendAll(new Packet15Place());*/
			}
			p.openInventory(ongoingTrades.get(p.getName()).getInventory());
		} else {
			if ( ongoingTrades.containsKey(p.getName()) ) {
				if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_MANAGE) ) {
					ongoingTrades.remove(p.getName());
					p.sendMessage(ChatColor.RED + npc.getFullName() +": user mode!");
				}
			} else {
				ongoingTrades.put(p.getName(),new ServerTrader(npc,npc.getTrait(TraderTrait.class)));
				ongoingTrades.get(p.getName()).setTraderStatus(TraderStatus.PLAYER_MANAGE);
				p.sendMessage(ChatColor.RED + npc.getFullName() +": manager mode!");
			}				
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if ( event.getRawSlot() < 0 )
			return;
		if ( !( event.getWhoClicked() instanceof Player ) )
			return;
		
		Player p = (Player) event.getWhoClicked();


		if ( ongoingTrades.containsKey(p.getName()) ) {

			if ( TraderStatus.hasManageMode(ongoingTrades.get(p.getName()).getTraderStatus()) ) {
				if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.PLAYER_MANAGE) )
					ongoingTrades.get(p.getName()).setTraderStatus(TraderStatus.PLAYER_MANAGE_SELL);
				ongoingTrades.get(p.getName()).managerMode(event);
				
			}
			else {
		/*		if ( config.getMode().equals("secure") ) 
					ongoingTrades.get(p.getName()).secureMode(event);
				else*/
				
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
}

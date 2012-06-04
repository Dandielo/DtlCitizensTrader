package net.dtl.trader;

import java.util.Collection;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCSelectEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.DtlProject; 

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class NpcListener implements Listener {
	DtlProject dProject; 
	private Trader plugin;
	public NpcListener(DtlProject dtlProject, Trader trader) {
		plugin = trader;
		dProject = dtlProject;
	}
	
	@EventHandler
	public void npcSelect(NPCSelectEvent event) {
		if ( event.getSelector() instanceof Player )
			if ( dProject.getPermissions().has(event.getSelector(), "dtl.citizens.characters.trader") ) {
				plugin.setSelected(event.getNPC().getId());
		//		event.getPlayer().sendMessage("you have selected " + CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()).getFullName() + ".");
			}
	}
	
	@EventHandler 
	public void npcSpawn(NPCSpawnEvent event) {
		if ( event.getNPC().getCharacter() instanceof TraderNpc ) {
			((TraderNpc)event.getNPC().getCharacter()).setTraderID(event.getNPC().getId());
		}
	}
	@EventHandler 
	public void npcSpawn(NPCLeftClickEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void playerInteractEntity(PlayerInteractEntityEvent event) {
		if ( !event.getPlayer().getItemInHand().getType().equals(Material.STICK) ) {
			Collection<NPC> npcs = CitizensAPI.getNPCRegistry().getNPCs(TraderNpc.class);
			if ( npcs.contains(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())) ) {
				Inventory inv = CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()).getInventory();
				((TraderNpc)CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()).getCharacter()).setInventory(inv,CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()).getId(),event.getPlayer(),dProject);
				event.getPlayer().openInventory(inv);
			} 
		}
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {
		if ( event.getWhoClicked() instanceof Player ) {
			Player p = (Player) event.getWhoClicked();
			Collection<NPC> npcs = CitizensAPI.getNPCRegistry().getNPCs(TraderNpc.class);

			for ( int i = 0 ; i < npcs.size() ; ++i ) {
				if ( ((NPC)npcs.toArray()[i]).getName().equals(event.getInventory().getName()) ) {
					if ( ( !event.getCursor().getType().equals(Material.AIR) && event.getCurrentItem().getType().equals(Material.FIRE) ) || event.getCurrentItem().getType().equals(Material.FIRE) ) {
						if ( ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).canBuy(event.getCursor(),((NPC)npcs.toArray()[i]).getId()) )
							if ( ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).bought(event.getCursor(),((NPC)npcs.toArray()[i]).getId(),dProject.getEconomy(),p) ) {
								event.setCursor(new ItemStack(0,0));
								event.setCancelled(true);
								return;
							}
						event.setCancelled(true);
					} else if ( !((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).checkSlot(((NPC)npcs.toArray()[i]).getId(),event.getSlot(),event.getCurrentItem()) && ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).canSell(event.getCurrentItem(),((NPC)npcs.toArray()[i]).getId()) || event.getCurrentItem().equals(Material.FIRE) ) {
						if ( event.isShiftClick() && ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).sold(event.getCurrentItem(),((NPC)npcs.toArray()[i]).getId(),dProject.getEconomy(),p) )
							p.getInventory().addItem(event.getCurrentItem());
						else
							p.sendMessage(ChatColor.GOLD + event.getCurrentItem().getType().name() + " kosztuje " + ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).getCost(((NPC)npcs.toArray()[i]).getId(),event.getSlot()) + "$ za sztuke.");
						event.setCancelled(true);
					} 
					if ( event.isShiftClick() )
						event.setCancelled(true);
				}
			}
		}
	}
	
}

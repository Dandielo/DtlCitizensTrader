package net.dtl.citizenstrader;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizenstrader.TraderStatus.Status;
import net.dtl.citizenstrader.traits.InventoryTrait;
import net.dtl.citizenstrader.traits.StockItem;
import net.milkbowl.vault.economy.Economy;


public class TraderNpc extends Character implements Listener {
	private Economy econ;
	
	private HashMap<String,TraderStatus> state = new HashMap<String,TraderStatus>();
	
	public TraderNpc() {
	}
	
	public void setEcon(Economy e) {
		econ = e;
	}
	
	public TraderStatus getStatus(String name) {
		if ( state.containsKey(name) )
			return state.get(name);
		return null;
	}

	@Override
	public void load(DataKey arg0) throws NPCLoadException {		
	}

	@Override
	public void save(DataKey arg0) {
	} 
	
	@Override
	public void onRightClick(NPC npc, Player p) {
		
		if ( p.getItemInHand().getTypeId() != 280 ) {
			if ( !state.containsKey(p.getName()) )
				state.put(p.getName(),new TraderStatus(npc));
			state.get(p.getName()).setInventory(npc.getTrait(InventoryTrait.class).inventoryView(54,npc.getName()));
			p.openInventory(state.get(p.getName()).getInventory());
			
		} else {
			if ( state.containsKey(p.getName()) && state.get(p.getName()).getTrader().getId() == npc.getId() ) {
				if ( !state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE) ) {
					state.get(p.getName()).setStatus(Status.PLAYER_MANAGE);
					p.sendMessage(ChatColor.RED + "Trader manager enabled");
				} else if ( state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE) ) { 
					state.get(p.getName()).setStatus(Status.PLAYER_SELL);
					p.sendMessage(ChatColor.RED + "Trader manager disabled");
				}
			} else {
				state.put(p.getName(),new TraderStatus(npc,Status.PLAYER_MANAGE));
				p.sendMessage(ChatColor.RED + "Trader manager enabled");
			}
		}
			
		
	}
	
	@Override
    public void onSet(NPC npc) {
        if( !npc.hasTrait(InventoryTrait.class) ){
            npc.addTrait( new InventoryTrait() );
        }
    }

	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {
		if ( event.getRawSlot() < 0 )
			return;
		if ( event.getWhoClicked() instanceof Player ) {
			Player p = (Player) event.getWhoClicked();
			if ( state.containsKey(p.getName()) ) {
				TraderStatus trader = state.get(p.getName());
				InventoryTrait sr = trader.getTrader().getTrait(InventoryTrait.class);
				boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
				
				if ( !trader.getStatus().equals(Status.PLAYER_MANAGE) && top ) {
					StockItem si = null;
					
					if ( trader.getStatus().equals(Status.PLAYER_SELL) || trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) ) {
						if ( trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) )
							si = trader.getStockItem();
						else
							si = sr.itemForSell(event.getSlot());
						if ( si != null ) {
							if ( event.isShiftClick() ) {
								if ( si.hasMultipleAmouts() ) {
									if ( econ.has(p.getName(), si.getPrice(event.getSlot())) ) {
										if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) &&
											 !event.getCurrentItem().getType().equals(Material.AIR)) {
											econ.withdrawPlayer(p.getName(), si.getPrice(event.getSlot()));
											p.getInventory().addItem(event.getCurrentItem());
											p.sendMessage(ChatColor.GOLD + "You bought " + event.getCurrentItem().getAmount() + " for " + si.getPrice(event.getSlot()) + ".");
										}
									} else {
										p.sendMessage(ChatColor.GOLD + "You don't have enough money.");
									}
								} else {
									if ( econ.has(p.getName(), si.getPrice()) ) {
										econ.withdrawPlayer(p.getName(), si.getPrice());
										p.getInventory().addItem(si.getItemStack());
										p.sendMessage(ChatColor.GOLD + "You bought " + si.getItemStack().getAmount() + " for " + si.getPrice() + ".");
									} else {
										p.sendMessage(ChatColor.GOLD + "You don't have enough money.");
									}
								}
							} else {
								if ( trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) ) {
									if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
										trader.getInventory().clear();
										sr.inventoryView(trader.getInventory(),Status.PLAYER_SELL);
										trader.setStatus(Status.PLAYER_SELL);
										trader.setStockItem(null);
									} else {
										if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) &&
											 !event.getCurrentItem().getType().equals(Material.AIR) ) 
											p.sendMessage(ChatColor.GOLD + "This item costs " + si.getPrice(event.getSlot()) + ".");
									}
								} else if ( trader.getStatus().equals(Status.PLAYER_SELL) ) {
									if ( si.hasMultipleAmouts() ) {
										if ( trader.getStatus().equals(Status.PLAYER_SELL) ) {
											trader.getInventory().clear();
											InventoryTrait.setInventoryWith(trader.getInventory(), si);
											trader.setStatus(Status.PLAYER_SELL_AMOUT);
											trader.setStockItem(si);
										}
									} else {
										
											p.sendMessage(ChatColor.GOLD + "This item costs " + si.getPrice() + ".");
									}
								}
							} 
						} else if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)5)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
							trader.getInventory().clear();
							sr.inventoryView(trader.getInventory(),Status.PLAYER_BUY);
							trader.setStatus(Status.PLAYER_BUY);
							trader.setStockItem(null);
						} 
					} else if ( trader.getStatus().equals(Status.PLAYER_BUY) ) {
						si = sr.wantItemBuy(event.getSlot());
						if ( si != null ) {
							if ( si.getItemStack().getType().equals(event.getCursor().getType()) ) {
								econ.depositPlayer(p.getName(), si.getPrice()*event.getCursor().getAmount());
								p.sendMessage(ChatColor.GOLD + "You sold " + event.getCursor().getAmount() + " for " + si.getPrice(event.getSlot()) + ".");
								event.setCursor(new ItemStack(Material.AIR));
							} else {
								if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) &&
									 !event.getCurrentItem().getType().equals(Material.AIR) ) 
								p.sendMessage(ChatColor.GOLD + "You can get for this item " + si.getPrice(event.getSlot()) + " money.");
							}
						} else {
							if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
								trader.getInventory().clear();
								sr.inventoryView(trader.getInventory(),Status.PLAYER_SELL);
								trader.setStatus(Status.PLAYER_SELL);
								trader.setStockItem(null);
							}
						}
					}					
					event.setCancelled(true);
				} else { 
					if ( top ) {
						
						StockItem si = null;
						if ( trader.getStockItem() == null ) {
							si = sr.itemForSell(event.getSlot());
							trader.setStockItem( si );
						} else {
							trader.getStockItem().setSlot(event.getSlot());
							trader.setStockItem(null);
						}
						
					} else {
						event.setCancelled(true);
					}
				}
			} 
		}
	}
	



	@EventHandler
	public void inventoryClose(InventoryCloseEvent event){
	    if(state.containsKey(event.getPlayer().getName())){
			if ( state.get(event.getPlayer().getName()).getStatus().equals(Status.PLAYER_MANAGE)) {
				
			}
	        state.remove(event.getPlayer().getName());
	    }
	}
}
	


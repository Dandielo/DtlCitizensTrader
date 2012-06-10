package net.dtl.citizenstrader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.trait.Owner;
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
			if ( state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE_SELL) ) {
				if ( npc.getId() == state.get(p.getName()).getTrader().getId() )
					npc.getTrait(InventoryTrait.class).inventoryView(state.get(p.getName()).getInventory(), Status.PLAYER_MANAGE_SELL);
				else {
					p.sendMessage(ChatColor.RED + state.get(p.getName()).getTrader().getFullName() +": is currently managed!");
				}
			}
			p.openInventory(state.get(p.getName()).getInventory());
			
		} else {
			if ( !npc.getTrait(Owner.class).isOwnedBy(p.getName()) && !p.isOp() ) {
				p.sendMessage(ChatColor.RED + "Only the owner can manage this trader.");
				return;
			}
			if ( state.containsKey(p.getName()) && state.get(p.getName()).getTrader().getId() == npc.getId() ) {
				if ( !state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE_SELL) ) {
					state.get(p.getName()).setStatus(Status.PLAYER_MANAGE_SELL);
					p.sendMessage(ChatColor.RED + npc.getFullName() +": managing mode!");
				} else if ( state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE_SELL) ) { 
					state.get(p.getName()).setStatus(Status.PLAYER_SELL);
					p.sendMessage(ChatColor.RED + npc.getFullName() +": user mode!");
				}
			} else {
				state.put(p.getName(),new TraderStatus(npc,Status.PLAYER_MANAGE_SELL));
				p.sendMessage(ChatColor.RED + npc.getFullName() +": managing mode!");
			}
		}
			
		
	}
	
	@Override
    public void onSet(NPC npc) {
        if( !npc.hasTrait(InventoryTrait.class) ) {
            npc.addTrait(InventoryTrait.class);
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
			//	sr.loadInventory(trader.getTrader().getId());
				boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
				DecimalFormat f = new DecimalFormat("#.##");
				
				if ( (!trader.getStatus().equals(Status.PLAYER_MANAGE_SELL) && 
					  !trader.getStatus().equals(Status.PLAYER_MANAGE_BUY) && 
					  !trader.getStatus().equals(Status.PLAYER_MANAGE_SELL_AMOUT) ) && 
					  !trader.getStatus().equals(Status.PLAYER_MANAGE_PRICE ) && top ) {
					StockItem si = null;
					
					if ( trader.getStatus().equals(Status.PLAYER_SELL) || trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) ) {
						if ( trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) )
							si = trader.getStockItem();
						else
							si = sr.itemForSell(event.getSlot());
						if ( si != null ) {
							if ( event.isShiftClick() ) {
								if ( si.hasMultipleAmouts() && trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) ) {
									if ( econ.has(p.getName(), si.getPrice(event.getSlot())) ) {
										if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) &&
											 !event.getCurrentItem().getType().equals(Material.AIR)) {
											econ.withdrawPlayer(p.getName(), si.getPrice(event.getSlot()));
											p.getInventory().addItem(event.getCurrentItem());
											p.sendMessage(ChatColor.GOLD + "You bought " + event.getCurrentItem().getAmount() + " for " + f.format(si.getPrice(event.getSlot())) + ".");
										}
									} else {
										p.sendMessage(ChatColor.GOLD + "You don't have enough money.");
									}
								} else {
									if ( econ.has(p.getName(), si.getPrice()) ) {
										econ.withdrawPlayer(p.getName(), si.getPrice());
										p.getInventory().addItem(si.getItemStack());
										p.sendMessage(ChatColor.GOLD + "You bought " + si.getItemStack().getAmount() + " for " + f.format(si.getPrice()) + ".");
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
											p.sendMessage(ChatColor.GOLD + "This item costs " + f.format(si.getPrice(event.getSlot())) + ".");
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
										
											p.sendMessage(ChatColor.GOLD + "This item costs " + f.format(si.getPrice()) + ".");
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
							if ( si.getItemStack().getType().equals(event.getCursor().getType()) &&
								 si.getItemStack().getData().equals(event.getCursor().getData()) ) {
								econ.depositPlayer(p.getName(), si.getPrice()*event.getCursor().getAmount());
								p.sendMessage(ChatColor.GOLD + "You sold " + event.getCursor().getAmount() + " for " + f.format(si.getPrice()*event.getCursor().getAmount()) + ".");
								event.setCursor(new ItemStack(Material.AIR));
							} else {
								if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) &&
									 !event.getCurrentItem().getType().equals(Material.AIR)  ) 
									p.sendMessage(ChatColor.GOLD + "You get " + f.format(si.getPrice()) + " for this item.");
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
					if ( ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) || 
						 ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)5)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) ||
						 ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)15)) && ( event.getSlot() == trader.getInventory().getSize() - 2 ) )||
						 ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1)) && ( event.getSlot() == trader.getInventory().getSize() - 2 ) ) )
							event.setCancelled(true);
					StockItem si = null;
					if ( top ) {
						if ( event.isShiftClick() ) {
							if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL) ) {
								si = sr.itemForSell(event.getSlot());
								if ( si != null ) {
									trader.getInventory().clear();
									InventoryTrait.setInventoryWith(trader.getInventory(), si);
									trader.setStatus(Status.PLAYER_MANAGE_SELL_AMOUT);
									trader.setStockItem(si);
								} 
							} 
							if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1)) && 
								 ( event.getSlot() == trader.getInventory().getSize() - 2 ) ) {
								trader.setStatus(Status.PLAYER_MANAGE_PRICE);
							//	trader.getInventory().clear();
							//	sr.inventoryView(trader.getInventory(),Status.PLAYER_MANAGE_PRICE);
								trader.getInventory().setItem(trader.getInventory().getSize()-2, new ItemStack(Material.WOOL,1,(short)0,(byte)15));
							} else if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)5)) && 
									 ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
									trader.setStatus(Status.PLAYER_MANAGE_BUY);
									trader.getInventory().clear();
									sr.inventoryView(trader.getInventory(),Status.PLAYER_MANAGE_BUY);
								//	trader.getInventory().setItem(trader.getInventory().getSize()-2, new ItemStack(Material.WOOL,1,(short)0,(byte)15));
							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL_AMOUT) ) {
								sr.saveNewAmouts(trader.getInventory(), trader.getStockItem());
								trader.getInventory().clear();
								sr.inventoryView(trader.getInventory(),Status.PLAYER_MANAGE_SELL);
								trader.setStatus(Status.PLAYER_MANAGE_SELL);
								trader.setStockItem(null);
							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_BUY) ) {
								if ( sr.wantItemBuy(event.getSlot()) == null ) {
									if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1)) && 
										 ( event.getSlot() == trader.getInventory().getSize() - 2 ) ) {
										trader.setStatus(Status.PLAYER_MANAGE_PRICE);
									//	trader.getInventory().clear();
									//	sr.inventoryView(trader.getInventory(),Status.PLAYER_MANAGE_PRICE);
										trader.getInventory().setItem(trader.getInventory().getSize()-2, new ItemStack(Material.WOOL,1,(short)0,(byte)15));
									} else if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) && 
											 ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
										trader.getInventory().clear();
										sr.inventoryView(trader.getInventory(),Status.PLAYER_MANAGE_SELL);
										trader.setStatus(Status.PLAYER_MANAGE_SELL);
										trader.setStockItem(null);
									}
								}
							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_PRICE) ) {
								 if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)15)) && 
								      ( event.getSlot() == trader.getInventory().getSize() - 2 ) ) {
									if ( trader.getInventory().getItem(trader.getInventory().getSize()-1).equals(new ItemStack(Material.WOOL,1,(short)0,(byte)5)))
										trader.setStatus(Status.PLAYER_MANAGE_SELL);
									else
										trader.setStatus(Status.PLAYER_MANAGE_BUY);
									trader.getInventory().setItem(trader.getInventory().getSize()-2, new ItemStack(Material.WOOL,1));
							//		sr.inventoryView(trader.getInventory(),Status.PLAYER_MANAGE_SELL);
								}
							}

							event.setCancelled(true);
						} else {
							if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL) ) {
								if ( event.isRightClick() ) {
									p.sendMessage(ChatColor.GOLD + "Cannot right click here!");
									event.setCancelled(true);
									return;
								}
								if ( trader.getStockItem() == null ) {
									trader.setStockItem( sr.itemForSell(event.getSlot()) );
								} else {
									if ( trader.getStockItem().getSlot() < 0 ) {
										trader.getStockItem().getAmouts().clear();
										trader.getStockItem().addAmout(event.getCursor().getAmount());
										sr.addItem(true, trader.getStockItem());
									}
									StockItem item = trader.getStockItem();
									
									if ( !event.getCurrentItem().getType().equals(Material.AIR) )
										trader.setStockItem(sr.itemForSell(event.getSlot()));
									else
										trader.setStockItem(null);
									item.setSlot(event.getSlot());
								}
							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL_AMOUT) ) {
								if ( !event.getCursor().getType().equals(Material.AIR) &&
									 !( event.getCursor().getType().equals(trader.getStockItem().getItemStack().getType()) &&
									    event.getCursor().getData().equals(trader.getStockItem().getItemStack().getData()) ) ||
									 ( !event.getCurrentItem().getType().equals(trader.getStockItem().getItemStack().getType()) &&
									   !event.getCurrentItem().getType().equals(Material.AIR) ) ) {
									p.sendMessage(ChatColor.GOLD + "Wrong item!");
									event.setCancelled(true);
								}
							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_BUY) ) {
								if ( event.isRightClick() ) {
									p.sendMessage(ChatColor.GOLD + "Cannot right click here!");
									event.setCancelled(true);
									return;
								}
								if ( trader.getStockItem() == null ) {
									trader.setStockItem( sr.wantItemBuy(event.getSlot()) );
								} else {
									if ( trader.getStockItem().getSlot() < 0 ) {
										trader.getStockItem().getAmouts().clear();
										trader.getStockItem().addAmout(event.getCursor().getAmount());
										sr.addItem(false, trader.getStockItem());
									}
									StockItem item = trader.getStockItem();
									
									if ( !event.getCurrentItem().getType().equals(Material.AIR) )
										trader.setStockItem(sr.wantItemBuy(event.getSlot()));
									else
										trader.setStockItem(null);
									item.setSlot(event.getSlot());
								}
							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_PRICE) ) {
								if ( trader.getInventory().getItem(trader.getInventory().getSize()-1).equals(new ItemStack(Material.WOOL,1,(short)0,(byte)5)))
									si = sr.itemForSell(event.getSlot());
								else
									si = sr.wantItemBuy(event.getSlot());
						//		si = sr.itemForSell(event.getSlot());
					//			if ( si == null )
						//			si = sr.wantItemBuy(event.getSlot());
								if ( si != null ) {
									if ( event.isLeftClick() )
										si.increasePrice(this.getManagePriceAmout(event.getCursor()));
									else if ( event.isRightClick() ) 
										si.lowerPrice(this.getManagePriceAmout(event.getCursor()));
									p.sendMessage(ChatColor.GOLD + "New price: " + f.format(si.getPrice()/si.getAmouts().get(0)));
									event.setCancelled(true);
								} else 
									p.sendMessage(ChatColor.GOLD + "Wrong Item!");
							}
						}
						trader.setLastInv(true);
					} else {
						if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL) || trader.getStatus().equals(Status.PLAYER_MANAGE_BUY) ) {
							if ( trader.getLastInv() && trader.getStockItem() != null ) {
								sr.removeItem(true, trader.getStockItem().getSlot());
								trader.setStockItem(null);
							} else {
								ItemStack is = event.getCurrentItem();
								String itemInfo = is.getTypeId()+":"+ is.getData().getData() +" a:"+is.getAmount() + " d:" + is.getDurability();
								if ( !is.getEnchantments().isEmpty() ) {
									itemInfo += " e:";
									for ( Enchantment ench : is.getEnchantments().keySet() ) 
										itemInfo += ench.getId() + "/" + is.getEnchantmentLevel(ench) + ",";
								}
								trader.setStockItem(new StockItem(itemInfo));
							}
						} else {
						}
						trader.setLastInv(false);
					}
				}
			} 
		}
	}
	



	@EventHandler
	public void inventoryClose(InventoryCloseEvent event){
	    if(state.containsKey(event.getPlayer().getName())){
			if ( state.get(event.getPlayer().getName()).getStatus().equals(Status.PLAYER_SELL_AMOUT) ||
				 state.get(event.getPlayer().getName()).getStatus().equals(Status.PLAYER_SELL) ||
				 state.get(event.getPlayer().getName()).getStatus().equals(Status.PLAYER_BUY) ) 
				state.remove(event.getPlayer().getName());
			else {
				TraderStatus trader = state.get(event.getPlayer().getName());
				InventoryTrait sr = trader.getTrader().getTrait(InventoryTrait.class);
				if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL_AMOUT) ){
					sr.saveNewAmouts(trader.getInventory(), trader.getStockItem());
					trader.getInventory().clear();
					sr.inventoryView(trader.getInventory(),Status.PLAYER_SELL);
				}
				trader.setStatus(Status.PLAYER_MANAGE_SELL);
				trader.setStockItem(null);
			}
	    }
	}
	
	public double getManagePriceAmout(ItemStack is) {
		if ( is.getType().equals(Material.WOOD) )
			return is.getAmount()*0.01;		
		else if ( is.getType().equals(Material.LOG) )
			return is.getAmount()*0.1;
		else if ( is.getType().equals(Material.DIRT) )
			return is.getAmount()*10;		
		else if ( is.getType().equals(Material.COBBLESTONE) )
			return is.getAmount()*100;
		return is.getAmount();
	}
	
}
	


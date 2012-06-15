package net.dtl.citizenstrader_new.traders;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.containers.StockItem;
import net.dtl.citizenstrader_new.traits.TraderTrait;

public class ServerTrader extends Trader {

	public ServerTrader(NPC n, TraderTrait c) {
		super(n, c);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void secureMode(InventoryClickEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void simpleMode(InventoryClickEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void managerMode(InventoryClickEvent event) {
		
		//Going to hide this in the future as an CustomEvent, for developers also
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		Player p = (Player) event.getWhoClicked();
		DecimalFormat f = new DecimalFormat("#.##");
		
		if ( top ) {
			/*
			 * When the players click on the top (trader) inventory
			 * 
			 */
			setInventoryClicked(true);
			
			if ( event.getSlot() >= getInventory().getSize() - 2 ) {
				/*
				 * Wool checking, also removing a bug that allowed placing items for sell in the wool slots 
				 * 
				 */
				if ( isWool(event.getCurrentItem(),(byte)0) ) {
					/*
					 * Price managing enabled
					 * 
					 */
					setTraderStatus(TraderStatus.PLAYER_MANAGE_PRICE);
					getInventory().setItem(getInventory().getSize()-2, new ItemStack(Material.WOOL,1,(short)0,(byte)15));
					
				} else if ( isWool(event.getCurrentItem(),(byte)15) ) {
					/*
					 * Price managing disabled
					 * 
					 */
					if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)3) )
						setTraderStatus(TraderStatus.PLAYER_MANAGE_BUY);
					if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)5) )
						setTraderStatus(TraderStatus.PLAYER_MANAGE_SELL);
					
					getInventory().setItem(getInventory().getSize()-2, new ItemStack(Material.WOOL,1,(short)0,(byte)0));
					
					
				} else if ( isWool(event.getCurrentItem(),(byte)5) ) {
					
					switchInventory(TraderStatus.PLAYER_MANAGE_BUY);
					
					
				} else if ( isWool(event.getCurrentItem(),(byte)3) ) {
					
					switchInventory(TraderStatus.PLAYER_MANAGE_SELL);
					
					
				} else if ( isWool(event.getCurrentItem(),(byte)14) ) {
					
					saveManagedAmouts();
					switchInventory(TraderStatus.PLAYER_MANAGE_SELL);
					
					
				}
				
				event.setCancelled(true);
				
			} else {
				if ( event.isShiftClick() ) {
					/*
					 * Shift click => amount managing mode 
					 * 
					 */
					
					if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) ) { 
						if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() ) {
							switchInventory(getSelectedItem());
							setTraderStatus(TraderStatus.PLAYER_MANAGE_SELL_AMOUNT); 
						} 
					} 
					event.setCancelled(true);
				} else {
					/*
					 * Managing item amounts, slots and prices
					 * 
					 */
					 if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) ) {
						 /*
						  * Managing items in the sell mode
						  * 
						  */
						 if ( event.isRightClick() ) {
							 /*
							  * RightClick Currently nut supported
							  * 
							  */
							 p.sendMessage(ChatColor.GOLD + "Cannot right click here!");
							 event.setCancelled(true);
							 return;
						 }
						 if ( hasSelectedItem() ) {
							 /*
							  * Changing item slot or adding a new item to the trader inventory (sell mode)
							  * 
							  */
							 
							 StockItem item = getSelectedItem();
							 if ( item.getSlot() == -1 ) {
								 item.resetAmounts(event.getCursor().getAmount());
								 getTraderStock().addItem(true, item);
							//	 p.sendMessage(ChatColor.GOLD + "Item added sucessfully.");
							 }
							
							if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() )
								getSelectedItem().setSlot(-2);
							
							item.setSlot(event.getSlot());
						} else {
							if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() )
								getSelectedItem().setSlot(-2);
						}
						return;
					} else if ( getTraderStatus().equals(TraderStatus.PLAYER_MANAGE_SELL_AMOUNT) ) {
						/*
						 * Managing multiple amounts for an item
						 *  
						 */
						if ( !equalsSelected(event.getCursor(),true,false) && !event.getCursor().getType().equals(Material.AIR) ) {
							p.sendMessage(ChatColor.GOLD + "Wrong item!");
							event.setCancelled(true);
						}
						return;
					} else if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_BUY) ) {
						 /*
						  * Managing items in the sell mode
						  * 
						  */
						 if ( event.isRightClick() ) {
							 /*
							  * RightClick Currently nut supported
							  * 
							  */
							 p.sendMessage(ChatColor.GOLD + "Cannot right click here!");
							 event.setCancelled(true);
							 return;
						 }
						 if ( hasSelectedItem() ) {
							 
							 
							 /*
							  * Changing item slot or adding a new item to the trader inventory (sell mode)
							  * 
							  */
							 StockItem item = getSelectedItem();
							 if ( item.getSlot() == -1 ) {
								 item.resetAmounts(event.getCursor().getAmount());
								 getTraderStock().addItem(false, item);
							 }
							
							 if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() )
								getSelectedItem().setSlot(-2);
							
							item.setSlot(event.getSlot());
						} else {
							if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() );
								getSelectedItem().setSlot(-2);
						}
						return;
					} else if ( getTraderStatus().equals(TraderStatus.PLAYER_MANAGE_PRICE) ) {
						/*
						 * Managing prices for an item
						 * 
						 */
						if ( event.getCursor().getType().equals(Material.AIR) ) {
							if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)3) ) {
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() ) 
									p.sendMessage(ChatColor.GOLD + "Price: " + f.format(getSelectedItem().getRawPrice()) );
								
							} else if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)5) )
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() ) 
									p.sendMessage(ChatColor.GOLD + "Price: " + f.format(getSelectedItem().getRawPrice()) );
						} else {
							if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)3) ) {
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() ) {
									if ( event.isRightClick() ) 
										getSelectedItem().lowerPrice(calculatePrice(event.getCursor()));
									else
										getSelectedItem().increasePrice(calculatePrice(event.getCursor()));
									p.sendMessage(ChatColor.GOLD + "New price: " + f.format(getSelectedItem().getRawPrice()) );
								}
							} else if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)5) )
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() ) {
									if ( event.isRightClick() ) 
										getSelectedItem().lowerPrice(calculatePrice(event.getCursor()));
									else
										getSelectedItem().increasePrice(calculatePrice(event.getCursor()));
									p.sendMessage(ChatColor.GOLD + "New price: " + f.format(getSelectedItem().getRawPrice()) );
								}
						}
						event.setCancelled(true);
					}
				}
			} 
		} else {
			if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) || equalsTraderStatus(TraderStatus.PLAYER_MANAGE_BUY) ) {
				if ( getInventoryClicked() && hasSelectedItem() ) {
					//StockItem item = trader.getStockItem();
					if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) )
						//if ( sr.itemForSell(item.getSlot()).equals(item) )
						getTraderStock().removeItem(true, getSelectedItem().getSlot());
					if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_BUY) )
					//	if ( sr.wantItemBuy(item.getSlot()).equals(item) )
						getTraderStock().removeItem(false, getSelectedItem().getSlot());
							//sr.removeItem(false, trader.getStockItem().getSlot());
					selectItem(null);
				} else {
					selectItem(toStockItem(event.getCurrentItem()));
				}
			} 
			setInventoryClicked(false);
		}
	}

}

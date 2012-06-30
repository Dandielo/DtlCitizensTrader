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
		
		((Player)event.getWhoClicked()).sendMessage(ChatColor.RED+"SecureMode Inactive! Switch to simple mode!");
		event.setCancelled(true);
	}

	@Override
	public void simpleMode(InventoryClickEvent event) {
		
		/* 
		 * will vanish after i've madeUp the simpleMode 
		 * 
		 */
		
		Player p = (Player) event.getWhoClicked();
		DecimalFormat f = new DecimalFormat("#.##");
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( top ) {
			/*
			 * top is for mostly for the "BuyFromTraderEvents"
			 * 	
			 */

			
			if ( isManagementSlot(event.getSlot(), 1) ) {
				/*
				 * Standard wool place (last item slot)
				 * 
				 */
				
				if ( isWool(event.getCurrentItem(),(byte) 14) ) {
					/*
					 * lest go back to the main selling inventory ;)
					 * 
					 */
					switchInventory(TraderStatus.PLAYER_SELL);		
				} else if ( isWool(event.getCurrentItem(),(byte) 3) ) {
					/*
					 * lest go back to the main selling inventory ;)
					 * 
					 */
					switchInventory(TraderStatus.PLAYER_SELL);		
				} else if ( isWool(event.getCurrentItem(),(byte) 5) ) {
					/*
					 * lest go back to the main selling inventory ;)
					 * 
					 */
					switchInventory(TraderStatus.PLAYER_BUY);		
				}
			} else if ( equalsTraderStatus(TraderStatus.PLAYER_SELL) ) {
				/*
				 * Player is buying from the trader
				 * 
				 */
				if ( selectItem(event.getSlot(), TraderStatus.PLAYER_SELL).hasSelectedItem() ) {
					if ( getSelectedItem().hasMultipleAmouts() ) {
						/*
						 * Switching to the amount select inventory
						 * 
						 */
						switchInventory(getSelectedItem());
						setTraderStatus(TraderStatus.PLAYER_SELL_AMOUNT);
					} else {
						if ( getClickedSlot() == event.getSlot() ) {
							/*
							 * This will trigger if some1 will click more than 1 amount on the same item  
							 * in the trader inventory
							 * 
							 */
							if ( inventoryHasPlace(p,0) && buyTransaction(p,getSelectedItem().getPrice()) ) {
								p.sendMessage(ChatColor.GOLD + "You bought " + getSelectedItem().getAmount() + " for " + f.format(getSelectedItem().getPrice()) + ".");
								
								/* *
								 * better version of Inventory.addItem();
								 * 
								 */
								addSelectedToInventory(p,0);
								
								/* *
								 * needs to be recoded
								 * 
								 */
								updateLimits(p.getName());
							} else 
								p.sendMessage(ChatColor.GOLD + "You don't have enough money or space.");
						} else {
							/*
							 * First click will display the price and instructions.
							 * Future: language support
							 * 
							 */
							p.sendMessage(ChatColor.GOLD + "This item costs " + f.format(getSelectedItem().getPrice()) + ".");
							p.sendMessage(ChatColor.GOLD + "Now click to buy it.");
							setClickedSlot(event.getSlot());
						}
					}
				}
			} else if ( equalsTraderStatus(TraderStatus.PLAYER_SELL_AMOUNT) ) {
				if ( !event.getCurrentItem().getType().equals(Material.AIR) ) {
					if ( getClickedSlot() == event.getSlot() ) { 
						if ( inventoryHasPlace(p,event.getSlot()) && buyTransaction(p,getSelectedItem().getPrice(event.getSlot())) ) {
							p.sendMessage(ChatColor.GOLD + "You bought " + getSelectedItem().getAmount(event.getSlot()) + " for " + f.format(getSelectedItem().getPrice(event.getSlot())) + ".");
							
							/* *
							 * better version of Inventory.addItem();
							 * 
							 */
							addSelectedToInventory(p,event.getSlot());
							
							/* *
							 * needs to be recoded
							 * 
							 */
							updateLimits(p.getName(),event.getSlot());
							
						} else
							p.sendMessage(ChatColor.GOLD + "You don't have enough money or space.");
					} else {
						p.sendMessage(ChatColor.GOLD + "This item costs " + f.format(getSelectedItem().getPrice(event.getSlot())) + ".");
						p.sendMessage(ChatColor.GOLD + "Click a second time to buy it.");
						setClickedSlot(event.getSlot());
					}
				}
			} else if ( equalsTraderStatus(TraderStatus.PLAYER_BUY) ) {
				if ( selectItem(event.getSlot(), TraderStatus.PLAYER_BUY).hasSelectedItem() ) {
					
					p.sendMessage(ChatColor.GOLD + "You get " + f.format(getSelectedItem().getPrice()) + " for this item.");
				
				}
			}
			setInventoryClicked(true);
		} else {
			/* *
			 * change the comparing (lesser it)
			 * 
			 */
			if ( equalsTraderStatus(TraderStatus.PLAYER_BUY) ) {
				if ( selectItem(event.getCurrentItem(),TraderStatus.PLAYER_BUY,true,true).hasSelectedItem() ) {
					if ( getClickedSlot() == event.getSlot() && !getInventoryClicked() ) {

						if ( checkLimits(p.getName()) && sellTransaction(p,getSelectedItem().getPrice()) ) {//*event.getCurrentItem().getAmount()
							p.sendMessage(ChatColor.GOLD + "You sold " + getSelectedItem().getAmount() + " for " + f.format(getSelectedItem().getPrice()) + ".");
							
							/* *
							 * needs to be recoded
							 * 
							 * #Recoded (not tested)
							 * 
							 */
							updateLimits(p.getName());

							/* *
							 * need to create removeFromInventory fnc (code cleanup)
							 * 
							 */
							if ( event.getCurrentItem().getAmount()-getSelectedItem().getAmount() > 0 )
								event.getCurrentItem().setAmount(event.getCurrentItem().getAmount()-getSelectedItem().getAmount());
							else 
								event.setCurrentItem(new ItemStack(Material.AIR));
						} else 
							p.sendMessage(ChatColor.GOLD + "Can't sell it");
					} else {
						p.sendMessage(ChatColor.GOLD + "You get " + f.format(getSelectedItem().getPrice()) + " for this item.");
						p.sendMessage(ChatColor.GOLD + "Click a second time to sell it.");
						setClickedSlot(event.getSlot());
					}
				}
			} else if ( selectItem(event.getCurrentItem(),TraderStatus.PLAYER_BUY,true,true).hasSelectedItem() ) {
				if ( getClickedSlot() == event.getSlot() && !getInventoryClicked() ) {
					
					if ( checkLimits(p.getName()) &&  sellTransaction(p,getSelectedItem().getPrice()) ) {
						p.sendMessage(ChatColor.GOLD + "You sold " + getSelectedItem().getAmount() + " for " + f.format(getSelectedItem().getPrice()) + ".");
						
						/* *
						 * needs to be recoded
						 * 
						 * #Recoded (not tested)
						 * 
						 */
						updateLimits(p.getName());
						
						/* *
						 * need to create removeFromInventoryFunction (code cleanup)
						 * 
						 */
						if ( event.getCurrentItem().getAmount()-getSelectedItem().getAmount() > 0 )
							event.getCurrentItem().setAmount(event.getCurrentItem().getAmount()-getSelectedItem().getAmount());
						else 
							event.setCurrentItem(new ItemStack(Material.AIR));
					}
					else 
						p.sendMessage(ChatColor.GOLD + "Can't sell it");
				} else {
					if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) &&
						 !event.getCurrentItem().getType().equals(Material.AIR) ) {
						p.sendMessage(ChatColor.GOLD + "You get " + f.format(getSelectedItem().getPrice()) + " for this item.");
						p.sendMessage(ChatColor.GOLD + "Click a second time to sell it.");
						setClickedSlot(event.getSlot());
					}
				}
			}
			setInventoryClicked(false);
		}
		event.setCancelled(true);
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
			
			if ( isManagementSlot(event.getSlot(), 3) ) {
				/*
				 * Wool checking, also removing a bug that allowed placing items for sell in the wool slots 
				 * 
				 */
				if ( isWool(event.getCurrentItem(),(byte)0) && event.getSlot() == getInventory().getSize() - 2 ) {
					/*
					 * Price managing enabled
					 * 
					 */
					setTraderStatus(TraderStatus.PLAYER_MANAGE_PRICE);
					
					/*
					 * WoolChanging
					 * 
					 */
					getInventory().setItem(getInventory().getSize()-2, new ItemStack(Material.WOOL,1,(short)0,(byte)15));
					getInventory().setItem(getInventory().getSize()-3, new ItemStack(Material.AIR));
					
				} else if ( isWool(event.getCurrentItem(),(byte)15) ) {
					/*
					 * Price managing disabled
					 * restoring the proper managing mode
					 * 
					 */
					if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)3) )
						setTraderStatus(TraderStatus.PLAYER_MANAGE_BUY);
					if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)5) )
						setTraderStatus(TraderStatus.PLAYER_MANAGE_SELL);
					
					/*
					 * WoolChanging
					 * 
					 */
					getInventory().setItem(getInventory().getSize()-2, new ItemStack(Material.WOOL,1,(short)0,(byte)0));
					getInventory().setItem(getInventory().getSize()-3, new ItemStack(Material.WOOL,1,(short)0,(byte)0));
					
					
				} else if ( isWool(event.getCurrentItem(),(byte)0) && event.getSlot() == getInventory().getSize() - 3 ) {
					/*
					 * Price managing enabled
					 * 
					 */
					setTraderStatus(TraderStatus.PLAYER_MANAGE_LIMIT);
					
					/*
					 * WoolChanging
					 * 
					 */
					getInventory().setItem(getInventory().getSize()-3, new ItemStack(Material.WOOL,1,(short)0,(byte)13));
					getInventory().setItem(getInventory().getSize()-2, new ItemStack(Material.AIR));
					
				} else if ( isWool(event.getCurrentItem(),(byte)13) ) {
					/*
					 * Price managing disabled
					 * restoring the proper managing mode
					 * 
					 */
					if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)3) )
						setTraderStatus(TraderStatus.PLAYER_MANAGE_BUY);
					if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)5) )
						setTraderStatus(TraderStatus.PLAYER_MANAGE_SELL);
					
					/*
					 * WoolChanging
					 * 
					 */
					getInventory().setItem(getInventory().getSize()-3, new ItemStack(Material.WOOL,1,(short)0,(byte)0));
					getInventory().setItem(getInventory().getSize()-2, new ItemStack(Material.WOOL,1,(short)0,(byte)0));
					
					
				} else if ( isWool(event.getCurrentItem(),(byte)5) ) {
					
					/*
					 * Switching to the BuyModeManagement
					 * ( player sells to trader )
					 * 
					 */
					switchInventory(TraderStatus.PLAYER_MANAGE_BUY);
					
					
				} else if ( isWool(event.getCurrentItem(),(byte)3) ) {
					
					/*
					 * Switching to the SellModeManagement
					 * ( player buys from trader )
					 * 
					 */
					switchInventory(TraderStatus.PLAYER_MANAGE_SELL);
					
					
				} else if ( isWool(event.getCurrentItem(),(byte)14) ) {
					
					/*
					 * Leaving the amount management 
					 * 
					 */
					saveManagedAmouts();
					switchInventory(TraderStatus.PLAYER_MANAGE_SELL);
				}
				
				event.setCancelled(true);
				
			} else {
				if ( event.isShiftClick() ) {
					/*
					 * Entering amount managing mode 
					 * 
					 */
					if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_LIMIT) ) {
						/*
						 * Managing limits for an item
						 * 
						 */
						if ( event.getCursor().getType().equals(Material.AIR) ) {
							/*
							 * Display Limits if nothing is set in the cursor
							 * 
							 */
							if ( isBuyModeByWool() ) {
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() ) 
										p.sendMessage(ChatColor.GOLD + "Timeout: " + getSelectedItem().getLimitSystem().getGlobalTimeout() );
								
							} else if ( isSellModeByWool() )
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() ) 
										p.sendMessage(ChatColor.GOLD + "Timeout: " + getSelectedItem().getLimitSystem().getGlobalTimeout() );
						} else {
							/*
							 * Change prices and display them after the change
							 * 
							 */
							if ( isBuyModeByWool() ) {
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() ) {
									if ( event.isRightClick() ) 
										getSelectedItem().getLimitSystem().changeGlobalTimeout(-calculateTimeout(event.getCursor()));
									else 
										getSelectedItem().getLimitSystem().changeGlobalTimeout(calculateTimeout(event.getCursor()));
									p.sendMessage(ChatColor.GOLD + "New timeout: " + getSelectedItem().getLimitSystem().getGlobalTimeout() );
								}
							} else if ( isSellModeByWool() )
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() ) {
									if ( event.isRightClick() ) 
										getSelectedItem().getLimitSystem().changeGlobalTimeout(-calculateTimeout(event.getCursor()));
									else
										getSelectedItem().getLimitSystem().changeGlobalTimeout(calculateTimeout(event.getCursor()));
									p.sendMessage(ChatColor.GOLD + "New timeout: " + getSelectedItem().getLimitSystem().getGlobalTimeout() );
								}
						}
						event.setCancelled(true);
						return;
					}
					 
					if ( event.isLeftClick() ) {
						if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) ) { 
							if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() ) {
								switchInventory(getSelectedItem());
								setTraderStatus(TraderStatus.PLAYER_MANAGE_SELL_AMOUNT); 
							} 
						} 
						
					/* *
					 * Future putting this into the price section
					 */
					} else {
						if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) ) { 
							if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() ) {
								if ( getSelectedItem().hasStackPrice() ) {
									getSelectedItem().setStackPrice(true);
									p.sendMessage(ChatColor.GOLD + "StackPrice disabled for this item.");
								} else {
									getSelectedItem().setStackPrice(true);
									p.sendMessage(ChatColor.GOLD + "StackPrice enabled for this item.");
								}
							}
						} else if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_BUY) ) {
							if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() ) {
								if ( getSelectedItem().hasStackPrice() ) {
									getSelectedItem().setStackPrice(true);
									p.sendMessage(ChatColor.GOLD + "StackPrice disabled for this item.");
								} else {
									getSelectedItem().setStackPrice(true);
									p.sendMessage(ChatColor.GOLD + "StackPrice enabled for this item.");
								}
							}
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
							  * RightClick currently not supported
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
								 /*
								  * if the slot equals -1 then it's a new item
								  * that should be added to the trader inventory
								  * 
								  * amounts reset, to be sure the item will have his amount
								  * from the cursor item
								  */
								 item.resetAmounts(event.getCursor().getAmount());
								 getTraderStock().addItem(true, item);
							 }
							 
							 /*
							  * Select a trader item and check if it exists
							  * if true set his slot to -2, (Item Editing)
							  */
							 if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() )
								 getSelectedItem().setSlot(-2);
							
							 /*
							  * Setting the slot for the current placed item
							  * 
							  */
							 item.setSlot(event.getSlot());
						} else {
							/*
							 * Select a trader item and check if it exists
							 * if true set his slot to -2, (Item Editing)
							 */
							if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() )
								getSelectedItem().setSlot(-2);
						}
						return;
					} else if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL_AMOUNT) ) {
						/*
						 * Managing multiple amounts for an item
						 *  
						 */
						if ( !equalsSelected(event.getCursor(),true,false) && !event.getCursor().getType().equals(Material.AIR) ) {
							/*
							 * The item placed in the amount selection window must have the same id, data and have less durability lost 
							 * than the item that will be set for sale
							 */
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
								/*
								 * if the slot equals -1 then it's a new item
								 * that should be added to the trader inventory
								 * 
								 * amounts reset, to be sure the item will have his amount
								 * from the cursor item
								 */
								item.resetAmounts(event.getCursor().getAmount());
								getTraderStock().addItem(false, item);
							}

							/*
							 * Select a trader item and check if it exists
							 * if true set his slot to -2, (Item Editing)
							 */
							if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() )
								getSelectedItem().setSlot(-2);
							/*
							 * Setting the slot for the current placed item
							 * 
							 */
							item.setSlot(event.getSlot());
						} else {
							/*
							 * Select a trader item and check if it exists
							 * if true set his slot to -2, (Item Editing)
							 */
							if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() );
								getSelectedItem().setSlot(-2);
						}
						return;
					} else if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_PRICE) ) {
						/*
						 * Managing prices for an item
						 * 
						 */
						if ( event.getCursor().getType().equals(Material.AIR) ) {
							/*
							 * Display Prices if nothing is set in the cursor
							 * 
							 */
							if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)3) ) {
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() ) 
									p.sendMessage(ChatColor.GOLD + "Price: " + f.format(getSelectedItem().getRawPrice()) );
								
							} else if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)5) )
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() ) 
									p.sendMessage(ChatColor.GOLD + "Price: " + f.format(getSelectedItem().getRawPrice()) );
						} else {
							/*
							 * Change prices and display them after the change
							 * 
							 */
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
					} else if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_LIMIT) ) {
						/*
						 * Managing limits for an item
						 * 
						 */
						if ( event.getCursor().getType().equals(Material.AIR) ) {
							/*
							 * Display Limits if nothing is set in the cursor
							 * 
							 */
							if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)3) ) {
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() )
										p.sendMessage(ChatColor.GOLD + "Limit: " + getSelectedItem().getLimitSystem().getGlobalLimit() );
								
							} else if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)5) )
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() )
										p.sendMessage(ChatColor.GOLD + "Limit: " + getSelectedItem().getLimitSystem().getGlobalLimit() );
						} else {
							/*
							 * Change prices and display them after the change
							 * 
							 */
							if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)3) ) {
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_BUY).hasSelectedItem() ) {
									if ( event.isRightClick() ) 
										getSelectedItem().getLimitSystem().changeGlobalLimit(-calculateLimit(event.getCursor()));
									else
										getSelectedItem().getLimitSystem().changeGlobalLimit(calculateLimit(event.getCursor()));
									p.sendMessage(ChatColor.GOLD + "New limit: " + getSelectedItem().getLimitSystem().getGlobalLimit() );
								}
							} else if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)5) )
								if ( selectItem(event.getSlot(),TraderStatus.PLAYER_MANAGE_SELL).hasSelectedItem() ) {
									if ( event.isRightClick() ) 
										getSelectedItem().getLimitSystem().changeGlobalLimit(-calculateLimit(event.getCursor()));
									else
										getSelectedItem().getLimitSystem().changeGlobalLimit(calculateLimit(event.getCursor()));
									p.sendMessage(ChatColor.GOLD + "New Limit: " + getSelectedItem().getLimitSystem().getGlobalLimit() );
								}
						}
						event.setCancelled(true);
					}
				} 
			} 
		} else {
			if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) || equalsTraderStatus(TraderStatus.PLAYER_MANAGE_BUY) ) {
				if ( getInventoryClicked() && hasSelectedItem() ) {
					/*
					 * Remove an item from the trader inventory
					 * 
					 */
					if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) )
						getTraderStock().removeItem(true, getSelectedItem().getSlot());
					if ( equalsTraderStatus(TraderStatus.PLAYER_MANAGE_BUY) )
						getTraderStock().removeItem(false, getSelectedItem().getSlot());
					selectItem(null);
				} else {
					/*
					 * Select an item to add it to the trader inventory
					 * 
					 */
					selectItem(toStockItem(event.getCurrentItem()));
				}
			} 
			setInventoryClicked(false);
		}
	}
}


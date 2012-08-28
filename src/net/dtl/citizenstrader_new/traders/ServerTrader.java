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
		int slot = event.getSlot();
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( top ) {
			/*
			 * top is for mostly for the "BuyFromTraderEvents"
			 * 	
			 */

			
			if ( isManagementSlot(slot, 1) ) {
				/*
				 * Standard wool place (last item slot)
				 * 
				 */
				
				if ( isWool(event.getCurrentItem(),config.getItemManagement(7)) ) {
					/*
					 * lest go back to the main selling inventory ;)
					 * 
					 */
					switchInventory(TraderStatus.SELL);		
				} else if ( isWool(event.getCurrentItem(), config.getItemManagement(0)) ) {
					/*
					 * lest go back to the main selling inventory ;)
					 * 
					 */
					switchInventory(TraderStatus.SELL);		
				} else if ( isWool(event.getCurrentItem(), config.getItemManagement(1)) ) {
					/*
					 * lest go to the buy inventory ;)
					 * 
					 */
					switchInventory(TraderStatus.BUY);		
				}
			} else if ( equalsTraderStatus(TraderStatus.SELL) ) {
				/*
				 * Player is buying from the trader
				 * 
				 */
				if ( selectItem(slot, TraderStatus.SELL).hasSelectedItem() ) {
					if ( getSelectedItem().hasMultipleAmouts() ) {
						/*
						 * Switching to the amount select inventory
						 * 
						 */
						switchInventory(getSelectedItem());
						setTraderStatus(TraderStatus.SELL_AMOUNT);
					} else {
						if ( getClickedSlot() == slot ) {
							/*
							 * This will trigger if some1 will click more than 1 amount on the same item  
							 * in the trader inventory
							 * 
							 */
							if ( checkLimits(p) && inventoryHasPlace(p,0) && buyTransaction(p,getSelectedItem().getPrice()) ) {
								p.sendMessage(locale.getMessage("buy-message").replace("{amount}", "" + getSelectedItem().getAmount() ).replace("{price}", f.format(getSelectedItem().getPrice()) ) );
								
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
								
								//logging
								log("buy", 
									p.getName(), 
									getSelectedItem().getItemStack().getTypeId(),
									getSelectedItem().getItemStack().getData().getData(), 
									getSelectedItem().getAmount(), 
									getSelectedItem().getPrice() );
								
								
							} else 
								p.sendMessage(locale.getMessage("transaction-falied"));
						} else {
							/*
							 * First click will display the price and instructions.
							 * Future: language support
							 * 
							 */
							p.sendMessage( locale.getMessage("price-message").replace("{price}", f.format(getSelectedItem().getPrice()) ) );
							p.sendMessage( locale.getMessage("click-to-continue").replace("{transaction}", "buy") );
							setClickedSlot(slot);
						}
					}
				}
			} else if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) ) {
				if ( !event.getCurrentItem().getType().equals(Material.AIR) ) {
					if ( getClickedSlot() == slot ) { 
						if ( checkLimits(p,slot) && inventoryHasPlace(p,slot) && buyTransaction(p,getSelectedItem().getPrice(slot)) ) {
							p.sendMessage(locale.getMessage("buy-message").replace("{amount}", "" + getSelectedItem().getAmount(slot) ).replace("{price}", f.format(getSelectedItem().getPrice(slot)) ) );
							
							/* *
							 * better version of Inventory.addItem();
							 * 
							 */
							addSelectedToInventory(p,slot);
							
							/* *
							 * needs to be recoded
							 * 
							 */
							updateLimits(p.getName(),slot);
							
							//logging
							log("buy", 
								p.getName(), 
								getSelectedItem().getItemStack().getTypeId(),
								getSelectedItem().getItemStack().getData().getData(), 
								getSelectedItem().getAmount(slot), 
								getSelectedItem().getPrice(slot) );
							
						} else
							p.sendMessage( locale.getMessage("transaction-falied") );
					} else {
						p.sendMessage( locale.getMessage("price-message").replace("{price}", f.format(getSelectedItem().getPrice(slot)) ) );
						p.sendMessage( locale.getMessage("click-to-continue").replace("{transaction}", "buy") );
						setClickedSlot(slot);
					}
				}
			} else if ( equalsTraderStatus(TraderStatus.BUY) ) {
				if ( selectItem(slot, TraderStatus.BUY).hasSelectedItem() ) {
					
					p.sendMessage( locale.getMessage("price-message").replace("{price}", f.format(getSelectedItem().getPrice()) ) );
				
				}
			}
			setInventoryClicked(true);
		} else {
			/* *
			 * change the comparing (lesser it)
			 * 
			 */
			if ( equalsTraderStatus(TraderStatus.BUY) ) 
			{
				if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true,true).hasSelectedItem() )
				{
					if ( getClickedSlot() == slot && !getInventoryClicked() ) {
						int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
						if ( checkBuyLimits(p, scale) && sellTransaction(p,getSelectedItem().getPrice(),event.getCurrentItem()) ) {//*event.getCurrentItem().getAmount()
						//	int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
							p.sendMessage( locale.getMessage("sell-message").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(getSelectedItem().getPrice()*scale) ) );
							
							/* *
							 * needs to be recoded
							 * 
							 * #Recoded (not tested)
							 * 
							 */
							updateBuyLimits(p.getName(),scale);

							/* *
							 * need to create removeFromInventory fnc (code cleanup)
							 * 
							 *//*
							if ( event.getCurrentItem().getAmount()-getSelectedItem().getAmount() > 0 )
								event.getCurrentItem().setAmount(event.getCurrentItem().getAmount()-getSelectedItem().getAmount());
							else 
								event.setCurrentItem(new ItemStack(Material.AIR));*/
							removeFromInventory(event.getCurrentItem(),event);
							
							//logging
							log("sell", 
								p.getName(), 
								getSelectedItem().getItemStack().getTypeId(),
								getSelectedItem().getItemStack().getData().getData(), 
								getSelectedItem().getAmount()*scale, 
								getSelectedItem().getPrice()*scale );
							
						} else 
							p.sendMessage( locale.getMessage("transaction-falied") );
					} else {
						p.sendMessage( locale.getMessage("price-message").replace("{price}", f.format(getSelectedItem().getPrice()*((int)event.getCurrentItem().getAmount() / getSelectedItem().getAmount())) ) );
						p.sendMessage( locale.getMessage("click-to-continue").replace("{transaction}", "sell") );
						setClickedSlot(slot);
					}
				}
			} else if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) ) { 
				p.sendMessage( locale.getMessage("amount-exception") );
				event.setCancelled(true);
				return;
			} else if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true,true).hasSelectedItem() ) {
				if ( getClickedSlot() == slot && !getInventoryClicked() ) {
					int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
					if ( checkBuyLimits(p, scale) && sellTransaction(p,getSelectedItem().getPrice(),event.getCurrentItem()) ) {
					//	int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
						p.sendMessage( locale.getMessage("sell-message").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(getSelectedItem().getPrice()*scale) ) );
						
						/* *
						 * needs to be recoded
						 * 
						 * #Recoded (not tested)
						 * 
						 */
						//if ( !updateLimitsTem(p.getName(),event.getCurrentItem()) )
							//updateLimits(p.getName());
						
						//limits update
						updateBuyLimits(p.getName(),scale);
						
						//inventory cleanup
						removeFromInventory(event.getCurrentItem(),event);
						
						//logging
						log("buy", 
							p.getName(), 
							getSelectedItem().getItemStack().getTypeId(),
							getSelectedItem().getItemStack().getData().getData(), 
							getSelectedItem().getAmount()*scale, 
							getSelectedItem().getPrice()*scale );
					//	}
					}
					else 
						p.sendMessage( locale.getMessage("transaction-falied") );
				} else {
					if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) &&
						 !event.getCurrentItem().getType().equals(Material.AIR) ) {
						p.sendMessage( locale.getMessage("price-message").replace("{price}", f.format(getSelectedItem().getPrice()*((int)event.getCurrentItem().getAmount() / getSelectedItem().getAmount())) ) );
						p.sendMessage( locale.getMessage("click-to-continue").replace("{transaction}", "sell") );
						
						setClickedSlot(slot);
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
		int slot = event.getSlot();		
		
		DecimalFormat f = new DecimalFormat("#.##");
		
		if ( top ) {
			/*
			 * When the players click on the top (trader) inventory
			 * 
			 */
			setInventoryClicked(true);

			// Wool checking, also removing a bug that allowed placing items for sell in the wool slots 
			if ( isManagementSlot(slot, 3) ) {
				
				
				//price managing
				if ( isWool(event.getCurrentItem(), config.getItemManagement(2)) ) {
					setTraderStatus(TraderStatus.MANAGE_PRICE);

					//wool updating
					getInventory().setItem(getInventory().getSize()-2, config.getItemManagement(6));
					getInventory().setItem(getInventory().getSize()-3, new ItemStack(Material.AIR));
					
					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "price") );
					
				} 
				else 
				//is any mode used? return to item adding
				if ( isWool(event.getCurrentItem(), config.getItemManagement(6)) ) 
				{
					//restore inventory
					//if ( isWool(getInventory().getItem(getInventory().getSize()-1), config.getItemManagement(7)) )
					//	setTraderStatus(TraderStatus.MANAGE_BUY);
					//if ( isWool(getInventory().getItem(getInventory().getSize()-1),(byte)5) )
					setTraderStatus(getBasicManageModeByWool());
					
					//restore wool
					getInventory().setItem(getInventory().getSize()-2, config.getItemManagement(2));
					getInventory().setItem(getInventory().getSize()-3, config.getItemManagement(4));

					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "item") );
					
				}
				else 
				//global limits management
				if ( isWool(event.getCurrentItem(), config.getItemManagement(4)) ) {

					//status update
					setTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL);
					
					//wool update
					getInventory().setItem(getInventory().getSize()-3, config.getItemManagement(6));
					getInventory().setItem(getInventory().getSize()-2, config.getItemManagement(5));

					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "global limit") );
					
				} 
				else 
				//player limits management
				if ( isWool(event.getCurrentItem(),config.getItemManagement(5)) ) 
				{
					
					//status update
					setTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER);
					
					//wool update
					getInventory().setItem(getInventory().getSize()-2, config.getItemManagement(4));

					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "player limit") );
					
				}
				else
				//buy mode
				if ( isWool(event.getCurrentItem(), config.getItemManagement(1)) ) 
				{
					
					//inventory and status update
					switchInventory(TraderStatus.MANAGE_BUY);
					
					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "buy") );
					
				} 
				else 
				//sell mode
				if ( isWool(event.getCurrentItem(), config.getItemManagement(0)) ) 
				{

					//inventory and status update
					switchInventory(TraderStatus.MANAGE_SELL);

					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "sell") );
					
				} 
				else 
				//leaving the amount managing
				if ( isWool(event.getCurrentItem(), config.getItemManagement(7)) ) {

					//update amounts and status
					saveManagedAmouts();
					switchInventory(TraderStatus.MANAGE_SELL);
					
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "item") );
					
				}
				
				event.setCancelled(true);
				
			} 
			else
			{
				//is shift clicked?
				//amount and limit timeout managing
				if ( event.isShiftClick() )
				{
					
					//Managing global timeout limits for an item
					if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL) )
					{

						//show the current limit
						if ( event.getCursor().getType().equals(Material.AIR) ) 
						{
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
								p.sendMessage(locale.getMessage("show-timeout").replace("{timeout}", "" + getSelectedItem().getLimitSystem().getGlobalTimeout()).replace("{type}", "Global") );
								
							
						}
						//timeout changing
						else
						{
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								
								if ( event.isRightClick() ) 
								{
									getSelectedItem().getLimitSystem().changeGlobalTimeout(-calculateTimeout(event.getCursor()));
								}
								else
								{
									getSelectedItem().getLimitSystem().changeGlobalTimeout(calculateTimeout(event.getCursor()));
								}
								
								p.sendMessage(locale.getMessage("change-timeout").replace("{timeout}", "" + getSelectedItem().getLimitSystem().getGlobalTimeout()).replace("{type}", "Global") );
							}

						}
						
						event.setCancelled(true);
						return;
					}
					
					if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER) ) 
					{

						//show the current limit
						if ( event.getCursor().getType().equals(Material.AIR) ) 
						{
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
								p.sendMessage(locale.getMessage("show-timeout").replace("{timeout}", "" + getSelectedItem().getLimitSystem().getPlayerTimeout()).replace("{type}", "Player") );
								
							
						}
						//timeout changing
						else
						{
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								
								if ( event.isRightClick() ) 
								{
									getSelectedItem().getLimitSystem().changePlayerTimeout(-calculateTimeout(event.getCursor()));
								}
								else
								{
									getSelectedItem().getLimitSystem().changePlayerTimeout(calculateTimeout(event.getCursor()));
								}
								
								p.sendMessage(locale.getMessage("change-timeout").replace("{timeout}", "" + getSelectedItem().getLimitSystem().getPlayerTimeout()).replace("{type}", "Player") );
							}

						}
						
						//reset the selected item
						selectItem(null);
						
						event.setCancelled(true);
						return;
					}
					
					
					//amount managing
					if ( event.isLeftClick() )
					{
						if ( equalsTraderStatus(TraderStatus.MANAGE_SELL) )
						{ 
							//we got sell managing?
							if ( selectItem(slot,TraderStatus.MANAGE_SELL).hasSelectedItem() )
							{
								//inventory and status update
								switchInventory(getSelectedItem());
								setTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT); 
							} 
						} 
					} 
					//nothing to do with the shift r.click...
					else
					{
						
					}
					event.setCancelled(true);
					
				} 
				else 
				//manager handling
				{
					
					 //items managing
					 if ( equalsTraderStatus(getBasicManageModeByWool()) ) {
						 
						 //r.click = stack price
						 if ( event.isRightClick() )
						 {

							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
							{
								if ( getSelectedItem().hasStackPrice() ) {
									getSelectedItem().setStackPrice(false);
									p.sendMessage( locale.getMessage("stackprice-toggle").replace("{value}", "disabled") );
								} else {
									getSelectedItem().setStackPrice(true);
									p.sendMessage( locale.getMessage("stackprice-toggle").replace("{value}", "enabled") );
								}
								
							}

							//reset the selection
							selectItem(null);
							
							//cancel the event
							event.setCancelled(true);
							return;
						 }
						 if ( hasSelectedItem() ) {
							 //if we got an selected item (new or old)
							 
							 StockItem item = getSelectedItem();
							 
							 //this item is new!
							 if ( item.getSlot() == -1 ) {

								 //get the real amount
								 item.resetAmounts(event.getCursor().getAmount());
								 
								 //set the item to the stock
								 if ( this.isBuyModeByWool() )
									 getTraderStock().addItem(false, item);
								 if ( this.isSellModeByWool() )
									 getTraderStock().addItem(true, item);
							 }
							 
							 //select an item if it exists in the traders inventory
							 if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
							 {
								 getSelectedItem().setSlot(-2);
								 p.sendMessage( locale.getMessage("item-selected") );
							 }
							 
							 //set the managed items slot
							 item.setSlot(slot);
						} else {

							 //select an item if it exists in the traders inventory
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
							{
								getSelectedItem().setSlot(-2);
								p.sendMessage( locale.getMessage("item-selected") );
							}
							
						}
						return;
					} 
					else
					//managing multiple amounts
					if ( equalsTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT) )
					{
						
						//is item id and data equal?
						if ( !equalsSelected(event.getCursor(),true,false) 
								&& !event.getCursor().getType().equals(Material.AIR) ) {

							//invalid item
							p.sendMessage( locale.getMessage("invalid-item").replace("{reason}", "") );
							event.setCancelled(true);
						}
						
						return;
					} 
					else
					//manage prices
					if ( equalsTraderStatus(TraderStatus.MANAGE_PRICE) ) 
					{

						//show prices
						if ( event.getCursor().getType().equals(Material.AIR) ) 
						{

							//select item
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
								p.sendMessage( locale.getMessage("show-price").replace("{price}", f.format(getSelectedItem().getRawPrice())) );
							
						} 
						else
						//change prices
						{

							//select item to change
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								//change price
								if ( event.isRightClick() ) 
									getSelectedItem().lowerPrice(calculatePrice(event.getCursor()));
								else
									getSelectedItem().increasePrice(calculatePrice(event.getCursor()));
								
								p.sendMessage( locale.getMessage("change-price").replace( "{price}", f.format(getSelectedItem().getRawPrice()) ) );
							}
							
						}
						
						//reset the selected item
						selectItem(null);
						
						//cancel the event
						event.setCancelled(true);
						
					} 
					else 
					//limit managing
					if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL) ) 
					{
						
						//show limits
						if ( event.getCursor().getType().equals(Material.AIR) )
						{
							
							//select item which limit will be shown up
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								p.sendMessage( locale.getMessage("show-limit").replace("{type}", "Global").replace("{limit}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()) );
							}
							
							
						} 
						//change limits
						else 
						{
							
							//select the item
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								
								if ( event.isRightClick() ) 
									getSelectedItem().getLimitSystem().changeGlobalLimit(-calculateLimit(event.getCursor()));
								else
									getSelectedItem().getLimitSystem().changeGlobalLimit(calculateLimit(event.getCursor()));
								
								p.sendMessage( locale.getMessage("change-limit").replace("{type}", "Global").replace("{limit}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()) );
							
							}

						}
						
						//reset the selected item
						selectItem(null);
						
						//cancel the event
						event.setCancelled(true);
						
					} 
					else 
					//player limits
					if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER) ) 
					{
						//show limits
						if ( event.getCursor().getType().equals(Material.AIR) )
						{
							
							//select item which limit will be shown up
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								p.sendMessage( locale.getMessage("show-limit").replace("{type}", "Player").replace("{limit}", "" + getSelectedItem().getLimitSystem().getPlayerLimit()) );
							}
							
							
						} 
						//change limits
						else 
						{
							
							//select the item
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								
								if ( event.isRightClick() ) 
									getSelectedItem().getLimitSystem().changePlayerLimit(-calculateLimit(event.getCursor()));
								else
									getSelectedItem().getLimitSystem().changePlayerLimit(calculateLimit(event.getCursor()));
								
								p.sendMessage( locale.getMessage("change-limit").replace("{type}", "Player").replace("{limit}", "" + getSelectedItem().getLimitSystem().getPlayerLimit()) );
							
							}

						}
						

						//reset the selected item
						selectItem(null);
						
						
						event.setCancelled(true);
					}
					 
				} 
				
			} 
			
		} 
		//bottom inventory click
		else
		{
			//is item managing
			if ( equalsTraderStatus(getBasicManageModeByWool()) )
			{
				
				//is an item is selected
				if ( getInventoryClicked() && hasSelectedItem() ) {

					//remove it from the stock
					if ( equalsTraderStatus(TraderStatus.MANAGE_SELL) )
						getTraderStock().removeItem(true, getSelectedItem().getSlot());
					if ( equalsTraderStatus(TraderStatus.MANAGE_BUY) )
						getTraderStock().removeItem(false, getSelectedItem().getSlot());
					
					//reset the item
					selectItem(null);
					
					//send a message
					p.sendMessage( locale.getMessage("item-removed") );
					
				} 
				else
				//select a new item ready to be a stock item
				{
					
					//we don't want to have air in our stock, dont we?
					if ( event.getCurrentItem().getTypeId() != 0 ) 
					{
						selectItem( toStockItem(event.getCurrentItem()) );
						//send a message
						p.sendMessage( locale.getMessage("item-selected") );
					}
				}
			} 
			
			setInventoryClicked(false);
		}
	}
}


package net.dtl.citizenstrader_new.traders;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.containers.LimitSystem;
import net.dtl.citizenstrader_new.containers.StockItem;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.TraderTrait;

public class PlayerTrader extends Trader {

	public PlayerTrader(NPC n, TraderTrait c) {
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
				
				if ( isWool(event.getCurrentItem(), config.getItemManagement(7)) ) {
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
			} 
			else
			//is slot management
			if ( equalsTraderStatus(TraderStatus.SELL) ) 
			{
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
			}
			else 
			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) ) 
			{
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
							switchInventory(getSelectedItem());
							
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
			} else if ( equalsTraderStatus(TraderStatus.BUY) ) 
			{
				if ( selectItem(slot, TraderStatus.BUY).hasSelectedItem() ) {
					
					p.sendMessage( locale.getMessage("price-message").replace("{price}", f.format(getSelectedItem().getPrice()) ) );
					p.sendMessage( locale.getMessage("show-limit-pt").replace("{type}", "Item").replace("{limit}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()).replace("{amount}", "" + getSelectedItem().getLimitSystem().getGlobalAmount()) );
				
				}
			}
			setInventoryClicked(true);
		} 
		else
		{
			/* *
			 * change the comparing (lesser it)
			 * 
			 */
			if ( equalsTraderStatus(TraderStatus.BUY) )
			{
				if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true,true).hasSelectedItem() ) 
				{
					if ( getClickedSlot() == slot && !getInventoryClicked() )
					{
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
						//	if ( !updateLimitsTem(p.getName(),event.getCurrentItem()) )
							updateBuyLimits(p.getName(), scale);

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
						
						p.sendMessage( locale.getMessage("sell-message").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(getSelectedItem().getPrice()*scale) ) );
						
						/* *
						 * needs to be recoded
						 * 
						 * #Recoded (not tested)
						 * 
						 */
						//if ( !updateLimitsTem(p.getName(),event.getCurrentItem()) )
						updateBuyLimits(p.getName(), scale);
						
						/* *
						 * need to create removeFromInventoryFunction (code cleanup)
						 * 
						 */
						
						/* *
						 * TEMPORARY!!!!!!
						 * 
						 */
						/*if ( event.getCurrentItem().getAmount() == 1 ) {
							if ( event.getCurrentItem().getAmount()-getMaxAmount(event.getCurrentItem()) > 0 )
								event.getCurrentItem().setAmount(event.getCurrentItem().getAmount()-getMaxAmount(event.getCurrentItem()));
							else 
								event.setCurrentItem(new ItemStack(Material.AIR));
						} else {*/
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

		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		Player p = (Player) event.getWhoClicked();
		DecimalFormat f = new DecimalFormat("#.##");
		
		int clickedSlot = event.getSlot();
		
		if ( top ) 
		{
			setInventoryClicked(true);
			
			if ( isManagementSlot(clickedSlot, 3) ) 
			{
				//is white wool clicked
				if ( isWool(event.getCurrentItem(), config.getItemManagement(6)) )
				{
					
					
					//close any management mode, switch to the default buy/sell management
					if ( isSellModeByWool() )
						this.setTraderStatus(TraderStatus.MANAGE_SELL);
					if ( isBuyModeByWool() )
						this.setTraderStatus(TraderStatus.MANAGE_BUY);
					
					
					getInventory().setItem(getInventory().getSize() - 2, config.getItemManagement(2) );//new ItemStack(Material.WOOL,1,(short)0,(byte)15));
					getInventory().setItem(getInventory().getSize() - 3, ( getBasicManageModeByWool().equals(TraderStatus.MANAGE_SELL) ? config.getItemManagement(5) : config.getItemManagement(3) ) );//new ItemStack(Material.WOOL,1,(short)0,(byte)( getBasicManageModeByWool().equals(TraderStatus.MANAGE_SELL) ? 11 : 12 ) ));
					
					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "item") );
				}
				else
				if ( isWool(event.getCurrentItem(), config.getItemManagement(2)) )
				{
					
					
					//switch to price setting mode
					this.setTraderStatus(TraderStatus.MANAGE_PRICE);
					
					

					getInventory().setItem(getInventory().getSize() - 2, config.getItemManagement(6));
					getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
					

					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "price") );
				}
				else
				// TODO add a support system ;P
				if ( isWool(event.getCurrentItem(), config.getItemManagement(5)) )
				{
					

					p.sendMessage(ChatColor.RED+"Sorry, atm this is not suported for a player trader");
					//Only player limit management is enabled
					//global limit used by this system
					//this.setTraderStatus(TraderStatus.PLAYER_MANAGE_LIMIT_PLAYER);
					
					
					
					//getInventory().setItem(getInventory().getSize() - 2, new ItemStack(Material.WOOL,1,(short)0,(byte)0));
				//	getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
					
					
				}
				else
				// Only for buy system!
				if ( isWool(event.getCurrentItem(), config.getItemManagement(3)) )
				{
					//trader's status update
					//p.sendMessage(ChatColor.RED+"Sorry, atm this is not suported for a player trader");
					
					setTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL);
					
					
					
					getInventory().setItem(getInventory().getSize() - 2, new ItemStack(Material.WOOL,1,(short)0,(byte)0));
					getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
					
					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "buy limit") );
					
					
				}
				else
				// add a nice support to this system
				if ( isWool(event.getCurrentItem(), config.getItemManagement(1)) )
				{
					
					//switch to sell mode
					//status switching included in Inventory switch
					switchInventory(TraderStatus.MANAGE_BUY);
					
					
					
					getInventory().setItem(getInventory().getSize() - 1, config.getItemManagement(0));
					getInventory().setItem(getInventory().getSize() - 3, config.getItemManagement(3));
				

					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "buy") );
					
				}
				else
				if ( isWool(event.getCurrentItem(), config.getItemManagement(0)) )
				{
					
					
					//switch to sell mode
					//status switching included in Inventory switch
					switchInventory(TraderStatus.MANAGE_SELL);
					
					
					
					getInventory().setItem(getInventory().getSize() - 1, config.getItemManagement(1));
					getInventory().setItem(getInventory().getSize() - 3, config.getItemManagement(5));
				

					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "sell") );
				}
				else
				if ( isWool(event.getCurrentItem(), config.getItemManagement(7)) )	//unsupported wool data value
				{
					
					
					// TODO Currently disabled!!
					//switch to sell mode, out of amount management
					//this.setTraderStatus(TraderStatus.MANAGE_SELL);
					this.saveManagedAmouts();
					this.switchInventory(TraderStatus.MANAGE_SELL);
					
					getInventory().setItem(getInventory().getSize() - 1, config.getItemManagement(1));
					

					//send message
					p.sendMessage( locale.getMessage("managing-changed-message").replace("{managing}", "item") );
				}
				
				//cancel the event, so no1 can take up wools and end
				event.setCancelled(true);
				return;
			}
			//items management 
			else
			{
				//shift click handling
				if ( event.isShiftClick() )
				{
					
					//we don't like shift click in the upper inventory ;)
					event.setCancelled(true);
					
					if ( isSellModeByWool() )
					{
						
						//but any shift click will remove an item from the traders stock ;> 
						//and return all the remaining amount to the player, (if he has enough space)
						if ( selectItem(clickedSlot, TraderStatus.MANAGE_SELL ).hasSelectedItem() ) 
						{
							if ( event.isLeftClick() )
							{
								//get the amount left in the stock
								int leftAmount = getSelectedItem().getLimitSystem().getGlobalLimit() - getSelectedItem().getLimitSystem().getGlobalAmount();
								
								//check if the player has enough space
								if ( inventoryHasPlaceAmount(p, leftAmount) )
								{
								
									//remove that item from stock room
									if ( isBuyModeByWool() )
										getTraderStock().removeItem(false, clickedSlot);
									if ( isSellModeByWool() )
										getTraderStock().removeItem(true, clickedSlot);
									
									
									//add the remaining amount to the player
									this.addAmountToInventory(p, leftAmount);
									
									
									getInventory().setItem(clickedSlot, new ItemStack(0));
									
									
									//clear the selecton and message the player
									selectItem(null);
		
									//send message
									p.sendMessage( locale.getMessage("item-removed-pt").replace("{amount}", "" + leftAmount) );
								}
							} 
							else
							//if right clicked open the multiple amounts tab
							{
								//inventory and status update
								switchInventory(getSelectedItem());
								setTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT); 
							}
							
						}
						
					}
					//buy mode acts in another way
					else if ( isBuyModeByWool() )
					{
						
						if ( selectItem(clickedSlot, TraderStatus.MANAGE_BUY ).hasSelectedItem() ) 
						{
							//get the amount left in the stock
							int stockedAmount = getSelectedItem().getLimitSystem().getGlobalAmount();
							
							//check if the player has enough space
							if ( inventoryHasPlaceAmount(p, stockedAmount) )
							{
							
								if ( event.isLeftClick() )
								{
									//remove that item from stock room
									if ( isBuyModeByWool() )
										getTraderStock().removeItem(false, clickedSlot);
									if ( isSellModeByWool() )
										getTraderStock().removeItem(true, clickedSlot);
									
									//clear the inventory
									getInventory().setItem(clickedSlot, new ItemStack(0));

									//send a remove message
									p.sendMessage( locale.getMessage("item-removed-pt").replace("{amount}", "" + stockedAmount) );
								}
								else
								{
									//send a item got amount message
									p.sendMessage( locale.getMessage("item-taken").replace("{amount}", "" + stockedAmount) );
									
									//reset the amount
									getSelectedItem().getLimitSystem().setGlobalAmount(0);
								}
									
								
								//add the remaining amount to the player
								this.addAmountToInventory(p, stockedAmount);
								
								
								//clear the selection and message the player
								selectItem(null);
	
							}
							
						}
						
					}
					return;
					
				}
				//sell management
				if ( equalsTraderStatus(getBasicManageModeByWool()) )
				{
					
					//if an item is right-clicked
					if ( event.isRightClick() ) 
					{
						if ( selectItem(event.getSlot(), getTraderStatus()).hasSelectedItem() )
						{
							//if it has the stack price change it back to "per-item" price
							if ( getSelectedItem().hasStackPrice() ) 
							{
								getSelectedItem().setStackPrice(false);
								p.sendMessage( locale.getMessage("stackprice-toggle").replace("{value}", "disabled") );
							} 
							//change the price to a stack-price
							else
							{
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
					
					
					//has a selected item, can change the position or throw away
					if ( hasSelectedItem() ) 
					{
						//switch the items selected
						
						
						
						StockItem stockItem = getSelectedItem();
						
						
						
						if ( selectItem(clickedSlot, getTraderStatus()).hasSelectedItem() )
						{
							getSelectedItem().setSlot(-2);
							p.sendMessage( locale.getMessage("item-selected") );
						}
						
						
						
						stockItem.setSlot(clickedSlot);
						
						
					}
					//no item selected, select an item and change it's slot to -2 (in management)
					else
					{
						
						if ( selectItem(clickedSlot, getTraderStatus()).hasSelectedItem() )
						{
							getSelectedItem().setSlot(-2);	
							p.sendMessage( locale.getMessage("item-selected") );
						}
						
						
					}
					return;
				}
				else 
				if ( equalsTraderStatus(TraderStatus.MANAGE_PRICE) )
				{
					
					//check the cursor (if nothing is held just show the items price)
					if ( event.getCursor().getType().equals(Material.AIR) ) {
						
						//select the item to get the information from, and show the price
						if ( selectItem(event.getSlot(), getBasicManageModeByWool()).hasSelectedItem() ) 
							p.sendMessage( locale.getMessage("show-price").replace("{price}", f.format(getSelectedItem().getRawPrice())) );
						
						
					} 
					//if some thing is held change the items price
					else
					{
							
						//select the item if it exists
						if ( selectItem(event.getSlot(), getBasicManageModeByWool()).hasSelectedItem() ) 
						{
							
							//if it's right clicked the lower the price, else rise it
							if ( event.isRightClick() ) 
								getSelectedItem().lowerPrice(calculatePrice(event.getCursor()));
							else
								getSelectedItem().increasePrice(calculatePrice(event.getCursor()));
							
							//show the new price
							p.sendMessage( locale.getMessage("change-price").replace("{price}", f.format(getSelectedItem().getRawPrice())) );
							
							
						}
						
						
					}
					
					//reset the selection
					selectItem(null);
					
					event.setCancelled(true);
					
					
				}
				else 
				//global limit as "item limit"
				if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL) )
				{
					//show limits
					if ( event.getCursor().getType().equals(Material.AIR) )
					{
						
						//select item which limit will be shown up
						if ( selectItem(clickedSlot, getBasicManageModeByWool()).hasSelectedItem() ) 
						{
							p.sendMessage( locale.getMessage("show-limit-pt").replace("{type}", "Item").replace("{limit}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()).replace("{amount}", "" + getSelectedItem().getLimitSystem().getGlobalAmount()) );
						}
						
						
					} 
					//change limits
					else 
					{
						
						//select the item
						if ( selectItem(clickedSlot, getBasicManageModeByWool()).hasSelectedItem() ) 
						{
							
							if ( event.isRightClick() ) 
								getSelectedItem().getLimitSystem().changeGlobalLimit(-calculateLimit(event.getCursor()));
							else
								getSelectedItem().getLimitSystem().changeGlobalLimit(calculateLimit(event.getCursor()));
							
							p.sendMessage( locale.getMessage("change-limit").replace("{type}", "Item").replace("{limit}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()) );
						
						}

					}
					
					//reset the selected item
					selectItem(null);
					
					//cancel the event
					event.setCancelled(true);
				}
				else 
				if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER) )
				{
					
				}
				//currently unsupported
				else 
				if ( equalsTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT) )
				{
					//should we add a new amount?
				//	if ( clickedSlot == 1 )
				//	{
					event.setCancelled(true);
					
					//left = +
					if ( event.isLeftClick() )
					{
						
						//add a new item in that slot (it will be either rearranged)
						if ( event.getCurrentItem().getType().equals(Material.AIR) )
						{
							ItemStack clonedStack = getSelectedItem().getItemStack().clone();
						//	clonedStack.setAmount(1);
							//event.setCursor(clonedStack);
						//	this.getNpc();
							getInventory().setItem(clickedSlot, clonedStack);
							event.setCancelled(false);
							//event.setCurrentItem(clonedStack);
							//getInventory().addItem(clonedStack);
							//System.out.print(clonedStack.getType().toString());
						}
						else
						{
							
							//geta amount info
							int addAmount = event.getCursor().getAmount();
							int oldAmount = event.getCurrentItem().getAmount();
							
							//add the amount
							if ( event.getCurrentItem().getMaxStackSize() < oldAmount + addAmount )
								event.getCurrentItem().setAmount(event.getCurrentItem().getMaxStackSize());
							else
								event.getCurrentItem().setAmount(oldAmount+addAmount);
							
						}
						
						
					}
					//right = you know... -.-
					else
					{
						if ( event.getCurrentItem().getTypeId() == 0 )
						{
						//	event.setCancelled(false);
						//	event.setCursor(null);
				//			ItemStack is = event.getCursor().clone();
				//			event.setCurrentItem(is);
				//			event.setCursor(null);
							return;
						}
						//get amount info
						int removeAmount = event.getCursor().getAmount();
						int oldAmount = event.getCurrentItem().getAmount();
						
						//decrease the amount, or delete the item
						if ( oldAmount - removeAmount <= 0 )
							event.setCurrentItem(new ItemStack(Material.AIR, 0));
						else
							event.getCurrentItem().setAmount(oldAmount-removeAmount);
					}
					
				 
				} 

			}
			
		}
		//bottom inventory management
		else 
		{
			if ( equalsTraderStatus(TraderStatus.MANAGE_PRICE)
					|| equalsTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL)
					|| equalsTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT) )
			{
				return;
			}
			
			//cancel the event, bottom always canceled
			event.setCancelled(true);
			
			if ( hasSelectedItem() )
			{
				if ( event.getCursor().getTypeId() != 0 )
				{
					event.setCursor(null);
					selectItem(null);
					switchInventory(getBasicManageModeByWool());
				}
			
			}
			//if top inventory was clicked before
		//	if ( this.getInventoryClicked() )
		//	{
				
				//we dont support this anymore!
		//		return;
				
				/*
				
				//if there is some thing selected
				if ( hasSelectedItem() ) 
				{
					

					//clear the stock
					if ( isSellModeByWool() )
						getTraderStock().removeItem(true, getSelectedItem().getSlot());
					if ( isBuyModeByWool() )
						getTraderStock().removeItem(false, getSelectedItem().getSlot());
					
					
					
					//remove the selection and clear the cursor
					selectItem(null);
					event.setCursor(new ItemStack(Material.AIR));
					
					
					//doesnt work...
					//event.getWhoClicked().getInventory().setItem(clickedSlot, new ItemStack(0));
				
				}*/
				
		//	}
			//if bottom inventory was clicked before
		//	else
		//	{
				
			//if an item is left-clicked
			if ( event.isLeftClick() && event.getCurrentItem().getTypeId() != 0 )
			{
				//save the amount 
				int backUpAmount = event.getCurrentItem().getAmount();
				
				
				//get the item information
				ItemStack itemToAdd = event.getCurrentItem();
				itemToAdd.setAmount(1);
				
				
				//if that item already exist, don't put it again
			//	if ( isSellModeByWool() )
			//		this.selectItem(itemToAdd, getTraderStatus(), false, false);
			//	if ( isBuyModeByWool() )
				this.selectItem(itemToAdd, getBasicManageModeByWool(), false, false);
				
				
				if ( hasSelectedItem() )
				{
					
					
					//message the player
					p.sendMessage( locale.getMessage("already-in-stock") );
					
					
					//reset the selection and set the clicked inventory (false = bottom)
					itemToAdd.setAmount(backUpAmount);
					selectItem(null);
					setInventoryClicked(false);
					return;
				}
				
				
				//get the first empty item slot
				int firstEmpty = getInventory().firstEmpty();
				
				
				
				//just to be sure nothing will be out of the inventory range (-3 for managing)
				if ( firstEmpty >= 0 && firstEmpty < getInventory().getSize() - 3 )
				{
					
					
					//set the item to the inventory
					getInventory().setItem(firstEmpty, itemToAdd.clone());
					
					
					
					//change the item into the stock type
					StockItem stockItem = toStockItem(itemToAdd.clone());
					
					
					//set the stock items slot
					stockItem.setSlot(firstEmpty);

					
					//set the limit system to 0/0/-2 (player empty configuration)
					LimitSystem limitSystem = stockItem.getLimitSystem();
					limitSystem.setGlobalLimit(0);
					limitSystem.setGlobalTimeout(-2000);
					
					
					//put it into the stock list
					if ( isSellModeByWool() )
						getTraderStock().addItem(true, stockItem);
					if ( isBuyModeByWool() )
						getTraderStock().addItem(false, stockItem);
					
					
					
					itemToAdd.setAmount(backUpAmount);
					
					//send message
					p.sendMessage( locale.getMessage("item-added") );
				}
				
				
			}
			else
			//if we are right clicking an item we will add the stock amount the trader will sell
			if ( event.getCurrentItem().getTypeId() != 0 )
			{
				//if it's not shift clicked it has no effect ;P
				if ( !event.isShiftClick() )
				{
					
					//message the player
					p.sendMessage( locale.getMessage("amount-add-help") );
					
					
					//reset the selection and set the clicked inventory (false = bottom)
					selectItem(null);
					setInventoryClicked(false);
					return;
				}

				if ( equalsTraderStatus(TraderStatus.MANAGE_BUY) )
					return;

				
				
				//get the item we want to add
				ItemStack itemToAdd = event.getCurrentItem();
				
				
				//get the item if it exists in the inventory
			//	if ( isSellModeByWool() )
			//		this.selectItem(itemToAdd, getTraderStatus(), false, false);
			//	if ( isBuyModeByWool() )
				this.selectItem(itemToAdd, getBasicManageModeByWool(), false, false);
				
				
				//if it exist allow the event to occur (let the item disappear)
				if ( hasSelectedItem() ) 
				{
					
					//let the item disappear
					event.setCancelled(false);
					
					
					//get the items limit system
					LimitSystem limitSystem = getSelectedItem().getLimitSystem();
					
					
					//timeout set to no timeout checks (-2000 = it will never reset)
					limitSystem.setGlobalTimeout(-2000);
					
					
					int getItemsLeft = limitSystem.getGlobalLimit() - limitSystem.getGlobalAmount();
					if ( getItemsLeft < 0 )
						getItemsLeft = 0;
					
					//set the new limit (how many items can players buy)
					limitSystem.setGlobalLimit(getItemsLeft + itemToAdd.getAmount());
					
					
					//set the amount to 0 to push it but don't change the top items amount 
					itemToAdd.setAmount(0);
					event.setCurrentItem(itemToAdd);
					
					
					//reset the amount
					limitSystem.setGlobalAmount(0);
				
					
					//send message
					p.sendMessage( locale.getMessage("amount-added") );
					
					//reset
					selectItem(null);
				}
				else
				{
					//that item isn't in the stock
					p.sendMessage( locale.getMessage("not-in-stock") );
					
				}
				
				
			}
			
			
		}
		
		setInventoryClicked(false);
	}
		
//	}


}

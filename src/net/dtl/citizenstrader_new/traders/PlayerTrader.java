package net.dtl.citizenstrader_new.traders;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.containers.LimitSystem;
import net.dtl.citizenstrader_new.containers.StockItem;
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
					switchInventory(TraderStatus.SELL);		
				} else if ( isWool(event.getCurrentItem(),(byte) 3) ) {
					/*
					 * lest go back to the main selling inventory ;)
					 * 
					 */
					switchInventory(TraderStatus.SELL);		
				} else if ( isWool(event.getCurrentItem(),(byte) 5) ) {
					/*
					 * lest go back to the main selling inventory ;)
					 * 
					 */
					switchInventory(TraderStatus.BUY);		
				}
			} else if ( equalsTraderStatus(TraderStatus.SELL) ) {
				/*
				 * Player is buying from the trader
				 * 
				 */
				if ( selectItem(event.getSlot(), TraderStatus.SELL).hasSelectedItem() ) {
					if ( getSelectedItem().hasMultipleAmouts() ) {
						/*
						 * Switching to the amount select inventory
						 * 
						 */
						switchInventory(getSelectedItem());
						setTraderStatus(TraderStatus.SELL_AMOUNT);
					} else {
						if ( getClickedSlot() == event.getSlot() ) {
							/*
							 * This will trigger if some1 will click more than 1 amount on the same item  
							 * in the trader inventory
							 * 
							 */
							if ( checkLimits(p) && inventoryHasPlace(p,0) && buyTransaction(p,getSelectedItem().getPrice()) ) {
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
			} else if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) ) {
				if ( !event.getCurrentItem().getType().equals(Material.AIR) ) {
					if ( getClickedSlot() == event.getSlot() ) { 
						if ( checkLimits(p,event.getSlot()) && inventoryHasPlace(p,event.getSlot()) && buyTransaction(p,getSelectedItem().getPrice(event.getSlot())) ) {
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
			} else if ( equalsTraderStatus(TraderStatus.BUY) ) {
				if ( selectItem(event.getSlot(), TraderStatus.BUY).hasSelectedItem() ) {
					
					p.sendMessage(ChatColor.GOLD + "You get " + f.format(getSelectedItem().getPrice()) + " for this item.");
				
				}
			}
			setInventoryClicked(true);
		} else {
			/* *
			 * change the comparing (lesser it)
			 * 
			 */
			if ( equalsTraderStatus(TraderStatus.BUY) ) {
				if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true,true).hasSelectedItem() ) {
					if ( getClickedSlot() == event.getSlot() && !getInventoryClicked() ) {

						if ( checkLimits(p) && sellTransaction(p,getSelectedItem().getPrice(),event.getCurrentItem()) ) {//*event.getCurrentItem().getAmount()
							p.sendMessage(ChatColor.GOLD + "You sold " + getSelectedItem().getAmount() + " for " + f.format(getSelectedItem().getPrice()) + ".");
							
							/* *
							 * needs to be recoded
							 * 
							 * #Recoded (not tested)
							 * 
							 */
							if ( !updateLimitsTem(p.getName(),event.getCurrentItem()) )
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
			} else if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true,true).hasSelectedItem() ) {
				if ( getClickedSlot() == event.getSlot() && !getInventoryClicked() ) {
					
					if ( checkLimits(p) && sellTransaction(p,getSelectedItem().getPrice(),event.getCurrentItem()) ) {
						p.sendMessage(ChatColor.GOLD + "You sold " + getSelectedItem().getAmount() + " for " + f.format(getSelectedItem().getPrice()) + ".");
						
						/* *
						 * needs to be recoded
						 * 
						 * #Recoded (not tested)
						 * 
						 */
						if ( !updateLimitsTem(p.getName(),event.getCurrentItem()) )
							updateLimits(p.getName());
						
						/* *
						 * need to create removeFromInventoryFunction (code cleanup)
						 * 
						 */
						
						/* *
						 * TEMPORARY!!!!!!
						 * 
						 */
						if ( event.getCurrentItem().getAmount() == 1 ) {
							if ( event.getCurrentItem().getAmount()-getMaxAmount(event.getCurrentItem()) > 0 )
								event.getCurrentItem().setAmount(event.getCurrentItem().getAmount()-getMaxAmount(event.getCurrentItem()));
							else 
								event.setCurrentItem(new ItemStack(Material.AIR));
						} else {
							if ( event.getCurrentItem().getAmount()-getSelectedItem().getAmount() > 0 )
								event.getCurrentItem().setAmount(event.getCurrentItem().getAmount()-getSelectedItem().getAmount());
							else 
								event.setCurrentItem(new ItemStack(Material.AIR));
						}
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

		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		Player p = (Player) event.getWhoClicked();
		DecimalFormat f = new DecimalFormat("#.##");
		
		int clickedSlot = event.getSlot();
		
		if ( top ) 
		{
			setInventoryClicked(true);
			
			if ( isManagementSlot(clickedSlot, 3) ) 
			{
				if ( isWool(event.getCurrentItem(), (byte) 0) )
				{
					
					
					//close any management mode, switch to the default buy/sell management
					if ( isSellModeByWool() )
						this.setTraderStatus(TraderStatus.MANAGE_SELL);
					if ( isBuyModeByWool() )
						this.setTraderStatus(TraderStatus.MANAGE_BUY);
					
					
					
					getInventory().setItem(getInventory().getSize() - 2, new ItemStack(Material.WOOL,1,(short)0,(byte)15));
					getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.WOOL,1,(short)0,(byte)11));
					
					
				}
				else
				if ( isWool(event.getCurrentItem(), (byte) 15) )
				{
					
					
					//switch to price setting mode
					this.setTraderStatus(TraderStatus.MANAGE_PRICE);
					
					

					getInventory().setItem(getInventory().getSize() - 2, new ItemStack(Material.WOOL,1,(short)0,(byte)0));
					getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
					
					
				}
				else
				// TODO add a support system ;P
				if ( isWool(event.getCurrentItem(), (byte) 11) )
				{
					

					p.sendMessage(ChatColor.RED+"Sorry, atm this is not suported for a player trader");
					//Only player limit management is enabled
					//global limit used by this system
					//this.setTraderStatus(TraderStatus.PLAYER_MANAGE_LIMIT_PLAYER);
					
					
					
					//getInventory().setItem(getInventory().getSize() - 2, new ItemStack(Material.WOOL,1,(short)0,(byte)0));
				//	getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
					
					
				}
				else
				// TODO add a nice support to this system
				if ( isWool(event.getCurrentItem(), (byte) 5) )
				{
					p.sendMessage(ChatColor.RED+"Sorry, atm this is not suported for a player trader");
					
					//switch to buy mode
					//status switching included in Inventory switch
					//switchInventory(TraderStatus.PLAYER_MANAGE_BUY);
					
					
					
					//getInventory().setItem(getInventory().getSize() - 1, new ItemStack(Material.WOOL,1,(short)0,(byte)3));
					
					
				}
				else
				if ( isWool(event.getCurrentItem(), (byte) 3) )
				{
					
					
					//switch to sell mode
					//status switching included in Inventory switch
					switchInventory(TraderStatus.MANAGE_SELL);
					
					
					
					getInventory().setItem(getInventory().getSize() - 1, new ItemStack(Material.WOOL,1,(short)0,(byte)5));
				
				
				}
				else
				if ( isWool(event.getCurrentItem(), (byte) 2) )	//unsupported wool data value
				{
					
					
					// TODO Currently disabled!!
					//switch to sell mode, out of amount management
					this.setTraderStatus(TraderStatus.MANAGE_SELL);
					
					
					
					getInventory().setItem(getInventory().getSize() - 1, new ItemStack(Material.WOOL,1,(short)0,(byte)5));
					
					
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
					
					
					//but any shift click will remove an item from the traders stock ;> 
					//and return all the remaining amount to the player, (if he has enough space)
					if ( selectItem(clickedSlot, getBasicManageModeByWool() ).hasSelectedItem() ) 
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
							p.sendMessage(ChatColor.RED+"You got " + leftAmount + " of this item back");
						}
						
						
					}
					
					
				}
				//sell management
				if ( equalsTraderStatus(TraderStatus.MANAGE_SELL) )
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
								p.sendMessage(ChatColor.GOLD + "StackPrice disabled for this item.");
							} 
							//change the price to a stack-price
							else
							{
								getSelectedItem().setStackPrice(true);
								p.sendMessage(ChatColor.GOLD + "StackPrice enabled for this item.");
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
							getSelectedItem().setSlot(-2);
						
						
						
						stockItem.setSlot(clickedSlot);
						
						
					}
					//no item selected, select an item and change it's slot to -2 (in management)
					else
					{
						
						
						//try to select an item (if it existis in that slot)
						if ( selectItem(clickedSlot, getTraderStatus()).hasSelectedItem() )
							getSelectedItem().setSlot(-2);	//found a item for management
						
						
						
					}
					return;
				}
				else
				if ( equalsTraderStatus(TraderStatus.MANAGE_BUY) ) 
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
								p.sendMessage(ChatColor.GOLD + "StackPrice disabled for this item.");
							} 
							//change the price to a stack-price
							else
							{
								getSelectedItem().setStackPrice(true);
								p.sendMessage(ChatColor.GOLD + "StackPrice enabled for this item.");
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
							getSelectedItem().setSlot(-2);
						
						
						
						stockItem.setSlot(clickedSlot);
						
						
					}
					//no item selected, select an item and change it's slot to -2 (in management)
					else
					{
						
						
						//try to select an item (if it existis in that slot)
						if ( selectItem(clickedSlot, getTraderStatus()).hasSelectedItem() )
							getSelectedItem().setSlot(-2);	//found a item for management
						
						
						
					}
					return;
				}
				else 
				if ( equalsTraderStatus(TraderStatus.MANAGE_PRICE) )
				{
					
					//check the cursor (if nothing is held just show the items price)
					if ( event.getCursor().getType().equals(Material.AIR) ) {
						
						
						// check if it's buy or sell mode
						if ( isBuyModeByWool() )
						{
							
							//select the item to get the information from, and show the price
							if ( selectItem(event.getSlot(),TraderStatus.MANAGE_BUY).hasSelectedItem() ) 
								p.sendMessage(ChatColor.GOLD + "Price: " + f.format(getSelectedItem().getRawPrice()) );
							
							
							//reset the selection
							selectItem(null);
						}
						else
						if ( isSellModeByWool() )
						{
							
							//select the item to get the information from, and show the price
							if ( selectItem(event.getSlot(),TraderStatus.MANAGE_SELL).hasSelectedItem() ) 
								p.sendMessage(ChatColor.GOLD + "Price: " + f.format(getSelectedItem().getRawPrice()) );
							
							
							//reset the selection
							selectItem(null);
						}
						
					} 
					//if some thing is held change the items price
					else
					{

						// check if it's buy or sell mode
						if ( isBuyModeByWool() ) 
						{
							
							//select the item if it exists
							if ( selectItem(event.getSlot(),TraderStatus.MANAGE_BUY).hasSelectedItem() ) 
							{
								
								//if it's right clicked the lower the price, else rise it
								if ( event.isRightClick() ) 
									getSelectedItem().lowerPrice(calculatePrice(event.getCursor()));
								else
									getSelectedItem().increasePrice(calculatePrice(event.getCursor()));
								
								//show the new price
								p.sendMessage(ChatColor.GOLD + "New price: " + f.format(getSelectedItem().getRawPrice()) );
								
								//deselect the item
								selectItem(null);
								
								
							}
							
							
						} 
						else
						if ( isSellModeByWool() )
						{
						
							//select the item if it exists
							if ( selectItem(event.getSlot(),TraderStatus.MANAGE_SELL).hasSelectedItem() ) 
							{

								//if it's right clicked the lower the price, else rise it
								if ( event.isRightClick() ) 
									getSelectedItem().lowerPrice(calculatePrice(event.getCursor()));
								else
									getSelectedItem().increasePrice(calculatePrice(event.getCursor()));
								
								//show the new price
								p.sendMessage(ChatColor.GOLD + "New price: " + f.format(getSelectedItem().getRawPrice()) );
								
								//deselct the item
								selectItem(null);
							}
							
							
						}
						
						
					}
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
					
				} 

			}
			
		}
		//bottom inventory management
		else 
		{
			if ( equalsTraderStatus(TraderStatus.MANAGE_PRICE) )
			{
				return;
			}
			
			//cancel the event, bottom always canceled
			event.setCancelled(true);
			
			
			//if top inventory was clicked before
			if ( this.getInventoryClicked() )
			{
				
				
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
				
				}
				
			}
			//if bottom inventory was clicked before
			else
			{


				
				//if an item is left-clicked
				if ( event.isLeftClick() && event.getCurrentItem().getTypeId() != 0 )
				{
					//save the amount 
					int backUpAmount = event.getCurrentItem().getAmount();
					
					
					//get the item information
					ItemStack itemToAdd = event.getCurrentItem();
					itemToAdd.setAmount(1);
					
					
					//if that item already exist, don't put it again
					if ( isSellModeByWool() )
						this.selectItem(itemToAdd, getTraderStatus(), false, false);
					if ( isBuyModeByWool() )
						this.selectItem(itemToAdd, getTraderStatus(), false, false);
					
					
					if ( hasSelectedItem() )
					{
						
						
						//message the player
						p.sendMessage(ChatColor.RED + "That item is alredy in the traders stock");
						
						
						//reset the selection and set the clicked inventory (false = bottom)
						itemToAdd.setAmount(backUpAmount);
						selectItem(null);
						setInventoryClicked(false);
						return;
					}
					
					
					//get the first empty item slot
					int firstEmpty = getInventory().firstEmpty();
					
					
					
					//just to be sure nothing will be out of the inventory range
					if ( firstEmpty >= 0 && firstEmpty < getInventory().getSize() )
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
						p.sendMessage(ChatColor.RED + "Shift click to add an amount");
						
						
						//reset the selection and set the clicked inventory (false = bottom)
						selectItem(null);
						setInventoryClicked(false);
						return;
					}
					
					
					//get the item we want to add
					ItemStack itemToAdd = event.getCurrentItem();
					
					
					//get the item if it exists in the inventory
					if ( isSellModeByWool() )
						this.selectItem(itemToAdd, getTraderStatus(), false, false);
					if ( isBuyModeByWool() )
						this.selectItem(itemToAdd, getTraderStatus(), false, false);
					
					
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
					
						
						//reset
						selectItem(null);
					}
					else
					{
						//that item isn't in the stock
						p.sendMessage(ChatColor.RED+"You don't have this item in you'r stock");
						
					}
					
					
				}
				
				
			}
			
			setInventoryClicked(false);
		}
		
	}


}

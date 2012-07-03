package net.dtl.citizenstrader_new.traders;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
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
			} else if ( equalsTraderStatus(TraderStatus.PLAYER_SELL_AMOUNT) ) {
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
			} else if ( selectItem(event.getCurrentItem(),TraderStatus.PLAYER_BUY,true,true).hasSelectedItem() ) {
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
		// TODO Auto-generated method stub
		
	}


}

package net.dtl.citizens.trader.traders;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.TraderCharacterTrait.TraderType;
import net.dtl.citizens.trader.events.TraderTransactionEvent;
import net.dtl.citizens.trader.events.TraderTransactionEvent.TransactionResult;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.objects.TransactionPattern;
import net.dtl.citizens.trader.traders.Banker.BankStatus;
import net.dtl.citizens.trader.traders.Trader.TraderStatus;
import net.dtl.citizens.trader.traits.TraderTrait;

public class ServerTrader extends Trader {

	private TransactionPattern pattern;
	
	public ServerTrader(NPC n, TraderTrait c) {
		super(n, c);
		pattern = patterns.getPattern(this.getTraderConfig().getPattern());
	}

	@Override
	public void settingsMode(InventoryClickEvent event) {
		
		((Player)event.getWhoClicked()).sendMessage(ChatColor.RED+"Settings Mode Inactive! Switch to simple mode!");
		event.setCancelled(true);
	}

	@Override
	public void simpleMode(InventoryClickEvent event) {
		
		
		Player p = (Player) event.getWhoClicked();
		DecimalFormat f = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		if ( slot < 0 )
		{
			event.setCursor(null);
			return;
		}
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( top ) 
		{
			
			if ( isManagementSlot(slot, 1) ) 
			{
				
				if ( isWool(event.getCurrentItem(), config.getItemManagement(7)) ) 
				{
					switchInventory(TraderStatus.SELL);		
				}
				else 
				if ( isWool(event.getCurrentItem(), config.getItemManagement(0)) )
				{
					if ( !permissions.has(p, "dtl.trader.options.sell") )
					{
						p.sendMessage( locale.getLocaleString("lacks-permissions-xxx","object:tab") );
					}
					else
					{
						switchInventory(TraderStatus.SELL);	
						p.sendMessage( locale.getLocaleString("xxx-transaction-tab","transaction:sell") );
					}
				} 
				else 
				if ( isWool(event.getCurrentItem(), config.getItemManagement(1)) ) 
				{
					if ( !permissions.has(p, "dtl.trader.options.buy") )
					{
						p.sendMessage( locale.getLocaleString("lacks-permissions-xxx","object:tab") );
					}
					else
					{
						switchInventory(TraderStatus.BUY);	
						p.sendMessage( locale.getLocaleString("xxx-transaction-tab","transaction:buy") );
					}	
				}
			} 
			else
			if ( equalsTraderStatus(TraderStatus.SELL) ) 
			{
				
				if ( selectItem(slot, TraderStatus.SELL).hasSelectedItem() )
				{
					
					if ( getSelectedItem().hasMultipleAmouts() 
							&& permissions.has(p, "dtl.trader.options.sell-amounts") )
					{
						
						switchInventory(getSelectedItem());
						setTraderStatus(TraderStatus.SELL_AMOUNT);
						
					}
					else 
					{
						if ( getClickedSlot() == slot ) 
						{

							//checks
							if ( !checkLimits(p) )
							{
								Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_LIMIT));
								p.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
							}
							else
							if ( !inventoryHasPlace(p,0) )
							{
								Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_SPACE));
								p.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
							}
							else
							if ( !buyTransaction(p,getSelectedItem().getPrice()) )
							{
								p.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
								Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_MONEY));
							}
							else
							{
								//send message
								p.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + getSelectedItem().getAmount() ).replace("{price}", f.format(getSelectedItem().getPrice()) ) );


								addSelectedToInventory(p,0);


								updateLimits(p.getName());
								
								//call event Denizen Transaction Trigger
								Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.SUCCESS_SELL));
								
								//logging
								log("buy", 
									p.getName(), 
									getSelectedItem().getItemStack().getTypeId(),
									getSelectedItem().getItemStack().getData().getData(), 
									getSelectedItem().getAmount(), 
									getSelectedItem().getPrice() );
								
							}
							
						}
						else
						{
							p.sendMessage( locale.getLocaleString("xxx-item-cost-xxx").replace("{price}", f.format(getSelectedItem().getPrice()) ) );
							p.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:buy") );
							setClickedSlot(slot);
						}
					}
				}
			} 
			else
			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) ) 
			{
				
				if ( !event.getCurrentItem().getType().equals(Material.AIR) ) 
				{
					
					if ( getClickedSlot() == slot ) 
					{
							
						if ( !checkLimits(p,slot) )
						{
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_LIMIT));
							p.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
						}
						else
						if ( !inventoryHasPlace(p,slot) )
						{
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_SPACE));
							p.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
						}
						else
						if ( !buyTransaction(p,getSelectedItem().getPrice(slot)) ) 
						{
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_MONEY));
							p.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
						}
						else
						{
							//send message
							p.sendMessage(locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + getSelectedItem().getAmount(slot) ).replace("{price}", f.format(getSelectedItem().getPrice(slot)) ) );
							
							/* *
							 * better version of Inventory.addItem();
							 * 
							 */
							addSelectedToInventory(p,slot);
							
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.SUCCESS_SELL));
							
							updateLimits(p.getName(),slot);
							switchInventory(getSelectedItem());
							
							//logging
							log("buy", 
								p.getName(), 
								getSelectedItem().getItemStack().getTypeId(),
								getSelectedItem().getItemStack().getData().getData(), 
								getSelectedItem().getAmount(slot), 
								getSelectedItem().getPrice(slot) );
							
						} 
					}
					else 
					{
						
						p.sendMessage( locale.getLocaleString("xxx-item-cost-xxx").replace("{price}", f.format(getSelectedItem().getPrice(slot)) ) );
						p.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:buy") );
						setClickedSlot(slot);
					}
				}
			} 
			else 
			if ( equalsTraderStatus(TraderStatus.BUY) )
			{
				
				if ( selectItem(slot, TraderStatus.BUY).hasSelectedItem() ) {
					

					p.sendMessage( locale.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getSelectedItem().getPrice()) ) );
				}
			}
			setInventoryClicked(true);
		} 
		else
		{
			if ( equalsTraderStatus(TraderStatus.BUY) ) 
			{
				
				if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true,true).hasSelectedItem() )
				{
					
					if ( getClickedSlot() == slot && !getInventoryClicked() ) 
					{
						
						int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
						if ( !checkBuyLimits(p, scale) )
						{
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_LIMIT));
							p.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:limit"));
						}
						else
						if ( !sellTransaction(p,getSelectedItem().getPrice(),event.getCurrentItem()) )
						{
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_MONEY));
							p.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:money"));
						}
						else
						{
							p.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:sold").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(getSelectedItem().getPrice()*scale) ) );

							
							updateBuyLimits(p.getName(),scale);

							removeFromInventory(event.getCurrentItem(),event);
							
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.SUCCESS_BUY));
							
							//logging
							log("sell", 
								p.getName(), 
								getSelectedItem().getItemStack().getTypeId(),
								getSelectedItem().getItemStack().getData().getData(), 
								getSelectedItem().getAmount()*scale, 
								getSelectedItem().getPrice()*scale );
							
						} 
					} 
					else
					{
						p.sendMessage( locale.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getSelectedItem().getPrice()*((int)event.getCurrentItem().getAmount() / getSelectedItem().getAmount())) ) );
						p.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:sell") );
						setClickedSlot(slot);
					}
				}
			} 
			else
			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) )
			{ 
				//p.sendMessage( locale.getLocaleString("amount-exception") );
				event.setCancelled(true);
				return;
			} 
			else
			if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true,true).hasSelectedItem() ) 
			{				
				
				if ( getClickedSlot() == slot && !getInventoryClicked() && permissions.has(p, "dtl.trader.options.buy") ) 
				{
					int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
					
					if ( !permissions.has(p, "dtl.trader.options.buy") )
					{
						p.sendMessage( locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:permission") );
					}
					else
					if ( !checkBuyLimits(p, scale) )
					{
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_MONEY));
						p.sendMessage( locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:limit") );
					}
					else
					if ( !sellTransaction(p,getSelectedItem().getPrice(),event.getCurrentItem()) )
					{
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.FAIL_MONEY));
						p.sendMessage( locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:money") );
					}
					else
					{
						p.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:sold").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(getSelectedItem().getPrice()*scale) ) );

						
						//limits update
						updateBuyLimits(p.getName(),scale);
						
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), TransactionResult.SUCCESS_BUY));
						
						//inventory cleanup
						removeFromInventory(event.getCurrentItem(),event);
						
						//logging
						log("sell", 
							p.getName(), 
							getSelectedItem().getItemStack().getTypeId(),
							getSelectedItem().getItemStack().getData().getData(), 
							getSelectedItem().getAmount()*scale, 
							getSelectedItem().getPrice()*scale );

					}
				}
				else
				{
					if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) &&
						 !event.getCurrentItem().getType().equals(Material.AIR) ) 
					{
						p.sendMessage( locale.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getSelectedItem().getPrice()*((int)event.getCurrentItem().getAmount() / getSelectedItem().getAmount())) ) );
						p.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:sell") );
						
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
		
		if ( slot < 0 )
		{
			event.setCancelled(true);
			switchInventory(getBasicManageModeByWool());
			return;
		}
		
		DecimalFormat f = new DecimalFormat("#.##");
		
		if ( top )
		{
			setInventoryClicked(true);

			// Wool checking, also removing a bug that allowed placing items for sell in the wool slots 
			if ( isManagementSlot(slot, 3) ) {
				
				
				//price managing
				if ( isWool(event.getCurrentItem(), config.getItemManagement(2)) ) 
				{
					
					if ( !permissions.has(p, "dtl.trader.managing.price") )
					{
						p.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "", "manage:price") );
					}
					else
					{
						//switch to price setting mode
						this.setTraderStatus(TraderStatus.MANAGE_PRICE);
						

						getInventory().setItem(getInventory().getSize() - 2, config.getItemManagement(6));
						getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
						

						//send message
						p.sendMessage( locale.getLocaleString("xxx-managing-toggled", "entity:player", "manage:price") );
					}
						
				} 
				else 
				//is any mode used? return to item adding
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
					p.sendMessage( locale.getLocaleString("xxx-managing-toggled", "entity:player", "manage:stock") );
					
				}
				else 
				//global limits management
				if ( isWool(event.getCurrentItem(), config.getItemManagement(4)) )
				{

					if ( !permissions.has(p, "dtl.trader.managing.global-limits") )
					{
						p.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy-limit") );
					}
					else
					{
						//status update
						setTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL);
						
						//wool update
						getInventory().setItem(getInventory().getSize()-3, config.getItemManagement(6));
						getInventory().setItem(getInventory().getSize()-2, config.getItemManagement(5));
	
						//send message
						p.sendMessage( locale.getLocaleString("xxx-managing-toggled", "entity:player", "manage:global-limit") );
					}					
				} 
				else 
				//player limits management
				if ( isWool(event.getCurrentItem(),config.getItemManagement(5)) ) 
				{
					if ( !permissions.has(p, "dtl.trader.managing.player-limits") )
					{
						p.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy-limit") );
					}
					else
					{
						//status update
						setTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER);
						
						//wool update
						getInventory().setItem(getInventory().getSize()-2, config.getItemManagement(4));
	
						//send message
						p.sendMessage( locale.getLocaleString("xxx-managing-toggled", "entity:player", "manage:player-limit") );
					}					
				}
				else
				// add a nice support to this system
				if ( isWool(event.getCurrentItem(), config.getItemManagement(1)) )
				{
					if ( !permissions.has(p, "dtl.trader.options.buy") )
					{
						p.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy") );
					}
					else
					{
						switchInventory(TraderStatus.MANAGE_BUY);
						
	
						//send message
						p.sendMessage( locale.getLocaleString("xxx-managing-toggled", "entity:player", "manage:buy") );
						
					}
				}
				else
				if ( isWool(event.getCurrentItem(), config.getItemManagement(0)) )
				{
					if ( !permissions.has(p, "dtl.trader.options.sell") )
					{
						p.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "", "manage:sell") );
					}
					else
					{
						//switch to sell mode
						//status switching included in Inventory switch
						switchInventory(TraderStatus.MANAGE_SELL);
						
						//send message
						p.sendMessage( locale.getLocaleString("xxx-managing-toggled", "entity:player", "manage:sell") );
						
					}
				}
				else 
				//leaving the amount managing
				if ( isWool(event.getCurrentItem(), config.getItemManagement(7)) ) {

					//update amounts and status
					saveManagedAmouts();
					switchInventory(TraderStatus.MANAGE_SELL);
					

					p.sendMessage( locale.getLocaleString("xxx-managing-toggled", "entity:player", "manage:stock") );
				}
				
				event.setCancelled(true);
				return;
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
								p.sendMessage(locale.getLocaleString("xxx-value", "manage:global-timeout").replace("{value}", "" + getSelectedItem().getLimitSystem().getGlobalTimeout()) );
								
							
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
								
								p.sendMessage(locale.getLocaleString("xxx-value-changed", "manage:global-timeout").replace("{value}", "" + getSelectedItem().getLimitSystem().getGlobalTimeout()) );
							}

						}
						
						selectItem(null);
						event.setCancelled(true);
						return;
					}
					
					if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER) ) 
					{

						//show the current limit
						if ( event.getCursor().getType().equals(Material.AIR) ) 
						{
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
								p.sendMessage(locale.getLocaleString("xxx-value", "manage:player-timeout").replace("{value}", "" + getSelectedItem().getLimitSystem().getPlayerTimeout()) );
							
							
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
								
								p.sendMessage(locale.getLocaleString("xxx-value-changed", "manage:player-timeout").replace("{value}", "" + getSelectedItem().getLimitSystem().getPlayerTimeout()) );
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
							if ( selectItem(slot,TraderStatus.MANAGE_SELL).hasSelectedItem()
									&& permissions.has(p, "dtl.trader.managing.multiple-amounts") )
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
							if ( !permissions.has(p, "dtl.trader.managing.stack-price") )
							{
								p.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "", "manage:stack-price") );
								selectItem(null);
								event.setCancelled(true);
								return;
							}
							 
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
							{
								//if it has the stack price change it back to "per-item" price
								if ( getSelectedItem().hasStackPrice() ) 
								{
									getSelectedItem().setStackPrice(false);
									p.sendMessage( locale.getLocaleString("xxx-value", "manage:stack-price").replace("{value}", "disabled") );
								} 
								//change the price to a stack-price
								else
								{
									getSelectedItem().setStackPrice(true);
									p.sendMessage( locale.getLocaleString("xxx-value", "manage:stack-price").replace("{value}", "enabled") );
								}

								getSelectedItem().setAsPatternItem(false);
							}

							//reset the selection
							selectItem(null);
							
							//cancel the event
							event.setCancelled(true);
							return;
						 }
						 if ( hasSelectedItem() ) 
						 {
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
								 
								 if ( pattern != null )
								 {
									 if ( isBuyModeByWool() )
										 pattern.getItemPrice(item, "buy");
									 if ( isSellModeByWool() )
										 pattern.getItemPrice(item, "sell");
								 }
									 
								 p.sendMessage( locale.getLocaleString("xxx-item", "action:added") );
							 }
							 
							 //select an item if it exists in the traders inventory
							 if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
							 {
								 getSelectedItem().setSlot(-2);
								 p.sendMessage( locale.getLocaleString("xxx-item", "action:selected") );
							 }
							 
							 //set the managed items slot
							 item.setSlot(slot);
							 item.setAsPatternItem(false);
							 p.sendMessage( locale.getLocaleString("xxx-item", "action:updated") );
							 
						} 
						else 
						{

							 //select an item if it exists in the traders inventory
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
							{
								getSelectedItem().setSlot(-2);
								p.sendMessage( locale.getLocaleString("xxx-item", "action:selected") );
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

							p.sendMessage( locale.getLocaleString("xxx-item", "action:invalid") );
							event.setCancelled(true);
						}
						if ( !event.getCursor().getType().equals(Material.AIR) )
							getSelectedItem().setAsPatternItem(false);
						
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
								p.sendMessage( locale.getLocaleString("xxx-value", "manage:price").replace("{value}", f.format(getSelectedItem().getRawPrice())) );
							
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

								getSelectedItem().setAsPatternItem(false);
								getSelectedItem().setPetternListening(false);
								p.sendMessage( locale.getLocaleString("xxx-value-changed", "", "manage:price").replace("{value}", f.format(getSelectedItem().getRawPrice())) );
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
								p.sendMessage( locale.getLocaleString("xxx-value", "manage:global-limit").replace("{value}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()) );
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

								getSelectedItem().setAsPatternItem(false);
								p.sendMessage( locale.getLocaleString("xxx-value-changed", "manage:global-limit").replace("{value}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()) );
							
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
								p.sendMessage( locale.getLocaleString("xxx-value", "manage:player-limit").replace("{value}", "" + getSelectedItem().getLimitSystem().getPlayerLimit()) );
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
								
								getSelectedItem().setAsPatternItem(false);
								p.sendMessage( locale.getLocaleString("xxx-value-changed", "manage:player-limit").replace("{value}", "" + getSelectedItem().getLimitSystem().getPlayerLimit()) );
							
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
					p.sendMessage( locale.getLocaleString("xxx-item", "action:removed") );
					
				} 
				else
				//select a new item ready to be a stock item
				{
					
					//we don't want to have air in our stock, dont we?
					if ( event.getCurrentItem().getTypeId() != 0 ) 
					{
						selectItem( toStockItem(event.getCurrentItem()) );
						//send a message
						p.sendMessage( locale.getLocaleString("xxx-item", "action:selected") );
					}
				}
			} 
			
			setInventoryClicked(false);
		}
	}

	@Override
	public void onRightClick(Player player, TraderCharacterTrait trait, NPC npc) {
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& permissions.has(player, "dtl.trader.bypass.creative") )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-creative") );
			return;
		}
		
		if ( player.getItemInHand().getTypeId() == config.getManageWand().getTypeId() )
		{
			
			if ( !permissions.has(player, "dtl.trader.options.manage") )
			{
				player.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "setting:trader") );
				return;
			}
			if ( !trait.getTraderTrait().getOwner().equals(player.getName()) )
			{
				
				if ( !permissions.has(player, "dtl.trader.bypass.managing") )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "setting:trader") );
					return;
				}
				else
				if ( !player.isOp() )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "setting:trader") );
					return;
				}
				
			}
			
			if ( TraderStatus.hasManageMode(this.getTraderStatus()) )
			{
				switchInventory( getStartStatus(player) );
				player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
				return;
			}	
			
			player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
			switchInventory( getManageStartStatus(player) );
			return;
		}

		player.openInventory(getInventory());
	}

}


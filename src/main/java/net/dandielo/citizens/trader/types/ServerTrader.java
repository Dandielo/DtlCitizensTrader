package net.dandielo.citizens.trader.types;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.TraderTrait.EType;
import net.dandielo.citizens.trader.events.TraderOpenEvent;
import net.dandielo.citizens.trader.events.TraderTransactionEvent;
import net.dandielo.citizens.trader.events.TraderTransactionEvent.TransactionResult;
import net.dandielo.citizens.trader.limits.Limits.Limit;
import net.dandielo.citizens.trader.locale.LocaleManager;
import net.dandielo.citizens.trader.objects.MetaTools;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.parts.TraderStockPart;
import net.dandielo.citizens.trader.patterns.TPattern;

public class ServerTrader extends Trader {

//	private TPattern pattern = getStock().getPattern();
	private LocaleManager locale = CitizensTrader.getLocaleManager();
	
	public ServerTrader(TraderTrait trait, NPC npc, Player player) {
		super(trait, npc, player);
	}

	@Override
	public void simpleMode(InventoryClickEvent event) 
	{
		NumberFormat f = NumberFormat.getCurrencyInstance();
	    //DecimalFormat f = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		if ( slot < 0 )
		{
			event.setCursor(null);
			return;
		}
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( top ) 
		{

			if ( event.isShiftClick() )
			{
				((Player)event.getWhoClicked()).sendMessage(ChatColor.GOLD + "You can't shift click this, Sorry");
				event.setCancelled(true);
				return;
			}
			
			if ( isManagementSlot(slot, 1) ) 
			{
				
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(7)) ) 
				{
					switchInventory(TraderStatus.SELL);		
				}
				else 
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(0)) )
				{
					if ( !permissionsManager.has(player, "dtl.trader.options.sell") )
					{
						locale.sendMessage(player, "error-nopermission");
					}
					else
					{
						switchInventory(TraderStatus.SELL);	
					}
				} 
				else 
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(1)) ) 
				{
					if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
					{
						locale.sendMessage(player, "error-nopermission");
					}
					else
					{
						switchInventory(TraderStatus.BUY);	
					}	
				}
			} 
			else
			if ( equalsTraderStatus(TraderStatus.SELL) ) 
			{
				
				if ( selectItem(slot, TraderStatus.SELL).hasSelectedItem() )
				{
					
					if ( getSelectedItem().hasMultipleAmounts() 
							&& permissionsManager.has(player, "dtl.trader.options.sell-amounts") )
					{
						switchInventory(getSelectedItem());
						setTraderStatus(TraderStatus.SELL_AMOUNT);
					}
					else 
					{
						double price = getPrice(player, "sell");
						//checks
						if ( !checkLimits() )
						{
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_LIMIT));
							locale.sendMessage(player, "trader-transaction-failed-limit");
						}
						else
						if ( !inventoryHasPlace(0) )
						{
							locale.sendMessage(player, "trader-transaction-failed-inventory");
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_SPACE));
						}
						else
						if ( !buyTransaction(price) )
						{
							locale.sendMessage(player, "trader-transaction-failed-money");
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
						}
						else
						{ 
							locale.sendMessage(player, "trader-transaction-success", "action", "#bought", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price).replace("$", ""));
						
							addSelectedToInventory(0);

							updateLimits();
							
							//call event Denizen Transaction Trigger
							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.SUCCESS_SELL));
							
							//logging
							log("buy", 
								getSelectedItem().getItemStack().getTypeId(),
								getSelectedItem().getItemStack().getData().getData(), 
								getSelectedItem().getAmount(), 
								price );
							
						}
					}
				}
			} 
			else
			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) ) 
			{
				
				if ( !event.getCurrentItem().getType().equals(Material.AIR) ) 
				{
					double price = getPrice(player, "sell", slot);
					if ( !checkLimits(slot) )
					{
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_LIMIT));
						locale.sendMessage(player, "trader-transaction-failed-limit");
					}
					else
					if ( !inventoryHasPlace(slot) )
					{
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_SPACE));
						locale.sendMessage(player, "trader-transaction-failed-inventory");
					}
					else
					if ( !buyTransaction(price) ) 
					{
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
						locale.sendMessage(player, "trader-transaction-failed-money");
					}
					else
					{
						locale.sendMessage(player, "trader-transaction-success", "action", "#bought", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price).replace("$", ""));
							
						addSelectedToInventory(slot);
						
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.SUCCESS_SELL));
						
						updateLimits(slot);
						switchInventory(getSelectedItem());
						
						//logging
						log("buy", 
							getSelectedItem().getItemStack().getTypeId(),
							getSelectedItem().getItemStack().getData().getData(), 
							getSelectedItem().getAmount(slot), 
							price );
						
					}
			
				}
			} 
		} 
		else
		{
			if ( equalsTraderStatus(TraderStatus.BUY) ) 
			{
				
				if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true).hasSelectedItem() )
				{
				
					double price = getPrice(player, "buy");
					int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
					
					if ( !checkBuyLimits(scale) )
					{
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_LIMIT));
						locale.sendMessage(player, "trader-transaction-failed-limit");
					}
					else
					if ( !sellTransaction(price*scale, event.getCurrentItem()) )
					{
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
						locale.sendMessage(player, "trader-transaction-failed-money");
					}
					else
					{
						locale.sendMessage(player, "trader-transaction-success", "action", "#sold", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
						//TODO
						updateBuyLimits(scale);

						MetaTools.removeDescription(event.getCurrentItem(), "player-inventory");
						removeFromInventory(event.getCurrentItem(), event);
						
						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.SUCCESS_BUY));
						
						//logging
						log("sell", 
							getSelectedItem().getItemStack().getTypeId(),
							getSelectedItem().getItemStack().getData().getData(), 
							getSelectedItem().getAmount()*scale, 
							price*scale );
						
					
					} 
				}
			} 
			else
			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) )
			{ 
				event.setCancelled(true);
				return;
			} 
			else
			if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true).hasSelectedItem() ) 
			{				
			
				double price = getPrice(player, "buy");

				int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
				if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:permission") );
				}
				else
				if ( !checkBuyLimits(scale) )
				{
					Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
					locale.sendMessage(player, "trader-transaction-failed-limit");
					//player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:limit") );
				}
				else
				if ( !sellTransaction(price*scale, event.getCurrentItem()) )
				{
					Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
					locale.sendMessage(player, "trader-transaction-failed-money");
				//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:money") );
				}
				else
				{
					//player.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:sold").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(price*scale) ) );
					locale.sendMessage(player, "trader-transaction-success", "action", "#sold", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
					
					//limits update
					updateBuyLimits(scale);
					
					Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.SUCCESS_BUY));
					
					MetaTools.removeDescription(event.getCurrentItem(), "player-inventory");
					
					//inventory cleanup
					removeFromInventory(event.getCurrentItem(),event);
					
					//logging
					log("sell", 
						getSelectedItem().getItemStack().getTypeId(),
						getSelectedItem().getItemStack().getData().getData(), 
						getSelectedItem().getAmount()*scale, 
						price*scale );

				}
			}
		}
		event.setCancelled(true);
	}
	

	@Override
	public void managerMode(InventoryClickEvent event) {
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		int slot = event.getSlot();	
	//	System.out.print(this.getTraderStatus().name());
		
		if ( slot < 0 )
		{
			event.setCursor(null);
			switchInventory(getBasicManageModeByWool());
			return;
		}
		
		NumberFormat f = NumberFormat.getCurrencyInstance();
		//DecimalFormat f = new DecimalFormat("#.##");
		
		if ( top )
		{
			setInventoryClicked(true);

			// Wool checking, also removing a bug that allowed placing items for sell in the wool slots 
			if ( isManagementSlot(slot, 3) ) {
				
				
				//price managing
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(2)) ) 
				{
					
					if ( !permissionsManager.has(player, "dtl.trader.managing.price") )
					{
						locale.sendMessage(player, "error-nopermission");
						//player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:price") );
					}
					else
					{
						//switch to price setting mode
						setTraderStatus(TraderStatus.MANAGE_PRICE);
						switchInventory(getBasicManageModeByWool(), "price");

						getInventory().setItem(getInventory().getSize() - 2, itemsConfig.getItemManagement(6));
						getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
						

						//send message
						locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-price");
						//player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:price") );
					}
						
				} 
				else 
				//is any mode used? return to item adding
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(6)) ) 
				{
					//close any management mode, switch to the default buy/sell management
					if ( isSellModeByWool() )
						this.setTraderStatus(TraderStatus.MANAGE_SELL);
					if ( isBuyModeByWool() )
						this.setTraderStatus(TraderStatus.MANAGE_BUY);

					switchInventory(getBasicManageModeByWool(), "manage");
					
					getInventory().setItem(getInventory().getSize() - 2, itemsConfig.getItemManagement(2) );//new ItemStack(Material.WOOL,1,(short)0,(byte)15));
					getInventory().setItem(getInventory().getSize() - 3, itemsConfig.getItemManagement(4) );// ( getBasicManageModeByWool().equals(TraderStatus.MANAGE_SELL) ?  : config.getItemManagement(3) ) );//new ItemStack(Material.WOOL,1,(short)0,(byte)( getBasicManageModeByWool().equals(TraderStatus.MANAGE_SELL) ? 11 : 12 ) ));
					
					//send message
					locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-stock");
						
				}
				else 
				//global limits management
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(4)) )
				{

					if ( !permissionsManager.has(player, "dtl.trader.managing.global-limits") )
					{
						locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy-limit") );
					}
					else
					{
						//status update
						setTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL);
						switchInventory(getBasicManageModeByWool(), "glimit");
						
						//wool update
						getInventory().setItem(getInventory().getSize()-3, itemsConfig.getItemManagement(6));
						getInventory().setItem(getInventory().getSize()-2, itemsConfig.getItemManagement(5));

						locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-global-limit");
						//send message
						//player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:global-limit") );
					}					
				} 
				else 
				//player limits management
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(5)) ) 
				{
					if ( !permissionsManager.has(player, "dtl.trader.managing.player-limits") )
					{
						locale.sendMessage(player, "error-nopermission");
						//player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy-limit") );
					}
					else
					{
						//status update
						setTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER);
						switchInventory(getBasicManageModeByWool(), "plimit");
						
						//wool update
						getInventory().setItem(getInventory().getSize()-3, itemsConfig.getItemManagement(6));
						getInventory().setItem(getInventory().getSize()-2, itemsConfig.getItemManagement(4));
	
						//send message
						locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-player-limit");
					//	player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:player-limit") );
					}					
				}
				else
				// add a nice support to this system
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(1)) )
				{
					if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
					{
						locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy") );
					}
					else
					{
						switchInventory(TraderStatus.MANAGE_BUY);
						
	
						//send message
						locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-stock");
					//	player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:buy") );
						
					}
				}
				else
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(0)) )
				{
					if ( !permissionsManager.has(player, "dtl.trader.options.sell") )
					{
						locale.sendMessage(player, "error-nopermission");
				//		player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:sell") );
					}
					else
					{
						//switch to sell mode
						//status switching included in Inventory switch
						switchInventory(TraderStatus.MANAGE_SELL);
						
						//send message
						locale.sendMessage(player, "error-nopermission");
				//		player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:sell") );
						
					}
				}
				else 
				//leaving the amount managing
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(7)) ) {

					//update amounts and status
					saveManagedAmounts();
					switchInventory(TraderStatus.MANAGE_SELL);
					

					locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-stock");
				//	player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:stock") );
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

						//show the current timeout
						if ( event.getCursor().getType().equals(Material.AIR) ) 
						{
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
								locale.sendMessage(player, "key-value", "key", "#global-timeout", "value", String.valueOf(getSelectedItem().getLimits().timeout("global")));

						}
						
						//timeout changing
						else
						{
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								if ( getSelectedItem().getLimits().get("global") == null )
									getSelectedItem().getLimits().set("global", new Limit(0,-1));
								
								if ( event.isRightClick() ) 
								{
									getSelectedItem().getLimits().get("global").changeTimeout(-calculateTimeout(event.getCursor()));
								}
								else
								{
									getSelectedItem().getLimits().get("global").changeTimeout(calculateTimeout(event.getCursor()));
								}
								
								MetaTools.removeDescription(event.getCurrentItem());
								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getLimitLore(getSelectedItem(), getTraderStatus().name(), player));

								locale.sendMessage(player, "key-change", "key", "#global-timeout", "value", String.valueOf(getSelectedItem().getLimits().timeout("global")));
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
								locale.sendMessage(player, "key-value", "key", "#player-limit", "value", String.valueOf(getSelectedItem().getLimits().timeout("player")));

			
						}
						//limit changing
						else
						{
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								if ( getSelectedItem().getLimits().get("player") == null )
									getSelectedItem().getLimits().set("player", new Limit(0,-1));
								
								if ( event.isRightClick() ) 
								{
									getSelectedItem().getLimits().get("player").changeTimeout(-calculateTimeout(event.getCursor()));
								}
								else
								{
									getSelectedItem().getLimits().get("player").changeTimeout(calculateTimeout(event.getCursor()));
								}
								
								MetaTools.removeDescription(event.getCurrentItem());
								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getPlayerLimitLore(getSelectedItem(), getTraderStatus().name(), player));
								
								//add to config 
								locale.sendMessage(player, "key-change", "key", "#player-limit", "value", String.valueOf(getSelectedItem().getLimits().timeout("player")));
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
									&& permissionsManager.has(player, "dtl.trader.managing.multiple-amounts") )
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
					 if ( equalsTraderStatus(getBasicManageModeByWool()) ) 
					 {
						 
						 if ( event.isRightClick() )
						 {
							if ( !permissionsManager.has(player, "dtl.trader.managing.stack-price") )
							{
								locale.sendMessage(player, "error-nopermission");
								selectItem(null);
								event.setCancelled(true);
								return;
							}
							 
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
							{
								//if it has the stack price change it back to "per-item" price
								if ( getSelectedItem().stackPrice() ) 
								{
									getSelectedItem().setStackPrice(false);
									locale.sendMessage(player, "key-value", "key", "#stack-price", "value", "#disabled");
								} 
								//change the price to a stack-price
								else
								{
									getSelectedItem().setStackPrice(true);
									locale.sendMessage(player, "key-value", "key", "#stack-price", "value", "#enabled");
								}
								
								MetaTools.removeDescription(event.getCurrentItem());
								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getManageLore(getSelectedItem(), getTraderStatus().name(), player));
								
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
								 item.setAmount(event.getCursor().getAmount());
								 
								 //set the item to the stock
								 if ( this.isBuyModeByWool() )
								 {
									 trait.getStock().addItem("buy", item);
									 getStock().addItem("buy", item);
								 }
								 if ( this.isSellModeByWool() )
								 {
									 trait.getStock().addItem("sell", item);
									 getStock().addItem("sell", item);
								 }

								 locale.sendMessage(player, "trader-stock-item-add");
							 }
							 
							 //select an item if it exists in the traders inventory
							 if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
							 {
								 getSelectedItem().setSlot(-2);
								 locale.sendMessage(player, "trader-stock-item-select");
							 }
							 
							 //set the managed items slot
							 item.setSlot(slot);
							 item.setAsPatternItem(false);
							 locale.sendMessage(player, "trader-stock-item-update");
							 
						} 
						else 
						{

							 //select an item if it exists in the traders inventory
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
							{
								getSelectedItem().setSlot(-2);
								locale.sendMessage(player, "trader-stock-item-select");
							//	player.sendMessage( localeManager.getLocaleString("xxx-item", "action:selected") );
							}
							
						}
						return;
					} 
					else
					//managing multiple amounts
					if ( equalsTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT) )
					{
						
						//is item id and data equal?
						if ( !equalsSelected(event.getCursor(),false,false) 
								&& !event.getCursor().getType().equals(Material.AIR) ) {
							
							locale.sendMessage(player, "trader-stock-item-invalid");
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
								locale.sendMessage(player, "key-value", "key", "#price", "value", f.format(getSelectedItem().getRawPrice()).replace("$", ""));
							
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
								getSelectedItem().setPatternPrice(false);
								
								MetaTools.removeDescription(event.getCurrentItem());
								event.setCurrentItem(TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getPriceLore(getSelectedItem(), 0, getBasicManageModeByWool().toString(), getStock().getPatterns(), player)));

								locale.sendMessage(player, "key-change", "key", "#price", "value", f.format(getSelectedItem().getRawPrice()).replace("$", ""));
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
								locale.sendMessage(player, "key-value", "key", "#global-limit", "value", String.valueOf(getSelectedItem().getLimits().get("global").getLimit()));
							//	player.sendMessage( localeManager.getLocaleString("xxx-value", "manage:global-limit").replace("{value}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()) );
							}
							
							
						} 
						//change limits
						else 
						{
							
							//select the item
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								if ( getSelectedItem().getLimits().get("global") == null )
									getSelectedItem().getLimits().set("global", new Limit(0,-1));
								
								if ( event.isRightClick() ) 
									getSelectedItem().getLimits().get("global").changeLimit(-calculateLimit(event.getCursor()));
								else
									getSelectedItem().getLimits().get("global").changeLimit(calculateLimit(event.getCursor()));

								MetaTools.removeDescription(event.getCurrentItem());
								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getLimitLore(getSelectedItem(), getTraderStatus().name(), player));
								
								getSelectedItem().setAsPatternItem(false);
								locale.sendMessage(player, "key-change", "key", "#global-limit", "value", String.valueOf(getSelectedItem().getLimits().get("global").getLimit()));
							
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
								locale.sendMessage(player, "key-value", "key", "#player-limit", "value", String.valueOf(getSelectedItem().getLimits().get("player").getLimit()));
							}
							
							
						} 
						//change limits
						else 
						{
							
							//select the item
							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
							{
								if ( getSelectedItem().getLimits().get("player") == null )
									getSelectedItem().getLimits().set("player", new Limit(0,-1));
								
								if ( event.isRightClick() ) 
									getSelectedItem().getLimits().get("player").changeLimit(-calculateLimit(event.getCursor()));
								else
									getSelectedItem().getLimits().get("player").changeLimit(calculateLimit(event.getCursor()));
								
								MetaTools.removeDescription(event.getCurrentItem());
								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getPlayerLimitLore(getSelectedItem(), getTraderStatus().name(), player));
								
								getSelectedItem().setAsPatternItem(false);
								locale.sendMessage(player, "key-value", "key", "#player-limit", "value", String.valueOf(getSelectedItem().getLimits().get("player").getLimit()));
							
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
		{//System.out.print(equalsTraderStatus(getBasicManageModeByWool()));
			//is item managing
			if ( equalsTraderStatus(getBasicManageModeByWool()) )
			{
				
				//is an item is selected
				if ( getInventoryClicked() && hasSelectedItem() ) {

					//remove it from the stock
					if ( equalsTraderStatus(TraderStatus.MANAGE_SELL) )
					{
						trait.getStock().removeItem("sell", getSelectedItem().getSlot());
						getStock().removeItem("sell", getSelectedItem().getSlot());
					}
					if ( equalsTraderStatus(TraderStatus.MANAGE_BUY) )
					{
						trait.getStock().removeItem("buy", getSelectedItem().getSlot());
						getStock().removeItem("sell", getSelectedItem().getSlot());
					}
					
					//reset the item
					selectItem(null);
					
					//send a message
					locale.sendMessage(player, "trader-stock-item-remove");
				} 
				else
				{
					if ( event.getCurrentItem().getTypeId() != 0 && !hasSelectedItem() ) 
					{
						selectItem( toStockItem(event.getCurrentItem()) );
						locale.sendMessage(player, "trader-stock-item-select");
					}
				}
			} 
			
			setInventoryClicked(false);
		}
	}

	@Override
	public boolean onRightClick(Player player, TraderTrait trait, NPC npc) 
	{
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& !permissionsManager.has(player, "dtl.trader.bypass.creative") )
		{
			locale.sendMessage(player, "error-nopermission-creative");
			return false;
		}
		if ( player.getItemInHand().getTypeId() == itemsConfig.getManageWand().getTypeId() )
		{
			
			if ( !permissionsManager.has(player, "dtl.trader.bypass.managing") 
				&& !player.isOp() )
			{
				if ( !permissionsManager.has(player, "dtl.trader.options.manage") )
				{
					locale.sendMessage(player, "error-nopermission");
					return false;
				}
				if ( !trait.getConfig().getOwner().equals(player.getName()) )
				{
					locale.sendMessage(player, "error-nopermission");
					return false;
				}
			}
			
			if ( getTraderStatus().isManaging() )
			{
				switchInventory( getStartStatus(player) );
				locale.sendMessage(player, "managermode-disabled", "npc", npc.getFullName());
				return true;
			}	

			locale.sendMessage(player, "managermode-enabled", "npc", npc.getFullName());
	
			switchInventory( getManageStartStatus(player) );
			return true;
		}

		MetaTools.removeDescriptions(player.getInventory());
		if ( !getTraderStatus().isManaging() )
			loadDescriptions(player, player.getInventory());	

	//	System.out.print('a');
		player.openInventory(getInventory());
		return true;
	}

	@Override
	public EType getType() {
		return EType.SERVER_TRADER;
	}
	
}


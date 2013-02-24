package net.dandielo.citizens.trader.types;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.TraderTrait.EType;
import net.dandielo.citizens.trader.events.TraderOpenEvent;
import net.dandielo.citizens.trader.limits.Limits;
import net.dandielo.citizens.trader.limits.Limits.Limit;
import net.dandielo.citizens.trader.objects.NBTTagEditor;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.parts.TraderStockPart;

public class MarketTrader extends Trader {

	//private TransactionPattern pattern;
	
	public MarketTrader(TraderTrait trait, NPC npc, Player player) {
		super(trait, npc, player);
		//pattern = patterns.getPattern(this.getTraderConfig().getPattern());
	}

	@Override
	public void simpleMode(InventoryClickEvent event) 
	{
		DecimalFormat f = new DecimalFormat("#.##");
		
	//	Player p = (Player) event.getWhoClicked();
		int slot = event.getSlot();
		
		if ( slot < 0 )
		{
			event.setCancelled(true);
			return;
		}
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( top ) {
			

			if ( event.isShiftClick() )
			{
				((Player)event.getWhoClicked()).sendMessage(ChatColor.GOLD + "You can't shift click this, Sorry");
				event.setCancelled(true);
				return;
			}
			
			if ( isManagementSlot(slot, 1) ) {
				
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
					//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-xxx","object:tab") );
					}
					else
					{
						switchInventory(TraderStatus.SELL);	
					//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-tab","transaction:sell") );
					}
						
					
				}
				else 
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(1)) ) 
				{
					if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
					{
						locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-xxx","object:tab") );
					}
					else
					{
						switchInventory(TraderStatus.BUY);	
					//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-tab","transaction:buy") );
					}	
				}
			} 
			else
			//is slot management
			if ( equalsTraderStatus(TraderStatus.SELL) ) 
			{

				if ( selectItem(slot, TraderStatus.SELL).hasSelectedItem() )
				{
					
					if ( getSelectedItem().hasMultipleAmounts() 
							&& permissionsManager.has(player, "dtl.trader.options.sell-amounts"))
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
							locale.sendMessage(player, "trader-transaction-failed-limit");
						}
						else
						if ( !inventoryHasPlace(0) )
						{
							locale.sendMessage(player, "trader-transaction-failed-inventory");
						}
						else
						if ( !buyTransaction(price) )
						{
							locale.sendMessage(player, "trader-transaction-failed-money");
						}
						else
						{
							locale.sendMessage(player, "trader-transaction-success", "action", "#bought", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
						

							addSelectedToInventory(0);
							updateLimits();
							
							//logging
							log("buy", 
								getSelectedItem().getItemStack().getTypeId(),
								getSelectedItem().getItemStack().getData().getData(), 
								getSelectedItem().getAmount(), 
								price );


							//remove items if bought them all
							if ( !checkLimits() )
							{
								getStock().removeItem("sell", slot);
								this.switchInventory(getTraderStatus());
							}
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
						locale.sendMessage(player, "trader-transaction-failed-limit");
					}
					else
					if ( !inventoryHasPlace(slot) )
					{
						locale.sendMessage(player, "trader-transaction-failed-inventory");
					}
					else
					if ( !buyTransaction(price) ) 
					{
						locale.sendMessage(player, "trader-transaction-failed-money");
					}
					else
					{
						locale.sendMessage(player, "trader-transaction-success", "action", "#bought", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
						
						addSelectedToInventory(slot);

						updateLimits(slot);
						switchInventory(getSelectedItem());
						
						//logging
						log("buy", 
							getSelectedItem().getItemStack().getTypeId(),
							getSelectedItem().getItemStack().getData().getData(), 
							getSelectedItem().getAmount(slot), 
							price );
						
						//removing item if all are bought
						if ( !checkLimits(slot) )
						{
							getStock().removeItem("sell", slot);
							this.switchInventory(this.getTraderStatus());
						}
					} 
				}
			} 
			setInventoryClicked(true);
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
						locale.sendMessage(player, "trader-transaction-failed-limit");
					}
					else
					if ( !sellTransaction(price*scale, event.getCurrentItem()) )
					{
						locale.sendMessage(player, "trader-transaction-failed-money");
					}
					else
					{
						locale.sendMessage(player, "trader-transaction-success", "action", "#sold", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
					

						removeFromInventory(event.getCurrentItem(),event);
						
						//logging
						log("sell", 
							getSelectedItem().getItemStack().getTypeId(),
							getSelectedItem().getItemStack().getData().getData(), 
							getSelectedItem().getAmount()*scale, 
							price*scale );


						addItem(getSelectedItem().getItemStack(), scale, getSelectedItem());
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
			if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true).hasSelectedItem() )
			{
				
				double price = getPrice(player, "buy");
				int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
				if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
				{
					locale.sendMessage(player, "error-nopermission");
				}
				else
				if ( !checkBuyLimits(scale) )
				{
					locale.sendMessage(player, "trader-transaction-failed-limit");
				}
				else
				if ( !sellTransaction(price*scale, event.getCurrentItem()) )
				{
					locale.sendMessage(player, "trader-transaction-failed-money");
				}
				else
				{
					locale.sendMessage(player, "trader-transaction-success", "action", "#sold", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
				
					updateBuyLimits(scale);
					removeFromInventory(event.getCurrentItem(),event);
					
					//logging
					log("sell", 
						getSelectedItem().getItemStack().getTypeId(),
						getSelectedItem().getItemStack().getData().getData(), 
						getSelectedItem().getAmount()*scale, 
						price*scale );

					addItem(getSelectedItem().getItemStack(), scale, getSelectedItem());
				}
			}
			setInventoryClicked(false);
		}
		event.setCancelled(true);
	}
	
	

	@Override
	public void managerMode(InventoryClickEvent event) {

		((Player)event.getWhoClicked()).sendMessage(ChatColor.RED + "This trader type may be only mamaged using patterns!" );
		event.setCancelled(true);
		return;
	}
		
	
	public void updateItem(ItemStack itemToAdd)
	{
		
		//get the item if it exists in the inventory
		this.selectItem(itemToAdd, getBasicManageModeByWool(), false, false);
		
		//if it exist allow the event to occur (let the item disappear)
		if ( hasSelectedItem() ) 
		{			
			
			//get the items limit system
			Limits limitSystem = getSelectedItem().getLimits();

			if ( limitSystem.get("global") == null )
				limitSystem.set("global", new Limit(0, -2));
			
			//timeout set to no timeout checks (-2000 = it will never reset)
			limitSystem.get("global").setTimeout(-2000);
			
			
			int getItemsLeft = limitSystem.get("global").getLimit() - limits.getLimit(this, "global", getSelectedItem()).getAmount();
			if ( getItemsLeft < 0 )
				getItemsLeft = 0;
			
			//set the new limit (how many items can players buy)
			limitSystem.get("global").setLimit(getItemsLeft + itemToAdd.getAmount());

			
			//set the amount to 0 to push it but don't change the top items amount 
			itemToAdd.setAmount(0);
			//event.setCurrentItem(itemToAdd);
			
			
			//reset the amount
			limits.getLimit(this, "global", getSelectedItem()).setAmount(0);
			//limitSystem.setGlobalAmount(0);
		
			
			//reset
			selectItem(null);
		}
	}
	
	public boolean addItem(ItemStack itemToAdd, int scale, StockItem oldStockItem)
	{
		this.selectItem(itemToAdd, TraderStatus.SELL, false, false);
		if ( hasSelectedItem() )
			return false;
		
		Inventory inventory = this.getStock().getInventory("sell", player);
		//get the first empty item slot
		int firstEmpty = inventory.firstEmpty();

		int backUpAmount = itemToAdd.getAmount();
		
		
		//just to be sure nothing will be out of the inventory range (-3 for managing)
		if ( firstEmpty >= 0 && firstEmpty < getInventory().getSize() - 3 )
		{
			//change the item into the stock type
			StockItem stockItem = toStockItem(itemToAdd.clone());
			
			//set the item to the inventory
			if ( getTraderStatus().equals(TraderStatus.SELL) )
			{
				getInventory().setItem(firstEmpty, TraderStockPart.setLore(itemToAdd.clone(), TraderStockPart.getPriceLore(stockItem, 0, "sell", getStock().getPatterns(), player)));
			}
			
			
			//link the items! :D
			stockItem.getLimits().linkWith(oldStockItem);
			oldStockItem.getLimits().linkWith(stockItem);
			
			//disable pattern listening
			stockItem.setAsPatternItem(false);
			stockItem.setPatternPrice(false);
			
			//set the stock items slot
			stockItem.setSlot(firstEmpty);
			
			
			//set the limit system to 0/0/-2 (player empty configuration)
			Limits limitSystem = stockItem.getLimits();

			if ( limitSystem.get("global") == null )
				limitSystem.set("global", new Limit(0, -2));			
			
			//set the new limit (how many items can players buy)
			limitSystem.get("global") .setLimit(itemToAdd.getAmount()*scale);
			
			stockItem.setPatternPrice(true);
			//put it into the stock list
			getStock().addItem("sell", stockItem);
			
			
			itemToAdd.setAmount(backUpAmount);
			
			//send message
		}
		else
		{
			//TODO not enough place message
		}
		selectItem(oldStockItem);
		return true;
	}
	

	@Override
	public boolean onRightClick(Player player, TraderTrait trait, NPC npc) {
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& !permissionsManager.has(player, "dtl.trader.bypass.creative") )
		{
			locale.sendMessage(player, "error-nopermission-creative");
		//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-creative") );
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
				//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
					return false;
				}
				if ( !permissionsManager.has(player, "dtl.trader.options.market") )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
					return false;
				}
			}
			
			if ( getTraderStatus().isManaging() )
			{
				switchInventory( getStartStatus(player) );
				locale.sendMessage(player, "managermode-disabled", "npc", npc.getFullName());
			//	player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
				return true;
			}	

			locale.sendMessage(player, "managermode-enabled", "npc", npc.getFullName());
			//player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
			switchInventory(getManageStartStatus(player) );
			return true;
		}

		NBTTagEditor.removeDescriptions(player.getInventory());
		if ( !getTraderStatus().isManaging() )
			loadDescriptions(player, player.getInventory());	
		
		player.openInventory(getInventory());
		return true;
	}

	public double getPrice(Player player, String transaction)
	{
		return getPrice(player, transaction, 0);
	}
	public double getPrice(Player player, String transaction, int slot)
	{
		return getStock().getPrice(getSelectedItem(), player, transaction, slot);//.getPattern().getItemPrice(player, getSelectedItem(), transaction, slot, 0.0);
	}

	@Override
	public EType getType() {
		return EType.MARKET_TRADER;
	}
}

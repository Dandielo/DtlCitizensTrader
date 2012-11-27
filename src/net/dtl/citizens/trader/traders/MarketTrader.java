package net.dtl.citizens.trader.traders;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.TraderCharacterTrait.EcoNpcType;
import net.dtl.citizens.trader.objects.LimitSystem;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.objects.TransactionPattern;

public class MarketTrader extends Trader {

	private TransactionPattern pattern;
	
	public MarketTrader(TraderCharacterTrait trait, NPC npc, Player player) {
		super(trait, npc, player);
		//pattern = patterns.getPattern(this.getTraderConfig().getPattern());
	}

	@Override
	public void simpleMode(InventoryClickEvent event) 
	{
		
	//	Player p = (Player) event.getWhoClicked();
		DecimalFormat f = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		if ( slot < 0 )
		{
			event.setCancelled(true);
			return;
		}
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( top ) {
			
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
						player.sendMessage( localeManager.getLocaleString("lacks-permissions-xxx","object:tab") );
					}
					else
					{
						switchInventory(TraderStatus.SELL);	
						player.sendMessage( localeManager.getLocaleString("xxx-transaction-tab","transaction:sell") );
					}
						
					
				}
				else 
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(1)) ) 
				{
					if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
					{
						player.sendMessage( localeManager.getLocaleString("lacks-permissions-xxx","object:tab") );
					}
					else
					{
						switchInventory(TraderStatus.BUY);	
						player.sendMessage( localeManager.getLocaleString("xxx-transaction-tab","transaction:buy") );
					}	
				}
			} 
			else
			//is slot management
			if ( equalsTraderStatus(TraderStatus.SELL) ) 
			{

				if ( selectItem(slot, TraderStatus.SELL).hasSelectedItem() )
				{
					
					if ( getSelectedItem().hasMultipleAmouts() 
							&& permissionsManager.has(player, "dtl.trader.options.sell-amounts"))
					{

						switchInventory(getSelectedItem());
						setTraderStatus(TraderStatus.SELL_AMOUNT);
						
					} 
					else
					{
					//	if ( getClickedSlot() == slot )
					//	{
						double price = getPrice(player, "sell");
						//checks
						if ( !checkLimits() )
						{
							player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
						}
						else
						if ( !inventoryHasPlace(0) )
						{
							player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
						}
						else
						if ( !buyTransaction(price) )
						{
							player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
						}
						else
						{
						//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + getSelectedItem().getAmount() ).replace("{price}", f.format(price) ) );


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
						//	}
						} 
						//else 
						//{
						//	player.sendMessage( locale.getLocaleString("xxx-item-cost-xxx").replace("{price}", f.format(getPrice(player, "sell")) ) );
						//	player.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:buy") );
						//	setClickedSlot(slot);
					//	}
						
					}
					
				}
				
			}
			else 
			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) ) 
			{
				
				if ( !event.getCurrentItem().getType().equals(Material.AIR) ) 
				{
					
				//	if ( getClickedSlot() == slot ) 
				//	{ 

					double price = getPrice(player, "sell", slot);
					if ( !checkLimits(slot) )
					{
						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
					}
					else
					if ( !inventoryHasPlace(slot) )
					{
						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
					}
					else
					if ( !buyTransaction(price) ) 
					{
						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
					}
					else
					{
						
					//	player.sendMessage(locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + getSelectedItem().getAmount(slot) ).replace("{price}", f.format(price) ) );
						
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
				//	}
				//	else 
				//	{
						
				//		player.sendMessage( locale.getLocaleString("xxx-item-cost-xxx").replace("{price}", f.format(getPrice(player, "sell", slot)) ) );
				//		player.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:buy") );
				//		setClickedSlot(slot);
				//	}
				}
			} 
		/*	else 
			if ( equalsTraderStatus(TraderStatus.BUY) ) 
			{
				if ( selectItem(slot, TraderStatus.BUY).hasSelectedItem() ) {
					
					player.sendMessage( locale.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getPrice(player, "buy")) ) );
					player.sendMessage( locale.getLocaleString("item-buy-limit").replace("{limit}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()).replace("{amount}", "" + getSelectedItem().getLimitSystem().getGlobalAmount()) );
				
				}
			}*/
			setInventoryClicked(true);
		} 
		else
		{
			if ( equalsTraderStatus(TraderStatus.BUY) )
			{
				
				if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true,true).hasSelectedItem() ) 
				{
					
			//		if ( getClickedSlot() == slot && !getInventoryClicked() )
			//		{
					double price = getPrice(player, "buy");
					int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
					if ( !checkBuyLimits(scale) )
					{
						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:limit"));
					}
					else
					if ( !sellTransaction(price, event.getCurrentItem()) )
					{
						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:money"));
					}
					else
					{
						player.sendMessage( localeManager.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:sold").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(price*scale) ) );


						//TODO FUNCTION: ADD ITEM TO STOCK!
				//		updateBuyLimits(player.getName(), scale);
						removeFromInventory(event.getCurrentItem(),event);
						
						//logging
						log("sell", 
							getSelectedItem().getItemStack().getTypeId(),
							getSelectedItem().getItemStack().getData().getData(), 
							getSelectedItem().getAmount()*scale, 
							price*scale );


						addItem(getSelectedItem().getItemStack(), scale, getSelectedItem());
						//remove item if all are bought
					/*	if ( !checkBuyLimits(p, getSelectedItem().getSlot()) )
						{
							getTraderStock().removeItem(false, getSelectedItem().getSlot());
							this.switchInventory(this.getTraderStatus());
						}*/
					} 
				//}
			//		else
			//		{
			//			player.sendMessage( locale.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getPrice(player, "buy")*((int)event.getCurrentItem().getAmount() / getSelectedItem().getAmount())) ) );
			//			player.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:sell") );
			//			setClickedSlot(slot);
			//		}
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
			//	if ( getClickedSlot() == slot && !getInventoryClicked() )
			//	{
					
				double price = getPrice(player, "buy");
				int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
				if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
				{
					player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:permission") );
				}
				else
				if ( !checkBuyLimits(scale) )
				{
					player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:limit") );
				}
				else
				if ( !sellTransaction(price, event.getCurrentItem()) )
				{
					player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:money") );
				}
				else
				{
					
					player.sendMessage(localeManager.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:sold").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(price*scale) ) );

					//TODO FUNCTION: ADD ITEM TO STOCK!
					updateBuyLimits(scale);
					removeFromInventory(event.getCurrentItem(),event);
					
					//logging
					log("sell", 
						getSelectedItem().getItemStack().getTypeId(),
						getSelectedItem().getItemStack().getData().getData(), 
						getSelectedItem().getAmount()*scale, 
						price*scale );

					addItem(getSelectedItem().getItemStack(), scale, getSelectedItem());
				//		updateItem(getSelectedItem().getItemStack());
				/*
					if ( !checkBuyLimits(p, getSelectedItem().getSlot()) )
					{
						getTraderStock().removeItem(false, getSelectedItem().getSlot());
						this.switchInventory(this.getTraderStatus());
					}*/
				}
				/*} 
				else 
				{
					
					if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) &&
						 !event.getCurrentItem().getType().equals(Material.AIR) ) 
					{
						player.sendMessage( locale.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getPrice(player, "buy")*((int)event.getCurrentItem().getAmount() / getSelectedItem().getAmount())) ) );
						p.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:sell") );
						
						setClickedSlot(slot);
					}
					
				}*/
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
			LimitSystem limitSystem = getSelectedItem().getLimitSystem();
			
			
			//timeout set to no timeout checks (-2000 = it will never reset)
			limitSystem.setGlobalTimeout(-2000);
			
			
			int getItemsLeft = limitSystem.getGlobalLimit() - limitSystem.getGlobalAmount();
			if ( getItemsLeft < 0 )
				getItemsLeft = 0;
			
			//set the new limit (how many items can players buy)
			limitSystem.setGlobalLimit(getItemsLeft + itemToAdd.getAmount());

			//send message
			//p.sendMessage( locale.getLocaleString("item-added-selling").replace("{amount}", itemToAdd.getAmount() + "").replace( ( itemToAdd.getAmount() != 1 ? "{ending}" : "{none}"), "s" ) );
			
			
			//set the amount to 0 to push it but don't change the top items amount 
			itemToAdd.setAmount(0);
			//event.setCurrentItem(itemToAdd);
			
			
			//reset the amount
			limitSystem.setGlobalAmount(0);
		
			
			//reset
			selectItem(null);
		}
		else
		{
			//that item isn't in the stock
		//	p.sendMessage( locale.getLocaleString("item-not-in-stock") );
			
		}
	}
	
	public boolean addItem(ItemStack itemToAdd, int scale, StockItem oldStockItem)
	{
		this.selectItem(itemToAdd, TraderStatus.SELL, false, false);
		if ( hasSelectedItem() )
			return false;
		
		Inventory inventory = this.getStock().getInventory("sell", player);
	//	this.getStock().inventoryView(inventory, TraderStatus.SELL, player);
		//get the first empty item slot
		int firstEmpty = inventory.firstEmpty();

		int backUpAmount = itemToAdd.getAmount();
		
		
		//just to be sure nothing will be out of the inventory range (-3 for managing)
		if ( firstEmpty >= 0 && firstEmpty < getInventory().getSize() - 3 )
		{
			//set the item to the inventory
			if ( getTraderStatus().equals(TraderStatus.SELL) )
				getInventory().setItem(firstEmpty, itemToAdd.clone());
			
	
			//change the item into the stock type
			StockItem stockItem = toStockItem(itemToAdd.clone());
			
			
			//link the items! :D
			stockItem.getLimitSystem().linkWith(oldStockItem);
			oldStockItem.getLimitSystem().linkWith(stockItem);
			
			//disable pattern listening
			stockItem.setAsPatternItem(false);
			stockItem.setPetternListening(false);
			
			
			//set the stock items slot
			stockItem.setSlot(firstEmpty);
			
			
			//set the limit system to 0/0/-2 (player empty configuration)
			LimitSystem limitSystem = stockItem.getLimitSystem();
			limitSystem.setGlobalLimit(0);
			limitSystem.setGlobalTimeout(-2000);
			
			
			//set the new limit (how many items can players buy)
			limitSystem.setGlobalLimit(itemToAdd.getAmount()*scale);
			
			stockItem.setPetternListening(true);
			//pattern.getItemPrice(stockItem, "sell");
			//put it into the stock list
			getStock().addItem("sell", stockItem);
			
			
			itemToAdd.setAmount(backUpAmount);
			
			//send message
		//	p.sendMessage( locale.getLocaleString("xxx-item", "action:added") );
		}
		else
		{
			//TODO not enough place message
		}
		selectItem(oldStockItem);
		return true;
	}
	

	@Override
	public boolean onRightClick(Player player, TraderCharacterTrait trait, NPC npc) {
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& !permissionsManager.has(player, "dtl.trader.bypass.creative") )
		{
			player.sendMessage( localeManager.getLocaleString("lacks-permissions-creative") );
			return false;
		}
		
		if ( player.getItemInHand().getTypeId() == itemsConfig.getManageWand().getTypeId() )
		{
			
			if ( !permissionsManager.has(player, "dtl.trader.bypass.managing") 
				&& !player.isOp() )
			{
				if ( !permissionsManager.has(player, "dtl.trader.options.manage") )
				{
					player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
					return false;
				}
				if ( !permissionsManager.has(player, "dtl.trader.options.market") )
				{
					player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
					return false;
				}
			}
			
			if ( getTraderStatus().isManaging() )
			{
				switchInventory( getStartStatus(player) );
				player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
				return true;
			}	
			
			player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
			switchInventory( getManageStartStatus(player) );
			return true;
		}

		player.openInventory(getInventory());
		return true;
	}

	public double getPrice(Player player, String transaction)
	{
		return getPrice(player, transaction, 0);
	}
	public double getPrice(Player player, String transaction, int slot)
	{
		return pattern.getItemPrice(player, getSelectedItem(), transaction, slot, 0.0);
	}

	@Override
	public EcoNpcType getType() {
		return EcoNpcType.MARKET_TRADER;
	}
}

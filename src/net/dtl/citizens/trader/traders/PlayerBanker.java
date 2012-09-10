package net.dtl.citizens.trader.traders;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.containers.BankItem;
import net.dtl.citizens.trader.containers.PlayerBankAccount;
import net.dtl.citizens.trader.traders.Banker.BankTabType;
import net.dtl.citizens.trader.traders.Trader.TraderStatus;
import net.dtl.citizens.trader.traits.BankTrait;

public class PlayerBanker extends Banker {

	
	int lastSlot = -1;
	
	public PlayerBanker(NPC traderNpc, BankTrait bankConfiguragion, String player) { 
		super(traderNpc, bankConfiguragion, player);
		
		withdrawFee = config.getDouble("bank.default-withdraw-fee");
		depositFee = config.getDouble("bank.default-deposit-fee");
		initializeTabPrices();
		
		account = bankAccounts.get(player);
		if ( account == null )
		{
			if ( tabPrices.containsKey(BankTabType.Tab1) && econ.getBalance(player) < tabPrices.get(BankTabType.Tab1) )
			{
				Bukkit.getPlayerExact(player).sendMessage( locale.getLocaleString("bank-account-no-money") );
				return;
			}
			//create new account
			account = new PlayerBankAccount(player, true);
			bankAccounts.put(player, account);
		}

		tabInventory = account.inventoryTabView(tab);

		//loading trader bank config
		this.switchInventory();
	}

	
	/**
	 * Settings mode used by bankers and AuctionHouse
	 *
	 * @param event this is a pure InventoryClickEvent, all other functions are available in parent class
	 */
	@Override
	public void settingsMode(InventoryClickEvent event) {
		event.setCancelled(true);
		
		Player player = (Player) event.getWhoClicked();
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( top )
		{
			if ( getBankStatus().equals(BankStatus.SETTING_TAB_ITEM) )
				return;
		
			if ( rowClicked( 6, slot) )
			{

				if ( event.getCurrentItem().getTypeId() == 35 )
				{
					if ( !isExistingTab(this.getRowSlot(slot+1)) )
					{
						if ( lastSlot != slot )
						{
							player.sendMessage( locale.getLocaleString("bank-tab-price").replace("{price}", decimalFormat.format(this.getTabPrice(this.nextBankTab()))) );

							lastSlot = slot;
							return;
						}
						
						if ( !permissions.has(player, "dtl.banker.settings.tab-buy") )
							return;
						
						if ( !this.tabTransaction(this.nextBankTab(), player.getName()) )
						{

							player.sendMessage( locale.getLocaleString("bank-no-money") );
							return;
						}
						
						player.sendMessage( locale.getLocaleString("bank-tab-bought") );
						if ( addBankTab() )
							settingsInventory();
						lastSlot = slot;
						return;
					}
				}
				if ( event.getCurrentItem().getTypeId() != 0 )
				{
					if ( !getBankTabType().equals(BankTabType.getTabByName("tab"+(getRowSlot(slot)+1))) )							
					{
						this.setBankTabType(BankTabType.getTabByName("tab"+(getRowSlot(slot)+1)));
						player.sendMessage( locale.getLocaleString("switch-tab").replace("{name}", this.getBankTab().getTabName()) );
						settingsInventory();
					}
				} 
			
			} 
			else 
			if ( rowClicked( 5, slot) )	
			{
			/*	if ( !hasTabSize(getRowSlot(slot+1)) )
				{
					increaseTabSize();
					this.settingsInventory();
					return;
				}*/

				if ( !permissions.has(player, "dtl.banker.settings.tab-item") )
					return;
				
				if ( getBankStatus().equals(BankStatus.SETTINGS) )
				{
					player.sendMessage(locale.getLocaleString("select-tab-item"));
					setBankStatus(BankStatus.SETTING_TAB_ITEM);
				}
				else
					setBankStatus(BankStatus.SETTINGS);
			}
			
		}
		else
		{
			if ( getBankStatus().equals(BankStatus.SETTING_TAB_ITEM) )
			{
				if ( event.getCurrentItem().getTypeId() != 0 )
				{
					setBankTabItem(event.getCurrentItem());
					player.sendMessage( locale.getLocaleString("tab-item-selected").replace("{name}", event.getCurrentItem().getType().name().toLowerCase()) );
					
					setBankStatus(BankStatus.SETTINGS);
					settingsInventory();
					lastSlot = slot;
					return;
				}
				
			}
			
			
		}
		lastSlot = slot;
		
	}

	@Override
	public void simpleMode(InventoryClickEvent event) {
		
		
		Player player = (Player) event.getWhoClicked();
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		if ( slot < 0 )
		{
			//if sth is in hand just throw it out
			if ( event.getCursor().getTypeId() != 0 )
			{
				BankItem item = getSelectedItem();
				
				if ( item != null )
				{
					if ( item.getSlot() != -1 )
					{
						if ( !withdrawFee(player.getName()) )
						{
							event.setCancelled(true);
							return;
						}
						removeItemFromBankAccount(item);
						selectItem(null);
					}
					
				}
				
				return;
			}
			

		}
		
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( this.getBankStatus().equals(BankStatus.ITEM_MANAGING) )
		{
		
			if ( top )
			{
				
				if ( this.rowClicked( this.getBankTab().getTabSize() + 1, slot) )
				{
					if ( event.getCurrentItem().getTypeId() != 0 
							&& event.getCursor().getTypeId() == 0 )
					{
						if ( !getBankTabType().equals(BankTabType.getTabByName("tab"+(getRowSlot(slot)+1))) )							
						{
							this.setBankTabType(BankTabType.getTabByName("tab"+(getRowSlot(slot)+1)));
							player.sendMessage( locale.getLocaleString("switch-tab").replace("{name}", this.getBankTab().getTabName()) );
							switchInventory();
							//switchInventory();
						}
					}
				/*	if ( getRowSlot(slot) == 1 && event.getCurrentItem().getTypeId() != 0 )
					{
						this.setBankTab(BankTab.Tab2);
						switchInventory();
					}
					if ( getRowSlot(slot) == 0 && event.getCurrentItem().getTypeId() != 0 )
					{
						this.setBankTab(BankTab.Tab1);
						switchInventory();
					}*/
				
					event.setCancelled(true);
					return;
				}
				
				//tabs managing so we can switch through bank/account tabs
				if ( getBankStatus().equals(BankStatus.TAB_DISPLAY) )
				{
					
				}
				else
				{
					if ( event.isShiftClick() )
					{
						
						
						BankItem item = getSelectedItem();
						
						if ( selectItem(slot).hasSelectedItem() )
						{
							if ( !this.playerInventoryHasPlace(player) )
							{
								event.setCancelled(true);
								selectItem(item);
								return;
							}
							if ( !withdrawFee(player.getName()) )
							{

								player.sendMessage( locale.getLocaleString("bank-no-money") );
								event.setCancelled(true);
								selectItem(item);
								return;
							}
							
							event.setCurrentItem(null);
							this.removeItemFromBankAccount(getSelectedItem());
							this.addSelectedToPlayerInventory(player);
							event.setCancelled(true);
						}
						
						selectItem(item);
						
						return;
					}
				
					BankItem item = getSelectedItem();
					
	
					selectItem(slot);
					
					
					if ( item != null )
					{
						
						if ( item.getSlot() == -1 )
						{
							if ( !depositFee(player.getName()) )
							{
								player.sendMessage( locale.getLocaleString("bank-no-money") );
								event.setCancelled(true);
								selectItem(item);
								return;
							}
							
							item.setSlot(slot);
							item.getItemStack().setAmount(event.getCursor().getAmount());
							addItemToBankAccount(item);
							item = null;
						}
						
						
						
					}
	
					
						//getSelectedItem().setSlot(-2);
					
					if ( item != null ) {
						BankItem oldItem = toBankItem(item.getItemStack());
						oldItem.setSlot(item.getSlot());
						item.setSlot(slot);
						updateBankAccountItem(oldItem, item);
					}
					
				}
				
			}
			else
			{
				
				//tabs managing so we can switch through bank/account tabs
				if ( getBankStatus().equals(BankStatus.TAB_DISPLAY) )
				{
					
				}
				else
				{
					
					if ( event.isShiftClick() )
					{
						BankItem item = getSelectedItem();
						int first = getInventory().firstEmpty();
						
						if ( selectItem(toBankItem(event.getCurrentItem())).hasSelectedItem() )
						{
							if ( !this.bankerInventoryHasPlace() )
							{
								selectItem(item);
								event.setCancelled(true);
								return;
							}

							if ( !depositFee(player.getName()) )
							{
								player.sendMessage( locale.getLocaleString("bank-no-money") );
								event.setCancelled(true);
								selectItem(item);
								return;
							}
							
						//	getInventory().setItem(first, event.getCurrentItem().clone());
						//	getSelectedItem().setSlot(first);
							this.addSelectedToBankerInventory();
						//	this.addItemToBankAccount(getSelectedItem());
							
							event.setCurrentItem(null);
						}
						
						selectItem(item);
						
						return;
					}
					
	
					BankItem item = getSelectedItem();
					
					if ( item != null )
					{
						if ( item.getSlot() != -1 )
						{

							if ( !withdrawFee(player.getName()) )
							{
								player.sendMessage( locale.getLocaleString("bank-no-money") );
								event.setCancelled(true);
								return;
							}
							
							removeItemFromBankAccount(item);
						}
						
					}
	
					
					if ( selectItem(toBankItem(event.getCurrentItem())).hasSelectedItem() )
						getSelectedItem().setSlot(-1);
					
				//	selectItem(item);
					
	
			//		if ( item != null )
			//			item.setSlot(slot);
					
				}
				
			}
		}
		else
		{
			
		}
		
	}

	@Override
	public void managerMode(InventoryClickEvent event) {

		
	}

}

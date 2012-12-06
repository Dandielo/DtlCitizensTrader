package net.dtl.citizens.trader.traders;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.objects.BankItem;
import net.dtl.citizens.trader.objects.PlayerBankAccount;
import net.dtl.citizens.trader.parts.BankerPart;
import net.dtl.citizens.trader.traders.Trader.TraderStatus;

public class PrivateBanker extends Banker {

	
	int lastSlot = -1;
	
	public PrivateBanker(NPC bankerNpc, BankerPart bankConfiguragion, String player) { 
		super(bankerNpc, bankConfiguragion, player);
		
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
			{
				player.sendMessage(locale.getLocaleString("tab-setting-xxx", "setting:tab-item", "action:canceled").replace("{tab}", getBankTab().getTabName()));
				setBankStatus(BankStatus.SETTINGS);
			}
				
			if ( rowClicked( 6, slot) )
			{

				if ( event.getCurrentItem().getTypeId() == 35 )
				{
					if ( !isExistingTab(this.getRowSlot(slot+1)) )
					{
						if ( lastSlot != slot )
						{
							player.sendMessage( locale.getLocaleString("tab-price").replace("{value}", decimalFormat.format(this.getTabPrice(this.nextBankTab()))) );

							lastSlot = slot;
							return;
						}
						
						if ( !permissions.has(player, "dtl.banker.settings.tab-buy") )
						{
							player.sendMessage( locale.getLocaleString("lacks-permissions") );
							return;
						}
						
						if ( !this.tabTransaction(this.nextBankTab(), player.getName()) )
						{

							player.sendMessage( locale.getLocaleString("not-enough-money") );
							return;
						}
						
						if ( addBankTab() )
							settingsInventory();
						
						player.sendMessage( locale.getLocaleString("tab-xxx", "action:{transaction}", "transaction:bought").replace("{tab}", this.getBankTab().getTabName()) );
						lastSlot = slot;
						return;
					}
				}
				if ( event.getCurrentItem().getTypeId() != 0 )
				{
					if ( !getBankTabType().equals(BankTabType.getTabByName("tab"+(getRowSlot(slot)+1))) )							
					{
						this.setBankTabType(BankTabType.getTabByName("tab"+(getRowSlot(slot)+1)));
						player.sendMessage( locale.getLocaleString("tab-switched").replace("{tab}", this.getBankTab().getTabName()) );
						settingsInventory();
					}
				} 
			
			} 
			else 
			if ( rowClicked( 5, slot) )	
			{

				if ( !permissions.has(player, "dtl.banker.settings.tab-item") )
				{
					player.sendMessage(locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{setting}", "setting:tab-item"));
					return;
				}
				
				if ( getBankStatus().equals(BankStatus.SETTINGS) )
				{
					player.sendMessage(locale.getLocaleString("select-tab-item").replace("{tab}", getBankTab().getTabName()) );
					setBankStatus(BankStatus.SETTING_TAB_ITEM);
				}
			}
			
		}
		else
		{
			if ( getBankStatus().equals(BankStatus.SETTING_TAB_ITEM) )
			{
				if ( event.getCurrentItem().getTypeId() != 0 )
				{
					setBankTabItem(event.getCurrentItem());
					player.sendMessage( locale.getLocaleString("tab-setting-xxx", "setting:tab-item", "action:selected").replace("{tab}", getBankTab().getTabName()) );
					
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
							player.sendMessage( locale.getLocaleString("not-enough-money") );
							event.setCancelled(true);
							return;
						}
						player.sendMessage( locale.getLocaleString("item-xxx", "action:removed") );
						
						removeItemFromBankAccount(item);
						selectItem(null);
					}
					
				}
				
				return;
			}
			

		}
		
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( this.getBankStatus().equals(BankStatus.TAB_DISPLAY) )
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
							player.sendMessage( locale.getLocaleString("tab-switched").replace("{tab}", this.getBankTab().getTabName()) );
							switchInventory();
						}
					}
				
					event.setCancelled(true);
					return;
				}
				
				//tabs managing so we can switch through bank/account tabs
				else
				{
					if ( event.isShiftClick() )
					{
						
						
						BankItem item = getSelectedItem();
						
						if ( selectItem(slot).hasSelectedItem() )
						{
							if ( !this.playerInventoryHasPlace(player) )
							{
								player.sendMessage( locale.getLocaleString("not-enough-space", "entity:player") );
								event.setCancelled(true);
								selectItem(item);
								return;
							}
							if ( !withdrawFee(player.getName()) )
							{

								player.sendMessage( locale.getLocaleString("not-enough-money") );
								event.setCancelled(true);
								selectItem(item);
								return;
							}
							player.sendMessage( locale.getLocaleString("item-xxx", "action:withdrawed") );
							
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
								player.sendMessage( locale.getLocaleString("not-enough-money") );
								event.setCancelled(true);
								selectItem(item);
								return;
							}
							
							player.sendMessage( locale.getLocaleString("item-xxx", "action:deposited") );
							
							item.setSlot(slot);
							item.getItemStack().setAmount(event.getCursor().getAmount());
							addItemToBankAccount(item);
							item = null;
						}
						
						
						
					}
	
					
						//getSelectedItem().setSlot(-2);
					
					if ( item != null ) {
						BankItem oldItem = toBankItem(item.getItemStack());
						player.sendMessage( locale.getLocaleString("item-xxx", "action:updated") );
						
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
						
						if ( selectItem(toBankItem(event.getCurrentItem())).hasSelectedItem() )
						{
							if ( !this.bankerInventoryHasPlace() )
							{
								player.sendMessage( locale.getLocaleString("not-enough-space", "entity:banker") );
							
								selectItem(item);
								event.setCancelled(true);
								return;
							}

							if ( !depositFee(player.getName()) )
							{
								player.sendMessage( locale.getLocaleString("not-enough-money") );
								event.setCancelled(true);
								selectItem(item);
								return;
							}
							
							player.sendMessage( locale.getLocaleString("item-xxx", "action:deposited") );
							this.addSelectedToBankerInventory();
							
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
								player.sendMessage( locale.getLocaleString("not-enough-money") );
								event.setCancelled(true);
								return;
							}
							
							player.sendMessage( locale.getLocaleString("item-xxx", "action:withdrawed") );
							removeItemFromBankAccount(item);
						}
						
					}
	
					
					if ( selectItem(toBankItem(event.getCurrentItem())).hasSelectedItem() )
						getSelectedItem().setSlot(-1);
					
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


	@Override
	public boolean onRightClick(Player player, TraderCharacterTrait trait, NPC npc) {
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& permissions.has(player, "dtl.banker.bypass.creative") )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-creative") );
			return false;
		}
		
		if ( player.getItemInHand().getTypeId() == itemConfig.getManageWand().getTypeId() )
		{
			
			if ( !permissions.has(player, "dtl.banker.options.manage") 
					&& !permissions.has(player, "dtl.banker.bypass.managing")
					&& !player.isOp() )
			{
				player.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "setting:banker") );
				return false;
			}
				
			
		//	if ( getTraderStatus() )
			{
			//	setTraderStatus(TraderStatus.BANK);
				player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
				
			//	return false;
			}	
			
			player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
		//	setTraderStatus(TraderStatus.MANAGE);
			
			return false;
		}
		else
		if ( player.getItemInHand().getTypeId() == itemConfig.getSettingsWand().getTypeId() )
		{
			//if ( TraderStatus.hasManageMode(this.getTraderStatus()) )
			{
			//	setTraderStatus(TraderStatus.BANK);
				player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
				
			//	return true;
			}	
			
			//setTraderStatus(TraderStatus.BANK_SETTINGS);
			useSettingsInv();
			settingsInventory();
			setBankStatus(BankStatus.SETTINGS);
		}
		else
		{
			player.sendMessage( locale.getLocaleString("xxx-value", "manage:withdraw-fee").replace("{value}", new DecimalFormat("#.##").format(getWithdrawFee()) ) );
			player.sendMessage( locale.getLocaleString("xxx-value", "manage:deposit-fee").replace("{value}", new DecimalFormat("#.##").format(getDepositFee()) ) );
			
			//player.sendMessage( locale.getLocaleString("bank-deposit-fee").replace("{fee}", new DecimalFormat("#.##").format(getDepositFee())) );
			//player.sendMessage( locale.getLocaleString("bank-withdraw-fee").replace("{fee}", new DecimalFormat("#.##").format(getWithdrawFee())) );
		}

		//if ( !TraderStatus.hasManageMode(this.getTraderStatus()) )
	//		player.openInventory(getInventory());
		return true;
		
	}

}

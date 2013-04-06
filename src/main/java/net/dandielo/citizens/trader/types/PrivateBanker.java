package net.dandielo.citizens.trader.types;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.TraderTrait.EType;
import net.dandielo.citizens.trader.objects.BankItem;
import net.dandielo.citizens.trader.parts.BankerPart;

public class PrivateBanker extends Banker {

	
	int lastSlot = -1;
	
	public PrivateBanker(NPC bankerNpc, BankerPart bankConfiguragion, String player) { 
		super(bankerNpc, bankConfiguragion, player);

		account = accounts.getAccount(player);
		
		tabInventory = account.inventoryView(player+"s bank account");
	}

	@Override
	public void settingsMode(InventoryClickEvent event) 
	{
		event.setCancelled(true);
		
		Player player = (Player) event.getWhoClicked();
		NumberFormat decimalFormat = NumberFormat.getCurrencyInstance();
		//DecimalFormat decimalFormat = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( top )
		{
			if ( getStatus().equals(BankStatus.SETTING_TAB) )
			{
				locale.sendMessage(player, "banker-tab-select-item-canceled");
			//	player.sendMessage(locale.getLocaleString("tab-setting-xxx", "setting:tab-item", "action:canceled").replace("{tab}", getTab().getName()));
				setStatus(BankStatus.SETTINGS);
			}
				
			if ( rowClicked(getTab().getTabSize()+1, slot) )
			{
				if ( event.getCurrentItem().getTypeId() == 35 )
				{
					if ( !hasTab(getRowSlot(slot)) )
					{
						if ( lastSlot != slot )
						{
							locale.sendMessage(player, "key-value", "key", "#tab-price", "value", decimalFormat.format(BankerPart.getTabPrice(tabs())).replace("$", ""));
						//	player.sendMessage( locale.getLocaleString("tab-price").replace("{value}", decimalFormat.format(BankerPart.getTabPrice(tabs()))) );

							lastSlot = slot;
							return;
						}
						
						if ( !permissions.has(player, "dtl.banker.settings.tab-buy") )
						{
							locale.sendMessage(player, "error-nopermission");
						//	player.sendMessage( locale.getLocaleString("lacks-permissions") );
							return;
						}
						
						if ( !tabTransaction(tabs(), player.getName()) )
						{
							locale.sendMessage(player, "banker-tab-buy-failed");
						//	player.sendMessage( locale.getLocaleString("not-enough-money") );
							return;
						}
						
						if ( addBankTab() )
						{
							setTab(tabs()-1);
							settingsInventory();
						}

						locale.sendMessage(player, "banker-tab-buy-success");
						//player.sendMessage( locale.getLocaleString("tab-xxx", "action:{transaction}", "transaction:bought").replace("{tab}", this.getTab().getName()) );
						lastSlot = slot;
						return;
					}
				}
				if ( event.getCurrentItem().getTypeId() != 0 )
				{
					if ( getTab().getId() != getTab(getRowSlot(slot)).getId() )							
					{
						setTab(getRowSlot(slot));
						locale.sendMessage(player, "banker-tab-switch", "tab", getTab().getName());
					//	player.sendMessage( locale.getLocaleString("tab-switched").replace("{tab}", getTab().getName()) );
						settingsInventory();
					}
				} 
			
			} 
			else 
			if ( rowClicked(getTab().getTabSize(), slot) )	
			{

				if ( !permissions.has(player, "dtl.banker.settings.tab-item") )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage(locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{setting}", "setting:tab-item"));
					return;
				}
				
				if ( getStatus().equals(BankStatus.SETTINGS) )
				{
					locale.sendMessage(player, "banker-tab-select-item");
				//	player.sendMessage(locale.getLocaleString("select-tab-item").replace("{tab}", getTab().getName()) );
					setStatus(BankStatus.SETTING_TAB);
				}
			}
			
		}
		else
		{
			if ( getStatus().equals(BankStatus.SETTING_TAB) )
			{
				if ( event.getCurrentItem().getTypeId() != 0 )
				{
					getTab().setTabItem(toBankItem(event.getCurrentItem()));
					locale.sendMessage(player, "banker-tab-select-item-success");
				//	player.sendMessage( locale.getLocaleString("tab-setting-xxx", "setting:tab-item", "action:selected").replace("{tab}", getTab().getName()) );
					
					setStatus(BankStatus.SETTINGS);
					settingsInventory();
					lastSlot = slot;
					return;
				}
				
			}
			
			
		}
		lastSlot = slot;
	}

	@Override
	public void simpleMode(InventoryClickEvent event)
	{

		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
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
						if ( !withdrawFee(player) )
						{
							locale.sendMessage(player, "banker-withdraw-failed-money");
						//	player.sendMessage( locale.getLocaleString("not-enough-money") );
							event.setCancelled(true);
							return;
						}
						locale.sendMessage(player, "banker-withdraw-item");
					//	player.sendMessage( locale.getLocaleString("item-xxx", "action:removed") );
						
						removeItemFromAccount(item);
						selectItem(null);
					}
					
				}
				
				return;
			}
			

		}
		
	 
		if ( top )
		{
			if ( rowClicked( getTab().getTabSize() + 1, slot) )
			{
				if ( event.getCurrentItem().getTypeId() != 0 )
				{
					if ( event.getCursor().getTypeId() == 0 )
					{
						if ( !getTab().getName().equals( getTab(getRowSlot(slot)).getId() ) )							
						{
							setTab( getTab(getRowSlot(slot)).getId() );
							locale.sendMessage(player, "banker-tab-switch", "tab", getTab().getName());
						//	player.sendMessage( locale.getLocaleString("tab-switched").replace("{tab}", getTab().getName()) );
							switchInventory();
						}
					}
					//Add the option to send an item into the tab that is clicked
					else
					{
						
					}
				}
				
				event.setCancelled(true);
				return;
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
							//TODO check
							locale.sendMessage(player, "banker-withdraw-failed-inventory");;
							event.setCancelled(true);
							selectItem(item);
							return;
						}
						if ( !withdrawFee(player) )
						{
							//TODO check
							locale.sendMessage(player, "banker-withdraw-failed-money");
							event.setCancelled(true);
							selectItem(item);
							return;
						}
						locale.sendMessage(player, "banker-withdraw-success");
						
						event.setCurrentItem(null);
						removeItemFromAccount(getSelectedItem());
						addSelectedToPlayerInventory(player);
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
						if ( !depositFee(player) )
						{
							locale.sendMessage(player, "banker-deposit-failed-money");
						//	player.sendMessage( locale.getLocaleString("not-enough-money") );
							event.setCancelled(true);
							selectItem(item);
							return;
						}

						locale.sendMessage(player, "banker-deposit-success");
					//	player.sendMessage( locale.getLocaleString("item-xxx", "action:deposited") );
						
						item.setSlot(slot);
						item.getItemStack().setAmount(event.getCursor().getAmount());
						addItemToAccount(item);
						item = null;
					}
					
					
					
				}

				
					//getSelectedItem().setSlot(-2);
				
				if ( item != null ) {
					BankItem oldItem = toBankItem(item.getItemStack());
					locale.sendMessage(player, "banker-item-update");
				//	player.sendMessage( locale.getLocaleString("item-xxx", "action:updated") );
					
					oldItem.setSlot(item.getSlot());
					item.setSlot(slot);
					updateAccountItem(oldItem, item);
				}
				
			}
			
		}
		else
		{
			System.out.print("bottom?!");
			//tabs managing so we can switch through bank/account tabs
				
			if ( event.isShiftClick() )
			{
				BankItem item = getSelectedItem();
				
				if ( selectItem(toBankItem(event.getCurrentItem())).hasSelectedItem() )
				{
					if ( !bankerInventoryHasPlace() )
					{
						//TODO check
						locale.sendMessage(player, "banker-deposit-failed-inventory");
					//	player.sendMessage( locale.getLocaleString("not-enough-space", "entity:banker") );
					
						selectItem(item);
						event.setCancelled(true);
						return;
					}

					if ( !depositFee(player) )
					{
						//TODO check
						locale.sendMessage(player, "banker-deposit-failed-money");
					//	player.sendMessage( locale.getLocaleString("not-enough-money") );
						event.setCancelled(true);
						selectItem(item);
						return;
					}

					locale.sendMessage(player, "banker-deposit-success");
				//	player.sendMessage( locale.getLocaleString("item-xxx", "action:deposited") );
					addSelectedToBankerInventory();
					
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

					if ( !withdrawFee(player) )
					{
						//TODO check
						locale.sendMessage(player, "banker-withdraw-falied-money");
					//	player.sendMessage( locale.getLocaleString("not-enough-money") );
						event.setCancelled(true);
						return;
					}

					locale.sendMessage(player, "banker-withdraw-success");
				//	player.sendMessage( locale.getLocaleString("item-xxx", "action:withdrawed") );
					removeItemFromAccount(item);
				}
				
			}

			
			if ( selectItem(toBankItem(event.getCurrentItem())).hasSelectedItem() )
				getSelectedItem().setSlot(-1);
			
		}
	
	}

	@Override
	public void managerMode(InventoryClickEvent event) {

		
	}


	@Override
	public boolean onRightClick(Player player, TraderTrait trait, NPC npc) {
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& !permissions.has(player, "dtl.banker.bypass.creative") )
		{
			locale.sendMessage(player, "error-nopermission-creative");
		//	player.sendMessage( locale.getLocaleString("lacks-permissions-creative") );
			return false;
		}
		
		if ( player.getItemInHand().getTypeId() == itemConfig.getSettingsWand().getTypeId() )
		{
			useSettingsInv();
			settingsInventory();
			setStatus(BankStatus.SETTINGS);
		}

		player.openInventory(getInventory());
		return true;
		
	}

	@Override
	public EType getType() {
		return EType.PRIVATE_BANKER;
	}

}

package net.dtl.citizenstrader_new.traders;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.containers.BankItem;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.BankTrait;

public class PlayerBanker extends Banker {

	public PlayerBanker(NPC traderNpc, BankTrait bankConfiguragion, String player) { 
		super(traderNpc, bankConfiguragion, player);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void settingsMode(InventoryClickEvent event) {
		//we just click nothing else :P
		event.setCancelled(true);
		
		Player player = (Player) event.getWhoClicked();
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		
		if ( lastRowClicked(slot) )
		{
			
			if ( event.getCurrentItem().getTypeId() != 0 )
			{
				if ( !getBankTab().equals(BankTab.getTabByName("tab"+(getRowSlot(slot)+1))) )							
				{
					this.setBankTab(BankTab.getTabByName("tab"+(getRowSlot(slot)+1)));
					settingsInventory();
				}
			}
		}
		
		//add tab button
		if ( slot == 0 )
		{
			if ( hasAllTabs() )
				return;
			addBankTab();
			settingsInventory();
		}
		
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
				
				if ( this.lastRowClicked(slot) )
				{
					if ( event.getCurrentItem().getTypeId() != 0 
							&& event.getCursor().getTypeId() == 0 )
					{
						if ( !getBankTab().equals(BankTab.getTabByName("tab"+(getRowSlot(slot)+1))) )							
						{
							this.setBankTab(BankTab.getTabByName("tab"+(getRowSlot(slot)+1)));
							switchInventory();
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

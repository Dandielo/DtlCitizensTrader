package net.dtl.citizenstrader_new.traders;

import java.text.DecimalFormat;

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
					}
					
				}
				
				return;
			}
			
			//status change
			if ( getBankStatus().equals(BankStatus.TAB_DISPLAY) )
			{
				setBankStatus(BankStatus.ITEM_MANAGING);
			}
			else	
			{
				setBankStatus(BankStatus.TAB_DISPLAY);
			}

		}
		
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		
		if ( top )
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

	@Override
	public void managerMode(InventoryClickEvent event) {

		
	}

}

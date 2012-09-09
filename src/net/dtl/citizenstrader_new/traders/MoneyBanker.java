package net.dtl.citizenstrader_new.traders;

import java.text.DecimalFormat;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.CitizensTrader;
import net.dtl.citizenstrader_new.containers.BankItem;
import net.dtl.citizenstrader_new.containers.PlayerBankAccount;
import net.dtl.citizenstrader_new.traits.BankTrait;
import net.milkbowl.vault.economy.Economy;

public class MoneyBanker extends Banker {
	
	private static ItemStack exchangeItem;
	private static double itemValue; 
	private String player;
	
	public MoneyBanker(NPC traderNpc, BankTrait bankConfiguragion, String player) { 
		super(traderNpc, bankConfiguragion, player);

		account = new PlayerBankAccount(player, false);
		
		this.player = player;

		exchangeItem = new BankItem(config.getString("money-bank.exchange-item", "388")).getItemStack();
		itemValue = config.getDouble("money-bank.item-value", 10.0);

		tabInventory = account.cleanInventory(54, "Banker " + npc.getName());

		switchInventory2();
	}


	public void switchInventory2()
	{
		double balance = econ.getBalance(this.player);
		int amount = (int) (balance / itemValue);
		selectItem(toBankItem(exchangeItem));
		this.addAmountToBankerInventory(tabInventory, amount);
		selectItem(null);
	}
	
	
	@Override
	public void settingsMode(InventoryClickEvent event) {
		//we just click nothing else :P
	/*	event.setCancelled(true);
		
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
				
				if ( event.getCurrentItem().getTypeId() != 0 )
				{
					if ( !getBankTab().equals(BankTab.getTabByName("tab"+(getRowSlot(slot)+1))) )							
					{
						this.setBankTab(BankTab.getTabByName("tab"+(getRowSlot(slot)+1)));
						settingsInventory();
					}
				}
			
			} 
			else 
			if ( rowClicked( 5, slot) )	
			{
				if ( getBankStatus().equals(BankStatus.SETTINGS) )
					setBankStatus(BankStatus.SETTING_TAB_ITEM);
				else
					setBankStatus(BankStatus.SETTINGS);
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
		else
		{
			if ( getBankStatus().equals(BankStatus.SETTING_TAB_ITEM) )
			{
				if ( event.getCurrentItem().getTypeId() != 0 )
				{
					setBankTabItem(event.getCurrentItem());
					setBankStatus(BankStatus.SETTINGS);
					settingsInventory();
					return;
				}
				
			}
			
			
		}*/
		
	}

	@Override
	public void simpleMode(InventoryClickEvent event) {
		
		Player p = (Player) event.getWhoClicked();
		String player = (String) event.getWhoClicked().getName();
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		if ( slot < 0 )
		{
			
			return;
		}
		
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		ItemStack current = event.getCurrentItem();
		ItemStack cursor = event.getCursor();
		
		if ( top )
		{
			if ( event.isShiftClick() )
			{
				if ( current.getTypeId() != exchangeItem.getTypeId() )
				{
					p.sendMessage( locale.getLocaleString("mbanker-wrong-item") );
					event.setCancelled(true);
					return;
				}
				
				if ( current.getTypeId() != 0 )
				{
					double withdraw = current.getAmount()*itemValue;
					econ.withdrawPlayer(player, withdraw);
					p.sendMessage( locale.getLocaleString("mbanker-got-item").replace("{item}", current.getType().name()) );
					p.sendMessage( locale.getLocaleString("mbanker-lost-money").replace("{money}", decimalFormat.format(withdraw)) );
				}
				if ( cursor.getTypeId() != 0 )
				{
					return;
				}
			}
			else
			{
				if ( current.getTypeId() != 0 )
				{
					double withdraw = current.getAmount()*itemValue;
					if ( event.isRightClick() )
						withdraw = ((current.getAmount()/2)+1)*itemValue;
						
					econ.withdrawPlayer(player, withdraw);
					p.sendMessage( locale.getLocaleString("mbanker-got-item").replace("{item}", current.getType().name()) );
					p.sendMessage( locale.getLocaleString("mbanker-lost-money").replace("{money}", decimalFormat.format(withdraw)) );
				}
				if ( cursor.getTypeId() != 0 )
				{
					if ( cursor.getTypeId() != exchangeItem.getTypeId() )
					{
						p.sendMessage( locale.getLocaleString("mbanker-wrong-item") );
						event.setCancelled(true);
						return;
					}
					
					double deposit = cursor.getAmount()*itemValue;
					if ( event.isRightClick() )
						deposit = itemValue;
					
					econ.depositPlayer(player, deposit);
					p.sendMessage( locale.getLocaleString("mbanker-lost-item").replace("{item}", cursor.getType().name()) );
					p.sendMessage( locale.getLocaleString("mbanker-got-money").replace("{money}", decimalFormat.format(deposit)) );
				}
			}
		}
		else
		{
			if ( event.isShiftClick() )
			{
				if ( current.getTypeId() != exchangeItem.getTypeId() )
				{
					p.sendMessage( locale.getLocaleString("mbanker-wrong-item") );
					event.setCancelled(true);
					return;
				}
				
				if ( current.getTypeId() != 0 )
				{
					double deposit = current.getAmount()*itemValue;
					econ.depositPlayer(player, deposit);
					p.sendMessage( locale.getLocaleString("mbanker-lost-item").replace("{item}", current.getType().name()) );
					p.sendMessage( locale.getLocaleString("mbanker-got-money").replace("{money}", decimalFormat.format(deposit)) );
				}
			}
		}
		
		
	}

	@Override
	public void managerMode(InventoryClickEvent event) {

		
	}

	
	
	
	public boolean addAmountToBankerInventory(Inventory nInventory, int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;
		
		
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			while ( amountToAdd > 0 )
			{
			
				ItemStack is = selectedItem.getItemStack().clone();
				is.setAmount(amountToAdd);
				
				//create a new bank item
				
				inventory.setItem(inventory.firstEmpty(), is);
				amountToAdd -= 64;
			}
			/* *
			 * setting the item into a free slot
			 * don't using the addItem() bacause it's a workaround for this function
			 * 
			 */
			return true;
		}
		
		/* *
		 * Item couldn't be added to the inventory
		 * 
		 */
		return false;
	}
	
	
	
}

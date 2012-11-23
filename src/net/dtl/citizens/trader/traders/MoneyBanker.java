package net.dtl.citizens.trader.traders;

import java.text.DecimalFormat;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.objects.BankItem;
import net.dtl.citizens.trader.objects.PlayerBankAccount;
import net.dtl.citizens.trader.traits.BankTrait;

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
		
		
	}

	@Override
	public void simpleMode(InventoryClickEvent event) {
		
		Player p = (Player) event.getWhoClicked();
		String player = (String) event.getWhoClicked().getName();
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		if ( slot < 0 )
		{
			//
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
					p.sendMessage( locale.getLocaleString("xxx-item", "action:invalid") );
					event.setCancelled(true);
					return;
				}
				
				if ( current.getTypeId() != 0 )
				{
					double withdraw = current.getAmount()*itemValue;
					econ.withdrawPlayer(player, withdraw);
					p.sendMessage( locale.getLocaleString("mbank-xxx-item", "entity:player", "action:{transaction}", "transaction:bought").replace("{item}", current.getType().name()).replace("{amount}", ""+ current.getAmount()) );
					p.sendMessage( locale.getLocaleString("xxx-money-xxx", "entity:player", "action:paid").replace("{money}", decimalFormat.format(withdraw)) );
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
					
					int amount = current.getAmount();
					if ( event.isRightClick() )
						amount = ( current.getAmount() % 2 == 0 ? current.getAmount()/2 : (current.getAmount()/2)+1);


					double withdraw = amount*itemValue;
						
					econ.withdrawPlayer(player, withdraw);
					p.sendMessage( locale.getLocaleString("mbank-xxx-item", "entity:player", "action:{transaction}", "transaction:bought").replace("{item}", current.getType().name()).replace("{amount}", ""+ amount) );
					p.sendMessage( locale.getLocaleString("xxx-money-xxx", "entity:player", "action:paid").replace("{money}", decimalFormat.format(withdraw)) );
				}
				if ( cursor.getTypeId() != 0 )
				{
					if ( cursor.getTypeId() != exchangeItem.getTypeId() )
					{
						p.sendMessage( locale.getLocaleString("xxx-item", "action:invalid") );
						event.setCancelled(true);
						return;
					}
					
					double deposit = cursor.getAmount()*itemValue;
					if ( event.isRightClick() )
						deposit = itemValue;
					
					econ.depositPlayer(player, deposit);
					p.sendMessage( locale.getLocaleString("mbank-xxx-item", "entity:player", "action:{transaction}", "transaction:sold").replace("{item}", cursor.getType().name()).replace("{amount}", ""+ cursor.getAmount()) );
					p.sendMessage( locale.getLocaleString("xxx-money-xxx", "entity:player", "action:got").replace("{money}", decimalFormat.format(deposit)) );
				}
			}
		}
		else
		{
			if ( event.isShiftClick() )
			{
				if ( current.getTypeId() != exchangeItem.getTypeId() )
				{
					p.sendMessage( locale.getLocaleString("xxx-item", "action:invalid") );
					event.setCancelled(true);
					return;
				}
				
				if ( current.getTypeId() != 0 )
				{
					double deposit = current.getAmount()*itemValue;
					econ.depositPlayer(player, deposit);
					p.sendMessage( locale.getLocaleString("mbank-xxx-item", "entity:player", "action:{transaction}", "transaction:sold").replace("{item}", current.getType().name()).replace("{amount}", ""+ current.getAmount()) );
					p.sendMessage( locale.getLocaleString("xxx-money-xxx", "entity:player", "action:got").replace("{money}", decimalFormat.format(deposit)) );
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
				if ( inventory.firstEmpty() < 0 || inventory.firstEmpty() >= 54 )
					return true;
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


	@Override
	public boolean onRightClick(Player player, TraderCharacterTrait trait, NPC npc) {
		

		player.openInventory(getInventory());
		return true;
		
	}
	
	
	
}

package net.dtl.citizens.trader.types;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.objects.BankItem;
import net.dtl.citizens.trader.objects.NBTTagEditor;
import net.dtl.citizens.trader.objects.PlayerBankAccount;
import net.dtl.citizens.trader.parts.BankerPart;
import net.milkbowl.vault.economy.Economy;

public class MoneyBanker extends Banker {
	
	private static Economy economy = CitizensTrader.getEconomy();
	
	private static ItemStack exchangeItem;
	private static double itemValue; 
	private String player;
	
	public MoneyBanker(NPC traderNpc, TraderCharacterTrait trait, String p) { 
		super(traderNpc, trait.getBankTrait(), p);

		account = new PlayerBankAccount(player);
		player = p;

		exchangeItem = itemConfig.getExchangeItem();
		itemValue = CitizensTrader.getInstance().getConfig().getDouble("bank.money-bank.item-value", 10.0);

		tabInventory = account.exchangeInventory(54, "Banker " + npc.getName());
		switchInventory();
	}


	public void switchInventory()
	{
		selectItem(toBankItem(exchangeItem));
		addAmountToBankerInventory(tabInventory, (int) (economy.getBalance(player) / itemValue));
		selectItem(null);
	}
	
	
	@Override
	public void settingsMode(InventoryClickEvent event) {
	}

	@Override
	public void simpleMode(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		String playerName = (String) event.getWhoClicked().getName();
		
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
					player.sendMessage( locale.getLocaleString("xxx-item", "action:invalid") );
					event.setCancelled(true);
					return;
				}
				
				if ( current.getTypeId() != 0 )
				{
					double withdraw = current.getAmount()*itemValue;
					economy.withdrawPlayer(playerName, withdraw);
					player.sendMessage( locale.getLocaleString("mbank-xxx-item", "entity:player", "action:{transaction}", "transaction:bought").replace("{item}", current.getType().name()).replace("{amount}", ""+ current.getAmount()) );
					player.sendMessage( locale.getLocaleString("xxx-money-xxx", "entity:player", "action:paid").replace("{money}", decimalFormat.format(withdraw)) );
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
						
					economy.withdrawPlayer(playerName, withdraw);
					player.sendMessage( locale.getLocaleString("mbank-xxx-item", "entity:player", "action:{transaction}", "transaction:bought").replace("{item}", current.getType().name()).replace("{amount}", ""+ amount) );
					player.sendMessage( locale.getLocaleString("xxx-money-xxx", "entity:player", "action:paid").replace("{money}", decimalFormat.format(withdraw)) );
				}
				if ( cursor.getTypeId() != 0 )
				{
					if ( cursor.getTypeId() != exchangeItem.getTypeId() )
					{
						player.sendMessage( locale.getLocaleString("xxx-item", "action:invalid") );
						event.setCancelled(true);
						return;
					}
					
					double deposit = cursor.getAmount()*itemValue;
					if ( event.isRightClick() )
						deposit = itemValue;
					
					economy.depositPlayer(playerName, deposit);
					player.sendMessage( locale.getLocaleString("mbank-xxx-item", "entity:player", "action:{transaction}", "transaction:sold").replace("{item}", cursor.getType().name()).replace("{amount}", ""+ cursor.getAmount()) );
					player.sendMessage( locale.getLocaleString("xxx-money-xxx", "entity:player", "action:got").replace("{money}", decimalFormat.format(deposit)) );
				}
			}
		}
		else
		{
			if ( event.isShiftClick() )
			{
				if ( current.getTypeId() != exchangeItem.getTypeId() )
				{
					player.sendMessage( locale.getLocaleString("xxx-item", "action:invalid") );
					event.setCancelled(true);
					return;
				}
				
				if ( current.getTypeId() != 0 )
				{
					double deposit = current.getAmount()*itemValue;
					economy.depositPlayer(playerName, deposit);
					player.sendMessage( locale.getLocaleString("mbank-xxx-item", "entity:player", "action:{transaction}", "transaction:sold").replace("{item}", current.getType().name()).replace("{amount}", ""+ current.getAmount()) );
					player.sendMessage( locale.getLocaleString("xxx-money-xxx", "entity:player", "action:got").replace("{money}", decimalFormat.format(deposit)) );
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
				
				//NBT description
				setDescription(is, amountToAdd);
				
				//create a new bank item
				if ( inventory.firstEmpty() < 0 || inventory.firstEmpty() >= 54 )
					return true;
				inventory.setItem(inventory.firstEmpty(), is);
				amountToAdd -= 64;
			}
			return true;
		}
		
		return false;
	}
	
	public static void setDescription(ItemStack item, int amount)
	{
		List<String> lore = new ArrayList<String>();
		lore.add("^r^7Value: ^6" + new DecimalFormat("#.##").format(amount * itemValue));
		NBTTagEditor.addDescription((CraftItemStack)item, lore);
	}

	@Override
	public boolean onRightClick(Player player, TraderCharacterTrait trait, NPC npc) {
		
		/*TODO add descriptions to player items be aware!
		 * 
		NBTTagEditor.removeDescriptions(player.getInventory());
		if ( !getTraderStatus().isManaging() )
			loadDescriptions(player.getInventory());
		 */
		
		player.openInventory(getInventory());
		return true;
	}
	
	
	
}

package net.dtl.citizenstrader_new.traders;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.CitizensTrader;
import net.dtl.citizenstrader_new.LocaleManager;
import net.dtl.citizenstrader_new.TraderCharacterTrait;
import net.dtl.citizenstrader_new.TraderCharacterTrait.TraderType;
import net.dtl.citizenstrader_new.ItemsConfig;
import net.dtl.citizenstrader_new.containers.StockItem;
import net.dtl.citizenstrader_new.containers.Wallet;
import net.dtl.citizenstrader_new.traits.InventoryTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait.WalletType;


/* *
 * Trader basic class
 * Providing all tools for item selection and managing
 * 
 */
public abstract class Trader implements EconomyNpc {
	
	/* *
	 * TraderStatus
	 * 
	 */
	
	/* *
	 * Going to rename the States in the future
	 * 
	 */
	public enum TraderStatus {
		SELL, BUY, BANK, MANAGE_BANK, SELL_AMOUNT, MANAGE_SELL, MANAGE_LIMIT_GLOBAL, MANAGE_LIMIT_PLAYER, MANAGE_SELL_AMOUNT, MANAGE_PRICE, MANAGE_BUY, MANAGE;
	
		/* *
		 * ManagerMode condition
		 * 
		 */
		public static boolean hasManageMode(TraderStatus status) {
			if ( !status.equals(SELL) 
				&& !status.equals(SELL_AMOUNT)  
				&& !status.equals(BUY) 
				&& !status.equals(BANK)
				&& !status.equals(MANAGE_BANK) )
				return true;
			return false;
		}
	}

	//trader config
	protected static ItemsConfig config = CitizensTrader.getInstance().getItemConfig();
	
	/* *
	 * Npc Traits
	 * @traderConfig - currently not edit-able in-game
	 * 
	 */
	protected LocaleManager locale;
	
	private InventoryTrait traderStock;
	private TraderStatus traderStatus;
	private TraderTrait traderConfig;
	
	
	//the npc id we are using here
	private NPC npc;
	
	/* *
	 * NpcInventory 
	 * 
	 */
	private Inventory inventory;
	
	/* *
	 * ItemManagement
	 * 
	 */
	private StockItem selectedItem = null; 
	
	/* *
	 * TempVariables 
	 * 
	 */
	private Boolean inventoryClicked = true;
	private Integer slotClicked = -1;
	
	/* *
	 * Constructor 
	 * @tradderNpc The NPC object from the trader
	 * @traderConfiguragion the TraderConfiguration
	 * 
	 */
	public Trader(NPC traderNpc,TraderTrait traderConfiguragion) {
		
		// Assign the configuration for later changes
		traderConfig = traderConfiguragion;
		
		//locale
		locale = CitizensTrader.getLocaleManager();
		
		// Initialize the trader
		traderStock = traderNpc.getTrait(TraderCharacterTrait.class).getInventoryTrait();
		inventory = traderStock.inventoryView(54, traderNpc.getName() + " trader");
		traderStatus = TraderStatus.SELL;
		
		npc = traderNpc;
		
		/* *
		 * SetAn Economy plugin to the trader's wallet
		 */
		traderConfig.getWallet().setEconomy(CitizensTrader.getInstance().getEconomy());
	}
	
	public int getNpcId() {
		return npc.getId();
	}
	
	public NPC getNpc() {
		return npc;		
	}
	
	/*
	 * Trader configuration
	 */
	public TraderTrait getTraderConfig() {
		return this.traderConfig;
	}
	
	/* * ===============================================================================================
	 * SelectedItem Management
	 * 
	 */
	public boolean equalsSelected(ItemStack itemToCompare,boolean durability,boolean amount) {
		/* *
		 * reset the equality state
		 * 
		 */
		boolean equal = false;
		
		if ( itemToCompare.getType().equals( selectedItem.getItemStack().getType() ) 
				&& itemToCompare.getData().equals( selectedItem.getItemStack().getData() ) ) {
			/* *
			 * id and data check passed
			 * 
			 */
			equal = true;
			
			/* *
			 * check durability
			 * 
			 */
			if ( durability ) 
				equal = itemToCompare.getDurability() >= selectedItem.getItemStack().getDurability();
				
			/* *
			 * check amount if durability passed
			 * 
			 */
			if ( amount && equal )
				equal =  itemToCompare.getAmount() == selectedItem.getItemStack().getAmount();
			
			/* *
			 * return the value
			 * 
			 */
			return equal;
		}
		
		/* *
		 * 100% this items are different ;)
		 * 
		 */
		return false;
	}

	/* *
	 * Select the given item
	 * 
	 */
	public final Trader selectItem(StockItem i) {
		selectedItem = i;
		return this;
	}
	
	/* * 
	 * Select item by slot and status
	 * 
	 */
	public final Trader selectItem(int slot,TraderStatus status) {
		selectedItem = traderStock.getItem(slot, status);
		
	//	if ( !TraderStatus.hasManageMode(status) )
	//		if ( selectedItem != null && !selectedItem.getLimitSystem().checkLimit("", 0) )
	//			selectedItem = null;
		return this;
	} 

	/* *
	 * Select item by equality to the given item stack
	 * checking the status, durability and amount
	 * 
	 */
	public final Trader selectItem(ItemStack item,TraderStatus status,boolean dura,boolean amount) {
		selectedItem = traderStock.getItem(item, status, dura, amount);
		return this;
	}
	
	/* *
	 * if there is currently any item selected
	 * 
	 */
	public final boolean hasSelectedItem() {
		return selectedItem != null;
	}
	
	/* *
	 * returns the selectedItem
	 * 
	 */
	public final StockItem getSelectedItem() {
		return selectedItem;
	}
	
	/* * ===============================================================================================
	 * Inventory Management
	 * 
	 */
	
	/* *
	 * Checking if the given slot is a inventory management slot
	 * 
	 */
	public boolean isManagementSlot(int slot, int range) {
		return slot >= ( getInventory().getSize() - range );
	}
	
	/* *
	 * Checking if the inventory has enough space to save the selected amount
	 * 
	 */
	public final boolean inventoryHasPlace(Player player, int slot) {
		int amountToAdd = selectedItem.getAmount(slot);
		return this.inventoryHasPlaceAmount(player, amountToAdd);
	}
	
	public final boolean inventoryHasPlaceAmount(Player player,int amount) {
		PlayerInventory inventory = player.getInventory();
		int amountToAdd = amount;
		/* *
		 * get all stacks with the same type (hmm... does it compares the data values?)
		 * 
		 */
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) {
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				/* *
				 * if the added amount isn't over the limit
				 *
				 */
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() )
					return true;
				
				/* *
				 * if the added amount is less than 64 (so we are not adding a whole stack)
				 * 
				 * lowering the amount to add
				 *
				 */ 
				if ( item.getAmount() < 64 ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % 64; 
				}
				
				/* *
				 * if there is nothing left just return
				 * 
				 */
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		/* *
		 * if any amount left to add check if there is place in the inventory
		 */
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			return true;
		}
		return false;
	}
	
	public final boolean addSelectedToInventory(Player player, int slot) {

		int amountToAdd = selectedItem.getAmount(slot);
		return addAmountToInventory(player, amountToAdd);
		
	}
	
	/**
	 * SelfWritten Inventory.addItem() function for a work around with a bukkit inventory function bug
	 * 
	 */
	public final boolean addAmountToInventory(Player player, int amount) {
		PlayerInventory inventory = player.getInventory();
		int amountToAdd = amount;
		
		/* *
		 * get all stacks with the same type (hmm... does it compares the data values?)
		 * 
		 */
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) {
			
			/* *
			 * Checking items by durability, so if you buy a diax sword it wont buy like it would be broken :P
			 * 
			 */
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				/* *
				 * if the added amount isn't over the limit
				 * 
				 * setting the new amount in the player's inventory 
				 *
				 */
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() ) {
					item.setAmount( item.getAmount() + amountToAdd );
					return true;
				} 
				
				/* *
				 * if the added amount is less than 64 (so we are not adding a whole stack)
				 * 
				 * maximizing the first item stack amount, and lowering the amount to add
				 *
				 */ 
				if ( item.getAmount() < selectedItem.getItemStack().getMaxStackSize() ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % selectedItem.getItemStack().getMaxStackSize(); 
					item.setAmount(selectedItem.getItemStack().getMaxStackSize());
				}
				
				/* *
				 * if there is nothing left just return
				 * 
				 */
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		/* *
		 * Stack's are maximized and there is some amount left
		 *  
		 *  Checking if there is any free space in the inventory (just for care)
		 *  
		 */
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			/* *
			 * creating a ItemStack clone from the existing saving
			 * and changing amount's
			 * 
			 */
			ItemStack is = selectedItem.getItemStack().clone();
			is.setAmount(amountToAdd);
			
			/* *
			 * setting the item into a free slot
			 * don't using the addItem() bacause it's a workaround for this function
			 * 
			 */
			inventory.setItem(inventory.firstEmpty(), is);
			return true;
		}
		
		/* *
		 * Item couldn't be added to the inventory
		 * 
		 */
		return false;
	}
	
	/* *
	 * remove item from inventory, a function to avoid large code declarations
	 * 
	 */
	public final boolean removeFromInventory(ItemStack item, InventoryClickEvent event) {
		if ( item.getAmount() != selectedItem.getAmount() ) {
			if ( item.getAmount() % selectedItem.getAmount() == 0 ) 
				event.setCurrentItem(new ItemStack(Material.AIR));
			else 
				item.setAmount( item.getAmount() % selectedItem.getAmount() );
		} else {
			event.setCurrentItem(new ItemStack(Material.AIR));
		}
		
		return false;
	}
	
	/* *
	 * Switching the inventory to the parsed status
	 * reseting the values with the given status
	 * 
	 */
	public final void switchInventory(TraderStatus status) {
		inventory.clear();
		traderStock.inventoryView(inventory, status);
		reset(status);
	}
	
	/* *
	 * Switching to the MultipleAmount's selection
	 * 
	 */
	public final void switchInventory(StockItem item) {
		inventory.clear();
		if ( TraderStatus.hasManageMode(traderStatus) )
			InventoryTrait.setManagerInventoryWith(inventory, item);
		else
			InventoryTrait.setInventoryWith(inventory, item);
		selectedItem = item;
	}
	
	
	/* * ===============================================================================================
	 * Limits (recoding) 
	 * 
	 */
	
	public boolean checkBuyLimits(Player p, int scale) {
		if ( !selectedItem.getLimitSystem().checkLimit(p.getName(),0,scale) ) {
		//	p.sendMessage(ChatColor.RED + "Limit reached, try again later.");
			return false;
		}
		return true;
	}
	
	public boolean checkLimits(Player p) {
		if ( !selectedItem.getLimitSystem().checkLimit(p.getName(),0) ) {
		//	p.sendMessage(ChatColor.RED + "Limit reached, try again later.");
			return false;
		}
		return true;
	}
	
	public boolean checkLimits(Player p, int slot) {
		if ( !selectedItem.getLimitSystem().checkLimit(p.getName(),slot) ) {
		//	p.sendMessage(ChatColor.RED + "Limit reached, try again later.");
			return false;
		}
		return true;
	}
	
	public boolean updateBuyLimits(String p, int scale) {
		return selectedItem.getLimitSystem().updateLimit(0, scale, p);
	}
	
	public boolean updateLimits(String p, int slot) {
		return selectedItem.getLimitSystem().updateLimit(slot, p);
	}
	
	public boolean updateLimits(String p) {
		return selectedItem.getLimitSystem().updateLimit(0, p);
	}
	
	/* *
	 * temporary Function!!!!!!!!!!!!!!!!!!!!
	 * 
	 */
	public boolean updateLimitsTem(String p, ItemStack item) {
		if ( item.getAmount() == 1 )
			return selectedItem.getLimitSystem().updateLimitWith(getMaxAmount(item), p);
		return false;
	}
	
	/* *
	 * get the max amount the limit can carry comparing to an Item
	 * 
	 */
	public int getMaxAmount(ItemStack item) {
		if ( selectedItem.getLimitSystem().getUnusedLimit() >= item.getAmount() ) {
			return item.getAmount(); 
		}
		return selectedItem.getLimitSystem().getUnusedLimit();
	}
	/* *
	 * Checking if an item has reached his limit
	 */
/*	public boolean checkLimit() {
		if ( selectedItem.checkLimit() && selectedItem.hasLimitAmount(selectedItem.getAmount()) )
			return true;
		return false;
	}
	public void updateSelectedItemLimit() {
		updateSelectedItemLimit(selectedItem.getAmount());
	}
	public void updateSelectedItemLimit(int amount) {
		selectedItem.changeLimitAmount(amount);
		if ( !selectedItem.checkLimit() || !selectedItem.hasLimitAmount(amount) ) {
			if ( !traderStatus.equals(TraderStatus.PLAYER_SELL_AMOUNT) ) {
				inventory.setItem(selectedItem.getSlot(), new ItemStack(Material.AIR));
			} else 
				switchInventory(selectedItem);
		}
	}*/
	
	/* * ===============================================================================================
	 * ManagerMode functions 
	 * 
	 */
	
	/* *
	 * saving the new amounts found in the select multiple items mode
	 */
	public final void saveManagedAmouts() {
		traderStock.saveNewAmouts(inventory, selectedItem);
	}
	
	/**
	 * checking sell/buy mode by wool color
	 * 
	 */
	public boolean isSellModeByWool() {
		return isWool(inventory.getItem(inventory.getSize()-1), config.getItemManagement(1));//5
	}
	public boolean isBuyModeByWool() {
		return isWool(inventory.getItem(inventory.getSize()-1), config.getItemManagement(0));//3
	}
	
	/**
	 * 
	 */
	public TraderStatus getBasicManageModeByWool() 
	{
		if ( isSellModeByWool() )
			return TraderStatus.MANAGE_SELL;
		if ( isBuyModeByWool() )
			return TraderStatus.MANAGE_BUY;
		return TraderStatus.MANAGE;
	}
	
	/* * ===============================================================================================
	 * Item Sell and buy management
	 * 
	 */
	
	/* *
	 * handling a transaction if the player buys something from the trader
	 * 
	 */
	public boolean buyTransaction(Player p, double price) {
		return traderConfig.buyTransaction(p, price);
	}
	
	/* *
	 * handling a transaction if the player sells something to the trader
	 * 
	 */
	public boolean sellTransaction(Player p, double price) {
		return traderConfig.sellTransaction(p, price);
	}
	
	public boolean sellTransaction(Player p, double price, ItemStack item) {
		/* *
		 * Fast implementation for a more comfortable selling
		 * going to change this in future
		 * 
		 */
	//	if ( item.getAmount() == 1 )
	//		return traderConfig.sellTransaction(p, price*getMaxAmount(item));
	//	int scale = ;
		return traderConfig.sellTransaction(p, price*((int)item.getAmount() / selectedItem.getAmount()));
	}
	
	/* * ===============================================================================================
	 * Other management functions 
	 * 
	 */
	
	/* *
	 * reset the trader with a given status (needed on inventory switching)
	 * 
	 */
	public final Trader reset(TraderStatus status) {
		traderStatus = status;
		selectedItem = null;
		inventoryClicked = true;
		slotClicked = -1;
		return this;
	}
	
	/* *
	 * Setting the trader Status
	 */
	public final void setTraderStatus(TraderStatus s) {
		traderStatus = s;
	}
	
	/* *
	 * Setting the last clicked inventory
	 * 
	 * true => top
	 * false => bottom
	 * 
	 */
	public final void setInventoryClicked(boolean c) {
		inventoryClicked = c;
	}
	
	/* *
	 * getting last clicked inventory
	 * 
	 */
	public final boolean getInventoryClicked() {
		return inventoryClicked;
	}
	
	/* *
	 * Setting the last clicked slot in any inventory
	 */
	public final void setClickedSlot(Integer s) {
		slotClicked = s;
	}
	
	/* *
	 * getting last clicked slot
	 */
	public final Integer getClickedSlot() {
		return slotClicked;
	}
	
	/* *
	 * comparing functions for TraderStatus, TraderType, WalletType
	 */
	public final boolean equalsTraderStatus(TraderStatus status) {
		return traderStatus.equals(status);
	}
	public final boolean equalsTraderType(TraderType type) {
		return traderConfig.getTraderType().equals(type);
	}
	public final boolean equalsWalletType(WalletType type) {
		return traderConfig.getWalletType().equals(type);
	}

	/* *
	 * getTraderStatus
	 * 
	 */
	public final TraderStatus getTraderStatus() {
		return traderStatus;
	}
	
	/* *
	 * get the traders inventory 
	 */
	public final Inventory getInventory() {
		return inventory;
	}
	
	/* *
	 * get the traders wallet 
	 */
	public final Wallet getWallet() {
		return traderConfig.getWallet();
	}	
	/* *
	 * get the traders inventory stock
	 */
	public final InventoryTrait getTraderStock() {
		return traderStock;
	}
	
	
	/* * ===============================================================================================
	 * Static functions for cleaner code
	 * 
	 */
	public static boolean isWool(ItemStack itemToCompare,ItemStack managementItem) {
		return itemToCompare.equals(new ItemStack(managementItem.getTypeId(),1,(short)0,managementItem.getData().getData()));
	}
	
	public static double calculatePrice(ItemStack is) {
		if ( is.getType().equals(Material.WOOD) )
			return is.getAmount()*0.01;		
		else if ( is.getType().equals(Material.LOG) )
			return is.getAmount()*0.1;
		else if ( is.getType().equals(Material.DIRT) )
			return is.getAmount()*10;		
		else if ( is.getType().equals(Material.COBBLESTONE) )
			return is.getAmount()*100;
		return is.getAmount();
	}
	
	public static int calculateLimit(ItemStack is) {
		if ( is.getType().equals(Material.DIRT) )
			return is.getAmount()*10;		
		else if ( is.getType().equals(Material.COBBLESTONE) )
			return is.getAmount()*100;
		return is.getAmount();
	}
	
	public static int calculateTimeout(ItemStack is) {
		if ( is.getType().equals(Material.DIRT) )
			return is.getAmount()*60;		
		else if ( is.getType().equals(Material.COBBLESTONE) )
			return is.getAmount()*3600;	
		else if ( is.getType().equals(Material.LOG) )
			return is.getAmount()*3600*24;
		return is.getAmount();
	}
	 
	public static StockItem toStockItem(ItemStack is) {
		String itemInfo = is.getTypeId()+":"+ is.getData().getData() +" a:"+is.getAmount() + " d:" + is.getDurability();
		if ( !is.getEnchantments().isEmpty() ) {
			itemInfo += " e:";
			for ( Enchantment ench : is.getEnchantments().keySet() ) 
				itemInfo += ench.getId() + "/" + is.getEnchantmentLevel(ench) + ",";
		}
		return new StockItem(itemInfo);
	}
	
	
	//loging function
	public void log(String action, String player, int id, byte data, int amount, double price) 
	{
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("HH-mm-ss");
		
		DecimalFormat dec = new DecimalFormat("#.##");
		
		CitizensTrader.getLoggingManager().log("["+df.format(date)+"]["+npc.getName()+"]["+action+"] - <" + player + ">\n      id:"+ id + " data:" + data + " amount:" + amount + " price:" + dec.format(price) );
	}
	
	
}

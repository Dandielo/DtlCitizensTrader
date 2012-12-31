package net.dtl.citizens.trader.types;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.ItemsConfig;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.TraderCharacterTrait.EcoNpcType;
import net.dtl.citizens.trader.managers.LocaleManager;
import net.dtl.citizens.trader.managers.LoggingManager;
import net.dtl.citizens.trader.managers.PatternsManager;
import net.dtl.citizens.trader.managers.PermissionsManager;
import net.dtl.citizens.trader.objects.NBTTagEditor;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.objects.Wallet;
import net.dtl.citizens.trader.parts.TraderConfigPart;
import net.dtl.citizens.trader.parts.TraderStockPart;


public abstract class Trader implements EconomyNpc {
	
	//Managers
	protected static PermissionsManager permissionsManager = CitizensTrader.getPermissionsManager();
	protected static LoggingManager loggingManager = CitizensTrader.getLoggingManager();
	protected static PatternsManager patternsManager = CitizensTrader.getPatternsManager();
	protected LocaleManager localeManager = CitizensTrader.getLocaleManager();
	
	//Configuration
	protected static ItemsConfig itemsConfig = CitizensTrader.getInstance().getItemConfig();
	
	//Trader parts
	private TraderStockPart traderStock;
	private TraderConfigPart traderConfig;
	
	//Trader info
	protected Player player;
	private TraderStatus traderStatus;
	private Inventory inventory;
	private NPC npc;
	
	//Trader runtime 
	private StockItem selectedItem = null; 
	private Boolean inventoryClicked = true;
//	private Integer lastSlot = -1;


	public Trader(TraderCharacterTrait trait, NPC npc, Player player) {
		
		// Initialize the trader
		traderStock = trait.getStock();
		traderConfig = trait.getConfig();
		
		//init info
		this.player = player;
		this.npc = npc;

		inventory = traderStock.getInventory("sell", player);
		traderStatus = TraderStatus.SELL;
	}
	
	@Override
	public int getNpcId() {
		return npc.getId();
	}

	@Override
	public NPC getNpc() {
		return npc;		
	}
	
	//traders config
	public TraderConfigPart getConfig() {
		return traderConfig;
	}
	
	//Operations on selected item pool
	public boolean equalsSelected(ItemStack itemToCompare, boolean durability, boolean amount) {
		boolean equal = false;
		
		if ( itemToCompare.getType().equals( selectedItem.getItemStack().getType() ) ) 
		{
			equal = true;
			
			if ( durability ) 
				equal = itemToCompare.getDurability() >= selectedItem.getItemStack().getDurability();
			else
				equal = itemToCompare.getData().equals( selectedItem.getItemStack().getData() );
		
			if ( amount && equal )
				equal = itemToCompare.getAmount() == selectedItem.getItemStack().getAmount();
			return equal;
		}
		return false;
	}

	public final Trader selectItem(StockItem i) {
		selectedItem = i;
		return this;
	}
	public final Trader selectItem(int slot,TraderStatus status) {
		selectedItem = traderStock.getItem(slot, status);
		return this;
	} 
	public final Trader selectItem(ItemStack item, TraderStatus status, boolean dura, boolean amount) {
		selectedItem = traderStock.getItem(item, status, dura, amount);
		return this;
	}
	
	public final boolean hasSelectedItem() {
		return selectedItem != null;
	}
	public final StockItem getSelectedItem() {
		return selectedItem;
	}

	//Inventory operations
	public boolean isManagementSlot(int slot, int range) {
		return slot >= ( getInventory().getSize() - range );
	}

	public final boolean inventoryHasPlace(int slot) {
		int amountToAdd = selectedItem.getAmount(slot);
		return this.inventoryHasPlaceAmount(amountToAdd);
	}
	public final boolean inventoryHasPlaceAmount(int amount) {
		PlayerInventory inventory = player.getInventory();
		int amountToAdd = amount;
		
		//get all item stack with the same type
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() )
		{
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) 
			{
				if ( NBTTagEditor.getName(item).equals(selectedItem.getName()) ) 
				{
					if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() )
						return true;
					
					if ( item.getAmount() < 64 ) {
						amountToAdd = ( item.getAmount() + amountToAdd ) % 64; 
					}
					
					if ( amountToAdd <= 0 )
						return true;
				}
			}
		}

		//if we still ahve some items to add, is there an empty slot for them?
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			return true;
		}
		return false;
	}
	
	public final boolean addSelectedToInventory(int slot) {
		return addAmountToInventory(selectedItem.getAmount(slot));
	}
	public final boolean addAmountToInventory(int amount) {
		PlayerInventory inventory = player.getInventory();
		int amountToAdd = amount;

		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) 
		{
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() )
			{
				if ( NBTTagEditor.getName(item).equals(selectedItem.getName()) ) 
				{
					//add amount to an item in the inventory, its done
					if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() ) {
						item.setAmount( item.getAmount() + amountToAdd );
						return true;
					} 
					
					//add amount to an item in the inventory, but we still got some left
					if ( item.getAmount() < selectedItem.getItemStack().getMaxStackSize() ) {
						amountToAdd = ( item.getAmount() + amountToAdd ) % selectedItem.getItemStack().getMaxStackSize(); 
						item.setAmount(selectedItem.getItemStack().getMaxStackSize());
					}
						
					//nothing left
					if ( amountToAdd <= 0 )
						return true;
				}
			}
		}
		
		//create new stack
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			//new stack
			ItemStack is = selectedItem.getItemStack().clone();
			is.setAmount(amountToAdd);
			NBTTagEditor.removeDescription(is);
			
			StockItem it = this.getStock().getItem(is, TraderStatus.BUY, true, false);
			
			if ( it != null )
			{
				int scale = is.getAmount() / it.getAmount();
				
				DecimalFormat f = new DecimalFormat("#.##");
				
				List<String> lore = new ArrayList<String>(); ;
				for ( String l : itemsConfig.getPriceLore("pbuy") )
					lore.add(l.replace("{unit}", f.format(it.getPrice())+"").replace("{stack}", f.format(it.getPrice()*scale)+""));
				
				if ( scale > 0 )
					NBTTagEditor.addDescription(is, lore);	
			}
			
			//set the item info the inv
			inventory.setItem(inventory.firstEmpty(), is);
			return true;
		}
		
		//could not be added to inventory
		return false;
	}
	
	public final boolean removeFromInventory(ItemStack item, InventoryClickEvent event) {
		if ( item.getAmount() != selectedItem.getAmount() ) 
		{
			if ( item.getAmount() % selectedItem.getAmount() == 0 ) 
				event.setCurrentItem(new ItemStack(Material.AIR));
			else 
				item.setAmount( item.getAmount() % selectedItem.getAmount() );
		}
		else
		{
			event.setCurrentItem(new ItemStack(Material.AIR));
		}
		return false;
	}
	
	//switching inventory = change items in it
	public final void switchInventory(TraderStatus status, String type) {
		inventory.clear();
		traderStock.inventoryView(inventory, status, player, type);
	//	reset(status);
	}
	//switching inventory = change items in it
	public final void switchInventory(TraderStatus status) {
		inventory.clear();
		traderStock.inventoryView(inventory, status, player, "manage");
		reset(status);
	}
	
	//swithing inventory (amounts selection)
	public final void switchInventory(StockItem item) {
		inventory.clear();
		if ( traderStatus.isManaging() )
			TraderStockPart.setManagerInventoryWith(inventory, item);
		else
			traderStock.setInventoryWith(inventory, item, player);
		selectedItem = item;
	}
	
	
	
	//===============================================================================================
	
	public boolean checkBuyLimits(int scale) {
		return selectedItem.getLimitSystem().checkLimit(player.getName(),0,scale);
	}
	
	public boolean checkLimits() {
		return selectedItem.getLimitSystem().checkLimit(player.getName(),0);
	}
	
	public boolean checkLimits(int slot) {
		return selectedItem.getLimitSystem().checkLimit(player.getName(),slot);
	}
	
	public boolean updateBuyLimits(int scale) {
		return selectedItem.getLimitSystem().updateLimit(0, scale, player.getName());
	}
	
	public boolean updateLimits(int slot) {
		return selectedItem.getLimitSystem().updateLimit(slot, player.getName());
	}
	
	public boolean updateLimits() {
		return selectedItem.getLimitSystem().updateLimit(0, player.getName());
	}

	//saving amounts
	public final void saveManagedAmouts() {
		TraderStockPart.saveNewAmouts(inventory, selectedItem);
	}
	
	//checking mode by wool
	public boolean isSellModeByWool() {
		return isWool(inventory.getItem(inventory.getSize()-1), itemsConfig.getItemManagement(1));//5
	}
	public boolean isBuyModeByWool() {
		return isWool(inventory.getItem(inventory.getSize()-1), itemsConfig.getItemManagement(0));//3
	}
	
	//getting mode
	public TraderStatus getBasicManageModeByWool() 
	{
		if ( isSellModeByWool() )
			return TraderStatus.MANAGE_SELL;
		if ( isBuyModeByWool() )
			return TraderStatus.MANAGE_BUY;
		return TraderStatus.MANAGE;
	}
	
	//when a player is buying
	public boolean buyTransaction(double price) {
		return traderConfig.buyTransaction(player.getName(), price);
	}
	
	//when a player is selling
	public boolean sellTransaction(double price) {
		return traderConfig.sellTransaction(player.getName(), price);
	}

	//when a player is selling
	public boolean sellTransaction(double price, ItemStack item) {
		return traderConfig.sellTransaction(player.getName(), price);
	}
	
	//reset the trader
	public final Trader reset(TraderStatus status) {
		traderStatus = status;
		selectedItem = null;
		inventoryClicked = true;
//		lastSlot = -1;
		return this;
	}
	
	//
	public final void setInventoryClicked(boolean c) {
		inventoryClicked = c;
	}
	
	//
	public final boolean getInventoryClicked() {
		return inventoryClicked;
	}
	
	//trader status easy check
	public final boolean equalsTraderStatus(TraderStatus status) {
		return traderStatus.equals(status);
	}

	public final boolean locked()
	{
		return traderStatus.isManaging();
	}
	
	//get traders status
	public final TraderStatus getTraderStatus() {
		return traderStatus;
	}
	
	//set trader status
	public final void setTraderStatus(TraderStatus status)
	{
		traderStatus = status;
	}
	
	//get the traders inventory
	@Override
	public final Inventory getInventory() {
		return inventory;
	}
	
	//getting wallet
	@Override
	public final Wallet getWallet() {
		return traderConfig.getWallet();
	}	

	//getting the stock
	public final TraderStockPart getStock() {
		return traderStock;
	}
	
	//Not needed in any trader type
	@Override
	public final void settingsMode(InventoryClickEvent event) {
		((Player)event.getWhoClicked()).sendMessage(ChatColor.RED+"Settings Mode can't be used here");
		event.setCancelled(true);
	}
	
	//static helper methods
	public static boolean isWool(ItemStack itemToCompare,ItemStack managementItem) {
		return ( itemToCompare.getType().equals(managementItem.getType()) &&
				itemToCompare.getDurability() == managementItem.getDurability() );
	}
	
	//TODO add to config
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

	//TODO add to config
	public static int calculateLimit(ItemStack is) {
		if ( is.getType().equals(Material.DIRT) )
			return is.getAmount()*10;		
		else if ( is.getType().equals(Material.COBBLESTONE) )
			return is.getAmount()*100;
		return is.getAmount();
	}

	//TODO add to config
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
			for ( Map.Entry<Enchantment, Integer> ench : is.getItemMeta().getEnchants().entrySet() ) 
				itemInfo += ench.getKey().getId() + "/" + ench.getValue() + ",";
		}		
		
		if ( is.getType().equals(Material.ENCHANTED_BOOK) )
		{
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) is.getItemMeta();
			if ( !meta.getStoredEnchants().isEmpty() )
			{
				itemInfo += " se:";
				for ( Map.Entry<Enchantment, Integer> e : meta.getStoredEnchants().entrySet() )
					itemInfo += e.getKey().getId() + "/" + e.getValue() + ",";
			}
		}
		
		String name = NBTTagEditor.getName(is).replace(" ", "[&]");
		if ( !name.isEmpty() )
			itemInfo += " n:" + name;
		return new StockItem(itemInfo);
	}
	
	public static TraderStatus getStartStatus(Player player) {
		if ( permissionsManager.has(player, "dtl.trader.options.sell") )
			return TraderStatus.SELL;
		else if ( permissionsManager.has(player, "dtl.trader.options.buy") )
			return TraderStatus.BUY;
		return null;
	}
	
	public static TraderStatus getManageStartStatus(Player player) {
		System.out.print(permissionsManager.has(player, "dtl.trader.options.sell"));
		if ( permissionsManager.has(player, "dtl.trader.options.sell") )
			return TraderStatus.MANAGE_SELL;
		else if ( permissionsManager.has(player, "dtl.trader.options.buy") )
			return TraderStatus.MANAGE_BUY;
		return null;
	}
	
	public abstract EcoNpcType getType();
	
	//loging function
	public void log(String action, int id, byte data, int amount, double price) 
	{
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("HH-mm-ss");
		
		DecimalFormat dec = new DecimalFormat("#.##");
		
		loggingManager.log("["+df.format(date)+"]["+npc.getName()+"]["+action+"] - <" + player.getName() + ">\n      id:"+ id + " data:" + data + " amount:" + amount + " price:" + dec.format(price) );
	}
	
	public void playerLog(String owner, String buyer, String action, StockItem item, int slot)
	{
		loggingManager.playerLog(owner, npc.getName(), localeManager.getLocaleString("xxx-transaction-xxx-item-log", "entity:name", "transaction:"+action).replace("{name}", buyer).replace("{item}", item.getItemStack().getType().name().toLowerCase() ).replace("{amount}", ""+item.getAmount(slot)) );
	}
	
	//Trader status enumeration
	public enum TraderStatus {
		SELL, BUY, SELL_AMOUNT, MANAGE_SELL, MANAGE_LIMIT_GLOBAL, MANAGE_LIMIT_PLAYER, MANAGE_SELL_AMOUNT, MANAGE_PRICE, MANAGE_BUY, MANAGE;
	
		public boolean isManaging()
		{
			switch( this )
			{
				case SELL:
				case BUY:
				case SELL_AMOUNT:
					return false;
				default:
					return true;
			}
		}
		
		@Override
		public String toString()
		{
			switch( this )
			{
				case SELL:
				case MANAGE_SELL:
					return "sell";
				case BUY:
				case MANAGE_BUY:
					return "buy";
				default:
					return "";
			}
		}
		
		public static TraderStatus getByName(String string)
		{
			if ( string.equals("sell") )
				return SELL;
			if ( string.equals("buy") )
				return BUY;
			return SELL;
		}
	}

	
	public double getPrice(Player player, String transaction)
	{
		return getPrice(player, transaction, 0);
	}
	public double getPrice(Player player, String transaction, int slot)
	{
		if ( getStock().getPattern() != null )
			return getStock().getPattern() .getItemPrice(player, getSelectedItem(), transaction, slot, 0.0);
		return getSelectedItem().getPrice(slot);
	}
	
	public void loadDescriptions(Inventory inventory)
	{
		DecimalFormat f = new DecimalFormat("#.##");
		for ( int i = 0 ; i < inventory.getSize() ; ++i )
		{
			ItemStack item = inventory.getItem(i);
			
			
			if ( item != null )
			{
				StockItem stockItem = this.getStock().getItem(item, TraderStatus.BUY, true, false);
				
				if ( stockItem != null )
				{
					int scale = item.getAmount() / stockItem.getAmount(); 

					List<String> lore = new ArrayList<String>(); ;
					for ( String l : itemsConfig.getPriceLore("pbuy") )
						lore.add(l.replace("{unit}", f.format(stockItem.getPrice())+"").replace("{stack}", f.format(stockItem.getPrice()*scale)+""));
					
					if ( scale > 0 )
						NBTTagEditor.addDescription(item, lore);			
				}
			}
		}
	}
	
	
}

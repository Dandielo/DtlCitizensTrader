package net.dandielo.citizens.trader.types;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.ItemsConfig;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.limits.LimitManager;
import net.dandielo.citizens.trader.locale.LocaleManager;
import net.dandielo.citizens.trader.managers.LogManager;
import net.dandielo.citizens.trader.managers.PermissionsManager;
import net.dandielo.citizens.trader.objects.MetaTools;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.objects.Wallet;
import net.dandielo.citizens.trader.parts.TraderConfigPart;
import net.dandielo.citizens.trader.parts.TraderStockPart;
import net.dandielo.citizens.trader.patterns.PatternsManager;


public abstract class Trader implements tNPC {
	
	//Managers
	protected static PermissionsManager permissionsManager = CitizensTrader.getPermissionsManager();
	protected static LogManager loggingManager = CitizensTrader.getLoggingManager();
	protected static PatternsManager patternsManager = CitizensTrader.getPatternsManager();
	protected static LimitManager limits = CitizensTrader.getLimitsManager();
	protected LocaleManager locale = CitizensTrader.getLocaleManager();
	
	//Configuration
	protected static ItemsConfig itemsConfig = CitizensTrader.getInstance().getItemConfig();
	
	//Trader parts
	private TraderStockPart traderStock;
	private TraderConfigPart traderConfig;
	protected TraderTrait trait;
	
	//Trader info
	protected Player player;
	private TraderStatus traderStatus;
	private Inventory inventory;
	private NPC npc;
	
	//Trader runtime 
	private StockItem selectedItem = null; 
	private Boolean inventoryClicked = true;


	public Trader(TraderTrait trait, NPC npc, Player player) {
		
		// Initialize the trader
		traderStock = trait.getStock().createStockFor(player);
		traderConfig = trait.getConfig();
		
		//init info
		this.player = player;
		this.npc = npc;
		this.trait = trait;

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

			if ( equal ) {
				// StockItem has 2 boolean properties that are set to true if its entry in an Items Pattern has the "ce" or "cel" flags  
				boolean checkEnchant = selectedItem.isCheckingEnchantments();
				boolean checkLevel = selectedItem.isCheckingEnchantmentLevels();

				if ( checkEnchant || checkLevel ) {
					Map<Enchantment,Integer> itemStackEnchantments = null;
					Map<Enchantment,Integer> stockItemEnchantments = null;
					
					// special handling for Enchanted Books and stored enchantments
					if ( itemToCompare.getType().equals(Material.ENCHANTED_BOOK) ) {
						EnchantmentStorageMeta itemStackStorageMeta = (EnchantmentStorageMeta)itemToCompare.getItemMeta();
						if (itemStackStorageMeta != null) {
							itemStackEnchantments = itemStackStorageMeta.getStoredEnchants();
						}

						EnchantmentStorageMeta stockItemStorageMeta = (EnchantmentStorageMeta)selectedItem.getItemStack().getItemMeta();
						if (stockItemStorageMeta != null) {
							itemStackEnchantments = stockItemStorageMeta.getStoredEnchants();
						}
					}
					else { // regular enchantments (not stored enchantments)
						itemStackEnchantments = itemToCompare.getEnchantments();
						stockItemEnchantments = selectedItem.getItemStack().getEnchantments();
					}
					
					if (itemStackEnchantments == null || itemStackEnchantments.isEmpty()) {
						equal = (stockItemEnchantments == null || stockItemEnchantments.isEmpty());
					}
					else {
						equal = ( stockItemEnchantments != null 
								&& !stockItemEnchantments.isEmpty() 
								&& itemStackEnchantments.keySet().equals(stockItemEnchantments.keySet()) );
					}

					// equal is still true if both itemStacks had the same enchanments
					if ( equal && checkLevel ) {
						for ( Map.Entry<Enchantment,Integer> ench : itemStackEnchantments.entrySet() ) {
							if ( ench.getValue() != stockItemEnchantments.get(ench.getKey()) ) {
								equal = false;
								break;
							}
						}
					}
				}
			}

			return equal;
		}
		return false;
	}

	public final Trader selectBaseItem(ItemStack item, TraderStatus status, boolean dura, boolean amount) {
		selectedItem = trait.getStock().getItem(item, status, dura, amount);
		return this;
	}
	public final Trader selectBaseItem(ItemStack item, TraderStatus status, boolean amount) {
		selectedItem = trait.getStock().getItem(item, status, StockItem.hasDurability(item), amount);
		return this;
	}
	
	public final Trader selectItem(StockItem i) {
		selectedItem = i;
		return this;
	}
	public final Trader selectItem(int slot, TraderStatus status) {
		selectedItem = trait.getStock().getItem(slot, status);
	//	System.out.print(selectedItem + " | " + trait.getStock().getStock("sell").size());
		if ( selectedItem != null )
			selectedItem = traderStock.getItem(slot, status);
	//	System.out.print(selectedItem + " | " + traderStock.getStock("sell").size());
		return this;
	} 
	public final Trader selectItem(ItemStack item, TraderStatus status, boolean dura, boolean amount) {
		selectedItem = trait.getStock().getItem(item, status, dura, amount);
		if ( selectedItem != null )
			selectedItem = traderStock.getItem(item, status, dura, amount);
		return this;
	}
	public final Trader selectItem(ItemStack item, TraderStatus status, boolean amount) {
		selectedItem = trait.getStock().getItem(item, status, StockItem.hasDurability(item), amount);
		if ( selectedItem != null )
			selectedItem = traderStock.getItem(item, status, StockItem.hasDurability(item), amount);
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
				if ( MetaTools.getName(item).equals(selectedItem.getName()) ) 
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
				if ( MetaTools.getName(item).equals(selectedItem.getName()) && selectedItem.equalsLores(item) && selectedItem.equalsFireworks(item) ) 
				{
					//add amount to an item in the inventory, its done
					if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() ) {
						item.setAmount( item.getAmount() + amountToAdd );
						setItemPriceLore(item);
						return true;
					} 
					
					//add amount to an item in the inventory, but we still got some left
					if ( item.getAmount() < selectedItem.getItemStack().getMaxStackSize() ) {
						amountToAdd = ( item.getAmount() + amountToAdd ) % selectedItem.getItemStack().getMaxStackSize(); 
						item.setAmount(selectedItem.getItemStack().getMaxStackSize());
						setItemPriceLore(item);
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
			
			setItemPriceLore(is);
			
			//set the item info the inv
			inventory.setItem(inventory.firstEmpty(), is);
			return true;
		}
		
		//could not be added to inventory
		return false;
	}
	
	
	public void setItemPriceLore(ItemStack is)
	{
		MetaTools.removeDescription(is, "player-inventory");
		StockItem it = this.getStock().getItem(is, TraderStatus.BUY, true, false);
		
		if ( it != null )
		{
			int scale = is.getAmount() / it.getAmount();
			
			NumberFormat f = NumberFormat.getCurrencyInstance();
			//DecimalFormat f = new DecimalFormat("#.##");
			
			List<String> lore = new ArrayList<String>(); ;
			for ( String l : CitizensTrader.getLocaleManager().lore("player-inventory") ) //itemsConfig.getPriceLore("pbuy") )
				lore.add(l.replace("{unit}", f.format(getPrice(player, "buy")).replace("$", "")+"").replace("{stack}", f.format(getPrice(player, "buy")*scale)+""));
			
			if ( scale > 0 )
				MetaTools.addDescription(is, lore);	
		}
	}
	
	public final boolean removeFromInventory(ItemStack item, InventoryClickEvent event)
	{
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
	
	public final void switchInventory(TraderStatus status, String type) {
		inventory.clear();
		traderStock.inventoryView(inventory, status, player, type);
	}

	public final void switchInventory(TraderStatus status) {
		inventory.clear();
		traderStock.inventoryView(inventory, status, player, "manage");
		reset(status);
	}
	
	public final void switchInventory(StockItem item) {
		inventory.clear();
		
		if ( traderStatus.isManaging() )
			TraderStockPart.setManagerInventoryWith(inventory, item);
		else
			traderStock.setInventoryWith(this, inventory, item, player);
		selectedItem = item;
	}
	
	public boolean checkBuyLimits(int scale) {
		return limits.checkLimit(this, player.getName(), selectedItem, selectedItem.getAmount()*scale) &&
				limits.checkLimit(this, "global limit", selectedItem, selectedItem.getAmount()*scale);
	}
	
	public boolean checkLimits() {
		return limits.checkLimit(this, player.getName(), selectedItem, selectedItem.getAmount()) &&
				limits.checkLimit(this, "global limit", selectedItem, selectedItem.getAmount());
	}
	
	public boolean checkLimits(int slot) {
		return limits.checkLimit(this, player.getName(), selectedItem, selectedItem.getAmount(slot)) &&
				limits.checkLimit(this, "global limit", selectedItem, selectedItem.getAmount(slot));
	}
	
	public void updateBuyLimits(int scale) {
		try {
			limits.updateLimit(this, player.getName(), selectedItem, selectedItem.getAmount()*scale);
			limits.updateLimit(this, "global limit", selectedItem, selectedItem.getAmount()*scale);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void updateLimits(int slot) {
		try {
			limits.updateLimit(this, player.getName(), selectedItem, selectedItem.getAmount(slot));
			limits.updateLimit(this, "global limit", selectedItem, selectedItem.getAmount(slot));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateLimits() {
		try {
			limits.updateLimit(this, player.getName(), selectedItem, selectedItem.getAmount());
			limits.updateLimit(this, "global limit", selectedItem, selectedItem.getAmount());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}

	//saving amounts
	public final void saveManagedAmounts() {
		TraderStockPart.saveNewAmounts(inventory, selectedItem);
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
		return TraderStatus.MANAGE_SELL;
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
		return itemToCompare != null && ( itemToCompare.getType().equals(managementItem.getType()) &&
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
		else if ( is.getType().equals(Material.STONE) )
			return is.getAmount()*10000;
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
		else
		if ( StockItem.isLeatherArmor(is) )
		{
			LeatherArmorMeta meta = (LeatherArmorMeta) is.getItemMeta();
			Color color = meta.getColor();
			itemInfo += " c:" + color.getRed() + "^" + color.getGreen() + "^" + color.getBlue();
		}
		else
		if ( is.getType().equals(Material.FIREWORK) )
		{
			itemInfo += " fw:" + StockItem.fireworkData(is);
		}
		
		String name = MetaTools.getName(is);
		if ( !name.isEmpty() )
			itemInfo += " n:" + name;
		if ( is.hasItemMeta() )
		{
			if ( is.getItemMeta().hasLore() )
			{
				itemInfo += "  lore";
			//	System.out.print(itemInfo);
				return new StockItem(itemInfo, is.getItemMeta().getLore());
			}
		}
		
		return new StockItem(itemInfo);
	}

	public TraderTrait getBase() {
		return trait;
	}
	
	public static TraderStatus getStartStatus(Player player) {
		if ( permissionsManager.has(player, "dtl.trader.options.sell") )
			return TraderStatus.SELL;
		else if ( permissionsManager.has(player, "dtl.trader.options.buy") )
			return TraderStatus.BUY;
		return null;
	}
	
	public static TraderStatus getManageStartStatus(Player player) {
		if ( permissionsManager.has(player, "dtl.trader.options.sell") )
			return TraderStatus.MANAGE_SELL;
		else if ( permissionsManager.has(player, "dtl.trader.options.buy") )
			return TraderStatus.MANAGE_BUY;
		return null;
	}
	
	//loging function
	public void log(String action, int id, byte data, int amount, double price) 
	{
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("HH-mm-ss");
		
		NumberFormat dec = NumberFormat.getCurrencyInstance();
		//DecimalFormat dec = new DecimalFormat("#.##");
		
		loggingManager.log("["+df.format(date)+"]["+npc.getName()+"]["+action+"] - <" + player.getName() + ">\n      id:"+ id + " data:" + data + " amount:" + amount + " price:" + dec.format(price).format("$", "") );
	}
	
	public void playerLog(String owner, String buyer, String action, StockItem item, int slot)
	{
		//TODO Log
	//	loggingManager.playerLog(owner, npc.getName(), locale.message("xxx-transaction-xxx-item-log", "entity:name", "transaction:"+action).replace("{name}", buyer).replace("{item}", item.getItemStack().getType().name().toLowerCase() ).replace("{amount}", ""+item.getAmount(slot)) );
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
		return getStock().getPrice(getSelectedItem(), player, transaction, slot);
	}
	
	public double getPrice(Player player, String transaction, StockItem item, int slot)
	{
		return getStock().getPrice(item, player, transaction, slot);
	}
	
	public static String playerMoney(Player player)
	{
		return NumberFormat.getNumberInstance().format(CitizensTrader.getEconomy().getBalance(player.getName()));
	}
	
	public void loadDescriptions(Player player, Inventory inventory)
	{
		NumberFormat f = NumberFormat.getCurrencyInstance();
	    //DecimalFormat f = new DecimalFormat("#.##");
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
					for ( String l : CitizensTrader.getLocaleManager().lore("player-inventory") )
						lore.add(l.replace("{unit}", f.format(getPrice(player, "buy", stockItem, 0)).replace("$", "")+"").replace("{stack}", f.format(getPrice(player, "buy", stockItem, 0)*scale).replace("$", "")+""));
					
					if ( scale > 0 )
						MetaTools.addDescription(item, lore);			
				}
			}
		}
	}
	
	
}

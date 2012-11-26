package net.dtl.citizens.trader.traders;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.ItemsConfig;
import net.dtl.citizens.trader.LocaleManager;
import net.dtl.citizens.trader.LoggingManager;
import net.dtl.citizens.trader.PatternsManager;
import net.dtl.citizens.trader.PermissionsManager;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.TraderCharacterTrait.EcoNpcType;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.objects.Wallet;
import net.dtl.citizens.trader.parts.TraderConfigPart;
import net.dtl.citizens.trader.parts.TraderStockPart;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;


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
	private TraderStatus traderStatus;
	private Inventory inventory;
	private Player player;
	private NPC npc;
	
	//Trader runtime 
	private StockItem selectedItem = null; 
	private Boolean inventoryClicked = true;
	private Integer lastSlot = -1;


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

	public final boolean inventoryHasPlace(Player player, int slot) {
		int amountToAdd = selectedItem.getAmount(slot);
		return this.inventoryHasPlaceAmount(player, amountToAdd);
	}
	public final boolean inventoryHasPlaceAmount(Player player,int amount) {
		PlayerInventory inventory = player.getInventory();
		int amountToAdd = amount;
		
		//get all item stack with the same type
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() )
		{
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) 
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

		//if we still ahve some items to add, is there an empty slot for them?
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			return true;
		}
		return false;
	}
	
	public final boolean addSelectedToInventory(Player player, int slot) {
		return addAmountToInventory(player, selectedItem.getAmount(slot));
	}
	public final boolean addAmountToInventory(Player player, int amount) {
		PlayerInventory inventory = player.getInventory();
		int amountToAdd = amount;

		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) 
		{
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() )
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
		
		//create new stack
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			//new stack
			ItemStack is = selectedItem.getItemStack().clone();
			is.setAmount(amountToAdd);
			
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
	public final void switchInventory(TraderStatus status) {
		inventory.clear();
		traderStock.inventoryView(inventory, status, player);
		reset(status);
	}
	
	
	/* *
	 * Switching to the MultipleAmount's selection
	 * 
	 */
	public final void switchInventory(StockItem item) {
		inventory.clear();
		if ( TraderStatus.hasManageMode(traderStatus) )
			TraderStockPart.setManagerInventoryWith(inventory, item);
		else
			traderStock.setInventoryWith(inventory, item);
		selectedItem = item;
	}
	
	public static void removeDescriptions(Inventory inventory)
	{		
		/*CraftItemStack i = new CraftItemStack(Material.APPLE);
		NBTTagCompound t = i.getHandle().getTag();
		
		if ( t == null )
			t = new NBTTagCompound();
		
		NBTTagList list = new NBTTagList();
		list.add(new NBTTagString("other", "A description1"));
		list.add(new NBTTagString("other", "A description2"));
		t.set("Lore", list);
		
		i.getHandle().setTag(t);
		
		addDescription(i, new String[] { "Item price", "Stack price" });*/
		
		for ( ItemStack item : inventory.getContents() )
		{
			if ( item != null )
			{
				net.minecraft.server.ItemStack c = ((CraftItemStack)item).getHandle();
				NBTTagCompound tc = c.getTag();
				
				if ( tc != null )
				{
					if ( tc.hasKey("display") )
					{
						NBTTagCompound d = tc.getCompound("display");
						
						if ( d != null )
						{
							if ( d.hasKey("Lore") )
							{
								
								NBTTagList oldList = d.getList("Lore");
								NBTTagList newList = new NBTTagList();
								
								for ( int j = 0 ; j < oldList.size() ; ++j )
									if ( !oldList.get(j).getName().equals("dtl_trader") && !oldList.get(j).getName().isEmpty() )
										newList.add(oldList.get(j));
								
								d.set("Lore", newList);
							}
						}
					}
				}
			}
		}		
	}
	
	public static void addDescription(CraftItemStack item, List<String> lore)
	{
		net.minecraft.server.ItemStack c = item.getHandle();
		NBTTagCompound tag = c.getTag();

		if ( tag == null )
			tag = new NBTTagCompound();
		c.setTag(tag);
		
		if(!tag.hasKey("display")) 
			tag.set("display", new NBTTagCompound());
		
		NBTTagCompound d = tag.getCompound("display");
		
		if ( !d.hasKey("Lore") )
			d.set("Lore", new NBTTagList());
		
		NBTTagList list = d.getList("Lore");
			
		for ( String line : lore )
			list.add(new NBTTagString("dtl_trader", line.replace('^', '§')));

	}
	
	public static void resetDescription(CraftItemStack item)
	{
		net.minecraft.server.ItemStack c = item.getHandle();
		NBTTagCompound tag = c.getTag();

		if ( tag == null )
			tag = new NBTTagCompound();
		c.setTag(tag);
		
		if(!tag.hasKey("display")) 
			tag.set("display", new NBTTagCompound());
		
		NBTTagCompound d = tag.getCompound("display");
		
		if ( !d.hasKey("Lore") )
			d.set("Lore", new NBTTagList());
		

		NBTTagList list = d.getList("Lore");
		NBTTagList newList = new NBTTagList();
		
		for ( int j = 0 ; j < list.size() ; ++j )
			if ( !list.get(j).getName().equals("dtl_trader") && !list.get(j).getName().isEmpty() )
				newList.add(list.get(j));
		
		d.set("Lore", newList);

	}
	
	
	//===============================================================================================
	
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
	public final boolean equalsTraderType(EcoNpcType type) {
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
	public final TraderStockPart getTraderStock() {
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
	
	public static TraderStatus getStartStatus(Player player) {
		if ( permissions.has(player, "dtl.trader.options.sell") )
			return TraderStatus.SELL;
		else if ( permissions.has(player, "dtl.trader.options.buy") )
			return TraderStatus.BUY;
		return null;
	}
	
	public static TraderStatus getManageStartStatus(Player player) {
		if ( permissions.has(player, "dtl.trader.options.sell") )
			return TraderStatus.MANAGE_SELL;
		else if ( permissions.has(player, "dtl.trader.options.buy") )
			return TraderStatus.MANAGE_BUY;
		return null;
	}
	
	
	
	//loging function
	public void log(String action, String player, int id, byte data, int amount, double price) 
	{
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("HH-mm-ss");
		
		DecimalFormat dec = new DecimalFormat("#.##");
		
		logging.log("["+df.format(date)+"]["+npc.getName()+"]["+action+"] - <" + player + ">\n      id:"+ id + " data:" + data + " amount:" + amount + " price:" + dec.format(price) );
	}
	
	public void playerLog(String owner, String buyer, String action, StockItem item, int slot)
	{
		logging.playerLog(owner, npc.getName(), locale.getLocaleString("xxx-transaction-xxx-item-log", "entity:name", "transaction:"+action).replace("{name}", buyer).replace("{item}", item.getItemStack().getType().name().toLowerCase() ).replace("{amount}", ""+item.getAmount(slot)) );
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


	
}

package net.dtl.citizenstrader_new.traders;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.containers.StockItem;
import net.dtl.citizenstrader_new.containers.Wallet;
import net.dtl.citizenstrader_new.traits.InventoryTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait.TraderType;
import net.dtl.citizenstrader_new.traits.TraderTrait.WalletType;

public abstract class Trader {
	
	public enum TraderStatus {
		PLAYER_SELL, PLAYER_BUY, PLAYER_SELL_AMOUNT, PLAYER_MANAGE_SELL, PLAYER_MANAGE_SELL_AMOUNT, PLAYER_MANAGE_PRICE, PLAYER_MANAGE_BUY, PLAYER_MANAGE;
	
		public static boolean hasManageMode(TraderStatus status) {
			if ( !status.equals(PLAYER_SELL) && 
				 !status.equals(PLAYER_SELL_AMOUNT) && 
				 !status.equals(PLAYER_BUY) )
				return true;
			return false;
		}
	}
	
	private TraderStatus traderStatus;
	private TraderTrait traderConfig;
	
	private NPC npc;
	private Inventory inventory;
	private InventoryTrait traderStock; 
	private StockItem selectedItem = null; 
	private Wallet wallet;
	
	private Boolean inventoryClicked = true;
	private Integer slotClicked = -1;
	
	public Trader(NPC n,TraderTrait c) {
		npc = n;
		traderStock = npc.getTrait(InventoryTrait.class);
		inventory = traderStock.inventoryView(54, npc.getName() + " selling");
		traderConfig = c;
		wallet = new Wallet(traderConfig.getWalletType());
		traderStatus = TraderStatus.PLAYER_SELL;
	}
	
	public final void saveManagedAmouts() {
		traderStock.saveNewAmouts(inventory, selectedItem);
	}
	public final void switchInventory(TraderStatus status) {
		inventory.clear();
		traderStock.inventoryView(inventory, status);
		reset(status);
	}
	public final void switchInventory(StockItem item) {
		inventory.clear();
		InventoryTrait.setInventoryWith(inventory, item);
		selectedItem = item;
	}
	public boolean equalsSelected(ItemStack itemToCompare,boolean durability,boolean amount) {
		boolean equal = false;
		if ( itemToCompare.getType().equals(selectedItem.getItemStack().getType()) &&
			 itemToCompare.getData().equals(selectedItem.getItemStack().getData()) ) {
			equal = true;
			if ( durability ) 
				equal = itemToCompare.getDurability() >= selectedItem.getItemStack().getDurability();
			if ( amount && equal )
				equal =  itemToCompare.getAmount() == selectedItem.getItemStack().getAmount();
			return equal;
		}
		return false;
	}
	
	public final Trader reset(TraderStatus status) {
		traderStatus = status;
		selectedItem = null;
		inventoryClicked = true;
		slotClicked = -1;
		return this;
	}
	
	public final void setTraderStatus(TraderStatus s) {
		traderStatus = s;
	}
	public final void setInventoryClicked(Boolean i) {
		inventoryClicked = i;
	}
	public final void setClickedSlot(Integer s) {
		slotClicked = s;
	}
	
	
	public final Trader selectItem(StockItem i) {
		selectedItem = i;
		return this;
	}
	public final Trader selectItem(int slot,TraderStatus status) {
		selectedItem = traderStock.getItem(slot, status);
		return this;
	}
	public final boolean hasSelectedItem() {
		return selectedItem != null;
	}
	public final StockItem getSelectedItem() {
		return selectedItem;
	}
	
	public final TraderStatus getTraderStatus() {
		return traderStatus;
	}
	public final TraderType getTraderType() {
		return traderConfig.getTraderType();
	}
	public final WalletType getWalletType() {
		return traderConfig.getWalletType();
	}
	
	public final boolean equalsTraderStatus(TraderStatus status) {
		return traderStatus.equals(status);
	}
	public final boolean equalsTraderType(TraderType type) {
		return traderConfig.getTraderType().equals(type);
	}
	public final boolean equalsWalletType(WalletType type) {
		return traderConfig.getWalletType().equals(type);
	}
	
	public final boolean isStockItem(int slot,TraderStatus status) {
		if ( status.equals(TraderStatus.PLAYER_MANAGE_SELL) )
			return traderStock.itemForSell(slot) != null;
		if ( status.equals(TraderStatus.PLAYER_MANAGE_BUY) )
			return traderStock.wantItemBuy(slot) != null;
		return false;
	}
	
	public final NPC getNpc() {
		return npc;
	}
	public final Inventory getInventory() {
		return inventory;
	}
	public final Wallet getWallet() {
		return wallet;
	}	
	
	public final InventoryTrait getTraderStock() {
		return traderStock;
	}
	
	public final boolean getInventoryClicked() {
		return inventoryClicked;
	}
	public final Integer getClickedSlot() {
		return slotClicked;
	}
	
	/*
	 * Static functions for cleaner code comparing
	 * 
	 */
	public static boolean isWool(ItemStack itemToCompare,byte colorData) {
		return itemToCompare.equals(new ItemStack(35,1,(short)0,colorData));
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
	
	public static StockItem toStockItem(ItemStack is) {
		String itemInfo = is.getTypeId()+":"+ is.getData().getData() +" a:"+is.getAmount() + " d:" + is.getDurability();
		if ( !is.getEnchantments().isEmpty() ) {
			itemInfo += " e:";
			for ( Enchantment ench : is.getEnchantments().keySet() ) 
				itemInfo += ench.getId() + "/" + is.getEnchantmentLevel(ench) + ",";
		}
		return new StockItem(itemInfo);
	}
	
	public abstract void secureMode(InventoryClickEvent event);
	
	public abstract void simpleMode(InventoryClickEvent event);
	
	public abstract void managerMode(InventoryClickEvent event);
}

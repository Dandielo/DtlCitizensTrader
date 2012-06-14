package net.dtl.citizenstrader_new.traders;

import org.bukkit.inventory.Inventory;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.containers.StockItem;
import net.dtl.citizenstrader_new.containers.Wallet;
import net.dtl.citizenstrader_new.traits.InventoryTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait.TraderType;
import net.dtl.citizenstrader_new.traits.TraderTrait.WalletType;

public abstract class Trader {
	
	public enum TraderStatus {
		PLAYER_SELL, PLAYER_BUY, PLAYER_SELL_AMOUT, PLAYER_MANAGE_SELL, PLAYER_MANAGE_SELL_AMOUT, PLAYER_MANAGE_PRICE, PLAYER_MANAGE_BUY,
	}
	
	private TraderStatus traderStatus;
	private TraderTrait traderConfig;
	
	private NPC npc;
	private Inventory inventory;
	private InventoryTrait traderStock;
	private StockItem selectedItem;
	private Wallet wallet;
	
	private Boolean inventoryClicked;
	private Integer slotClicked;
	
	public void setTraderStatus(TraderStatus s) {
		traderStatus = s;
	}
	public void setInventoryClicked(Boolean i) {
		inventoryClicked = i;
	}
	public void setClickedSlot(Integer s) {
		slotClicked = s;
	}
	public void selectItem(StockItem i) {
		selectedItem = i;
	}
	
	public TraderStatus getTraderStatus() {
		return traderStatus;
	}
	public TraderType getTraderType() {
		return traderConfig.getTraderType();
	}
	public WalletType getWalletType() {
		return traderConfig.getWalletType();
	}
	
	public boolean equalsTraderStatus(TraderStatus status) {
		return traderStatus.equals(status);
	}
	public boolean equalsTraderType(TraderType type) {
		return traderConfig.getTraderType().equals(type);
	}
	public boolean equalsWalletType(WalletType type) {
		return traderConfig.getWalletType().equals(type);
	}
	
	public NPC getNpc() {
		return npc;
	}
	public Inventory getInventory() {
		return inventory;
	}
	public StockItem getSelectedItem() {
		return selectedItem;
	}
	public Wallet getWallet() {
		return wallet;
	}
	
	public boolean getInventoryClicked() {
		return inventoryClicked;
	}
	public Integer getSlotClicked() {
		return slotClicked;
	}
}

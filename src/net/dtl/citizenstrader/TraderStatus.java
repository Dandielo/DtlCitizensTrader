package net.dtl.citizenstrader;

import org.bukkit.inventory.Inventory;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader.traits.StockItem;

public class TraderStatus {
	
	public enum Status {
		PLAYER_SELL, PLAYER_BUY, PLAYER_SELL_AMOUT, PLAYER_MANAGE, PLAYER_SELECTED
	}
	
	private NPC trader;
	private Status status;
	private Inventory inventory;
	private StockItem itemSelected;
	
	public TraderStatus(NPC t) {
		trader = t;
		status = Status.PLAYER_SELL;
	}
	public TraderStatus(NPC t,Status s) {
		trader = t;
		status = s;
	}
	
	public void setStatus(Status s) {
		status = s;
	}
	public Status getStatus() {
		return status;
	}
	
	public void setInventory(Inventory i) {
		inventory = i;
	}
	public Inventory getInventory() {
		return inventory;
	}
	
	public void setStockItem(StockItem si) {
		itemSelected = si;
	}
	public StockItem getStockItem() {
		return itemSelected;
	}
	
	public NPC getTrader() {
		return trader;
	}
	
	
}

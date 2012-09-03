package net.dtl.citizenstrader_new.traders;


import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface EconomyNpc {
	
	
	public abstract void secureMode(InventoryClickEvent event);
	
	public abstract void simpleMode(InventoryClickEvent event);
	
	public abstract void managerMode(InventoryClickEvent event);
	
	public abstract TraderStatus getTraderStatus();
	public abstract void setTraderStatus(TraderStatus status);
	
	public abstract int getNpcId();
	
	public abstract Inventory getInventory();
	
}

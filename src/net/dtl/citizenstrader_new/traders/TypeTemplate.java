package net.dtl.citizenstrader_new.traders;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface TypeTemplate {
	
	public abstract void secureMode(InventoryClickEvent event);
	
	public abstract void simpleMode(InventoryClickEvent event);
	
	public abstract void managerMode(InventoryClickEvent event);
	
}

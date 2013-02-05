package net.dtl.citizens.trader.types;


import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderTrait;
import net.dtl.citizens.trader.TraderTrait.EType;
import net.dtl.citizens.trader.objects.Wallet;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface tNPC {
	
	
	public abstract void settingsMode(InventoryClickEvent event);
	public abstract void simpleMode(InventoryClickEvent event);
	public abstract void managerMode(InventoryClickEvent event);
	
	public abstract boolean onRightClick(Player player, TraderTrait trait, NPC npc);
	
	public abstract int getNpcId();
	public abstract NPC getNpc();
	public abstract Inventory getInventory();
	public abstract Wallet getWallet();
	
	public abstract EType getType();
	
	public abstract boolean locked();
	
}

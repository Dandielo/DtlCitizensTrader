package net.dtl.citizens.trader.traders;


import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.objects.Wallet;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface EconomyNpc {
	
	
	public abstract void settingsMode(InventoryClickEvent event);
	public abstract void simpleMode(InventoryClickEvent event);
	public abstract void managerMode(InventoryClickEvent event);
	
	public abstract boolean onRightClick(Player player, TraderCharacterTrait trait, NPC npc);
	
	public abstract int getNpcId();
	public abstract NPC getNpc();
	public abstract Inventory getInventory();
	public abstract Wallet getWallet();
	
	public abstract boolean locked();
	
}

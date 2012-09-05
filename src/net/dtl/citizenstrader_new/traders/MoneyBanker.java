package net.dtl.citizenstrader_new.traders;

import org.bukkit.event.inventory.InventoryClickEvent;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.traits.BankTrait;

public class MoneyBanker extends Banker {

	public MoneyBanker(NPC bankerNpc, BankTrait bankConfiguration, String player) {
		super(bankerNpc, bankConfiguration, player);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void settingsMode(InventoryClickEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void simpleMode(InventoryClickEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void managerMode(InventoryClickEvent event) {
		// TODO Auto-generated method stub
		
	}

}

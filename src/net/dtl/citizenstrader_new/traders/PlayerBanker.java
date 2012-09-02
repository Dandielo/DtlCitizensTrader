package net.dtl.citizenstrader_new.traders;

import org.bukkit.event.inventory.InventoryClickEvent;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.traits.BankTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait;

public class PlayerBanker extends Banker {

	public PlayerBanker(NPC traderNpc, BankTrait bankConfiguragion) {
		super(traderNpc, bankConfiguragion);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void secureMode(InventoryClickEvent event) {
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

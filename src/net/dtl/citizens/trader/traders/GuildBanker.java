package net.dtl.citizens.trader.traders;

import org.bukkit.event.inventory.InventoryClickEvent;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.traders.Trader.TraderStatus;
import net.dtl.citizens.trader.traits.BankTrait;

public class GuildBanker extends Banker {

	public GuildBanker(NPC traderNpc, BankTrait bankConfiguragion) {
		super(traderNpc, bankConfiguragion, "");
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

	@Override
	public TraderStatus getTraderStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTraderStatus(TraderStatus status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNpcId() {
		// TODO Auto-generated method stub
		return 0;
	}

}

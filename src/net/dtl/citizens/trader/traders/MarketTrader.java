package net.dtl.citizens.trader.traders;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.traits.TraderTrait;

public class MarketTrader extends Trader {

	public MarketTrader(NPC traderNpc, TraderTrait traderConfiguragion) {
		super(traderNpc, traderConfiguragion);
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
	public void onRightClick(Player player, TraderCharacterTrait trait, NPC npc) {
		// TODO Auto-generated method stub
		
	}

}

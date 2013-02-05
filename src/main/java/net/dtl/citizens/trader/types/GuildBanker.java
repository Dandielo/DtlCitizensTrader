package net.dtl.citizens.trader.types;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderTrait;
import net.dtl.citizens.trader.TraderTrait.EType;
import net.dtl.citizens.trader.parts.BankerPart;

public class GuildBanker extends Banker {

	public GuildBanker(NPC traderNpc, BankerPart bankConfiguragion) {
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
	public int getNpcId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean onRightClick(Player player, TraderTrait trait, NPC npc) {
		return false;
		// TODO Auto-generated method stub
		
	}

	@Override
	public EType getType() {
		// TODO Auto-generated method stub
		return null;
	}

}

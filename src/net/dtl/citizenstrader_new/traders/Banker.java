package net.dtl.citizenstrader_new.traders;

import java.util.List;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.traits.BankAccount;
import net.dtl.citizenstrader_new.traits.TraderTrait;

abstract public class Banker extends Trader {
	//BankTab System
	enum BankTab {
		Tab1, Tab2, Tab3, Tab4, Tab5, Tab6, Tab7, Tab8, Tab9
	}
	
	//players using the Banker atm
	private static List<BankAccount> bankAccounts;
	
	public Banker(NPC traderNpc, TraderTrait traderConfiguragion) {
		super(traderNpc, traderConfiguragion);

	}

}

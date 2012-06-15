package net.dtl.citizenstrader_new.traits;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class TraderTrait extends Trait {

	public enum WalletType {
		PLAYER_WALLET, PLAYER_BANK, NPC_WALLET, SERVER_BANK, SERVER_INFINITY, GROUP_WALLET //Future FACTION_WALLET
	}
	
	public enum TraderType {
		PLAYER_TRADER, SERVER_TRADER, AUCTIONHUSE, BANK, CUSTOM
	}

	private WalletType wType;
	private TraderType tType = TraderType.SERVER_TRADER;
	
	public TraderType getTraderType() {
		return tType;
	}
	public WalletType getWalletType() {
		return wType;
	}
	
	@Override
	public void load(DataKey arg0) throws NPCLoadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save(DataKey arg0) {
		// TODO Auto-generated method stub
		
	}

}

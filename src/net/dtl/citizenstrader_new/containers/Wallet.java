package net.dtl.citizenstrader_new.containers;

import net.dtl.citizenstrader_new.traits.TraderTrait.WalletType;
import net.milkbowl.vault.economy.Economy;

public class Wallet {
	private WalletType type;
	private Economy economy;
	
	public Wallet(WalletType t,Economy e) {
		type = t;
		economy = e;
	}
	
	public WalletType getWalletType() {
		return type;
	}
	
	public void hasMoney() {
		
	}
	public boolean deposit() {
		return false;
		
	}
	public boolean withdraw() {
		return false;
		
	}
	
}

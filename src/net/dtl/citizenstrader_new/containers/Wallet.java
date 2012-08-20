package net.dtl.citizenstrader_new.containers;

import net.dtl.citizenstrader_new.traits.TraderTrait.WalletType;
import net.milkbowl.vault.economy.Economy;

public class Wallet {
	private WalletType type;
	private Economy economy;
	
	private double money; 
	private String bank;
	
	public Wallet(WalletType t) {
		type = t;
	}
	
	public void setEconomy(Economy e) {
		economy = e;
	}
	
	public WalletType getWalletType() {
		return type;
	}
	public void setWalletType(WalletType w) {
		type = w;
	}
	public void setMoney(double m) {
		money = m;
	}
	public double getMoney() {
		return money;
	}
	
	/*public boolean hasMoney(Player p, double m) {
		if ( type.equals(WalletType.PLAYER_WALLET) ) 
			return economy.has(p.getName(), m);
		return true;
	}*/
	public void deposit(String p, double m, boolean isOwner) {
		if ( isOwner ) {
			if ( type.equals(WalletType.PLAYER_WALLET) )
				economy.depositPlayer(p, m);
			else if ( type.equals(WalletType.NPC_WALLET) )
				money += m;
			else if ( type.equals(WalletType.SERVER_INFINITE) ) {
			}
		} else {
			economy.depositPlayer(p, m);
		}
	}
	public boolean withdraw(String p, double m, boolean isOwner) {
		if ( isOwner ) {
			if ( type.equals(WalletType.PLAYER_WALLET) ) {
				if ( economy.getBalance(p) >= m ) {
					economy.withdrawPlayer(p, m);
					return true;
				}
			} else if ( type.equals(WalletType.NPC_WALLET) ) {
				if ( money >= m ) {
					money -= m;
					return true;
				}
			} else if ( type.equals(WalletType.SERVER_INFINITE) ) {
				return true;
			}
		} else {
			if ( economy.getBalance(p) >= m ) {
				economy.withdrawPlayer(p, m);
				return true;
			}
		}
		return false;
	}
	
}

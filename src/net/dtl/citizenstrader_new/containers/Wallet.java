package net.dtl.citizenstrader_new.containers;

import net.dtl.citizenstrader_new.CitizensTrader;
import net.dtl.citizenstrader_new.traits.TraderTrait.WalletType;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public class Wallet {
	private WalletType type;
	private Economy economy;
	private Clan clan; 
	
	private double money; 
	private String bank;
	
	public Wallet(WalletType t) {
		type = t;
		bank = "";
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
	
	public void setClan(String clanTag) {
		clan = CitizensTrader.getSimpleClans().getClanManager().getClan(clanTag);
	}
	
	public String getClan()
	{
		return clan.getTag();
	}
	
	public boolean setBank(String player, String bankName)
	{
		//does we support banks?
		if ( !economy.hasBankSupport() )
			return false;
		
		//check if the given bank belongs to the traders owner
		if (// !economy.isBankMember(bank, player).type.equals(ResponseType.SUCCESS)&&
				 !economy.isBankOwner(bank, player).equals(ResponseType.SUCCESS) )
			return false;
		
		//set the bank
		bank = bankName;
		return true;
	}
	
	public String getBank()
	{
		return bank;
	}
	
	/*public boolean hasMoney(Player p, double m) {
		if ( type.equals(WalletType.PLAYER_WALLET) ) 
			return economy.has(p.getName(), m);
		return true;
	}*/
	public void deposit(String p, double m, boolean isOwner) {
		//is the given player the trader owner?
		if ( isOwner ) 
		{
			if ( type.equals(WalletType.OWNER_WALLET) )
				economy.depositPlayer(p, m);
			else 
			if ( type.equals(WalletType.NPC_WALLET) )
				money += m;
			else 
			if ( type.equals(WalletType.INFINITE) ) 
				return;
			else
			if ( type.equals(WalletType.BANK) )
				economy.bankDeposit(bank, m);
			else
			if ( type.equals(WalletType.SIMPLE_CLANS) )
				clan.deposit(money, CitizensTrader.getSimpleClans().getClanManager().getClanPlayer(p));
		} else {
			economy.depositPlayer(p, m);
		}
	}
	public boolean withdraw(String p, double m, boolean isOwner) {
		//is the given player the trader owner?
		if ( isOwner ) 
		{
			if ( type.equals(WalletType.OWNER_WALLET) )
			{
				//have we enough money?
				if ( economy.getBalance(p) >= m ) 
				{
					economy.withdrawPlayer(p, m);
					return true;
				}
			} 
			else 
			if ( type.equals(WalletType.NPC_WALLET) ) 
			{
				//have we enough money?
				if ( money >= m ) {
					money -= m;
					return true;
				}
			} 
			else
			if ( type.equals(WalletType.BANK) )
			{
				//have we enough money?
				if ( economy.bankBalance(bank).balance >= m )
				{
					economy.bankWithdraw(bank, m);
					return true;
				}
			}
			else
			if ( type.equals(WalletType.SIMPLE_CLANS) )
			{
				if ( clan.getBalance() >= m )
				{
					clan.withdraw(money, CitizensTrader.getSimpleClans().getClanManager().getClanPlayer(p));
				}
			}
			else 
			if ( type.equals(WalletType.INFINITE) ) 
			{
				return true;
			}
		} else {
			if ( type.equals(WalletType.SIMPLE_CLANS) )
			{
				System.out.print(clan.getTag());
				System.out.print(CitizensTrader.getSimpleClans().getClanManager().getClanPlayer(p).getName());
				
				
				if ( economy.getBalance(p) >= m )
					return true;
			}
			else
			{
				if ( economy.getBalance(p) >= m ) {
					economy.withdrawPlayer(p, m);
					return true;
				}
			}
		}
		return false;
	}
	
}

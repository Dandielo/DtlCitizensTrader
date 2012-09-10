package net.dtl.citizens.trader.containers;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.traits.TraderTrait.WalletType;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public class Wallet {
	private WalletType type;
	private Economy economy;
	private Clan clan; 
	private Town town;
	
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
	
	public void setTown(String townName)
	{
		town = CitizensTrader.getTowny().getTownyUniverse().getTownsMap().get(townName);
	}
	
	public String getTown()
	{
		return town.getName();
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
			{
				clan.setBalance(clan.getBalance()+m);
			//	clan.deposit(m, CitizensTrader.getSimpleClans().getClanManager().getClanPlayer(p));
			}
			else
			if ( type.equals(WalletType.TOWNY) )
			{
				if ( CitizensTrader.getTowny() != null )
				{
					try {
						double bankcap = TownySettings.getTownBankCap();
						if (bankcap > 0) 
							if (m + town.getHoldingBalance() > bankcap)
								return;
						
						town.setBalance(town.getHoldingBalance()+m, "Trader income");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
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

					clan.setBalance(clan.getBalance()-m);
					//clan.withdraw(money, CitizensTrader.getSimpleClans().getClanManager().getClanPlayer(p));
					return true;
				}
			}
			else
			if ( type.equals(WalletType.TOWNY) )
			{
				if ( CitizensTrader.getTowny() != null )
				{
					try {
						if ( town.getHoldingBalance() >= m )
						{
							town.setBalance(town.getHoldingBalance()-m);
							return true;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else 
			if ( type.equals(WalletType.INFINITE) ) 
			{
				return true;
			}
		} else {
			if ( type.equals(WalletType.TOWNY) )
			{
				if ( economy.getBalance(p) >= m ) {
					economy.withdrawPlayer(p, money);
					return true;
				}
			}
			else
			if ( type.equals(WalletType.SIMPLE_CLANS) )
			{
				if ( economy.getBalance(p) >= m ) {
					economy.withdrawPlayer(p, money);
					return true;
				}
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

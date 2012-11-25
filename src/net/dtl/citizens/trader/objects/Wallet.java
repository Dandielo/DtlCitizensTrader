package net.dtl.citizens.trader.objects;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.integration.Econ;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Town;

import net.dtl.citizens.trader.CitizensTrader;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import net.sacredlabyrinth.phaed.simpleclans.Clan;

public class Wallet {
	//The wallet type
	private WalletType type;
	
	//The associated plugin accounts
	private static Economy economy;
	private Clan clan; 
	private Town town;
	private Faction faction;
	private String bank;
	
	//for private money
	private double money; 
	
	//create a wallet with the givet Type
	public Wallet(WalletType t) {
		type = t;
		economy = CitizensTrader.getInstance().getEconomy();
	}
	
	// get/set wallet type
	public WalletType getWalletType() {
		return type;
	}
	public void setWalletType(WalletType w) {
		type = w;
	}
	
	//set/get money for "private" wallet
	public void setMoney(double m) {
		money = m;
	}
	public double getMoney() {
		return money;
	}
	
	//Towny
	public void setTown(Town ntown)
	{
		town = ntown;
	}
	public String getTown()
	{
		return town.getName();
	}
	public void townyDeposit(double m)
	{
		try
		{
			double bankcap = TownySettings.getTownBankCap();
			if (bankcap > 0) 
				if (m + town.getHoldingBalance() <= bankcap)
					town.setBalance(town.getHoldingBalance()+m, "Trader income");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	//SimpleClans
	public void setClan(Clan nClan) {
		clan = nClan;
	}
	public String getClan()
	{
		return clan.getTag();
	}
	
	//Factions
	public void setFaction(Faction nFaction)
	{
		faction = nFaction;
	}
	public String getFaction()
	{
		return faction.getTag();
	}
	
	//Bank
	public boolean setBank(String player, String bankName)
	{
		//does we support banks?
		if ( !economy.hasBankSupport() )
			return false;
		
		//check if the given bank belongs to the traders owner
		if ( !economy.isBankOwner(bank, player).equals(ResponseType.SUCCESS) )
			return false;
		
		//set the bank
		bank = bankName;
		return true;
	}
	public String getBank()
	{
		return bank;
	}

	//depositing money
	public void deposit(String p, double m)
	{
		switch( type )
		{
			case OWNER: economy.depositPlayer(p, m); break;
			case BANK: economy.bankDeposit(bank, m); break;
			case NPC: money += m; break;
			case SIMPLE_CLANS: clan.setBalance(clan.getBalance()+m); break;
			case FACTIONS: Econ.deposit(faction.getAccountId(), m); break;
			case TOWNY: townyDeposit(m); break;
			default:
				break;
		}
		
		/*
		if ( type.equals(WalletType.OWNER) )
			economy.depositPlayer(p, m);
		else 
		if ( type.equals(WalletType.NPC) )
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
			//clan.deposit(m, CitizensTrader.getSimpleClans().getClanManager().getClanPlayer(p));
		}
		else
		if ( type.equals(WalletType.FACTIONS) )
		{
			Econ.deposit(faction.getAccountId(), m);
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
		}*/
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
				//	clan.withdraw(money, CitizensTrader.getSimpleClans().getClanManager().getClanPlayer(p));
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
			if ( type.equals(WalletType.FACTIONS) )
			{
				if ( CitizensTrader.getFactions() != null )
				{
					if ( Econ.getBalance(faction.getAccountId()) >= m )
					{
						/*((EconomyParticipator)faction).;
						faction.money -= m;*/
						Econ.withdraw(faction.getAccountId(), m);
						
						return true;
					}
				}
			}
			else 
			if ( type.equals(WalletType.INFINITE) ) 
			{
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

	//Deprecated functions
	@Deprecated
	public void setEconomy(Economy e) {
		economy = e;
	}
	
	
	public enum WalletType
	{
		OWNER, NPC, BANK, INFINITE, SIMPLE_CLANS, TOWNY, FACTIONS;

		//Get the type by name
		public static WalletType getTypeByName(String n) {
			if ( n.equalsIgnoreCase("owner") ) 
				return WalletType.OWNER;
			if ( n.equalsIgnoreCase("owner-wallet") ) 
				return WalletType.OWNER;
			else if ( n.equalsIgnoreCase("npc") )
				return WalletType.NPC;
			else if ( n.equalsIgnoreCase("npc-wallet") )
				return WalletType.NPC;
			else if ( n.equalsIgnoreCase("bank") )
				return WalletType.BANK;
			else if ( n.equalsIgnoreCase("infinite") )
				return WalletType.INFINITE;
			else if ( n.equalsIgnoreCase("server-infinite") )
				return WalletType.INFINITE;
			else if ( n.startsWith("simple-clans") )
				return WalletType.SIMPLE_CLANS;
			else if ( n.startsWith("towny") )
				return WalletType.TOWNY;
			else if ( n.startsWith("factions") )
				return WalletType.FACTIONS;
			return null;
		}
		
		//get the name of the type
		public String toString() {
			switch( this )
			{
			case OWNER:
				return "owner";
			case NPC:
				return "npc";
			case BANK:
				return "bank";
			case INFINITE:
				return "infinite";
			case SIMPLE_CLANS:
				return "simle-clans";
			case TOWNY:
				return "towny";
			case FACTIONS:
				return "factions";
			default: 
				break;
			}
			return "";
		}
	}
	
}

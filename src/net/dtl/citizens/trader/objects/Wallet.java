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
	private Clan clan = null; 
	private Town town = null;
	private Faction faction = null;
	private String bank = "";
	
	//for private money
	private double money; 
	
	//create a wallet with the givet Type
	public Wallet(WalletType t) {
		type = t;
		economy = CitizensTrader.getEconomy();
	}
	
	// get/set wallet type
	public WalletType getType() {
		return type;
	}
	public void setType(WalletType type) {
		this.type = type;
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
		if ( town == null )
			return "";
		return town.getName();
	}
	private void townyDeposit(double m)
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
	private boolean townyWithdraw(double m)
	{
		try 
		{
			if ( town.getHoldingBalance() >= m )
				town.setBalance(town.getHoldingBalance()-m);
			return town.getHoldingBalance() >= m;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	//SimpleClans
	public void setClan(Clan nClan) {
		clan = nClan;
	}
	public String getClan()
	{
		if ( clan == null )
			return "";
		return clan.getTag();
	}
	
	//Factions
	public void setFaction(Faction nFaction)
	{
		faction = nFaction;
	}
	public String getFaction()
	{
		if ( faction == null )
			return "";
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
			case OWNER: 
				economy.depositPlayer(p, m); 
				break;
			case BANK: 
				economy.bankDeposit(bank, m); 
				break;
			case NPC: 
				money += m;
				break;
			case SIMPLE_CLANS: 
				clan.setBalance(clan.getBalance()+m); 
				break;
			case FACTIONS: 
				Econ.deposit(faction.getAccountId(), m); 
				break;
			case TOWNY: 
				townyDeposit(m);
				break;
			default:
				break;
		}
	}
	
	public boolean withdraw(String p, double m) 
	{
		switch( type )
		{
			case OWNER:
				return economy.withdrawPlayer(p, m).transactionSuccess();
			case BANK: 
				return economy.bankWithdraw(bank, m).transactionSuccess();
			case NPC: 
				if ( money >= m ) 
					money -= m; 
				return money >= m;
			case FACTIONS:
				return Econ.withdraw(faction.getAccountId(), m);
			case SIMPLE_CLANS:
				if ( clan.getBalance() >= m ) 
					clan.setBalance(clan.getBalance()-m);
				return clan.getBalance() >= m;
			case TOWNY:
				return townyWithdraw(m);
			default:
				return true;
		}
	}

	//Deprecated functions
	@Deprecated
	public void setEconomy(Economy e) {
		//economy = e;
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
			else if ( n.startsWith("clan") )
				return WalletType.SIMPLE_CLANS;
			else if ( n.startsWith("towny") )
				return WalletType.TOWNY;
			else if ( n.startsWith("faction") )
				return WalletType.FACTIONS;
			else if ( n.startsWith("factions") )
				return WalletType.FACTIONS;
			return null;
		}
		
		//get the name of the type
		@Override
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
				return "clan";
			case TOWNY:
				return "towny";
			case FACTIONS:
				return "faction";
			default: 
				break;
			}
			return "";
		}
	}
	
}

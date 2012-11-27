package net.dtl.citizens.trader.parts;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.palmergames.bukkit.towny.object.Town;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.objects.Wallet;
import net.dtl.citizens.trader.objects.Wallet.WalletType;
import net.sacredlabyrinth.phaed.simpleclans.Clan;


public class TraderConfigPart {
	
	private Wallet wallet;
	private String owner;
	private boolean enabled;
	
	public TraderConfigPart() {
		wallet = null;
		owner = "no owner";
		enabled = true;
	}
	
	//set/get the traders owner
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getOwner() {
		return this.owner;
	}
	
	/*
	public void setWalletType(WalletType type) {
		// if the trader is a server trader (admin shop) allow to sat anny walet
		if ( tType.equals(TraderType.SERVER_TRADER) ) 
		{
			wType = type;
			w.setWalletType(wType);
		}
		else
		// if the trader is a player trader
		if ( tType.equals(TraderType.PLAYER_TRADER) ) 
		{
			
			//disallow infinite money
			if ( type.equals(WalletType.INFINITE) )
				return;
			
			
			wType = type;
			w.setWalletType(wType);
		}
		else
		// if the trader is a player trader
		if ( tType.equals(TraderType.MARKET_TRADER) ) 
		{
			
			//disallow infinite money
			if ( type.equals(WalletType.INFINITE) )
				return;
			
			
			wType = type;
			w.setWalletType(wType);
		}
	}*/
	
	/*
	public void setTraderType(TraderType type) {
		if ( type.equals(TraderType.PLAYER_TRADER) ) {
			tType = type;
			wType = WalletType.NPC_WALLET;
		}
		if ( type.equals(TraderType.SERVER_TRADER) ) {
			tType = type;
			wType = WalletType.INFINITE;
		}
		if ( type.equals(TraderType.MARKET_TRADER) ) {
			tType = type;
			wType = WalletType.OWNER_WALLET;
		}
		w.setWalletType(wType);
		return;
	}*/
	
	/*
	public TraderType getTraderType() {
		return tType;
	}
	public WalletType getWalletType() {
		return wType;
	}*/
	
	public Wallet getWallet() {
		return wallet;
	}
	
	public boolean buyTransaction(String player, double price) {
		boolean success = CitizensTrader.getEconomy().withdrawPlayer(player, price).transactionSuccess();
		if ( success )
			wallet.deposit(owner, price);
		return success;
	}
	
	public boolean sellTransaction(String player, double price) {
		boolean success = wallet.withdraw(owner, price);
		if ( success )
			CitizensTrader.getEconomy().depositPlayer(player, price);
		return success;
	}
	
	public Clan getClan(String tag)
	{
		return CitizensTrader.getSimpleClans().getClanManager().getClan(tag);
	}
	public Town getTown(String tag)
	{
		return CitizensTrader.getTowny().getTownyUniverse().getTownsMap().get(tag);
	}
	public Faction getFaction(String tag)
	{
		return Factions.i.getByTag(tag);
	}
	
	public void load(DataKey data) throws NPCLoadException {
		if ( !CitizensTrader.dtlWalletsEnabled() )
		{
			if ( data.keyExists("wallet") )
			{
				wallet = new Wallet( WalletType.getTypeByName( data.getString("wallet.type") ) );
				if ( data.keyExists("wallet.clan") )
					wallet.setClan( getClan( data.getString("wallet.clan") ) );
				if ( data.keyExists("wallet.town") )
					wallet.setTown( getTown( data.getString("wallet.town") ) );
				if ( data.keyExists("wallet.faction") )
					wallet.setFaction( getFaction( data.getString("wallet.faction") ) );
				if ( data.keyExists("wallet.bank") )
					wallet.setBank( data.getString("owner", ""), data.getString("wallet.bank") );
				
				wallet.setMoney( data.getDouble("wallet.money", 0.0) );
			}
			else
			//TODO this one is deprecated, remove with version 3.0!!
			{
				wallet = new Wallet( WalletType.getTypeByName(data.getString("wallet-type")) );
				
				if ( wallet.getType().equals(WalletType.SIMPLE_CLANS)
						&& CitizensTrader.getSimpleClans() != null )
				{
					wallet.setClan( getClan( data.getString("wallet-type").split(":")[1] ) );
				}
				else
				if ( wallet.getType().equals(WalletType.TOWNY) )
				{
					wallet.setTown( getTown( data.getString("wallet-type").split(":")[1] ) );
				}
				else
				if ( wallet.getType().equals(WalletType.FACTIONS) )
				{
					wallet.setFaction( getFaction( data.getString("wallet-type").split(":")[1] ) );
				}
				else
					wallet.setType(WalletType.NPC);
				
				wallet.setMoney( data.getDouble("money", 0.0) );
			}
		}
			
		owner = data.getString("owner", "no-owner");
		enabled = data.getBoolean("trading", true);
		
	}

	
	public void save(DataKey data) {
		if ( !CitizensTrader.dtlWalletsEnabled() )
		{
			data.setString("wallet.type", wallet.getType().toString());
			
			if ( !wallet.getTown().isEmpty() )
				data.setString("wallet.town", wallet.getTown());
			if ( !wallet.getClan().isEmpty() )
				data.setString("wallet.clan", wallet.getClan());
			if ( !wallet.getFaction().isEmpty() )
				data.setString("wallet.faction", wallet.getFaction());
			if ( !wallet.getBank().isEmpty() )
				data.setString("wallet.bank", wallet.getBank());
			
			if ( wallet.getMoney() != 0.0 )
				data.setDouble("money", wallet.getMoney());
		}
		
		data.setString("owner", owner);
		data.setBoolean("trading", enabled);
	}

}

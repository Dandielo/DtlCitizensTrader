package net.dtl.citizens.trader.parts;
/*
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.palmergames.bukkit.towny.object.Town;
*/
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.dandielo.citizens.wallets.AbstractWallet;
import net.dandielo.citizens.wallets.Wallets;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.objects.Wallet;
import net.dtl.citizens.trader.objects.Wallet.WalletType;


public class TraderConfigPart {
	
	private AbstractWallet dtlWallet;
	
	private Wallet wallet;
	private String owner;
	private boolean enabled;
	
	public TraderConfigPart() {
		wallet = new Wallet(WalletType.NPC);
		
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
	
	private void deposit(String player, double money)
	{
		if ( dtlWallet != null )
			dtlWallet.deposit(money);
		else
			wallet.deposit(player, money);
	}
	
	private boolean withdraw(String player, double money)
	{
		if ( dtlWallet != null )
			return dtlWallet.withdraw(money);
		else
			return wallet.withdraw(player, money);
	}
	
	public void loadDtlWallet(NPC npc)
	{
		dtlWallet = Wallets.getWallet(npc);
	}
	
	public Wallet getWallet() {
		return wallet;
	}
	
	public boolean buyTransaction(String player, double price) {
		boolean success = CitizensTrader.getEconomy().withdrawPlayer(player, price).transactionSuccess();
		if ( success )
			deposit(owner, price);
		return success;
	}
	
	public boolean sellTransaction(String player, double price) {
		boolean success = withdraw(owner, price);
		if ( success )
			CitizensTrader.getEconomy().depositPlayer(player, price);
		return success;
	}
	
	/*public static Clan getClan(String tag)
	{
		return CitizensTrader.getSimpleClans().getClanManager().getClan(tag);
	}
	public static Town getTown(String tag)
	{
		return CitizensTrader.getTowny().getTownyUniverse().getTownsMap().get(tag);
	}
	public static Faction getFaction(String tag)
	{
		return Factions.i.getByTag(tag);
	}*/
	
	public void load(DataKey data) throws NPCLoadException 
	{
		if ( data.keyExists("wallet") )
		{
			wallet = new Wallet( WalletType.getTypeByName( data.getString("wallet.type") ) );
		/*	if ( data.keyExists("wallet.clan") )
				wallet.setClan( getClan( data.getString("wallet.clan") ) );
			if ( data.keyExists("wallet.town") )
				wallet.setTown( getTown( data.getString("wallet.town") ) );
			if ( data.keyExists("wallet.faction") )
				wallet.setFaction( getFaction( data.getString("wallet.faction") ) );*/
			if ( data.keyExists("wallet.bank") )
				wallet.setBank( data.getString("owner", ""), data.getString("wallet.bank") );
			
			wallet.setMoney( data.getDouble("wallet.money", 0.0) );
		}
		else
		//TODO this one is deprecated, remove with version 3.0!!
		{
			wallet = new Wallet( WalletType.getTypeByName(data.getString("wallet-type")) );
			
	/*		if ( wallet.getType().equals(WalletType.SIMPLE_CLANS)
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
			else*/
			wallet.setType(WalletType.NPC);
			
			wallet.setMoney( data.getDouble("money", 0.0) );
		}
			
		owner = data.getString("owner", "no-owner");
		enabled = data.getBoolean("trading", true);
		
	}

	
	public void save(DataKey data)
	{
		data.setString("wallet.type", wallet.getType().toString());
		
		/*if ( !wallet.getTown().isEmpty() )
			data.setString("wallet.town", wallet.getTown());
		if ( !wallet.getClan().isEmpty() )
			data.setString("wallet.clan", wallet.getClan());
		if ( !wallet.getFaction().isEmpty() )
			data.setString("wallet.faction", wallet.getFaction());*/
		if ( !wallet.getBank().isEmpty() )
			data.setString("wallet.bank", wallet.getBank());
		
		if ( wallet.getMoney() != 0.0 )
			data.setDouble("money", wallet.getMoney());
		
		data.setString("owner", owner);
		data.setBoolean("trading", enabled);
	}

}

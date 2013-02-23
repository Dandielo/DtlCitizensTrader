package net.dandielo.citizens.trader.parts;
/*
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.palmergames.bukkit.towny.object.Town;
*/
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.objects.Wallet;
import net.dandielo.citizens.trader.objects.Wallet.WalletType;
import net.dandielo.citizens.wallets.AbstractWallet;
import net.dandielo.citizens.wallets.Wallets;


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
	public void load(DataKey data) throws NPCLoadException 
	{
		if ( data.keyExists("wallet") )
		{
			wallet = new Wallet( WalletType.getTypeByName( data.getString("wallet.type") ) );
			if ( data.keyExists("wallet.bank") )
				wallet.setBank( data.getString("owner", ""), data.getString("wallet.bank") );
			
			wallet.setMoney( data.getDouble("wallet.money", 0.0) );
		}
		else
		//TODO this one is deprecated, remove with version 3.0!!
		{
			wallet = new Wallet( WalletType.getTypeByName(data.getString("wallet-type")) );
			wallet.setType(WalletType.NPC);
			
			wallet.setMoney( data.getDouble("money", 0.0) );
		}
			
		owner = data.getString("owner", "no-owner");
		enabled = data.getBoolean("trading", true);
		
	}

	
	public void save(DataKey data)
	{
		data.setString("wallet.type", wallet.getType().toString());
		
		if ( !wallet.getBank().isEmpty() )
			data.setString("wallet.bank", wallet.getBank());
		
		if ( wallet.getMoney() != 0.0 )
			data.setDouble("money", wallet.getMoney());
		
		data.setString("owner", owner);
		data.setBoolean("trading", enabled);
	}

}

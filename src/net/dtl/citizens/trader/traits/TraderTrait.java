package net.dtl.citizens.trader.traits;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.TraderCharacterTrait.TraderType;
import net.dtl.citizens.trader.containers.Wallet;

public class TraderTrait {

	public enum WalletType {
		OWNER_WALLET, NPC_WALLET, BANK, INFINITE //Future CLAN_WALLET
, SIMPLE_CLANS, TOWNY
;

		public static WalletType getTypeByName(String n) {
			if ( n.equalsIgnoreCase("owner") ) 
				return WalletType.OWNER_WALLET;
			if ( n.equalsIgnoreCase("owner-wallet") ) 
				return WalletType.OWNER_WALLET;
			else if ( n.equalsIgnoreCase("npc") )
				return WalletType.NPC_WALLET;
			else if ( n.equalsIgnoreCase("npc-wallet") )
				return WalletType.NPC_WALLET;
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
			return null;
		}
		
		public String toString() {
			switch( this )
			{
			case OWNER_WALLET:
				return "owner";
			case NPC_WALLET:
				return "npc";
			case BANK:
				return "bank";
			case INFINITE:
				return "infinite";
			case SIMPLE_CLANS:
				return "simle-clans";
			case TOWNY:
				return "towny";
			default: 
				break;
			}
			return "";
		}
		
		public static String toString(WalletType w) {
			switch( w ) {
			case OWNER_WALLET:
				return "owner";
			case NPC_WALLET:
				return "npc";
			case BANK:
				return "bank";
			case INFINITE:
				return "infinite";
			case SIMPLE_CLANS:
				return "simple-clans";
			case TOWNY:
				return "towny";
			}
			return "";
		}
	}
	
	

	private WalletType wType = WalletType.INFINITE;
	private TraderType tType = TraderType.SERVER_TRADER;
	
	private Wallet w;
	private String owner;
	
	public TraderTrait() {
	//	super("type");
		w = new Wallet(wType);
		w.setMoney(0.0);
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getOwner() {
		return this.owner;
	}
	
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
	}
	
	public void setTraderType(TraderType type) {
		if ( type.equals(TraderType.PLAYER_TRADER) ) {
			tType = TraderType.PLAYER_TRADER;
			wType = WalletType.NPC_WALLET;
			return;
		}
		if ( type.equals(TraderType.SERVER_TRADER) ) {
			tType = TraderType.SERVER_TRADER;
			wType = WalletType.INFINITE;
			return;
		}
	}
	
	public TraderType getTraderType() {
		return tType;
	}
	public WalletType getWalletType() {
		return wType;
	}
	
	public Wallet getWallet() {
		return w;
	}
	
	public boolean buyTransaction(Player pBuying, final double price) {
		if ( w.withdraw(pBuying.getName(), price, false) ) {
			w.deposit(owner, price, true);
			return true;
		}
		return false;
	}
	public boolean sellTransaction(Player pSelling, double price) {
		if ( w.withdraw(owner, price, true) ) {
			w.deposit(pSelling.getName(), price, false);
			return true;
		}
		return false;
	}
	
	public void load(DataKey data) throws NPCLoadException {
		/*if ( data.keyExists("trader-type") ) {
			tType = TraderType.getTypeByName(data.getString("trader-type", "server"));
		}*/
		if ( data.keyExists("wallet-type") ) {
			String walletType = data.getString("wallet-type", "infinite");
			if ( walletType.startsWith("simple-clans") )
			{
				if ( CitizensTrader.getSimpleClans() == null )
				{
					wType = WalletType.NPC_WALLET;
				}
				else
				{
					wType = WalletType.SIMPLE_CLANS;
					w.setClan(walletType.substring(13));
				}
			}
			else
			if ( walletType.startsWith("towny") )
			{
				if ( CitizensTrader.getTowny() == null )
				{
					wType = WalletType.NPC_WALLET;
					
				}
				else
				{
					wType = WalletType.TOWNY;
					w.setTown(walletType.substring(6));
				}
			}
			else
				wType = WalletType.getTypeByName(walletType);
			
			w.setWalletType(wType);
		}
		if ( data.keyExists("owner") ) {
			owner = data.getString("owner","no-owner");
		}
		if ( data.keyExists("money") ) {
			w.setMoney(data.getDouble("money"));
		}
	}

	public void save(DataKey data) {
		String account = ""; 
		if ( getWalletType().equals(WalletType.TOWNY) )
			account = getWallet().getTown();
		if (  getWalletType().equals(WalletType.SIMPLE_CLANS) )
			account =  getWallet().getClan();
		if (  getWalletType().equals(WalletType.BANK) )
			account =  getWallet().getBank();
	//	data.setString("trader-type", TraderType.toString(tType))
		data.setString("wallet-type", WalletType.toString(wType) + ( account.isEmpty() ? "" : "." + account ) );
		data.setString("owner", owner);
		data.setDouble("money", w.getMoney());
	}

}

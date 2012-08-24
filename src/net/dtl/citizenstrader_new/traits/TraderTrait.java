package net.dtl.citizenstrader_new.traits;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizenstrader_new.containers.Wallet;

public class TraderTrait {

	public enum WalletType {
		OWNER_WALLET, NPC_WALLET, BANK, INFINITE //Future CLAN_WALLET
;

		public static WalletType getTypeByName(String n) {
			if ( n.equalsIgnoreCase("owner-wallet") ) 
				return WalletType.OWNER_WALLET;
			else if ( n.equalsIgnoreCase("npc-wallet") )
				return WalletType.NPC_WALLET;
			else if ( n.equalsIgnoreCase("bank") )
				return WalletType.BANK;
			else if ( n.equalsIgnoreCase("infinite") )
				return WalletType.INFINITE;
			else if ( n.equalsIgnoreCase("server-infinite") )
				return WalletType.INFINITE;
			return null;
		}
		
		public static String toString(WalletType w) {
			switch( w ) {
			case OWNER_WALLET:
				return "owner-wallet";
			case NPC_WALLET:
				return "npc-wallet";
			case BANK:
				return "bank";
			case INFINITE:
				return "infinite";
			}
			return "";
		}
	}
	
	public enum TraderType {
		PLAYER_TRADER, SERVER_TRADER, AUCTIONHOUSE, BANK, CUSTOM
;
		
		public static TraderType getTypeByName(String n) {
			if ( n.equalsIgnoreCase("server") ) 
				return TraderType.SERVER_TRADER;
			else if ( n.equalsIgnoreCase("player") )
				return TraderType.PLAYER_TRADER;
			else if ( n.equalsIgnoreCase("auctionhouse") )
				return TraderType.AUCTIONHOUSE;
			else if ( n.equalsIgnoreCase("bank") )
				return TraderType.BANK;
			return TraderType.CUSTOM;
		}
		
		public static String toString(TraderType w) {
			switch( w ) {
			case PLAYER_TRADER:
				return "player";
			case SERVER_TRADER:
				return "server";
			case AUCTIONHOUSE:
				return "auctionhouse";
			case BANK:
				return "bank";
			default:
				break;
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
		w.setMoney(000.0);
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
	
	public boolean buyTransaction(Player pBuying, double price) {
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
		if ( data.keyExists("trader-type") ) {
			tType = TraderType.getTypeByName(data.getString("trader-type", "server"));
		}
		if ( data.keyExists("wallet-type") ) {
			wType = WalletType.getTypeByName(data.getString("wallet-type", "infinite"));
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
		data.setString("trader-type", TraderType.toString(tType));
		data.setString("wallet-type", WalletType.toString(wType));
		data.setString("owner", owner);
		data.setDouble("money", w.getMoney());
	}

}

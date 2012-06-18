package net.dtl.citizenstrader_new.traits;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizenstrader_new.containers.Wallet;

public class TraderTrait extends Trait {

	public enum WalletType {
		PLAYER_WALLET, PLAYER_BANK, NPC_WALLET, SERVER_BANK, SERVER_INFINITE, GROUP_WALLET //Future FACTION_WALLET
;

		public static WalletType getTypeByName(String n) {
			if ( n.equalsIgnoreCase("player-wallet") ) 
				return WalletType.PLAYER_WALLET;
			else if ( n.equalsIgnoreCase("player-bank") )
				return WalletType.PLAYER_BANK;
			else if ( n.equalsIgnoreCase("group-wallet") )
				return WalletType.GROUP_WALLET;
			else if ( n.equalsIgnoreCase("npc-wallet") )
				return WalletType.NPC_WALLET;
			else if ( n.equalsIgnoreCase("server-bank") )
				return WalletType.SERVER_BANK;
			else if ( n.equalsIgnoreCase("server-infinite") )
				return WalletType.SERVER_INFINITE;
			return WalletType.SERVER_INFINITE;
		}
		
		public static String toString(WalletType w) {
			switch( w ) {
			case PLAYER_WALLET:
				return "player-wallet";
			case PLAYER_BANK:
				return "player-bank";
			case GROUP_WALLET:
				return "group-wallet";
			case NPC_WALLET:
				return "npc-wallet";
			case SERVER_BANK:
				return "server-bank";
			case SERVER_INFINITE:
				return "server-infinite";
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
			}
			return "";
		}
	}

	private WalletType wType = WalletType.SERVER_INFINITE;
	private TraderType tType = TraderType.SERVER_TRADER;
	
	private Wallet w;
	private String owner = "server";
	
	public TraderTrait() {
		w = new Wallet(wType);
		w.setMoney(0.0);
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
	
	@Override
	public void load(DataKey data) throws NPCLoadException {
		if ( data.keyExists("trader-type") ) {
			tType = TraderType.getTypeByName(data.getString("trader-type"));
		}
		if ( data.keyExists("wallet-type") ) {
			wType = WalletType.getTypeByName(data.getString("wallet-type"));
			w.setWalletType(wType);
		}
		if ( data.keyExists("owner") ) {
			owner = data.getString("owner");
		}
		if ( data.keyExists("money") ) {
			w.setMoney(data.getDouble("money"));
		}
	}

	@Override
	public void save(DataKey data) {
		data.setString("trader-type", TraderType.toString(tType));
		data.setString("wallet-type", WalletType.toString(wType));
		data.setString("owner", owner);
		data.setDouble("money", w.getMoney());
	}

}

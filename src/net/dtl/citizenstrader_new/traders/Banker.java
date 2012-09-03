package net.dtl.citizenstrader_new.traders;

import java.util.Map;

import org.bukkit.inventory.Inventory;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.CitizensTrader;
import net.dtl.citizenstrader_new.containers.BankAccount;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.BankTrait;

abstract public class Banker implements EconomyNpc {
	//BankTab System
	public enum BankTab {
		Tab1, Tab2, Tab3, Tab4, Tab5, Tab6, Tab7, Tab8, Tab9;
		
		@Override 
		public String toString()
		{
			switch( this )
			{
			case Tab1:
				return "tab1";
			case Tab2:
				return "tab2";
			case Tab3:
				return "tab3";
			case Tab4:
				return "tab4";
			case Tab5:
				return "tab5";
			case Tab6:
				return "tab6";
			case Tab7:
				return "tab7";
			case Tab8:
				return "tab8";
			case Tab9:
				return "tab9";
			} 
			return "";
		}
		
		public static BankTab getTabByName(String tabName) 
		{
			if ( tabName.equals("tab1") )
				return Tab1;
			if ( tabName.equals("tab2") )
				return Tab2;
			if ( tabName.equals("tab3") )
				return Tab3;
			if ( tabName.equals("tab4") )
				return Tab4;
			if ( tabName.equals("tab5") )
				return Tab5;
			if ( tabName.equals("tab6") )
				return Tab6;
			if ( tabName.equals("tab7") )
				return Tab7;
			if ( tabName.equals("tab8") )
				return Tab8;
			if ( tabName.equals("tab9") )
				return Tab9;
			return null;
		}
	}
	
	//players using the Banker atm
	private static Map<String, BankAccount> bankAccounts;
	
	//bank settings
	private BankTrait bank;
	private Inventory tabInventory;
	private TraderStatus traderStatus;
	private NPC npc;
	
	public Banker(NPC bankerNpc, BankTrait bankConfiguration) {
		
		//loading accoutns
		if ( bankAccounts == null )
			reloadAccounts();
		
		traderStatus = TraderStatus.BANK;
		
		tabInventory = bankConfiguration.getInventory();
		npc = bankerNpc;
		
		//loading trader bank config
		bank = bankConfiguration;

		bank.getInventory();
	}

	public void reloadAccounts()
	{
		System.out.print("a");
		//loading accounts
		bankAccounts = CitizensTrader.getBackendManager().getBankAccounts();
	}
	
	public void switchInventory(String player, TraderStatus status)
	{
		if ( status.equals(TraderStatus.BANK) )
			bankAccounts.get(player).inventoryView(tabInventory);
	}

	public Inventory getInventory() {
		return tabInventory;
	}
	
	
	@Override
	public TraderStatus getTraderStatus() {
		return traderStatus;
	}

	@Override
	public void setTraderStatus(TraderStatus status) {
		traderStatus = status;
	}

	@Override
	public int getNpcId() {
		return npc.getId();
	}
	
	
	
	
}

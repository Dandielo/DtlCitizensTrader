package net.dtl.citizenstrader_new.containers;

import net.dtl.citizenstrader_new.traders.Banker.BankTabType;

import org.bukkit.inventory.ItemStack;

public class PlayerBankAccount extends BankAccount {
	//super
	public PlayerBankAccount(String accountName) {
		//super
		super();
		
		
		owner = accountName;
		
		backend.newAccount(accountName);
		bankTabs.clear();
		
		bankTabs.put(BankTabType.Tab1, new BankTab(new ItemStack(35,1), "tab1", 1));
	}
}

package net.dtl.citizenstrader_new.backends.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.dtl.citizenstrader_new.containers.BankAccount;
import net.dtl.citizenstrader_new.containers.BankItem;
import net.dtl.citizenstrader_new.traders.Banker.BankTab;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import static net.dtl.citizenstrader_new.backends.file.FileBackend.*;

public class FileBankAccount extends BankAccount {
	
	public FileBankAccount(String accountName, FileConfiguration accounts) {
		//super
		super();
		
		owner = accountName;

		//geting the overall account info
		ConfigurationSection accountInfo = accounts.getConfigurationSection(buildPath("accounts",accountName));

		//System.out.print("ab");
		//loading tabs
		ItemStack tabItem = null;
		
		for ( String tab : accountInfo.getConfigurationSection("tabs").getKeys(false) )
		{
		//	System.out.print("a");
			//list to save the items
			List<BankItem> items = new ArrayList<BankItem>();
			tabItem = new BankItem( accountInfo.getString(buildPath("tabs",tab,"tab-item")) ).getItemStack();
			
			//fetching item list
			for ( String item : accountInfo.getStringList(buildPath("tabs",tab,"content")) )
			{
		//		System.out.print(BankTab.getTabByName(tab));
				items.add(new BankItem(item));
			}
			
			this.storedItems.put(BankTab.getTabByName(tab), items);
			this.tabItems.put(BankTab.getTabByName(tab), tabItem);
		}

		if ( storedItems.isEmpty() )
			storedItems.put(BankTab.Tab1, new ArrayList<BankItem>());
		
	}

}

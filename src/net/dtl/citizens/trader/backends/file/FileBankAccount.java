package net.dtl.citizens.trader.backends.file;

import java.util.ArrayList;
import java.util.List;
import net.dtl.citizens.trader.objects.BankAccount;
import net.dtl.citizens.trader.objects.BankItem;
import net.dtl.citizens.trader.objects.BankTab;
import net.dtl.citizens.trader.traders.Banker.BankTabType;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import static net.dtl.citizens.trader.backends.file.FileBackend.*;

public class FileBankAccount extends BankAccount {
	
	public FileBankAccount(String accountName, FileConfiguration accounts) {
		//super
		super();
		
		owner = accountName;
		
		//geting the overall account info
		ConfigurationSection accountInfo = accounts.getConfigurationSection(buildPath("accounts",accountName));

		//System.out.print("ab");
		//loading tabs
		ItemStack tabItem = new ItemStack(35,1);
		String tabName = "";
		int tabSize = 1;

		availableTabs = accountInfo.getInt("available-tabs");
				
		for ( String tab : accountInfo.getConfigurationSection("tabs").getKeys(false) )
		{
		//	System.out.print("a");
			//list to save the items
			tabItem = new BankItem( accountInfo.getString(buildPath("tabs",tab,"tab-item")) ).getItemStack();
			tabName = accountInfo.getString(buildPath("tabs",tab,"tab-name"));
			tabSize = accountInfo.getInt(buildPath("tabs",tab,"tab-size"));//accountInfo.getInt(buildPath("tabs",tab,"tab-size"));

			List<BankItem> items = new ArrayList<BankItem>();
			//fetching item list
			for ( String item : accountInfo.getStringList(buildPath("tabs",tab,"content")) )
			{
		//		System.out.print(BankTab.getTabByName(tab));
				items.add(new BankItem(item));
			}
			
			BankTab bankTab = new BankTab(tabItem, tabName, tabSize);
			bankTab.setTabItems(items);
			
			bankTabs.put(BankTabType.getTabByName(tab), bankTab);
			
			
		//	this.storedItems.put(BankTab.getTabByName(tab), items);
		//	this.tabItems.put(BankTab.getTabByName(tab), tabItem);
		}

		//if ( storedItems.isEmpty() )
		//	storedItems.put(BankTab.Tab1, new ArrayList<BankItem>());
		
	}

}

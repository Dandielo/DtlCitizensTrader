package net.dtl.citizens.trader.backends.file;

import java.util.ArrayList;
import java.util.List;
import net.dtl.citizens.trader.objects.BankAccount;
import net.dtl.citizens.trader.objects.BankItem;
import net.dtl.citizens.trader.objects.BankTab;
import net.dtl.citizens.trader.objects.PlayerBankAccount;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static net.dtl.citizens.trader.backends.file.FileBackend.*;

public class FileBankAccount extends BankAccount {
	
	public FileBankAccount(String owner, FileConfiguration accounts) {
		//super
		super(owner);
		
		//geting the overall account info
		ConfigurationSection accountInfo = accounts.getConfigurationSection(buildPath("accounts", owner));

		//loading tabs
		BankItem tabItem = new BankItem("35 a:1");//new ItemStack(35,1);
		String tabName = "";
		int tabSize = 1;

	//	availableTabs = accountInfo.getInt("available-tabs", );
				
		for ( String tab : accountInfo.getConfigurationSection("tabs").getKeys(false) )
		{
			//list to save the items
			tabItem = new BankItem( accountInfo.getString(buildPath("tabs",tab,"tab-item")) );
			tabName = accountInfo.getString(buildPath("tabs",tab,"tab-name"));
			//TODO tab size 
	//		tabSize = accountInfo.getInt(buildPath("tabs",tab,"tab-size"));//accountInfo.getInt(buildPath("tabs",tab,"tab-size"));

			List<BankItem> items = new ArrayList<BankItem>();
			for ( String item : accountInfo.getStringList(buildPath("tabs",tab,"content")) )
			{
				items.add(new BankItem(item));
			}
			
			BankTab bankTab = new BankTab(tabItem, tabName, tabSize);
			bankTab.setTabItems(items);
			
			bankTabs.put(tabName, bankTab);
			
			
		}

		
	}
	
	public PlayerBankAccount toPlayerAccount()
	{
		return new PlayerBankAccount(this);
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

	@Override
	public AccountType getType() {
		return AccountType.ABSTRACT;
	}

	@Override
	public BankTab getBankTab(String tab) {
		return null;
	}

	@Override
	public boolean maxed() {
		return false;
	}

	@Override
	public String nextTabName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addBankTab() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addItem(String tab, BankItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeItem(String tab, BankItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BankItem getItem(String tab, int slot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateItem(String tab, BankItem oldItem, BankItem newItem) {
		// TODO Auto-generated method stub
		
	}

}

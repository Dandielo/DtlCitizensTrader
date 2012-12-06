package net.dtl.citizens.trader.traders;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.ItemsConfig;
import net.dtl.citizens.trader.managers.LocaleManager;
import net.dtl.citizens.trader.managers.PermissionsManager;
import net.dtl.citizens.trader.objects.BankAccount;
import net.dtl.citizens.trader.objects.BankItem;
import net.dtl.citizens.trader.objects.BankTab;
import net.dtl.citizens.trader.objects.Wallet;
import net.dtl.citizens.trader.objects.Wallet.WalletType;
import net.dtl.citizens.trader.parts.BankerPart;
import net.dtl.citizens.trader.traders.Trader.TraderStatus;
import net.milkbowl.vault.economy.Economy;

abstract public class Banker implements EconomyNpc {
	
	public enum BankStatus {
		TAB_DISPLAY, SETTING_TAB_ITEM, SETTINGS;
	}
	
	//static global settigns
	protected static FileConfiguration config;
	protected static ItemsConfig itemConfig;
	protected static Map<BankTabType, Double> tabPrices;
	
	protected static Economy econ;
	
	//players using the Banker atm
	protected static Map<String, BankAccount> bankAccounts;
	
	protected static PermissionsManager permissions;
	protected static LocaleManager locale;
	
	//bank settings
	protected BankAccount account;
	protected BankerPart bank;
	protected BankTabType tab;
	
	protected BankItem selectedItem;
	
	protected Inventory tabInventory;
//	protected TraderStatus traderStatus;
	protected BankStatus bankStatus;
	protected NPC npc;
	
	public Banker(NPC bankerNpc, BankerPart bankConfiguration, String player) {

		permissions = CitizensTrader.getPermissionsManager();
		
		econ = CitizensTrader.getEconomy();
		config = CitizensTrader.getInstance().getConfig();
		itemConfig = CitizensTrader.getInstance().getItemConfig();
		
		locale = CitizensTrader.getLocaleManager();
		//loading accoutns
		
		bankStatus = BankStatus.TAB_DISPLAY;
		tab = BankTabType.Tab1;

		bank = bankConfiguration;
		npc = bankerNpc;
		
	}

	public static boolean hasAccount(Player player)
	{
		return bankAccounts.containsKey(player.getName());
	}
	
	protected static void initializeTabPrices()
	{
		tabPrices = new HashMap<BankTabType, Double>();
		for ( String key : config.getConfigurationSection("bank.tab-prices").getKeys(false) )
		{
			tabPrices.put(BankTabType.getTabByName(key), config.getDouble("bank.tab-prices."+key, 0.0));
		}
	}
	
	public static void reloadAccounts()
	{
		bankAccounts = CitizensTrader.getBackendManager().getBankAccounts();
	}
	
	public void switchInventory()
	{
		tabInventory.clear();
		if ( bankStatus.equals(BankStatus.TAB_DISPLAY) )
			account.inventoryView(tabInventory, tab);
	}
	
	public double getTabPrice(BankTabType type)
	{
		if ( type == null || !tabPrices.containsKey(type) )
			return 0.0;
		return tabPrices.get(type);
	}
	
	public double getWithdrawFee()
	{
		return this.getbankTrait().getWithdrawFee();
	}
	
	public double getDepositFee()
	{
		return this.getbankTrait().getDepositFee();
	}
	
	public boolean tabTransaction(BankTabType type, String player)
	{
		if ( type == null )
			return false;
		
		double price = tabPrices.get(type);
		if ( price == 0.0 )
			return true;
		
		if ( econ.getBalance(player) >= price )
		{
			econ.withdrawPlayer(player, price);
			return true;
		}
		
		return false;
	}
	
	public boolean depositFee(String player)
	{
		if ( econ.getBalance(player) >= this.getbankTrait().getDepositFee() )
		{
			econ.withdrawPlayer(player, this.getbankTrait().getDepositFee() );
			return true;
		}
		
		return false;
	}
	
	public boolean withdrawFee(String player)
	{
		if ( econ.getBalance(player) >= this.getbankTrait().getWithdrawFee() )
		{
			econ.withdrawPlayer(player, this.getbankTrait().getWithdrawFee() );
			return true;
		}
		
		return false;
	}
	
	public NPC getNpc()
	{
		return npc;
	}
	
	public BankerPart getbankTrait()
	{
		return this.bank;
	}
	
	public void useSettingsInv()
	{
		tabInventory = account.inventoryView(54, "Bank account settings");
	}
	
	public void settingsInventory()
	{
		tabInventory.clear();
		account.settingsView(tabInventory, tab);
	}
	
	public BankStatus getBankStatus()
	{
		return bankStatus;
	}
	
	public void setBankStatus(BankStatus status)
	{
		bankStatus = status;
	}

	public void setBankTabType(BankTabType tab)
	{
		this.tab = tab;
	}
	
	public BankTabType getBankTabType()
	{
		return tab;
	}
	
	public BankTab getBankTab()
	{
		return account.getBankTab(tab);
	}
	
	public boolean isExistingTab(int slot)
	{
		if ( account.getBankTab(BankTabType.getTabByName("tab"+slot)) != null )
			return true;
		return false;
	}
	
	public void setBankTabItem(ItemStack item)
	{
		account.setBankTabItem(tab, toBankItem(item));
	}
	//tab function
	
	public boolean hasAllTabs()
	{
		return account.hasAllTabs();
	}
	
	public BankTabType nextBankTab()
	{
		return account.nextTab();
	}
	
	public boolean addBankTab()
	{
		BankTabType newTab = account.addBankTab();
		if ( newTab != null )
		{
			tab = newTab;
			return true;
		}
		return false;
	}
	
	public boolean hasTabSize(int size)
	{
		if ( account.getBankTab(tab).getTabSize() < size )
			return false;
		return true;
					
	}
	
	public void increaseTabSize()
	{
		account.increaseTabSize(tab);
	}
	
	//selecting items
	public final Banker selectItem(BankItem i) {
		selectedItem = i;
		return this;
	}
	
	public final Banker selectItem(int slot) {
		selectedItem = account.getItem(slot, tab);

		return this;
	} 
	
	public final boolean hasSelectedItem() {
		return selectedItem != null;
	}
	
	public final BankItem getSelectedItem() {
		return selectedItem;
	}
	
	public void updateBankAccountItem(BankItem oldItem, BankItem newItem)
	{
		account.updateItem(tab, oldItem, newItem);
	}
	
	public void addItemToBankAccount(BankItem item)
	{
		account.addItem(tab, item);
	}
	
	public void removeItemFromBankAccount(BankItem item)
	{
		account.removeItem(tab, item);
	}
	
	//inventory events
	public final boolean playerInventoryHasPlace(Player player) {
		int amountToAdd = selectedItem.getItemStack().getAmount();
		return this.inventoryHasPlaceAmount(player.getInventory(), amountToAdd);
	}
	
	//inventory events
	public final boolean bankerInventoryHasPlace() {
		int amountToAdd = selectedItem.getItemStack().getAmount();
		return this.bankerInventoryHasPlaceAmount(tabInventory, amountToAdd);
	}

	public final boolean inventoryHasPlaceAmount(Inventory nInventory,int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;
		
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) {
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() )
					return true;
				
				
				if ( item.getAmount() < 64 ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % 64; 
				}
				
				
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			return true;
		}
		return false;
	}
	
	public final boolean bankerInventoryHasPlaceAmount(Inventory nInventory,int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;


		for ( Map.Entry<Integer, ? extends ItemStack> itemEntry : inventory.all(selectedItem.getItemStack().getType()).entrySet() ) {
			ItemStack item = itemEntry.getValue();
			
			if ( this.rowClicked(account.getBankTab(tab).getTabSize()+1, itemEntry.getKey()) )
				continue;
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() )
					return true;
				
				if ( item.getAmount() < 64 ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % 64; 
				}
				
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			return true;
		}
		return false;
	}
	
	public final boolean addSelectedToPlayerInventory(Player player) {

		int amountToAdd = selectedItem.getItemStack().getAmount();
		return addAmountToInventory(player.getInventory(), amountToAdd);
		
	}
	
	public final boolean addSelectedToBankerInventory() {

		int amountToAdd = selectedItem.getItemStack().getAmount();
		return addAmountToBankerInventory(tabInventory, amountToAdd);
		
	}
	
	/**
	 * SelfWritten Inventory.addItem() function for a work around with a bukkit inventory function bug
	 * 
	 */
	public final boolean addAmountToInventory(Inventory nInventory, int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;
		
		
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) {
			
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() ) {
					item.setAmount( item.getAmount() + amountToAdd );
					return true;
				} 
				
			 
				if ( item.getAmount() < selectedItem.getItemStack().getMaxStackSize() ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % selectedItem.getItemStack().getMaxStackSize(); 
					item.setAmount(selectedItem.getItemStack().getMaxStackSize());
				}
				
				
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			
			ItemStack is = selectedItem.getItemStack().clone();
			is.setAmount(amountToAdd);
			
			
			inventory.setItem(inventory.firstEmpty(), is);
			return true;
		}
		
		
		return false;
	}
	
	public boolean addAmountToBankerInventory(Inventory nInventory, int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;
		
		for ( Map.Entry<Integer, ? extends ItemStack> itemEntry : inventory.all(selectedItem.getItemStack().getType()).entrySet() ) {
			ItemStack item = itemEntry.getValue();
			
			if ( this.rowClicked(account.getBankTab(tab).getTabSize()+1, itemEntry.getKey()) )
				continue;
			
			selectItem(itemEntry.getKey());
			BankItem oldItem = null;
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				 
				
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() ) {
					oldItem = toBankItem(selectedItem.getItemStack());
					oldItem.setSlot(selectedItem.getSlot());
					selectedItem.getItemStack().setAmount(item.getAmount() + amountToAdd);
					updateBankAccountItem(oldItem, selectedItem);
					
					item.setAmount( item.getAmount() + amountToAdd );
					return true;
				} 
				
				
				if ( item.getAmount() < selectedItem.getItemStack().getMaxStackSize() ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % selectedItem.getItemStack().getMaxStackSize(); 
					item.setAmount(selectedItem.getItemStack().getMaxStackSize());
					
					oldItem = toBankItem(selectedItem.getItemStack());
					oldItem.setSlot(selectedItem.getSlot());
					
					selectedItem.getItemStack().setAmount(selectedItem.getItemStack().getMaxStackSize());
					updateBankAccountItem(oldItem, selectedItem);
				}
				
				
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			
			ItemStack is = selectedItem.getItemStack().clone();
			is.setAmount(amountToAdd);
			
			//create a new bank item
			selectedItem = toBankItem(is);
			selectedItem.setSlot(inventory.firstEmpty());
			addItemToBankAccount(selectedItem);
			
			
			inventory.setItem(inventory.firstEmpty(), is);
			return true;
		}
		
		return false;
	}
	
	
	public boolean rowClicked( int row, int slot )
	{
		//int rows = ( tabInventory.getSize() / 9 ) - 1;
		if ( ( ( row - 1 ) * 9 ) <= slot && slot < ( row * 9 ) )
			return true;
		return false;
	}
	
	public int getRowSlot( int slot )
	{
		return slot % 9;
	}
	
	public final boolean removeFromInventory(ItemStack item, InventoryClickEvent event) {
		if ( item.getAmount() != selectedItem.getItemStack().getAmount() ) {
			if ( item.getAmount() % selectedItem.getItemStack().getAmount() == 0 ) 
				event.setCurrentItem(new ItemStack(Material.AIR));
			else 
				item.setAmount( item.getAmount() % selectedItem.getItemStack().getAmount() );
		} else {
			event.setCurrentItem(new ItemStack(Material.AIR));
		}
		
		return false;
	}
	
	//Overridden
	@Override
	public Inventory getInventory() {
		return tabInventory;
	}
	
	@Override
	public final boolean locked()
	{
		return false;
	}

	@Override
	public int getNpcId() {
		return npc.getId();
	}
	
	@Override
	public Wallet getWallet()
	{
		return new Wallet(WalletType.NPC);
	}
	
	
	//utilities
	public static BankItem toBankItem(ItemStack is) {
		if ( is.getTypeId() == 0 )
			return null;
		
		String itemInfo = is.getTypeId()+":"+ is.getData().getData() +" a:"+is.getAmount() + " d:" + is.getDurability();
		if ( !is.getEnchantments().isEmpty() ) {
			itemInfo += " e:";
			for ( Enchantment ench : is.getEnchantments().keySet() ) 
				itemInfo += ench.getId() + "/" + is.getEnchantmentLevel(ench) + ",";
		}
		return new BankItem(itemInfo);
	}

	
	//BankTab System
		public enum BankTabType {
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
			
			public static BankTabType getTabByName(String tabName) 
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
}

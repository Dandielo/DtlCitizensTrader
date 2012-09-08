package net.dtl.citizenstrader_new.traders;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.CitizensTrader;
import net.dtl.citizenstrader_new.LocaleManager;
import net.dtl.citizenstrader_new.containers.BankAccount;
import net.dtl.citizenstrader_new.containers.BankItem;
import net.dtl.citizenstrader_new.containers.BankTab;
import net.dtl.citizenstrader_new.containers.PlayerBankAccount;
import net.dtl.citizenstrader_new.containers.StockItem;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.BankTrait;

abstract public class Banker implements EconomyNpc {
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
	
	
	public enum BankStatus {
		ITEM_MANAGING, TAB_DISPLAY, SETTING_TAB_ITEM, SETTINGS, INVENTORY_REOPEN;
	}
	
	//players using the Banker atm
	protected static Map<String, BankAccount> bankAccounts;
	
	
	protected static LocaleManager locale;
	
	//bank settings
	protected BankAccount account;
	protected BankTrait bank;
	protected BankTabType tab;
	
	protected BankItem selectedItem;
	
	protected Inventory tabInventory;
	protected TraderStatus traderStatus;
	protected BankStatus bankStatus;
	protected NPC npc;
	
	public Banker(NPC bankerNpc, BankTrait bankConfiguration, String player) {
		locale = CitizensTrader.getLocaleManager();
		//loading accoutns
		if ( bankAccounts == null )
			reloadAccounts();
		
		account = bankAccounts.get(player);
		if ( account == null )
		{
			//create new account
			account = new PlayerBankAccount(player);
			bankAccounts.put(player, account);
		}
		
		
		traderStatus = TraderStatus.BANK;
		bankStatus = BankStatus.ITEM_MANAGING;
		tab = BankTabType.Tab1;

		bank = bankConfiguration;
		npc = bankerNpc;

		tabInventory = account.inventoryTabView(tab);
		//loading trader bank config
		this.switchInventory();
		
	}

	public void reloadAccounts()
	{
	//	System.out.print("a");
		//loading accounts
		bankAccounts = CitizensTrader.getBackendManager().getBankAccounts();
	}
	
	public void switchInventory()
	{
		tabInventory.clear();
		if ( bankStatus.equals(BankStatus.ITEM_MANAGING) )
			account.inventoryView(tabInventory, tab);
		else
		if ( bankStatus.equals(BankStatus.TAB_DISPLAY) )
			account.tabSelectionView(tabInventory);
	}
	
	public void reopenInventory(Player player)
	{
	}
	
	public NPC getNpc()
	{
		return npc;
	}
	
	public BankTrait getbankTrait()
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
		return this.inventoryHasPlaceAmount(tabInventory, amountToAdd);
	}

	public final boolean inventoryHasPlaceAmount(Inventory nInventory,int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;
		/* *
		 * get all stacks with the same type (hmm... does it compares the data values?)
		 * 
		 */
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) {
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				/* *
				 * if the added amount isn't over the limit
				 *
				 */
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() )
					return true;
				
				/* *
				 * if the added amount is less than 64 (so we are not adding a whole stack)
				 * 
				 * lowering the amount to add
				 *
				 */ 
				if ( item.getAmount() < 64 ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % 64; 
				}
				
				/* *
				 * if there is nothing left just return
				 * 
				 */
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		/* *
		 * if any amount left to add check if there is place in the inventory
		 */
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
		
		/* *
		 * get all stacks with the same type (hmm... does it compares the data values?)
		 * 
		 */
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) {
			
			/* *
			 * Checking items by durability, so if you buy a diax sword it wont buy like it would be broken :P
			 * 
			 */
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				/* *
				 * if the added amount isn't over the limit
				 * 
				 * setting the new amount in the player's inventory 
				 *
				 */
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() ) {
					item.setAmount( item.getAmount() + amountToAdd );
					return true;
				} 
				
				/* *
				 * if the added amount is less than 64 (so we are not adding a whole stack)
				 * 
				 * maximizing the first item stack amount, and lowering the amount to add
				 *
				 */ 
				if ( item.getAmount() < selectedItem.getItemStack().getMaxStackSize() ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % selectedItem.getItemStack().getMaxStackSize(); 
					item.setAmount(selectedItem.getItemStack().getMaxStackSize());
				}
				
				/* *
				 * if there is nothing left just return
				 * 
				 */
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		/* *
		 * Stack's are maximized and there is some amount left
		 *  
		 *  Checking if there is any free space in the inventory (just for care)
		 *  
		 */
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			/* *
			 * creating a ItemStack clone from the existing saving
			 * and changing amount's
			 * 
			 */
			ItemStack is = selectedItem.getItemStack().clone();
			is.setAmount(amountToAdd);
			
			/* *
			 * setting the item into a free slot
			 * don't using the addItem() bacause it's a workaround for this function
			 * 
			 */
			inventory.setItem(inventory.firstEmpty(), is);
			return true;
		}
		
		/* *
		 * Item couldn't be added to the inventory
		 * 
		 */
		return false;
	}
	
	public boolean addAmountToBankerInventory(Inventory nInventory, int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;
		
		/* *
		 * get all stacks with the same type (hmm... does it compares the data values?)
		 * 
		 */
		for ( Map.Entry<Integer, ? extends ItemStack> itemEntry : inventory.all(selectedItem.getItemStack().getType()).entrySet() ) {
			ItemStack item = itemEntry.getValue();
			selectItem(itemEntry.getKey());
			BankItem oldItem = null;
			/* *
			 * Checking items by durability, so if you buy a diax sword it wont buy like it would be broken :P
			 * 
			 */
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				 
				/* *
				 * if the added amount isn't over the limit
				 * 
				 * setting the new amount in the player's inventory 
				 *
				 */
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() ) {
					oldItem = toBankItem(selectedItem.getItemStack());
					oldItem.setSlot(selectedItem.getSlot());
					selectedItem.getItemStack().setAmount(item.getAmount() + amountToAdd);
					updateBankAccountItem(oldItem, selectedItem);
					
					item.setAmount( item.getAmount() + amountToAdd );
					return true;
				} 
				
				/* *
				 * if the added amount is less than 64 (so we are not adding a whole stack)
				 * 
				 * maximizing the first item stack amount, and lowering the amount to add
				 *
				 */ 
				if ( item.getAmount() < selectedItem.getItemStack().getMaxStackSize() ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % selectedItem.getItemStack().getMaxStackSize(); 
					item.setAmount(selectedItem.getItemStack().getMaxStackSize());
					
					oldItem = toBankItem(selectedItem.getItemStack());
					oldItem.setSlot(selectedItem.getSlot());
					
					selectedItem.getItemStack().setAmount(selectedItem.getItemStack().getMaxStackSize());
					updateBankAccountItem(oldItem, selectedItem);
				}
				
				/* *
				 * if there is nothing left just return
				 * 
				 */
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		/* *
		 * Stack's are maximized and there is some amount left
		 *  
		 *  Checking if there is any free space in the inventory (just for care)
		 *  
		 */
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			/* *
			 * creating a ItemStack clone from the existing saving
			 * and changing amount's
			 * 
			 */
			ItemStack is = selectedItem.getItemStack().clone();
			is.setAmount(amountToAdd);
			
			//create a new bank item
			selectedItem = toBankItem(is);
			selectedItem.setSlot(inventory.firstEmpty());
			addItemToBankAccount(selectedItem);
			
			/* *
			 * setting the item into a free slot
			 * don't using the addItem() bacause it's a workaround for this function
			 * 
			 */
			inventory.setItem(inventory.firstEmpty(), is);
			return true;
		}
		
		/* *
		 * Item couldn't be added to the inventory
		 * 
		 */
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
	
}

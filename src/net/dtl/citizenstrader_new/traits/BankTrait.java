package net.dtl.citizenstrader_new.traits;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;

public class BankTrait implements InventoryHolder {
	//deposit fee
	private double depositFee;
	private double withdrawFee;
	
	//max tabs to show
	private int maxTabs;
	private int tabSize;
	
	//money to "item" converter
//	private boolean moneyConverter;
	
	public BankTrait()
	{
		this(54);
	}
	
	public BankTrait(int size)
	{
        if( size <= 0 || size > 54 ){
        	throw new IllegalArgumentException("Size must be between 1 and 54");}
		tabSize = size;
	}
	
	public void load(DataKey data) throws NPCLoadException {
		depositFee = data.getDouble("deposit-fee", 0.0);
		depositFee = data.getDouble("withdraw-fee", 0.0);
		maxTabs = data.getInt("max-tabs", 9);
	//	tabSize = data.getInt("tab-size", 54);
		
	}

	public void save(DataKey data) {
		data.setDouble("deposit-fee", depositFee);
		data.setDouble("withdraw-fee", withdrawFee);
		data.setInt("max-tabs", maxTabs);
	//	data.setInt("tab-size", tabSize);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, tabSize, "Banker");
		
		return inv;
	}
	
	
	
}
;
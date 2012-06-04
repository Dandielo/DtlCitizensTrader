package net.dtl.citizenstrader.traits;

import java.util.Map;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class StockRoomTrait extends Trait implements InventoryHolder {
	
	private Inventory sellStock;
	private Inventory buyStock;
	Map<ItemStack,Integer> prices;
	
	public StockRoomTrait() {
		this(54); 
	}
	
	private StockRoomTrait(int size){
        if( size <= 0 || size > 54 ){
        	throw new IllegalArgumentException("Size must be between 1 and 54");}

        sellStock = Bukkit.createInventory(this,size,"sellstockroom");
        buyStock = Bukkit.createInventory(this,size,"buystockroom");
    }
	
	@Override
	public void load(DataKey data) throws NPCLoadException {

		//load the inventory
		for ( DataKey slotKey : data.getRelative("inv").getRelative("sell").getIntegerSubKeys() ) {
			sellStock.setItem(Integer.parseInt(slotKey.name()), ItemStorage.loadItemStack(slotKey));
		}
		for ( DataKey slotKey : data.getRelative("inv").getRelative("buy").getIntegerSubKeys() ) {
			buyStock.setItem(Integer.parseInt(slotKey.name()), ItemStorage.loadItemStack(slotKey));
		}
	}
	@Override
	public void save(DataKey data) {
		
		//save the inventory
		int i = 0;
        for(ItemStack is : sellStock.getContents()){
            if ( is != null ) {
            	ItemStorage.saveItem(data.getRelative("inv").getRelative("sell." + i),is);
            }
            ++i;
        }
             
        i = 0;
        for(ItemStack is : buyStock.getContents()){
            if ( is != null ) {
            	ItemStorage.saveItem(data.getRelative("inv").getRelative("buy." + i),is);
            }
            ++i;
        }
		
	}
	
	//Returning the displayInventory
	@Override
	public Inventory getInventory() {
		return sellStock;
	}
	
	
	public Inventory inventoryView(int size) {
		Inventory view = Bukkit.createInventory(this, size, "Store");
		
		int i = 0;
		for(ItemStack is : sellStock){
            if( is != null ) {
	            ItemStack chk = new ItemStack(is.getType(),1,is.getDurability());
	            chk.addEnchantments(is.getEnchantments());
	            if ( view.contains(chk) == false ) {
	                view.setItem(i,chk);
	            }
            }
            i++;
        }
		
		return view;
	}
	
	
	
	
	
}

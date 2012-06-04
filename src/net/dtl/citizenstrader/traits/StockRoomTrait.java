package net.dtl.citizenstrader.traits;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class StockRoomTrait extends Trait implements InventoryHolder {
	
	//private Inventory sellStock;
	private List<StockItem> sellStock = new ArrayList<StockItem>();					//What the trader sells the player
	private List<StockItem> buyStock = new ArrayList<StockItem>();					//What the trader buys from the player 
	private int size;
	//private Inventory buyStock;
	//Map<ItemStack,Integer> prices;
	
	public StockRoomTrait() {
		this(54); 
	}
	
	private StockRoomTrait(int stockSize){
		size = stockSize;
        if( size <= 0 || size > 54 ){
        	throw new IllegalArgumentException("Size must be between 1 and 54");}

    //    sellStock = Bukkit.createInventory(this,size,"sellstockroom");
    //    buyStock = Bukkit.createInventory(this,size,"buystockroom");
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public void load(DataKey data) throws NPCLoadException {
		if ( !data.keyExists("stock") || !data.getRelative("").keyExists("sell") )
			return;
		//load the inventoryd
		for ( String slotKey : (List<String>) data.getRelative("stock").getRaw("sell") ) {
			sellStock.add(new StockItem(slotKey));
		}
		for ( String slotKey : (List<String>) data.getRelative("stock").getRaw("buy") ) {
			buyStock.add(new StockItem(slotKey));
		}
	}
	
	@Override
	public void save(DataKey data) {
		/*	
		//save the inventory
		int i = 0;
        for(StockItem item : sellStock ){
        //    if ( is != null ) {
            	ItemStorage.saveItem(data.getRelative("inv").getRelative("sell." + i),is);
        //    }
        //    ++i;
        }
             
        i = 0;
        for(ItemStack is : buyStock.getContents()){
            if ( is != null ) {
            	ItemStorage.saveItem(data.getRelative("inv").getRelative("buy." + i),is);
            }
            ++i;
        }*/
        
        List<String> sellList = new ArrayList<String>();
        for ( StockItem item : sellStock )
            	sellList.add(item.toString());
        
		List<String> buyList = new ArrayList<String>();
        for ( StockItem item : buyStock )
        	sellList.add(item.toString());
        
        
		data.setString("inv", "");
		data.getRelative("inv").setRaw("sell", sellList);
		data.getRelative("inv").setRaw("buy", buyList);
		
	}
	
	//Returning the displayInventory
	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this,size,"sellstockroom");

        for ( StockItem item : sellStock )
            inv.addItem(item.getItemStack());
        
		return inv;
	}
	
	
	public Inventory inventoryView(int size, String name) {
		Inventory view = Bukkit.createInventory(this, size, name);
		
		int i = 0;
		for( StockItem item : sellStock ){
         //   if( item != null ) {
            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
            chk.addEnchantments(item.getItemStack().getEnchantments());
            if ( view.contains(chk) == false ) {
                view.setItem(i,chk);
            }
        //    }
            i++;
        }
		
		return view;
	}
	
	
	
	
}

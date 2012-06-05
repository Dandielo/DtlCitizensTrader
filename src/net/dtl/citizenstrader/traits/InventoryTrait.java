package net.dtl.citizenstrader.traits;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizenstrader.TraderStatus.Status;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InventoryTrait extends Trait implements InventoryHolder {
	
	//private Inventory sellStock;
	private List<StockItem> sellStock = new ArrayList<StockItem>();					//What the trader sells the player
	private List<StockItem> buyStock = new ArrayList<StockItem>();					//What the trader buys from the player 
	private int size;
	//private Inventory buyStock;
	//Map<ItemStack,Integer> prices;
	
	public InventoryTrait() {
		this(54); 
	}
	
	private InventoryTrait(int stockSize){
		size = stockSize;
        if( size <= 0 || size > 54 ){
        	throw new IllegalArgumentException("Size must be between 1 and 54");}
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public void load(DataKey data) throws NPCLoadException {
		if ( data.keyExists("sell") ) {
			for ( String item :  (List<String>) data.getRaw("sell") ) {
				sellStock.add(new StockItem(item));
			}
		}

		if ( data.keyExists("buy") ) {
			for ( String item :  (List<String>) data.getRaw("buy") ) 
				sellStock.add(new StockItem(item));
		}
	}
	
	@Override
	public void save(DataKey data) {
		
        List<String> sellList = new ArrayList<String>();
		if ( !sellStock.isEmpty() )
	        for ( int i = 0 ; i < sellStock.size() ; ++i )
	            	sellList.add(sellStock.get(i).toString());
        
		List<String> buyList = new ArrayList<String>();
		if ( !buyStock.isEmpty() )
			for ( StockItem item : buyStock )
				sellList.add(item.toString());

		data.setRaw("sell", sellList);
		data.setRaw("buy", buyList);
		
	}
	
	//Returning the displayInventory
	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this,size,"sellstockroom");

        for ( StockItem item : sellStock )
            inv.addItem(item.getItemStack());
        
		return inv;
	}
	
	public Inventory inventoryView(Inventory view,Status s) {
		//	Inventory view = Bukkit.createInventory(this, size, name);
			
		int i = 0;
		if ( s.equals(Status.PLAYER_SELL) ) {
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
            if ( !buyStock.isEmpty() )
            	view.setItem(view.getSize()-1, new ItemStack(Material.WOOL,1,(short)0,(byte)5));//3
		} else if ( s.equals(Status.PLAYER_BUY ) ) {
			for( StockItem item : buyStock ) {
		         //   if( item != null ) {
	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());
	            if ( view.contains(chk) == false ) {
	                view.setItem(i,chk);
	            }
	        //    }
	            i++;
	        }
            view.setItem(view.getSize()-1, new ItemStack(Material.WOOL,1,(short)0,(byte)3));//3
		}
		
		return view;
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

        if ( !buyStock.isEmpty() )
        	view.setItem(view.getSize()-1, new ItemStack(Material.WOOL,1,(short)0,(byte)5));//3
        
		return view;
	}
	
	public StockItem itemForSell(int slot) {
		if ( slot < sellStock.size() && slot >= 0 )
			return sellStock.get(slot);
		return null;
	}

	public StockItem wantItemBuy(int slot) {
		if ( slot < buyStock.size() && slot >= 0 )
			return buyStock.get(slot);
		return null;
	}
	
	public static void setInventoryWith(Inventory inv,StockItem si) {
		int i = 0;
		for ( Integer amount : si.getAmouts() ) {
			ItemStack is = si.getItemStack().clone();
			is.setAmount(amount);
			inv.setItem(i++,is);
		}
		inv.setItem(inv.getSize()-1,new ItemStack(Material.WOOL,1,(short)0,(byte)14));
	}
	
	public void addItem(boolean sell,String data) {
		if ( sell )
			sellStock.add(new StockItem(data));
		else
			buyStock.add(new StockItem(data));
	}
	public void removeItem(boolean sell,int i) {
		if ( sell && sellStock.size() > i )
			sellStock.remove(i);
		else if ( buyStock.size() > i )
			buyStock.remove(i);
	}

	
}

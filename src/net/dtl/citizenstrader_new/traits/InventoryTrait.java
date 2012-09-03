package net.dtl.citizenstrader_new.traits;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizenstrader_new.CitizensTrader;
import net.dtl.citizenstrader_new.ItemsConfig;
import net.dtl.citizenstrader_new.containers.StockItem;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InventoryTrait implements InventoryHolder {
	//trader config
	protected static ItemsConfig config = CitizensTrader.getInstance().getItemConfig();
	
	private List<StockItem> sellStock = new ArrayList<StockItem>();					//What the trader sells the player
	private List<StockItem> buyStock = new ArrayList<StockItem>();					//What the trader buys from the player 
	private int size;
	
	public InventoryTrait() {
		this(54); 
	}
	
	private InventoryTrait(int stockSize){

		size = stockSize;
        if( size <= 0 || size > 54 ){
        	throw new IllegalArgumentException("Size must be between 1 and 54");}
    }
	
	public void load(DataKey data) throws NPCLoadException {
		if ( data.keyExists("sell") ) {
			for ( String item :  (List<String>) data.getRaw("sell") ) {
				sellStock.add(new StockItem(item));
			}
		}

		if ( data.keyExists("buy") ) {
			for ( String item :  (List<String>) data.getRaw("buy") ) 
				buyStock.add(new StockItem(item));
		}
	}
	
	
	public void save(DataKey data) {
	//	System.out.print(data);
		
        List<String> sellList = new ArrayList<String>();
		if ( !sellStock.isEmpty() )
	        for ( StockItem item : sellStock )
	            	sellList.add(item.toString());
        
		List<String> buyList = new ArrayList<String>();
		if ( !buyStock.isEmpty() )
			for ( StockItem item : buyStock )
				buyList.add(item.toString());

		data.setRaw("sell", sellList);
		data.setRaw("buy", buyList);
		
	}
	
	//Returning the displayInventory
	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this,size,"sellstockroom");

        for ( StockItem item : sellStock )
        {
        	if ( item.getSlot() < 0 
        			|| getItem(item.getSlot(), TraderStatus.SELL) != null )
        		item.setSlot(inv.firstEmpty());
            inv.setItem( item.getSlot() ,item.getItemStack());
        }
		return inv;
	}
	
	public Inventory inventoryView(Inventory view,TraderStatus s) {

		if ( s.equals(TraderStatus.SELL) ) {
			for( StockItem item : sellStock ) {
				
	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());

            	if ( item.getSlot() < 0 
            			|| getItem(item.getSlot(), TraderStatus.SELL) != null )
            		item.setSlot(view.firstEmpty());
	            view.setItem( item.getSlot() ,chk);
	            
	            

	        }
            if ( !buyStock.isEmpty() )
            	view.setItem(view.getSize()-1, config.getItemManagement(1));
            
		} else if ( s.equals(TraderStatus.BUY ) ) {
			for( StockItem item : buyStock ) {

	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());

	            if ( item.getSlot() < 0 
            			|| getItem(item.getSlot(), TraderStatus.BUY) != null )
            		item.setSlot(view.firstEmpty());
                view.setItem( item.getSlot() ,chk);

	        }
            view.setItem(view.getSize()-1, config.getItemManagement(0));//3
		} else if ( s.equals(TraderStatus.MANAGE_SELL ) ) {
			for( StockItem item : sellStock ) {

	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());

	            if ( item.getSlot() < 0 
            			|| getItem(item.getSlot(), TraderStatus.MANAGE_SELL) != null )
            		item.setSlot(view.firstEmpty());
                view.setItem( item.getSlot() ,chk);

	        }
            view.setItem(view.getSize()-3, config.getItemManagement(4));//3
            view.setItem(view.getSize()-2, config.getItemManagement(2));//3
            view.setItem(view.getSize()-1, config.getItemManagement(1));//3
		} else if ( s.equals(TraderStatus.MANAGE_BUY ) ) {
			for( StockItem item : buyStock ) {

	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());

	            if ( item.getSlot() < 0 
            			|| getItem(item.getSlot(), TraderStatus.MANAGE_BUY) != null )
            		item.setSlot(view.firstEmpty());
	            view.setItem( item.getSlot() ,chk);

	        }
            view.setItem(view.getSize()-3, config.getItemManagement(4));//3
            view.setItem(view.getSize()-2, config.getItemManagement(2));//3
            view.setItem(view.getSize()-1, config.getItemManagement(0));//3
		} 
		
		return view;
	}
	
	public Inventory inventoryView(int size, String name) {
		Inventory view = Bukkit.createInventory(this, size, name);
		
		for( StockItem item : sellStock ) {

	        ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	        chk.addEnchantments(item.getItemStack().getEnchantments());
	
	        if ( item.getSlot() < 0 
        			|| getItem(item.getSlot(), TraderStatus.SELL) != null )
        		item.setSlot(view.firstEmpty());
	        view.setItem(item.getSlot(),chk);
        }

        if ( !buyStock.isEmpty() )
        	view.setItem(view.getSize()-1, config.getItemManagement(1));//3
        
		return view;
	}
	
	public StockItem getItem(int slot,TraderStatus status) {
		if ( status.equals(TraderStatus.MANAGE_BUY) ||
			 status.equals(TraderStatus.BUY) ) {
			for ( StockItem item : buyStock )
				if ( item.getSlot() == slot )
					return item;
		} if ( status.equals(TraderStatus.MANAGE_SELL) ||
			   status.equals(TraderStatus.SELL ) ) {
			for ( StockItem item : sellStock  )
				if ( item.getSlot() == slot )
					return item;
		}
		return null;
	}
	
	public List<String> getItemList(TraderStatus status, String format, int page) 
	{
		//the list we will privide the player 
		List<String> items = new ArrayList<String>();
		
		//the fariable to be fetched through
		List<StockItem> stockItems = null;
		
		
		//set the stockItems variable
		if ( TraderStatus.SELL.equals(status) )
			stockItems = sellStock;
		if ( TraderStatus.BUY.equals(status) )
			stockItems = buyStock;	

		
		//index variable
		int i = 0;
		
		//fetch the list we will display
		for ( StockItem item : stockItems )
		{
			String itemDisplay = format;
			
			//item id display
			itemDisplay = itemDisplay.replace("{id}", String.valueOf(item.getItemStack().getTypeId()) );

			//item id display
			itemDisplay = itemDisplay.replace("{data}", String.valueOf(item.getItemStack().getData().getData()) );
			
			//amount display
			itemDisplay = itemDisplay.replace("{amount}", String.valueOf(item.getAmount()) );
			
			//price display
			itemDisplay = itemDisplay.replace("{price}", String.valueOf(item.getPrice()) );
			
			//slot display
			itemDisplay = itemDisplay.replace("{slot}", String.valueOf(item.getSlot()) );
			
			//global limit display
			itemDisplay = itemDisplay.replace("{gl}", String.valueOf(item.getLimitSystem().getGlobalLimit()) );

			//item name display
			itemDisplay = itemDisplay.replace("{name}", String.valueOf(item.getItemStack().getType().name().toLowerCase()) );
			
			if ( i >= page * 10 && i < ( ( 1 + page ) * 10 ) + 1 )
				items.add(itemDisplay);
			
			++i;
		}
		
		
		
		return items;
	}
	
	public StockItem getItem(ItemStack itemStack, TraderStatus status, boolean dura,
			boolean amount) {

		boolean equal = false;
		if ( status.equals(TraderStatus.MANAGE_BUY) ||
			 status.equals(TraderStatus.BUY) ) {
			for ( StockItem item : buyStock ) {
				equal = false;
				if ( itemStack.getType().equals(item.getItemStack().getType()) &&
					 itemStack.getData().equals(item.getItemStack().getData()) ) {
						equal = true;
					if ( dura ) 
						equal = itemStack.getDurability() >= item.getItemStack().getDurability();
					if ( amount && equal )
						equal =  itemStack.getAmount() >= item.getItemStack().getAmount();
					if ( equal )
						return item;
				}
			}
		} 
		if ( status.equals(TraderStatus.MANAGE_SELL) ||
		     status.equals(TraderStatus.SELL ) ) {
			for ( StockItem item : sellStock ) {
				equal = false;
				if ( itemStack.getType().equals(item.getItemStack().getType()) &&
					 itemStack.getData().equals(item.getItemStack().getData()) ) {
						equal = true;
					if ( dura ) 
						equal = itemStack.getDurability() >= item.getItemStack().getDurability();
					if ( amount && equal )
						equal =  itemStack.getAmount() >= item.getItemStack().getAmount();
					if ( equal )
						return item;
				}
			}
		}
		return null;
	}
	
	public static void setManagerInventoryWith(Inventory inv,StockItem si) {
		int i = 0;
		for ( Integer amount : si.getAmounts() ) {
			ItemStack is = si.getItemStack().clone();
			is.setAmount(amount);
		//	if ( si.getLimitSystem().checkLimit("", i) )
				inv.setItem(i++,is);
		}
		inv.setItem(inv.getSize()-1, config.getItemManagement(7));
	}
	
	public static void setInventoryWith(Inventory inv,StockItem si) {
		int i = 0;
		for ( Integer amount : si.getAmounts() ) {
			ItemStack is = si.getItemStack().clone();
			is.setAmount(amount);
			if ( si.getLimitSystem().checkLimit("", i) )
				inv.setItem(i++,is);
		}
		inv.setItem(inv.getSize()-1, config.getItemManagement(7));
	}
	
	public void addItem(boolean sell,String data) {
		if ( sell )
			sellStock.add(new StockItem(data));
		else
			buyStock.add(new StockItem(data));
	}
	public void addItem(boolean sell,StockItem si) {
		if ( sell )
			sellStock.add(si);
		else
			buyStock.add(si);
	}
	public void removeItem(boolean sell,int slot) {
		if ( sell ) {
			for ( StockItem item : sellStock )
				if ( item.getSlot() == slot ) {
					sellStock.remove(item);
					return;
				}
		} else 
			for ( StockItem item : buyStock )
				if ( item.getSlot() == slot ) {
					buyStock.remove(item);
					return;
				}
	}
	public void saveNewAmouts(Inventory inv,StockItem si) {
		si.getAmounts().clear();
		for ( ItemStack is : inv.getContents() ) {
			if ( is != null ) {
				si.addAmount(is.getAmount());
			}
		}
		if ( si.getAmounts().size() > 1 )
			si.getAmounts().remove(si.getAmounts().size()-1);
	}

	
	
}

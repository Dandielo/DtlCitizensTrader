package net.dtl.citizens.trader.traits;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.ItemsConfig;
import net.dtl.citizens.trader.PatternsManager;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.traders.Trader.TraderStatus;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InventoryTrait implements InventoryHolder {
	//patterns manager
	private PatternsManager patterns;
	
	//trader config
	protected static ItemsConfig config = CitizensTrader.getInstance().getItemConfig();
	
	private List<StockItem> sellStock = new ArrayList<StockItem>();					//What the trader sells the player
	private List<StockItem> buyStock = new ArrayList<StockItem>();					//What the trader buys from the player 
	private int size;
	private String pattern;
	private Player tempPlayer;
	
	public InventoryTrait(String pattern)
	{
		this(54); 
		this.pattern = pattern;
	}
	
	public void setPlayer(Player player)
	{
		tempPlayer = player;
	}
	
	public InventoryTrait() {
		this(""); 
		
		patterns = CitizensTrader.getPatternsManager();
	}
	
	private InventoryTrait(int stockSize){

		size = stockSize;
        if( size <= 0 || size > 54 ){
        	throw new IllegalArgumentException("Size must be between 1 and 54");}
    }
	
	public String getPattern()
	{
		return pattern;
	}
	
	public void removePattern()
	{
		pattern = "";
		this.reloadStock();
	}
	
	public boolean setPattern(String newPattern)
	{
		if ( patterns.getPattern(newPattern) == null )
			return false;
		
		pattern = newPattern;
		this.reloadStock();
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public void load(DataKey data, Class<? extends StockItem> itemClass) throws NPCLoadException {
		if ( data.keyExists("sell") ) {
			for ( String item :  (List<String>) data.getRaw("sell") ) {
				StockItem stockItem = StockItem.createItem(itemClass, item);//new StockItem(item);
				if ( stockItem.getSlot() < 0 )
					sellStock.add(stockItem);
				else
					sellStock.add(0, stockItem);
			}
		}

		if ( data.keyExists("buy") ) {
			for ( String item :  (List<String>) data.getRaw("buy") ) {
				StockItem stockItem = StockItem.createItem(itemClass, item);//new StockItem(item);
				if ( stockItem.getSlot() < 0 )
					buyStock.add(stockItem);
				else
					buyStock.add(0, stockItem);
			}
		}
	}
	
	public void linkItems()
	{
		for ( StockItem item : sellStock )
		{
			for ( int i = 0 ; i < buyStock.size() ; ++i )
			{
				if ( item.equals(buyStock.get(i)) )
				{
					item.getLimitSystem().linkWith(buyStock.get(i));
					buyStock.get(i).getLimitSystem().setGlobalAmount(item.getLimitSystem().getGlobalLimit());
					buyStock.get(i).getLimitSystem().linkWith(item);
				}
			}
		}
	}
	
	public void reloadStock()
	{
		List<StockItem> tempSellStock = new ArrayList<StockItem>();
		for ( StockItem item : sellStock )
		{
			if ( !item.isPatternItem() )
				tempSellStock.add(item);
		}
		
		List<StockItem> tempBuyStock = new ArrayList<StockItem>();
		for ( StockItem item : buyStock )
		{
			if ( !item.isPatternItem() )
				tempBuyStock.add(item);
		}

		
		sellStock.clear();
		buyStock.clear();

		if ( !pattern.isEmpty() )
		{
			sellStock.addAll( patterns.getPattern(pattern).getStockItems("sell") );
			buyStock.addAll( patterns.getPattern(pattern).getStockItems("buy") );
		}

		
		for ( StockItem item : tempSellStock ) {
			sellStock.remove(item);
			sellStock.add(item);
		}

		for ( StockItem item : tempBuyStock ) {
			buyStock.remove(item);
			buyStock.add(item);
		}
		
		/*for ( StockItem item : sellStock ) {
			if ( item.isPatternListening() && !pattern.isEmpty() )
				patterns.getPattern(pattern).getItemPrice(item, "sell");
		}

		for ( StockItem item : buyStock ) {
			if ( item.isPatternListening() && !pattern.isEmpty() )
				patterns.getPattern(pattern).getItemPrice(item, "buy");
		}*/
	}
	
	public void clearStock(String stock)
	{
		if ( stock.equals("sell") )
			sellStock.clear();
		else
		if ( stock.equals("buy") )
			buyStock.clear();
		else
		{
			sellStock.clear();
			buyStock.clear();
		}
	}
	
	public void save(DataKey data) {
	//	//System.out.print(data);
		
        List<String> sellList = new ArrayList<String>();
		if ( !sellStock.isEmpty() )
	        for ( StockItem item : sellStock )
	        {
				if ( !item.isPatternItem() )
					sellList.add(item.toString());
	        }
        
		List<String> buyList = new ArrayList<String>();
		if ( !buyStock.isEmpty() )
			for ( StockItem item : buyStock )
			{
				if ( !item.isPatternItem() )
					buyList.add(item.toString());
			}

		data.setRaw("sell", sellList);
		data.setRaw("buy", buyList);
		
	}
	
	//Returning the displayInventory
	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this,size,"sellstockroom");

        for ( StockItem item : sellStock )
        {
        	if ( item.getSlot() < 0 )
        		item.setSlot(inv.firstEmpty());
            inv.setItem( item.getSlot() ,item.getItemStack());
        }
		return inv;
	}
	
	public Inventory inventoryView(Inventory view,TraderStatus s) {

		if ( s.equals(TraderStatus.SELL) ) {
			for( StockItem item : sellStock ) {
				String price = "";
				
				if ( patterns.getPattern(pattern) != null )
				{
					price = "^7" + new DecimalFormat("#.##").format(patterns.getPattern(pattern).getItemPrice(tempPlayer, item, "sell", 0, 0.0));
					price += '$';
				} 
				else
				{
					price = "^7" + new DecimalFormat("#.##").format(item.getPrice());
					price += '$';
				}
				
	            ItemStack chk = priceLore(new CraftItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability()), price);
	            chk.addEnchantments(item.getItemStack().getEnchantments());
	            
	            
            	if ( item.getSlot() < 0 )
            		item.setSlot(view.firstEmpty());
            	
	            view.setItem( item.getSlot() ,chk);
	            
	        }
            if ( !buyStock.isEmpty() )
            	view.setItem(view.getSize()-1, config.getItemManagement(1));
            
		} else if ( s.equals(TraderStatus.BUY ) ) {
			for( StockItem item : buyStock ) {

				String price = "";
				
				if ( patterns.getPattern(pattern) != null )
				{
					price = "^7" + new DecimalFormat("#.##").format(patterns.getPattern(pattern).getItemPrice(tempPlayer, item, "buy", 0, 0.0));
					price += '$';
				} 
				else
				{
					price = "^7" + new DecimalFormat("#.##").format(item.getPrice());
					price += '$';
				}
				
	            ItemStack chk = priceLore(new CraftItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability()), price);
	            
	           // ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());

	            if ( item.getSlot() < 0 )
            		item.setSlot(view.firstEmpty());
                view.setItem( item.getSlot() ,chk);

	        }
            view.setItem(view.getSize()-1, config.getItemManagement(0));//3
		} else if ( s.equals(TraderStatus.MANAGE_SELL ) ) {
			for( StockItem item : sellStock ) {
				String price = "";
				
				if ( patterns.getPattern(pattern) != null )
				{
					price = "^7" + new DecimalFormat("#.##").format(patterns.getPattern(pattern).getItemPrice(tempPlayer, item, "sell", 0, 0.0));
					price += '$';
				} 
				else
				{
					price = "^7" + new DecimalFormat("#.##").format(item.getPrice());
					price += '$';
				}
				
	            ItemStack chk = priceLore(new CraftItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability()), price);
	            
	            //ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());

	            if ( item.getSlot() < 0 )
            		item.setSlot(view.firstEmpty());
                view.setItem( item.getSlot() ,chk);

	        }
            view.setItem(view.getSize()-3, config.getItemManagement(4));//3
            view.setItem(view.getSize()-2, config.getItemManagement(2));//3
            view.setItem(view.getSize()-1, config.getItemManagement(1));//3
		} else if ( s.equals(TraderStatus.MANAGE_BUY ) ) {
			for( StockItem item : buyStock ) {
				String price = "";
				
				if ( patterns.getPattern(pattern) != null )
				{
					price = "^7" + new DecimalFormat("#.##").format(patterns.getPattern(pattern).getItemPrice(tempPlayer, item, "buy", 0, 0.0));
					price += '$';
				} 
				else
				{
					price = "^7" + new DecimalFormat("#.##").format(item.getPrice());
					price += '$';
				}
				
	            ItemStack chk = priceLore(new CraftItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability()), price);
	            
	           // ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());

	            if ( item.getSlot() < 0 )
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

		//System.out.print(18);
		for( StockItem item : sellStock ) {
			String price = "";
	
			if ( patterns.getPattern(pattern) != null )
			{
				price = "^7" + new DecimalFormat("#.##").format(patterns.getPattern(pattern).getItemPrice(tempPlayer, item, "sell", 0, 0.0));
				price += '$';
			} 
			else
			{
				price = "^7" + new DecimalFormat("#.##").format(item.getPrice());
				price += '$';
			}
			//System.out.print(1);
            ItemStack chk = priceLore(new CraftItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability()), price);

			//System.out.print(12);
	     //   ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	        chk.addEnchantments(item.getItemStack().getEnchantments());

			//System.out.print(13);
	        if ( item.getSlot() < 0 )
        		item.setSlot(view.firstEmpty());
			//System.out.print(14);
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
	
	public int getStockSize(TraderStatus status) 
	{
		if ( status.equals(TraderStatus.SELL) )
			return sellStock.size();
		return buyStock.size();
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
			itemDisplay = itemDisplay.replace("{nr}", String.valueOf(i+1) );
			
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
			
			if ( i >= page * 10 && i < ( ( 1 + page ) * 10 ) )
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
				if ( itemStack.getType().equals(item.getItemStack().getType()) ) {
						equal = true;
					if ( dura ) 
						equal = itemStack.getDurability() <= item.getItemStack().getDurability();
					else
						equal = itemStack.getData().equals(item.getItemStack().getData());
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
	
	public void setInventoryWith(Inventory inv,StockItem si) {
		int i = 0;
		for ( Integer amount : si.getAmounts() ) {
			
			String price = "";
			
			if ( patterns.getPattern(pattern) != null )
			{
				price = "^7" + new DecimalFormat("#.##").format(patterns.getPattern(pattern).getItemPrice(tempPlayer, si, "sell", 0, 0.0)*amount);
				price += '$';
			} 
			else
			{
				price = "^7" + new DecimalFormat("#.##").format(si.getPrice()*amount);
				price += '$';
			}
			
			ItemStack chk = priceLore(new CraftItemStack(si.getItemStack().getType(),si.getItemStack().getAmount(),si.getItemStack().getDurability()), price);

			
		//	ItemStack is = si.getItemStack().clone();
			chk.setAmount(amount);
			if ( si.getLimitSystem().checkLimit("", i) )
				inv.setItem(i++,chk);
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

	//NBT tags
	public ItemStack priceLore(CraftItemStack cis, String lore)
	{
		//CraftItemStack cis = new CraftItemStack(item);
		net.minecraft.server.ItemStack mis = cis.getHandle();
		
		NBTTagCompound c = mis.getTag(); 
		if ( c == null )
			c = new NBTTagCompound();
		mis.setTag(c);
		
		if(!c.hasKey("display")) {
			c.set("display", new NBTTagCompound());
		}
		 
		NBTTagCompound d = c.getCompound("display");
		 
		if(!d.hasKey("Lore")) {
		  d.set("Lore", new NBTTagList());
		}
		
		NBTTagList l = d.getList("Lore");
		
		
		if ( lore != null )
		//	for ( String str : lore )
			//	//System.out.print(str);
				if ( !lore.isEmpty() )
					l.add(new NBTTagString("", lore.replace('^', '§')));
		 
		d.set("Lore", l);
		return cis;
	}

}

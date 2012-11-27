package net.dtl.citizens.trader.parts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.ItemsConfig;
import net.dtl.citizens.trader.managers.PatternsManager;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.objects.TransactionPattern;
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

public class TraderStockPart implements InventoryHolder {
	private static PatternsManager patternsManager = CitizensTrader.getPatternsManager();
	private static ItemsConfig itemsConfig = CitizensTrader.getInstance().getItemConfig(); 
	
	public TraderStockPart(String name)
	{
		this(54, name);
		pattern = null;
	}
	private TraderStockPart(int size, String name)
	{
		this.name = name;
		stockSize = size;
        if( size <= 0 || size > 54 ){
        	throw new IllegalArgumentException("Size must be between 1 and 54");}
        
        stock = new HashMap<String, List<StockItem>>();
        stock.put("sell", new ArrayList<StockItem>());
        stock.put("buy", new ArrayList<StockItem>());
    }
	
	private int stockSize;
	private String name;
	private TransactionPattern pattern;
	
	private Map<String,List<StockItem>> stock;
	
	//set/get the pattern
	public TransactionPattern getPattern()
	{
		return pattern;
	}
	public boolean setPattern(String pattern)
	{
		if ( patternsManager.getPattern(pattern) == null )
			return false;
		
		this.pattern = patternsManager.getPattern(pattern);
		this.reloadStock();
		return true;
	}
	public void removePattern()
	{
		pattern = null;
		reloadStock();
	}
	
	
	public void reloadStock()
	{
		List<StockItem> oldSellStock = new ArrayList<StockItem>();
		for ( StockItem item : stock.get("sell") )
			if ( !item.isPatternItem() )
				oldSellStock.add(item);
		
		List<StockItem> oldBuyStock = new ArrayList<StockItem>();
		for ( StockItem item : stock.get("buy") )
			if ( !item.isPatternItem() )
				oldBuyStock.add(item);

		stock.get("sell").clear();
		stock.get("buy").clear();

		if ( pattern != null )
		{
			stock.get("sell").addAll( pattern.getStockItems("sell") );
			stock.get("buy").addAll( pattern.getStockItems("buy") );
		}

		
		for ( StockItem item : oldSellStock ) {
			stock.get("sell").remove(item);
			stock.get("sell").add(item);
		}

		for ( StockItem item : oldBuyStock ) {
			stock.get("buy").remove(item);
			stock.get("buy").add(item);
		}
		
	}
	
	public void clearStock(String stock)
	{
		this.stock.get(stock).clear();
	}
	public void clearStock()
	{
		clearStock("sell");
		clearStock("buy");
	}
	
	
	public Inventory getInventory(String startingStock, Player player)
	{
		Inventory inventory = Bukkit.createInventory(this, stockSize, name);
		
		for( StockItem item : stock.get(startingStock) ) 
		{
			ItemStack chk = setLore(createCraftItem(item), getPriceLore(item, startingStock, pattern, player));

			chk.addEnchantments(item.getItemStack().getEnchantments());

	        if ( item.getSlot() < 0 )
        		item.setSlot(inventory.firstEmpty());
	        inventory.setItem(item.getSlot(),chk);
        }

        if ( !stock.get( opositeStock(startingStock) ).isEmpty() )
        	inventory.setItem(stockSize - 1, itemsConfig.getItemManagement( opositeStock(startingStock) ) );
        
		return inventory;
	}
	public Inventory inventoryView(Inventory inventory, TraderStatus s, Player player) {

		if ( !s.isManaging() )
		{
			for( StockItem item : stock.get(s.toString()) ) 
			{
				ItemStack chk = setLore(createCraftItem(item), getPriceLore(item, s.toString(), pattern, player));
            	
				chk.addEnchantments(item.getItemStack().getEnchantments());
				
				if ( item.getSlot() < 0 )
            		item.setSlot(inventory.firstEmpty());
            	
	            inventory.setItem( item.getSlot() ,chk);
	            
	        }
			if ( !stock.get( opositeStock(s.toString()) ).isEmpty() )
	        	inventory.setItem(stockSize - 1, itemsConfig.getItemManagement( opositeStock(s.toString()) ) );
	        
		} 
		else 
		{
			for( StockItem item : stock.get(s.toString()) )
			{
				ItemStack chk = setLore(createCraftItem(item), getPriceLore(item, s.toString(), pattern, player));
 
	            //ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());

	            if ( item.getSlot() < 0 )
            		item.setSlot(inventory.firstEmpty());
	            inventory.setItem( item.getSlot() ,chk);

	        }
            inventory.setItem(stockSize - 3, itemsConfig.getItemManagement(4) );
            inventory.setItem(stockSize - 2, itemsConfig.getItemManagement(2) );
            inventory.setItem(stockSize - 1, itemsConfig.getItemManagement(opositeStock(s.toString())) );
		} 
		
		return inventory;
	}

	public void addItem(String stock ,String data) {
		this.stock.get(stock).add(new StockItem(data));
	}
	public void addItem(String stock, StockItem stockItem) {
		this.stock.get(stock).add(stockItem);
	}
	public void removeItem(String stock, int slot) {
		for ( StockItem item : this.stock.get(stock) )
			if ( item.getSlot() == slot ) 
			{
				this.stock.get(stock).remove(item);
				return;
			}
	}
	
	public StockItem getItem(int slot, TraderStatus status) {
		for ( StockItem item : stock.get(status.toString()) )
			if ( item.getSlot() == slot )
				return item;
		return null;
	}
	public StockItem getItem(ItemStack itemStack, TraderStatus status, boolean dura,
			boolean amount) {
		boolean equal = false;

		for ( StockItem item : stock.get(status.toString()) ) 
		{
			equal = false;
			if ( itemStack.getType().equals(item.getItemStack().getType()) ) 
			{
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
		return null;
	}
	
	public void setInventoryWith(Inventory inventory, StockItem item, Player player) {
		int i = 0;
		for ( Integer amount : item.getAmounts() ) {
			
			ItemStack chk = setLore(createCraftItem(item), getPriceLore(item, "sell", pattern, player));
			chk.addEnchantments(item.getItemStack().getEnchantments());
			
			
			chk.setAmount(amount);
			if ( item.getLimitSystem().checkLimit("", i) )
				inventory.setItem(i++,chk);
		}
		inventory.setItem(stockSize - 1, itemsConfig.getItemManagement(7));
	}
	public static void setManagerInventoryWith(Inventory inventory, StockItem item) {
		int i = 0;
		for ( Integer amount : item.getAmounts() ) 
		{
			ItemStack itemStack = item.getItemStack().clone();
			itemStack.setAmount(amount);
			inventory.setItem(i++, itemStack);
		}
		inventory.setItem(inventory.getSize() - 1, itemsConfig.getItemManagement(7));
	}
		
	/*//patterns manager
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
	*/
	//Returning the displayInventory
	@Override
	public Inventory getInventory() 
	{
		Inventory inventory = Bukkit.createInventory(this, stockSize, name);
		
		for( StockItem item : stock.get("sell") ) 
		{
			ItemStack chk = setLore(createCraftItem(item), getPriceLore(item, "sell", pattern, null));

			chk.addEnchantments(item.getItemStack().getEnchantments());

	        if ( item.getSlot() < 0 )
        		item.setSlot(inventory.firstEmpty());
	        inventory.setItem(item.getSlot(),chk);
        }

        if ( !stock.get( "buy" ).isEmpty() )
        	inventory.setItem(stockSize - 1, itemsConfig.getItemManagement( "buy" ) );
        
		return inventory;
	}
	/*
	
	
	
	
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
	
	
	
	
	
	

	//NBT tags
	*/

	@SuppressWarnings("unchecked")
	public void load(DataKey data) {
		if ( data.keyExists("sell") )
		{
			for ( String item : (List<String>) data.getRaw("sell") ) 
			{
				StockItem stockItem = new StockItem(item);
				if ( stockItem.getSlot() < 0 )
					stock.get("sell").add(stockItem);
				else
					stock.get("sell").add(0, stockItem);
			}
		}

		if ( data.keyExists("buy") ) 
		{
			for ( String item :  (List<String>) data.getRaw("buy") )
			{
				StockItem stockItem = new StockItem(item);
				if ( stockItem.getSlot() < 0 )
					stock.get("buy").add(stockItem);
				else
					stock.get("buy").add(0, stockItem);
			}
		}
	
	}

	public void save(DataKey data)
	{
		List<String> sellList = new ArrayList<String>();
        for ( StockItem item : stock.get("sell") )
			if ( !item.isPatternItem() )
				sellList.add(item.toString());
        
        
		List<String> buyList = new ArrayList<String>();
		for ( StockItem item : stock.get("buy") )
		{
			if ( !item.isPatternItem() )
				buyList.add(item.toString());
		}

		data.setRaw("sell", sellList);
		data.setRaw("buy", buyList);
	}

	//Static methods
	public static ItemStack setLore(CraftItemStack cis, List<String> lore)
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
			for ( String str : lore )
				if ( !str.isEmpty() )
					l.add(new NBTTagString("dtl_trader", str.replace('^', '§')));
		 
		d.set("Lore", l);
		return cis;
	}
	
	public static List<String> getPriceLore(StockItem item, String stock, TransactionPattern pattern, Player player)
	{
		String price = "";
		DecimalFormat format = new DecimalFormat("#.##");
		
		if ( pattern != null )
			price = format.format(pattern.getItemPrice(player, item, stock, 0, 0.0));
		else
			price = format.format(item.getPrice());
		
		List<String> lore = new ArrayList<String>();
		for ( String line : itemsConfig.getPriceLore("sell") )
			lore.add(line.replace("{price}", price));
		
		return lore;
	}
	
	public static CraftItemStack createCraftItem(StockItem item)
	{
		return new CraftItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(), item.getItemStack().getDurability());
	}

	public static String opositeStock(String stock)
	{
		return ( stock.equals("sell") ? "buy" : "sell" );
	}

	public static void saveNewAmouts(Inventory inventory, StockItem si) {
		si.getAmounts().clear();
		for ( ItemStack is : inventory.getContents() ) 
			if ( is != null ) 
				si.addAmount(is.getAmount());
		
		if ( si.getAmounts().size() > 1 )
			si.getAmounts().remove(si.getAmounts().size()-1);
	}
}

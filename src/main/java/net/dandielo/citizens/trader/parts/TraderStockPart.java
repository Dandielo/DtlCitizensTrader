package net.dandielo.citizens.trader.parts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.citizensnpcs.api.util.DataKey;
import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.ItemsConfig;
import net.dandielo.citizens.trader.managers.PermissionsManager;
import net.dandielo.citizens.trader.objects.NBTTagEditor;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.patterns.PatternsManager;
import net.dandielo.citizens.trader.patterns.TPattern;
import net.dandielo.citizens.trader.patterns.types.ItemPattern;
import net.dandielo.citizens.trader.patterns.types.PricePattern;
import net.dandielo.citizens.trader.patterns.types.PricePattern.Price;
import net.dandielo.citizens.trader.types.Trader;
import net.dandielo.citizens.trader.types.Trader.TraderStatus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class TraderStockPart implements InventoryHolder {
	private static PermissionsManager perms = CitizensTrader.getPermissionsManager();
	
	// allow to set different stocks for different players 
	private static Map<String, Map<String, List<StockItem>>> playerStocks = new HashMap<String, Map<String, List<StockItem>>>();
	
	public static Map<String, List<StockItem>> getPlayerStock(String player) {
		return playerStocks.get(player);
	}
	
	// patterns
	private static PatternsManager patternsManager = CitizensTrader.getPatternsManager();
	
	// config
	private static ItemsConfig itemsConfig = CitizensTrader.getInstance().getItemConfig(); 
	
	public TraderStockPart(String name)
	{
		this(54, name);
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
	
	// general stock configuration
	private Map<Integer, TPattern> patterns = new TreeMap<Integer, TPattern>();
	private int stockSize;
	private String name;
	
	private Map<String, List<StockItem>> stock;
	
	//set/get the pattern
	public Map<Integer, TPattern> getPatterns()
	{
		return patterns;
	}
	
	public TPattern getPattern(String pattern)
	{
		TPattern p = null;
		for ( TPattern pat : patterns.values() )
			if ( pat.getName().equals(pattern) )
				p = pat;
		return p;
	}
	
	public boolean addPattern(String pattern, int priority)
	{
		if ( patternsManager.getPattern(pattern) == null )
			return false;
		
		patterns.put(priority, patternsManager.getPattern(pattern));
		return true;
	}
	
	public void removePattern(String pattern)
	{	
		Iterator<TPattern> it = patterns.values().iterator();
		while(it.hasNext())
			if ( it.next().getName().equals(pattern) )
				it.remove();
		//patterns.remove(patternsManager.getPattern(pattern));
	}
	
	public void removeAllPatterns()
	{	
		patterns.clear();
	}
	
	public List<StockItem> getStock(String stock)
	{
		return this.stock.get(stock);
	}
	
	public List<StockItem> getStock(Player player, String st)
	{
		List<StockItem> result = new ArrayList<StockItem>();
		
		for ( Entry<Integer, TPattern> pattern : patterns.entrySet() )
		{
			TPattern pat = pattern.getValue();
			if ( pat instanceof ItemPattern && perms.has(player, "") )
			{
				result.addAll( ((ItemPattern)pat).getStock(player, st) );
			}
		}

		for ( StockItem item : stock.get(st) ) {
			result.remove(item);
			result.add(item);
		}

		return result;
	}
	
	public TraderStockPart createStockFor(Player player)
	{
		TraderStockPart pstock = new TraderStockPart(stockSize, name);

		for ( Entry<Integer, TPattern> pattern : patterns.entrySet() )
		{
			TPattern pat = pattern.getValue();
			if ( pat instanceof ItemPattern && perms.has(player, "dtl.trader.pattern." + pat.getName()) )
			{
				pstock.stock.get("sell").addAll( ((ItemPattern)pat).getStock(player, "sell") );
				pstock.stock.get("buy").addAll( ((ItemPattern)pat).getStock(player, "buy") );
			}
			else
			if ( pat instanceof PricePattern && perms.has(player, "dtl.trader.pattern." + pat.getName()) )
			{
				pstock.patterns.put(pattern.getKey(), pat);
			}
		}

		for ( StockItem item : stock.get("sell") ) {
			pstock.stock.get("sell").remove(item);
			pstock.stock.get("sell").add(item);
		}

		for ( StockItem item : stock.get("buy") ) {
			pstock.stock.get("buy").remove(item);
			pstock.stock.get("buy").add(item);
		}
		
		return pstock;
	}
	
	
	public Inventory getInventory(String startingStock, Player player)
	{
		Inventory inventory = Bukkit.createInventory(this, stockSize, name);
		if ( startingStock == null )
			startingStock = stock.get("sell").isEmpty() ? "buy" : "sell";
		
		for( StockItem item : stock.get(startingStock) ) 
		{
			ItemStack chk = setLore(item.getItemStack(), getPriceLore(item, 0, startingStock, patterns, player));

	        if ( item.getSlot() < 0 )
        		item.setSlot(inventory.firstEmpty());
	        inventory.setItem(item.getSlot(),chk);
        }

        if ( !stock.get( opositeStock(startingStock) ).isEmpty() )
        	inventory.setItem(stockSize - 1, itemsConfig.getItemManagement( opositeStock(startingStock) ) );
        
		return inventory;
	}
	
	
	public Inventory inventoryView(Inventory inventory, TraderStatus s, Player player, String type)
	{
		
		if ( !s.isManaging() )
		{
			for( StockItem item : stock.get(s.toString()) ) 
			{
				ItemStack chk = setLore(item.getItemStack(), getPriceLore(item, 0, s.toString(), patterns, player));
            
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
				ItemStack chk = setLore(item.getItemStack(), getLore(type, item, s.toString(), patterns, player));
 
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
	
	public StockItem getItem(ItemStack itemStack, TraderStatus status, boolean dura, boolean amount) {
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

				if ( equal ) {
					// StockItem has 2 boolean properties that are set to true if its entry in an Items Pattern has the "ce" or "cel" flags  
					boolean checkEnchant = item.isCheckingEnchantments();
					boolean checkLevel = item.isCheckingEnchantmentLevels();

					if ( checkEnchant || checkLevel ) {
						Map<Enchantment,Integer> itemStackEnchantments = null;
						Map<Enchantment,Integer> stockItemEnchantments = null;
						
						// special handling for Enchanted Books and stored enchantments
						if ( itemStack.getType().equals(Material.ENCHANTED_BOOK) ) {
							EnchantmentStorageMeta itemStackStorageMeta = (EnchantmentStorageMeta)itemStack.getItemMeta();
							if (itemStackStorageMeta != null) {
								itemStackEnchantments = itemStackStorageMeta.getStoredEnchants();
							}

							EnchantmentStorageMeta stockItemStorageMeta = (EnchantmentStorageMeta)item.getItemStack().getItemMeta();
							if (stockItemStorageMeta != null) {
								stockItemEnchantments = stockItemStorageMeta.getStoredEnchants();
							}
						}
						else { // regular enchantments (not stored enchantments)
							itemStackEnchantments = itemStack.getEnchantments();
							stockItemEnchantments = item.getItemStack().getEnchantments();
						}
						
						if (itemStackEnchantments == null || itemStackEnchantments.isEmpty()) {
							equal = (stockItemEnchantments == null || stockItemEnchantments.isEmpty());
						}
						else {
							equal = ( stockItemEnchantments != null 
									&& !stockItemEnchantments.isEmpty() 
									&& itemStackEnchantments.keySet().equals(stockItemEnchantments.keySet()) );
						}

						// equal is still true if both itemStacks had the same enchanments
						if ( equal && checkLevel ) {
							for ( Map.Entry<Enchantment,Integer> ench : itemStackEnchantments.entrySet() ) {
								if ( ench.getValue() != stockItemEnchantments.get(ench.getKey()) ) {
									equal = false;
									break;
								}
							}
						}
					}
				}

				if ( equal )
					return item;
			}
		}
		return null;
	}
	
	public void setInventoryWith(Trader trader, Inventory inventory, StockItem item, Player player) {
		int i = 0;

		for ( Integer amount : item.getAmounts() ) 
		{
			ItemStack chk = setLore(item.getItemStack(), getPriceLore(item, i, "sell", patterns, player));
			
			chk.setAmount(amount);
			if ( CitizensTrader.getLimitsManager().checkLimit(trader, "sell", item, amount) )
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
	//TODO Limits

	/*public void linkItems()
	{
		for ( StockItem item : stock.get("sell") )
		{
			for ( int i = 0 ; i < stock.get("buy").size() ; ++i )
			{
				if ( item.equals(stock.get("buy").get(i)) )
				{
				//	item.getLimitSystem().linkWith(stock.get("buy").get(i));
				//	stock.get("buy").get(i).getLimitSystem().setGlobalAmount(item.getLimitSystem().getGlobalLimit());
				//	stock.get("buy").get(i).getLimitSystem().linkWith(item);
				}
			}
		}
	}*/

	//Returning the displayInventory
	@Override
	public Inventory getInventory() 
	{
		Inventory inventory = Bukkit.createInventory(this, stockSize, name);
		String startingStock = stock.get("sell").isEmpty() ? "buy" : "sell";
		
		for( StockItem item : stock.get(startingStock) ) 
		{
			ItemStack chk = setLore(item.getItemStack(), getPriceLore(item, 0, startingStock, patterns, null));

	        if ( item.getSlot() < 0 )
        		item.setSlot(inventory.firstEmpty());
	        inventory.setItem(item.getSlot(),chk);
        }

        if ( !stock.get( opositeStock(startingStock) ).isEmpty() )
        	inventory.setItem(stockSize - 1, itemsConfig.getItemManagement( opositeStock(startingStock) ) );
        
		return inventory;
	}
	
	@SuppressWarnings("unchecked")
	public void load(DataKey data) 
	{
		List<String> pat = (List<String>) data.getRaw("patterns");
		if ( pat != null )
			for ( String pattern : pat )
			{
				addPattern(pattern.split(" ")[0], Integer.valueOf(pattern.split(" ")[1]));
			}
		
		if ( data.keyExists("sell") )
		{
			for ( Object item : (List<Object>) data.getRaw("sell") ) 
			{
				if ( item instanceof String )
				{
					StockItem stockItem = new StockItem((String)item);
					if ( stockItem.getSlot() < 0 )
						stock.get("sell").add(stockItem);
					else
						stock.get("sell").add(0, stockItem);
				}
				else
				{
					StockItem stockItem = null;
					for ( Map.Entry<String, List<String>> entry : ((Map<String, List<String>>) item).entrySet() )
						stockItem = new StockItem(entry.getKey(), entry.getValue());

					if ( stockItem.getSlot() < 0 )
						stock.get("sell").add(stockItem);
					else
						stock.get("sell").add(0, stockItem);
				}
			}
		}

		if ( data.keyExists("buy") ) 
		{
			for ( Object item :  (List<Object>) data.getRaw("buy") )
			{
				if ( item instanceof String )
				{
					StockItem stockItem = new StockItem((String)item);
					if ( stockItem.getSlot() < 0 )
						stock.get("buy").add(stockItem);
					else
						stock.get("buy").add(0, stockItem);
				}
				else
				{
					StockItem stockItem = null;
					for ( Map.Entry<String, List<String>> entry : ((Map<String, List<String>>) item).entrySet() )
						stockItem = new StockItem(entry.getKey(), entry.getValue());

					if ( stockItem.getSlot() < 0 )
						stock.get("buy").add(stockItem);
					else
						stock.get("buy").add(0, stockItem);
				}
			}
		}
		
	}

	public void save(DataKey data)
	{
		List<String> patList = new ArrayList<String>();
		for ( Entry<Integer, TPattern> pat : patterns.entrySet() )
			patList.add(pat.getValue().getName() + " " + pat.getKey());
		
		List<Object> sellList = new ArrayList<Object>();
        for ( StockItem item : stock.get("sell") )
			if ( !item.patternItem() )
			{
				if ( item.hasLore() )
				{
					Map<String, List<String>> temp = new HashMap<String, List<String>>();
					temp.put(item.toString(), item.getLore());
					sellList.add(temp);
				}
				else
					sellList.add(item.toString());
			}
        
		List<Object> buyList = new ArrayList<Object>();
		for ( StockItem item : stock.get("buy") )
			if ( !item.patternItem() )
			{
				if ( item.hasLore() )
				{
					Map<String, List<String>> temp = new HashMap<String, List<String>>();
					temp.put(item.toString(), item.getLore());
					buyList.add(temp);
				}
				else
					buyList.add(item.toString());
			}

		data.setRaw("sell", sellList);
		data.setRaw("buy", buyList);
	}

	//Static methods
	public static ItemStack setLore(ItemStack cis, List<String> lore)
	{		
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(cis.getType());
		
		List<String> list = cis.getItemMeta().getLore();
		if ( list == null )
			list = new ArrayList<String>();
		
		for ( String s : lore )
			list.add(s.replace('^', '§'));
		
		meta.setLore(list);
		meta.setDisplayName(NBTTagEditor.getName(cis));
		
		for ( Map.Entry<Enchantment, Integer> e : cis.getEnchantments().entrySet() )
			meta.addEnchant(e.getKey(), e.getValue(), true);
		
		if ( cis.getType().equals(Material.ENCHANTED_BOOK) )
		{
			for ( Map.Entry<Enchantment, Integer> e : ((EnchantmentStorageMeta)cis.getItemMeta()).getStoredEnchants().entrySet() )
				((EnchantmentStorageMeta)meta).addStoredEnchant(e.getKey(),	e.getValue(), true);
		}
		
		Map<String, Object> map = cis.serialize();
		
		map.put("meta", meta);
		
		return ItemStack.deserialize(map);
	}
	 
	public static List<String> getLore(String type, StockItem item, String stock, Map<Integer, TPattern> patterns, Player player)
	{
		if ( type.equals("glimit") )
			return getLimitLore(item, stock, player);
		if ( type.equals("plimit") )
			return getPlayerLimitLore(item, stock, player);
		if ( type.equals("manage") )
			return getManageLore(item, stock, player);
		return getPriceLore(item, 0, stock, patterns, player);
	}
	
	public static List<String> getPriceLore(StockItem item, int slot, String stock, Map<Integer, TPattern> patterns, Player player)
	{
		String price = "";
		DecimalFormat format = new DecimalFormat("#.##");

		price = format.format(TraderStockPart.getPrice(item, patterns, player, stock, slot));
		
		List<String> lore = new ArrayList<String>();
		for ( String line : CitizensTrader.getLocaleManager().lore("trader-inventory-" + stock) )//itemsConfig.getPriceLore(stock) )
			lore.add(line.replace("{price}", price).replace("{amount}", "not supported"));
		
		return lore;
	}
	
	public static List<String> getManageLore(StockItem item, String stock, Player player)
	{
		List<String> lore = new ArrayList<String>();
		if ( item.stackPrice() )
			lore.add("^7Stack price");
		if ( item.patternPrice() )
			lore.add("^7Pattern price");
		if ( item.patternItem() )
			lore.add("^7Pattern item");
		
		return lore;
	}
	
	public static List<String> getPlayerLimitLore(StockItem item, String stock, Player player)
	{
		List<String> lore = new ArrayList<String>();
		if ( item.getLimits().get("player") != null )
		{
			lore.add("^7Limit: ^6" +item.getLimits().limit("player"));
			lore.add("^7Timeout: ^6" + item.getLimits().timeout("player"));
		}
		
		return lore;
	}
	
	public static List<String> getLimitLore(StockItem item, String stock, Player player)
	{
		List<String> lore = new ArrayList<String>();
		if ( item.getLimits().get("global") != null )
		{
			lore.add("^7Limit: ^6" + item.getLimits().limit("global"));
			lore.add("^7Timeout: ^6" + item.getLimits().timeout("global"));
		}
		
		return lore;
	}

	public static String opositeStock(String stock)
	{
		return ( stock.equals("sell") ? "buy" : "sell" );
	}

	public static void saveNewAmounts(Inventory inventory, StockItem si) {
		si.getAmounts().clear();
		for ( ItemStack is : inventory.getContents() ) 
			if ( is != null ) 
				si.addAmount(is.getAmount());
		
		if ( si.getAmounts().size() > 1 )
			si.getAmounts().remove(si.getAmounts().size()-1);
	}
	
	public double getPrice(StockItem item, Player player, String stock, int slot)
	{
		double price = item.getPrice(slot);
		
		Price prc = new Price(0);
		for ( Entry<Integer, TPattern> pat : patterns.entrySet() )
		{
			if ( pat.getValue() instanceof PricePattern && perms.has(player, "dtl.trader.patterns." + pat.getValue().getName()) )
				prc.merge(((PricePattern)pat.getValue()).getPrice(item, player, stock));
		}
		if ( prc.hasPrice() )
			price = prc.endPrice(item.patternMultiplier());
		return price;
	}
	
	public static double getPrice(StockItem item, Map<Integer, TPattern> patterns, Player player, String stock, int slot)
	{
		double price = item.getPrice(slot);
		
		Price prc = new Price(0);
		for ( Entry<Integer, TPattern> pat : patterns.entrySet() )
		{
			if ( pat.getValue() instanceof PricePattern && perms.has(player, "dtl.trader.patterns." + pat.getValue().getName()) )
				prc.merge(((PricePattern)pat.getValue()).getPrice(item, player, stock));
		}
		if ( prc.hasPrice() )
			price = prc.endPrice(item.patternMultiplier());
		return price;
	}

	public void reset(String st, String target)
	{
		if ( target.equals("prices") )
		{
			if ( st == null )
			{
				for ( StockItem item : stock.get("sell") )
				{
					item.setRawPrice(0.0);
					item.setPatternPrice(true);
				}
				for ( StockItem item : stock.get("buy") )
				{
					item.setRawPrice(0.0);
					item.setPatternPrice(true);
				}
			}
			else
				for ( StockItem item : stock.get(st) )
				{
					item.setRawPrice(0.0);
					item.setPatternPrice(true);
				}
		}
		else
		if ( target.equals("stock") )
		{
			if ( st == null )
			{
				stock.get(st).clear();
				stock.get(st).clear();
			}
			else
			{
				stock.get(stock).clear();
			}
		}
	}
}

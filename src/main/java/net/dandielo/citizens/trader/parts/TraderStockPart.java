package net.dandielo.citizens.trader.parts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
	PermissionsManager perms = CitizensTrader.getPermissionsManager();
	
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
	//	reloadStock();
		return true;
	}
	
	public void removePattern(String pattern)
	{	
		patterns.remove(patternsManager.getPattern(pattern));
	//	reloadStock();
	}
	
	public void removeAllPatterns()
	{	
		patterns.clear();
	//	reloadStock();
	}
	
	public List<StockItem> getStock(String stock)
	{
		return this.stock.get(stock);
	}
	
	public TraderStockPart createStockFor(Player p)
	{
		TraderStockPart pstock = new TraderStockPart(stockSize, name);
		
		for ( Entry<Integer, TPattern> pattern : patterns.entrySet() )
		{
			TPattern pat = pattern.getValue();
			if ( pat.getType().equals("item") && perms.has(p, pat.getName()) )
			{
				pstock.stock.get("sell").addAll( pat.getStockItems("sell") );
				pstock.stock.get("buy").addAll( pat.getStockItems("buy") );
			}
			else
			if ( pat.equals("price") )
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
	/*
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
		
	}*/
	/*
	public Inventory getInventory(String startingStock, Player player)
	{
		Inventory inventory = Bukkit.createInventory(this, stockSize, name);
		
		for( StockItem item : stock.get(startingStock) ) 
		{
			ItemStack chk = setLore(item.getItemStack(), getPriceLore(item, 0, startingStock, pattern, player));

		//	chk.addEnchantments(item.getItemStack().getEnchantments());

	        if ( item.getSlot() < 0 )
        		item.setSlot(inventory.firstEmpty());
	        inventory.setItem(item.getSlot(),chk);
        }

        if ( !stock.get( opositeStock(startingStock) ).isEmpty() )
        	inventory.setItem(stockSize - 1, itemsConfig.getItemManagement( opositeStock(startingStock) ) );
        
		return inventory;
	}
	/*
	
	public Inventory inventoryView(Inventory inventory, TraderStatus s, Player player, String type)
	{
		
		if ( !s.isManaging() )
		{
			for( StockItem item : stock.get(s.toString()) ) 
			{
				ItemStack chk = setLore(item.getItemStack(), getPriceLore(item, 0, s.toString(), pattern, player));
            	
			//	chk.addEnchantments(item.getItemStack().getEnchantments());
				
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
				ItemStack chk = setLore(item.getItemStack(), getLore(type, item, s.toString(), pattern, player));
 
	         //ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	         //   chk.addEnchantments(item.getItemStack().getEnchantments());

	            if ( item.getSlot() < 0 )
            		item.setSlot(inventory.firstEmpty());
	            inventory.setItem( item.getSlot() ,chk);

	        }
            inventory.setItem(stockSize - 3, itemsConfig.getItemManagement(4) );
            inventory.setItem(stockSize - 2, itemsConfig.getItemManagement(2) );
            inventory.setItem(stockSize - 1, itemsConfig.getItemManagement(opositeStock(s.toString())) );
		} 
		
		return inventory;
	}*/

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
	
	public void setInventoryWith(Inventory inventory, StockItem item, Player player) {
		int i = 0;

		for ( Integer amount : item.getAmounts() ) 
		{
			ItemStack chk = setLore(item.getItemStack(), getPriceLore(item, i, "sell", patterns, player));
			
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
		
		for( StockItem item : stock.get("sell") ) 
		{
			ItemStack chk = setLore(item.getItemStack(), getPriceLore(item, 0, "sell", patterns, null));

			if ( item.getSlot() < 0 )
        		item.setSlot(inventory.firstEmpty());
	        inventory.setItem(item.getSlot(),chk);
        }

        if ( !stock.get( "buy" ).isEmpty() )
        	inventory.setItem(stockSize - 1, itemsConfig.getItemManagement( "buy" ) );
        
		return inventory;
	}
	
	@SuppressWarnings("unchecked")
	public void load(DataKey data) 
	{
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
		
	//	setPattern( data.getString("pattern", "") );
	}

	public void save(DataKey data)
	{
		if ( pattern != null )
			if ( !pattern.getName().isEmpty() )
				data.setString("pattern", pattern.getName());
		
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
		
	//	cis.setItemMeta(ItemStack.deserialize(map).getItemMeta());
		
		return ItemStack.deserialize(map);
	}
	 
	public static List<String> getLore(String type, StockItem item, String stock, Map<Integer, TPattern> patterns, Player player)
	{
		if ( type.equals("glimit") )
			return getLimitLore(item, stock, player);
		if ( type.equals("plimit") )
			return getPlayerLimitLore(item, stock, player);
		if ( type.equals("manage") )
			return getManageLore(item, stock, patterns, player);
		return getPriceLore(item, 0, stock, patterns, player);
	}
	
	public static List<String> getPriceLore(StockItem item, int i, String stock, Map<Integer, TPattern> patterns, Player player)
	{
		String price = "";
		DecimalFormat format = new DecimalFormat("#.##");

		//TODO price
		if ( patterns != null )
			price = format.format(patterns.getItemPrice(player, item, stock, i, 0.0));
		else
			price = format.format(item.getPrice(i));
		
		List<String> lore = new ArrayList<String>();
		for ( String line : CitizensTrader.getLocaleManager().lore("trader-inventory-" + stock) )//itemsConfig.getPriceLore(stock) )
			lore.add(line.replace("{price}", price).replace("{amount}", item.getLimitSystem().getStackAmount()));
		
		return lore;
	}
	
	public static List<String> getManageLore(StockItem item, String stock, Player player)
	{
		org.bukkit.event.entity.EntityDeathEvent e;
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
		if ( item.getLimitSystem().hasLimit() )
		{
			lore.add("^7Limit: ^6" +item.getLimitSystem().getPlayerLimit());
			lore.add("^7Timeout: ^6" + item.getLimitSystem().getPlayerTimeout());
		}
		
		return lore;
	}
	
	public static List<String> getLimitLore(StockItem item, String stock, Player player)
	{
		List<String> lore = new ArrayList<String>();
		if ( item.getLimitSystem().hasLimit() )
		{
			lore.add("^7Limit: ^e" + item.getLimitSystem().getGlobalAmount() + "^6/" + item.getLimitSystem().getGlobalLimit());
			lore.add("^7Timeout: ^6" + item.getLimitSystem().getGlobalTimeout());
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

	public void reset(String st, String target)
	{
		if ( target.equals("prices") )
		{
			if ( st == null )
			{
				for ( StockItem item : stock.get("sell") )
				{
					item.setRawPrice(0.0);
					item.setPatternListening(true);
				}
				for ( StockItem item : stock.get("buy") )
				{
					item.setRawPrice(0.0);
					item.setPatternListening(true);
				}
			}
			else
				for ( StockItem item : stock.get(st) )
				{
					item.setRawPrice(0.0);
					item.setPatternListening(true);
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

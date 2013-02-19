package net.dandielo.citizens.trader.objects;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dandielo.citizens.trader.limits.Limits;
import net.dandielo.citizens.trader.limits.Limits.Limit;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class StockItem {
	//item fields
	protected ItemStack item;
	protected List<Integer> amounts = new ArrayList<Integer>();
	
	protected String name;
	protected List<String> lore;
	protected String bookId;
	
	//trader fields 
	protected double price;
	protected Double multiply = null;
	protected int slot = -1;
	
	protected Limits limits;
	
	protected boolean stackPrice = false;
	protected boolean unitPrice = false;
	
	//pattern fields 
	protected boolean checkEnchantments = false;
	protected boolean checkEnchantmentLevels = false;
	protected boolean patternMultiplier = false;
	protected boolean patternPrice = true;
	protected boolean patternItem = false;
	
	protected Double spawnChance = null;
	
	protected Integer matchPriority = 0;
	protected String matcherString;
	protected String tier;
	
	//load item from string
	public StockItem(String data)
	{
		this(data, null);
	}

	private static Pattern pattern = Pattern.compile("((n):(([^:\\s]+)( [^:\\s]{2,})*))|((\\S+){1}:){0,1}([^:\\s]+)");//((\\S+){1}:){0,1}(([^:\\s]+)+( [^:\\s]{2,})*)");
	
	//load item from string with lore
	public StockItem(String data, List<String> lore) 
	{
		matcherString = data;
		// init limits
		limits = new Limits(this);
		
		Matcher matcher = pattern.matcher(data);
		
		// look for values
		while(matcher.find())
		{
			String key = matcher.group(7);
			String value = matcher.group(8);
			
			if ( item == null )
			{
				if ( key == null )
				{
					Material mat = Material.getMaterial(value);
					if ( mat == null )
						mat = Material.getMaterial(Integer.parseInt(value));
					
					item = new ItemStack(mat, 1);
					amounts.add(1);
				}
				else
				{
					Material mat = Material.getMaterial(key);
					if ( mat == null )
						mat = Material.getMaterial(Integer.parseInt(key));
					
					item = new ItemStack(mat, 1, Byte.parseByte(value));
					amounts.add(1);
				}
			}
			else 
			{
				//item name or flag
				if ( key == null )
				{
					if ( matcher.group(2) != null && matcher.group(2).equals("n") )
					{
						//backward compatibility
						setName(matcher.group(3).replace("[&]", " "));
					}
					else
					//use enchantments for comparison
					if ( value.equals("ce") ) 
					{
						checkEnchantments = true;
					}
					//use enchantments and their levels for comparison
					else
					if ( value.equals("cel") ) 
					{
						checkEnchantmentLevels = true;
					}
					//stack price
					else
					if ( value.equals("sp") ) 
					{ 
						stackPrice = true;
					}
					//unit price
					else
					if ( value.equals("up") ) 
					{ 
						unitPrice = true;
					}
					else
					//stack price management
					if ( value.equals("pat") )
					{
						patternPrice = true;
					}
					else
					//stack price management
					if ( value.equals("pm") )
					{
						patternMultiplier = true;
					}
					else
					//stack price management
					if ( value.equals("lore") ) 
					{
						this.lore = lore;
						NBTTagEditor.addDescription(item, lore);
					}
				}
				else
				{
					if ( key.equals("p") )
					{
						price = Double.parseDouble(value);
					}
					else
					if ( key.equals("m") )
					{
						multiply = new Double(value);
					}
					else
					if ( key.equals("c") )
					{
						spawnChance = new Double(value);
					}
					else
					if ( key.equals("a") )
					{
						amounts.clear();
						for ( String amout : value.split(",") )
							amounts.add( Integer.parseInt(amout) < 1 ? 1 : Integer.parseInt(amout) );
						item.setAmount(amounts.get(0));
					}
					else
					if ( key.equals("s") )
					{
						slot = Integer.parseInt(value);
					}
					else
					if ( key.equals("t") )
					{
						tier = value;
					}
					else
					if ( key.equals("d") ) 
					{
						item.setDurability( Short.parseShort(value) );
					}
					else
					if ( key.equals("gl") ) 
					{
						String[] limitData = value.split("/");
						limits.setLimit("global", new Limit(Integer.parseInt(limitData[0]), Integer.parseInt(limitData[1])));
					}
					else
					if ( key.equals("pl") ) 
					{
						String[] limitData = value.split("/");
						limits.setLimit("global", new Limit(Integer.parseInt(limitData[0]), Integer.parseInt(limitData[1])));
					}
					else
					if ( key.equals("e") ) 
					{
						for ( String enchantment : value.split(",") )
						{
							String[] enchData = enchantment.split("/");
							Enchantment ench = Enchantment.getByName( enchData[0] );
							if ( ench == null )
								ench = Enchantment.getById( Integer.parseInt(enchData[0]));
							item.addUnsafeEnchantment(ench, Integer.parseInt(enchData[1]));
						}
					}
					else
					if ( key.equals("se")  )
					{
						for ( String enchantment : value.split(",") )
						{
							String[] enchData = enchantment.split("/");
							EnchantmentStorageMeta meta = ((EnchantmentStorageMeta)item.getItemMeta());
							if ( item.getType().equals(Material.ENCHANTED_BOOK) )
							{
								Enchantment ench = Enchantment.getByName( enchData[0] );
								if ( ench == null )
									ench = Enchantment.getById( Integer.parseInt(enchData[0]) );
								meta.addStoredEnchant(ench, Integer.parseInt(enchData[1]), true);
							}
							item.setItemMeta(meta);
						}
					}
				}
			}
		}
	}
	
	public ItemStack getItemStack() {
		item.setAmount(amounts.get(0));
		return item;
	}
	
	public double getMultiplier()
	{
		return multiply;
	}
	
	public boolean hasMupltiplier()
	{
		return multiply != null;
	}
/*	public ItemStack getItemStack(int slot) {
		item.setAmount(amounts.get(slot));
		if ( stackPrice )
			item.setAmount(amounts.get(0));
		return item;
	}*/
	
	public void setName(String name)
	{
		NBTTagEditor.setName(item, name);
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

	public String name()
	{
		return name.isEmpty() ? item.getType().name() : name;
	}
	
	@Override
	public String toString() {
		
		//saving the item id and data
		String itemString = "" + item.getTypeId() + ( item.getData().getData() != 0 ? ":" + item.getData().getData() : "" );
		
		//saving the item price
		if ( !patternPrice )
			itemString += " p:" + new DecimalFormat("#.##").format(price);
		
		//saving the item slot
		itemString += " s:" + slot;
		
		//saving the item slot
		itemString += " d:" + item.getDurability();
		
		//saving the item amounts
		itemString += " a:";
		for ( int i = 0 ; i < amounts.size() ; ++i )
			itemString += amounts.get(i) + ( i + 1 < amounts.size() ? "," : "" );
		
		//saving the item global limits
		if ( limits.getLimit("global") != null ) 
			itemString += " gl:" + limits.getLimit("global").toString();
		
		//saving the item global limits
		if ( limits.getLimit("player") != null ) 
			itemString += " pl:" + limits.getLimit("player").toString();
		
		//saving enchantment's
		if ( !item.getEnchantments().isEmpty() ) {
			itemString += " e:";
				
			for ( int i = 0 ; i < item.getEnchantments().size() ; ++i ) {
				Enchantment e = (Enchantment) item.getItemMeta().getEnchants().keySet().toArray()[i];
				itemString += e.getId() + "/" + item.getEnchantmentLevel(e) + ( i + 1 < item.getEnchantments().size() ? "," : "" );
			}
		}

		if ( item.getType().equals(Material.ENCHANTED_BOOK) )
		{
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta)item.getItemMeta();
			
			if ( !meta.getStoredEnchants().isEmpty() )
			{
				itemString += " se:";
				int i = 0;
				for ( Map.Entry<Enchantment, Integer> e : meta.getStoredEnchants().entrySet() ) {
					itemString += e.getKey().getId() + "/" + e.getValue() + ( i + 1 < ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().size() ? "," : "" );
					++i;
				}
			}
		}
		
		if ( !name.isEmpty() ) 
		{
			itemString += " n:" + name;
		}
		
		//saving additional configurations
		if ( stackPrice )
		{
			itemString += " sp";
		}
		if ( patternPrice )
		{
			itemString += " pat";
		}
		if ( patternMultiplier )
		{
			itemString += " mp";
		}
		
		//use enchantments for comparison
		if ( checkEnchantments ) 
		{
			itemString += " ce";
		}
		//use enchantments and their levels for comparison
		else 
		if ( checkEnchantmentLevels )
		{
			itemString += " cel";
		}
		
		return itemString;
	}

	public boolean stackPrice() {
		return stackPrice;
	}
	
	public void setStackPrice(boolean b) {
		stackPrice = b;
	}
	
	public void increasePrice(double d) {
		price += d;
	}
	
	public void lowerPrice(double p) {
		if ( ( price - p ) < 0 ) {
			price = 0;
			return;
		}
		price -= p;
	}
	
	public double getRawPrice() 
	{
		return price;
	}
	
	public double getPrice(int slot) 
	{
		return price*amounts.get(slot);
	}
	
	public void setRawPrice(double newPrice)
	{
		price = newPrice;
	}
	
	/*
	public double getRawPrice() {
		return price;
	}
	public double getPrice(int i) {
		if ( stackPrice )
			return price;
		if ( i < amounts.size() ) 
			return price*amounts.get(i);
		return 0;
	}*/
	
	public boolean hasMultipleAmounts() {
		//Allow MA for stack price
		//if ( stackPrice )
		//	return false;
		return ( amounts.size() > 1 ? true : false );
	}
	
	// item slot
	public int getSlot() {
		return slot;
	}
	
	public void setSlot(int s) {
		slot = s;
	}
	
	// item amounts
	public void setAmount(int a) {
		amounts.clear();
		item.setAmount(a);
		amounts.add(a);
	}
	
	public List<Integer> getAmounts()
	{
		return amounts;
	}
	
	public void addAmount(int a) {
		patternItem = false;
		amounts.add(a);
	}
	
	public int getAmount() {
		return amounts.get(0);
	}
	
	public int getAmount(int slot) {
		return amounts.get(slot);
	}
	
	// pattern flags
	public boolean patternItem()
	{
		return patternItem;
	}
	
	public void setAsPatternItem(boolean pItem)
	{
		patternItem = pItem;
	}
	
	public boolean patternPrice()
	{
		return patternPrice;
	}
	
	public void setPatternPrice(boolean p)
	{
		patternPrice = p;
	}
	
	public boolean patternMultiplier()
	{
		return patternPrice;
	}
	
	public boolean isCheckingEnchantments()
	{
		return checkEnchantments;
	}
	
	public boolean isCheckingEnchantmentLevels()
	{
		return checkEnchantmentLevels;
	}
	
	// limit systems
	public Limits getLimitSystem() {
		return limits;
	}

	// get id and data as string
	public String getIdAndData()
	{
		return item.getTypeId() + ( item.getData().getData() == 0 ? "" : ":" + item.getData().getData() );
	}	
	
	public int getMatchPriority()
	{
		return matchPriority;
	}
	
	public boolean matches(StockItem item)
	{
		return matches(item, true);
	}
	public boolean matches(StockItem item, boolean amount)
	{
		Matcher matcher = pattern.matcher(item.matcherString);

		boolean result = true;
		
		// look for values
		while(matcher.find() && result)
		{
			String key = matcher.group(7);
			String value = matcher.group(8);
			
			//item name or flag
			if ( key == null )
			{
				if ( matcher.group(2) != null && matcher.group(2).equals("n") )
				{
					result = name.equals(matcher.group(3).replace("[&]", " "));
					item.matchPriority += 300; 
				}
				else
				{
					Material mat = Material.getMaterial(value);
					if ( mat == null )
						mat = Material.getMaterial(Integer.parseInt(value));
					
					if ( mat != null )
						result = item.getItemStack().getType().equals(mat);
					item.matchPriority += 130;
				}
			}
			else
			{
				if ( key.equals("a") && amount )
				{
					amounts.clear();
					result = this.item.getAmount() == Integer.parseInt(value.split(",")[0]);
					item.matchPriority += 5;
				}
				else
				if ( key.equals("d") ) 
				{
					result = hasDurability(this.item) && this.item.getDurability() == Short.parseShort(value);
					item.matchPriority += 45;
				}
				else
				if ( key.equals("t") ) 
				{
					result = this.tier.equals(value);
					item.matchPriority += 25;
				}
				else
				if ( key.equals("e") ) 
				{
					String[] enchants = value.split(",");
					
					if ( enchants.length != this.item.getEnchantments().size() )
					{
						result = false;
					}
					else
					{
						Map<Enchantment, Integer> enchs = new HashMap<Enchantment, Integer>(this.item.getEnchantments());
						boolean has = true;
						
						for ( int i = 0 ; i < enchants.length && has ; ++i )
						{
							String[] enchData = enchants[0].split("/");
							Enchantment ench = Enchantment.getByName( enchData[0] );
							if ( ench == null )
								ench = Enchantment.getById( Integer.parseInt(enchData[0]));
							int lvl = Integer.valueOf(enchData[0]);
							
							boolean h = false;
							Iterator<Enchantment> it = enchs.keySet().iterator();
							while(it.hasNext()&&!h) h = it.next().equals(ench) && enchs.get(ench) == lvl;
							has = h;
						}
						result = has;
					}
					item.matchPriority += 5;
				}
				else
				if ( key.equals("se")  )
				{
					String[] enchData = value.split("/");
					
					if ( this.item.getType().equals(Material.ENCHANTED_BOOK) )
					{
						EnchantmentStorageMeta meta = ((EnchantmentStorageMeta)this.item.getItemMeta());
						Enchantment ench = Enchantment.getByName( enchData[0] );
						if ( ench == null )
							ench = Enchantment.getById( Integer.parseInt(enchData[0]) );
						result = meta.getStoredEnchants().get(ench) == Integer.parseInt(enchData[1]);
					}
					else
						result = false;
				}
				else
				{
					Material mat = Material.getMaterial(key);
					if ( mat == null )
						mat = Material.getMaterial(Integer.parseInt(key));

					if ( mat != null )
						result = this.item.getType().equals(mat) && this.item.getData().getData() == Byte.valueOf(value);
				}
				item.matchPriority += 5;
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		StockItem item = (StockItem) obj;
		if ( item.getItemStack().getTypeId() == this.item.getTypeId()
			 && item.getItemStack().getData().getData() == this.item.getData().getData() ) {
			
			if ( checkEnchantments || checkEnchantmentLevels ) {
				if ( !item.getItemStack().getEnchantments().keySet().equals(this.item.getEnchantments().keySet()) )
					return false;
			}
		
			if ( checkEnchantmentLevels ) {
				if ( !item.getItemStack().getEnchantments().values().equals(this.item.getEnchantments().values()) )
					return false;
			}
		
			return true;
		}
		
		return false;
	}

	/*public double calcPrice(Player player, TransactionPattern pattern, String stock)
	{
		return calcPrice(player, pattern, stock, 0);
	}
	
	public double calcPrice(Player player, TransactionPattern pattern, String stock, int slot)
	{
		if ( pattern != null )
			return pattern.getItemPrice(player, this, stock, slot, 0.0);
		return getPrice(slot);
	}*/

	public static boolean hasDurability(ItemStack item)
	{
		int id = item.getTypeId();
		return ( id > 275 && id < 289 ) || ( id > 291 && id < 296 ) || ( id > 298 && id < 304 ) || ( id > 306 && id < 326 );// ? true : false );
	}
	
	@SuppressWarnings("unchecked")
	public static StockItem loadItem(Object data)
	{
		if ( data instanceof String )
		{
			return new StockItem((String) data);
		}
		for ( Map.Entry<String, Object> entry : ((Map<String, Object>) data).entrySet() )
			return new StockItem(entry.getKey(), (List<String>) entry.getValue());
		return null;
	} 
}

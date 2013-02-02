package net.dtl.citizens.trader.objects;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.dtl.citizens.trader.CitizensTrader.*;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class StockItem {	
	protected ItemStack item = null;
	protected List<Integer> amounts = new ArrayList<Integer>();
	protected boolean stackPrice = false;
	protected double price = 0;
	protected int slot = -1;
	protected LimitSystem limit;
	protected String name = "";
	
	protected boolean listenPattern = true;
	protected boolean patternItem = false;
	
	protected boolean checkEnchantments = false;
	protected boolean checkEnchantmentLevels = false;
	
	//just for override compatibility
	protected StockItem()
	{
		limit = new LimitSystem(this);
	}
	
	public StockItem(String data) {
		limit = new LimitSystem(this);
		String[] values = data.split(" ");
		for ( String value : values ) {
			if ( item == null ) {
				
				if ( value.contains(":") ) {
					String[] itemData = value.split(":");
					item = new ItemStack(Integer.parseInt(itemData[0]), 1, Byte.parseByte(itemData[1]));
					amounts.add(1);
				} else {
					item = new ItemStack(Integer.parseInt(value),1);
					amounts.add(1);
				}
			} else {
				if ( value.length() > 2 ) {
					
					
					if ( value.startsWith("p:") && !value.contains("/") && !value.contains(";") ) {
						try 
						{
							price = Double.parseDouble(value.substring(2));
						}
						catch (NumberFormatException e)
						{
							info("Has the locale changed? Decimal format has changed.");
						}
						listenPattern = false;
					}
					if ( value.startsWith("s:") && !value.contains("/") && !value.contains(";") ) {
						slot = Integer.parseInt(value.substring(2));
					}
					if ( value.startsWith("n:") && !value.contains("/") && !value.contains(";") )
					{
						setName(value.substring(2).replace("[&]", " ").replace("[@]", " "));
					}
					if ( value.startsWith("d:") && !value.contains("/") && !value.contains(";") ) {
						item.setDurability(Short.parseShort(value.substring(2)));
					}
					if ( value.startsWith("a:") && !value.contains("/") && !value.contains(";") ) {
						amounts.clear();
						for ( String amout : value.substring(2).split(",") )
							amounts.add((Integer.parseInt(amout)==0?1:Integer.parseInt(amout)));
						if ( amounts.size() > 0 )
							item.setAmount(amounts.get(0));
					}
					if ( value.startsWith("gl:") && !value.contains(";") ) {
						String[] limitData = value.substring(3).split("/");
						limit.setItemGlobalLimit(Integer.parseInt(limitData[0]), Integer.parseInt(limitData[1]), Integer.parseInt(limitData[2])*1000);
					}
					if ( value.startsWith("pl:") && !value.contains(";") ) {
						String[] limitData = value.substring(3).split("/");
						limit.setItemPlayerLimit(Integer.parseInt(limitData[0]), Integer.parseInt(limitData[1]), Integer.parseInt(limitData[2])*1000);
					}
					if ( value.startsWith("e:") && !value.contains(";")  ) {
						for ( String ench : value.substring(2).split(",") )
						{
							String[] enchData = ench.split("/");
							item.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(enchData[0])), Integer.parseInt(enchData[1]));
						}
					}
					if ( value.startsWith("se:") && !value.contains(";")  ) {
						for ( String ench : value.substring(3).split(",") )
						{
							String[] enchData = ench.split("/");
							EnchantmentStorageMeta meta = ((EnchantmentStorageMeta)item.getItemMeta());
							if ( item.getType().equals(Material.ENCHANTED_BOOK) )
								meta.addStoredEnchant(Enchantment.getById(Integer.parseInt(enchData[0])), Integer.parseInt(enchData[1]), true);
							item.setItemMeta(meta);
						}
					}
					
					//use enchantments for comparison
					if ( value.equals("ce") ) {
						checkEnchantments = true;
					}
					//use enchantments and their levels for comparison
					else if ( value.equals("cel") ) {
						checkEnchantmentLevels = true;
					}
				}
				else
				{
					//stack price management
					if ( value.equals("sp") ) { 
						stackPrice = true;
					}
					//stack price management
					if ( value.equals("pat") ) {
						listenPattern = true;
					}
				}
			}
		}
	}
	
	public ItemStack getItemStack() {
		item.setAmount(amounts.get(0));
		return item;
	}
	public ItemStack getItemStack(int slot) {
		item.setAmount(amounts.get(slot));
		if ( stackPrice )
			item.setAmount(amounts.get(0));
		return item;
	}
	
	public void setName(String name)
	{
		NBTTagEditor.setName(item, name);
		this.name = name;
	}
	
	public String getName()
	{
		return name.isEmpty() ? item.getType().name() : name;
	}

	@Override
	public String toString() {
		
		//saving the item id and data
		String itemString = "" + item.getTypeId() + ( item.getData().getData() != 0 ? ":" + item.getData().getData() : "" );
		
		//saving the item price
		if ( !listenPattern )
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
		if ( limit.hasLimit() ) 
			itemString += " gl:" + limit.toString();
		
		//saving the item global limits
		if ( limit.hasPlayerLimit() ) 
			itemString += " pl:" + limit.playerLimitToString();
		
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
			itemString += " n:" + name.replace(" ", "[&]");
		
		//saving additional configurations
		if ( stackPrice )
			itemString += " sp";
		if ( listenPattern )
			itemString += " pat";

		//use enchantments for comparison
		if ( checkEnchantments ) {
			itemString += " ce";
		}
		//use enchantments and their levels for comparison
		else if ( checkEnchantmentLevels ) {
			itemString += " cel";
		}
		
		return itemString;
	}

	public boolean hasStackPrice() {
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
	
	public static boolean hasDurability(ItemStack item)
	{
		int id = item.getTypeId();
		return ( id > 275 && id < 289 ) || ( id > 291 && id < 296 ) || ( id > 298 && id < 304 ) || ( id > 306 && id < 326 );// ? true : false );
	}
	
	public double getPrice() {
		if ( stackPrice )
			return price;
		return price*amounts.get(0);
	}
	public double getRawPrice() {
		return price;
	}
	public void setRawPrice(double newPrice)
	{
		price = newPrice;
	}
	public double getPrice(int i) {
		if ( stackPrice )
			return price;
		if ( i < amounts.size() ) 
			return price*amounts.get(i);
		return 0;
	}
	public boolean hasMultipleAmounts() {
		if ( stackPrice )
			return false;
		return ( amounts.size() > 1 ? true : false );
	}
	
	public int getSlot() {
		return slot;
	}
	public void setSlot(int s) {
		slot = s;
	}
	public void resetAmounts(int a) {
		amounts.clear();
		item.setAmount(a);
		amounts.add(a);
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
	public List<Integer> getAmounts() {
		return amounts;
	}
	
	public boolean isPatternItem()
	{
		return patternItem;
	}
	public void setAsPatternItem(boolean pItem)
	{
		patternItem = pItem;
	}
	
	public boolean isPatternListening()
	{
		return listenPattern;
	}
	public void setPatternListening(boolean listen)
	{
		listenPattern = listen;
	}
	
	public boolean isCheckingEnchantments()
	{
		return checkEnchantments;
	}
	
	public boolean isCheckingEnchantmentLevels()
	{
		return checkEnchantmentLevels;
	}
	
	public LimitSystem getLimitSystem() {
		return limit;
	}

	public String getIdAndData()
	{
		return item.getTypeId() + ( item.getData().getData() == 0 ? "" : ":" + item.getData().getData() );
	}
	
	public static StockItem createItem(Class<? extends StockItem> itemClass, String data)
	{
		try {
			return itemClass.getConstructor(String.class).newInstance(data);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void test()
	{
	}
	
	@Override
	public boolean equals(Object obj)
	{
		StockItem item = (StockItem) obj;
		if ( //item.getSlot() == slot 
			 item.getItemStack().getTypeId() == this.item.getTypeId()
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

	
}

package net.dtl.citizens.trader.objects;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class StockItem {	
	protected ItemStack item = null;
	protected List<Integer> amouts = new ArrayList<Integer>();
	protected boolean stackPrice = false;
	protected double price = 0;
	protected int slot = -1;
	protected LimitSystem limit;
	
	protected boolean listenPattern = true;
	protected boolean patternItem = false;
	
	public StockItem(String data) {
		limit = new LimitSystem(this);
		String[] values = data.split(" ");
		for ( String value : values ) {
			if ( item == null ) {
				
				if ( value.contains(":") ) {
					String[] itemData = value.split(":");
					item = new ItemStack(Integer.parseInt(itemData[0]), 1, (short) 0, Byte.parseByte(itemData[1]));
					amouts.add(1);
				} else {
					item = new ItemStack(Integer.parseInt(value),1);
					amouts.add(1);
				}
			} else {
				if ( value.length() > 2 ) {
					
					
					if ( value.startsWith("p:") && !value.contains("/") && !value.contains(";") ) {
						price = Double.parseDouble(value.substring(2));
						listenPattern = false;
					}
					if ( value.startsWith("s:") && !value.contains("/") && !value.contains(";") ) {
						slot = Integer.parseInt(value.substring(2));
					}
					if ( value.startsWith("d:") && !value.contains("/") && !value.contains(";") ) {
						item.setDurability(Short.parseShort(value.substring(2)));
					}
					if ( value.startsWith("a:") && !value.contains("/") && !value.contains(";") ) {
						amouts.clear();
						for ( String amout : value.substring(2).split(",") )
							amouts.add((Integer.parseInt(amout)==0?1:Integer.parseInt(amout)));
						if ( amouts.size() > 0 )
							item.setAmount(amouts.get(0));
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
						for ( String ench : value.substring(2).split(",") ) {
							String[] enchData = ench.split("/");
							item.addEnchantment(Enchantment.getById(Integer.parseInt(enchData[0])), Integer.parseInt(enchData[1]));
						}
					}
				} 
				else
				{
					//stack price management
					if ( value.equals("sp") ) { //&& !value.contains("/") && !value.contains(";") ) {
						stackPrice = true;
					}
					//stack price management
					if ( value.equals("pat") ) { //&& !value.contains("/") && !value.contains(";") ) {
						listenPattern = true;
					}
				}
			}
		}
	}
	
	public ItemStack getItemStack() {
		item.setAmount(amouts.get(0));
		return item;
	}
	public ItemStack getItemStack(int slot) {
		item.setAmount(amouts.get(slot));
		if ( stackPrice )
			item.setAmount(amouts.get(0));
		return item;
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
		for ( int i = 0 ; i < amouts.size() ; ++i )
			itemString += amouts.get(i) + ( i + 1 < amouts.size() ? "," : "" );
		
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
				Enchantment e = (Enchantment) item.getEnchantments().keySet().toArray()[i];
				itemString += e.getId() + "/" + item.getEnchantmentLevel(e) + ( i + 1 < item.getEnchantments().size() ? "," : "" );
			}
		}
		
		//saving additional configurations
		if ( stackPrice )
			itemString += " sp";
		if ( listenPattern )
			itemString += " pat";
		
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
	
	public double getBuyPrice() {
		if ( stackPrice )
			return price/amouts.get(0);
		return price;
	}
	
	public double getPrice() {
		if ( stackPrice )
			return price;
		return price*amouts.get(0);
	}
	public double getRawPrice() {
		return price;
	}
	public void setRawPrice(double newPrice)
	{
		price = newPrice;
	}
	public double getPrice(int i) {
		if ( i < amouts.size() )
			return price*amouts.get(i);
		return 0;
	}
	public boolean hasMultipleAmouts() {
		if ( stackPrice )
			return false;
		return ( amouts.size() > 1 ? true : false );
	}
	
	public int getSlot() {
		return slot;
	}
	public void setSlot(int s) {
		slot = s;
	}
	public void resetAmounts(int a) {
		amouts.clear();
		item.setAmount(a);
		amouts.add(a);
	}
	public void addAmount(int a) {
		patternItem = false;
		amouts.add(a);
	}
	public int getAmount() {
		return amouts.get(0);
	}
	public int getAmount(int slot) {
		return amouts.get(slot);
	}
	public List<Integer> getAmounts() {
		return amouts;
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
	public void setPetternListening(boolean listen)
	{
		listenPattern = listen;
	}
	
	public LimitSystem getLimitSystem() {
		return limit;
	}

	public String getIdAndData()
	{
		return item.getTypeId() + ( item.getData().getData() == 0 ? "" : ":" + item.getData().getData() );
	}
	
	@Override
	public boolean equals(Object obj)
	{
		StockItem item = (StockItem) obj;
		if ( //item.getSlot() == slot 
				 item.getItemStack().getTypeId() == this.item.getTypeId()
				&& item.getItemStack().getData().getData() == this.item.getData().getData() )
			return true;
		return false;
	}

	
}

package net.dtl.citizenstrader_new.containers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class StockItem {
	private ItemStack item = null;
	private List<Integer> amouts = new ArrayList<Integer>();
	private boolean stackPrice = false;
	private double price = 0;
	private int slot = -1;
	private Limit limit = new Limit();
	
	public StockItem() {
	}
	
	public StockItem(String data) {
		String[] values = data.split(" ");
		for ( String value : values ) {
			if ( item == null ) {
				
				
				/* *
				 * StockItem required properties
				 * id => ItemId
				 * data => itemData
				 * 
				 */
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
					
					
					/* *
					 * Additional StockItem properties
					 * p => price
					 * s => slot
					 * d => durability
					 * a => amounts (list)
					 * e => enchants (list)
					 * l => limit
					 *  
					 */
					if ( value.startsWith("p:") && !value.contains("/") && !value.contains(";") ) {
						price = Double.parseDouble(value.substring(2));
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
							amouts.add(Integer.parseInt(amout));
						if ( amouts.size() > 0 )
							item.setAmount(amouts.get(0));
					}
					if ( value.startsWith("l:") && !value.contains(";") ) {
						String[] limitData = value.substring(2).split("/");
						limit.setLimit(Integer.parseInt(limitData[0]));
						limit.setAmount(Integer.parseInt(limitData[1]));
						limit.setTimeout(Integer.parseInt(limitData[2])*1000);
					}
					if ( value.startsWith("e:") && !value.contains(";")  ) {
						for ( String ench : value.substring(2).split(",") ) {
							String[] enchData = ench.split("/");
							item.addEnchantment(Enchantment.getById(Integer.parseInt(enchData[0])), Integer.parseInt(enchData[1]));
						}
					}
					
					/* *
					 * StockItem configurations
					 * sp => stackPrice
					 * 
					 */
					if ( value.equals("sp") ) { //&& !value.contains("/") && !value.contains(";") ) {
						stackPrice = true;
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
		itemString += " p:" + price;
		
		//saving the item slot
		itemString += " s:" + slot;
		
		//saving the item slot
		itemString += " d:" + item.getDurability();
		
		//saving the item amounts
		itemString += " a:";
		for ( int i = 0 ; i < amouts.size() ; ++i )
			itemString += amouts.get(i) + ( i + 1 < amouts.size() ? "," : "" );
		
		//saving the item limits
		if ( limit.hasLimit() ) 
			itemString += " l:" + limit.toString();
		
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
	
	/* *
	 * Limit handling
	 * 
	 */
	public boolean checkLimit() {
		if ( limit.checkTimer(new Date()).reachedLimit() ) 
			return false;
		return true;
	}
	public String getLimitReset() {
		return limit.getNextReset();
	}
	public void changeLimitAmount(int a) {
		limit.changeAmount(a);
	}
	public boolean hasLimitAmount(int a) { 
		if ( limit.hasLimit() )
			return limit.hasAmount(a);
		return true;
	}
	public void changeLimit(int l) { 
		limit.changeLimit(l);
	}
	public int getLimit() {
		return limit.getLimit();
	}
	
	public void changeTimeout(int t) { 
		limit.changeTimeout(t);
	}
	public String getTimeout() {
		return limit.getTimeout();
	}
	
	private class Limit {
		private int limit = -1;
		private int amount = 0;
		private Date timer = new Date();
		private long timeout = 0;
		
		/* *
		 * Limit and amount management
		 */
		public void setLimit(int l) {
			limit = l;
		}
		public int getLimit() {
			return limit;
		}
		public void changeTimeout(int t) {
			timeout += t*1000;
			if ( timeout < 0 )
				timeout = 0;
		}
		public void changeLimit(int l) {
			limit += l;
			if ( limit < 0 )
				limit = -1;
		}
		public void setAmount(int a) {
			amount = a;
		}
		public void changeAmount(int a) {
			amount += a;
		}
		public void resetAmount() {
			amount = 0;
		}
		public boolean hasAmount(int a) {
			return ( limit - amount ) >= a ;
		}
		
		public boolean reachedLimit() {
			if ( limit < 1 )
				return false;
			return limit <= amount;
		}
		public boolean hasLimit() {
			if ( limit < 0 )
				return false;
			return true;
		}
		
		/* *
		 * Time and reset management
		 * 
		 */
		public Limit setTimeout(long t) {
			timeout = t;
			return this;
		}
		public Limit resetTimer() {
			timer = new Date();
			return this;
		}
		public Limit checkTimer(Date d) {
			if ( limit < 0 )
				return this;
			if ( d.getTime() - timer.getTime() > timeout ) {
				reset();
			}
			return this;
		}
		public String getNextReset() {
			Date d = new Date();
			d.setTime(new Date().getTime() - timer.getTime());
			return new SimpleDateFormat("HH mm ss").format(d);
		}
		public String getTimeout() {
			Date d = new Date();
			d.setTime(timeout);
			return new SimpleDateFormat("dd HH mm ss").format(d);
		}
		
		/* *
		 * mainReset
		 * 
		 */
		public void reset() {
			resetTimer();
			resetAmount();
		}
		
		/* *
		 * toString Override
		 * 
		 */
		@Override
		public String toString() {
			return limit + "/" + amount + "/" + ( timeout / 1000 );
		}
	}
}

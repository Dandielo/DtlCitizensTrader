package net.dtl.citizens.trader.objects;

import java.text.DecimalFormat;
import java.util.Date;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class MarketItem extends StockItem {
	protected String itemOwner;
	protected Date time;
	
	public MarketItem(String data) {
		super("");
		
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
					if ( value.startsWith("o:") ) {
						setItemOwner(value.substring(2));
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
		
		itemString += " o:" + itemOwner;
		
		//saving additional configurations
		if ( stackPrice )
			itemString += " sp";
		if ( listenPattern )
			itemString += " pat";
		
		return itemString;
	}
	
	public String getItemOwner()
	{
		return itemOwner;
	}
	
	public void setItemOwner(String owner)
	{
		itemOwner = owner;
	}
}

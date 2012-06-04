package net.dtl.citizenstrader.traits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class StockItem {
	private ItemStack item = null;
	private List<Integer> amouts = new ArrayList<Integer>();
	private int price;
	
	public StockItem() {
		//'1': id[:data] p:price a:1,3,4,5 e:id/lvl,
		
	}
	
	public StockItem(String data) {
		String[] values = data.split(" ");
		for ( String value : values ) {
			if ( item == null ) {
				if ( value.contains(":") ) {
					String[] itemData = value.split(":");
					item = new ItemStack(Integer.parseInt(itemData[0]), 1, (short) 0, Byte.parseByte(itemData[1]));
				} else {
					item = new ItemStack(Integer.parseInt(value),1);
				}
			} else {
				if ( value.startsWith("p:") ) {
					price = Integer.parseInt(value.substring(2));
				}
				if ( value.startsWith("a:") ) {
					for ( String amout : value.substring(2).split(",") )
						amouts.add(Integer.parseInt(amout));
					if ( amouts.size() == 1 )
						item.setAmount(amouts.get(0));
				}
				if ( value.startsWith("e:") ) {
					for ( String ench : value.substring(2).split(",") ) {
						String[] enchData = ench.split("/");
						item.addEnchantment(Enchantment.getById(Integer.parseInt(enchData[0])), Integer.parseInt(enchData[1]));
					}
				}
			}
		}
	}
	
	public ItemStack getItemStack() {
		return item;
	}

	@Override
	public String toString() {
		//saving the item id and data
		String itemString = "" + item.getTypeId() + ( item.getData().getData() != 0 ? ":" + item.getData().getData() : "" );
		//saving the item price
		itemString += " p:" + price;
		//saving the item amouts
		itemString += " a:";
		for ( int i = 0 ; i < amouts.size() ; ++i )
			itemString += amouts.get(i) + ( i + 1 < amouts.size() ? "," : "" );
		//saving enchantments
		if ( !item.getEnchantments().isEmpty() ) {
			itemString += " e:";
			for ( int i = 0 ; i < item.getEnchantments().size() ; ++i ) {
				Enchantment e = (Enchantment) item.getEnchantments().keySet().toArray()[i];
				itemString += e.getId() + "/" + item.getEnchantments().get(i) + ( i + 1 < item.getEnchantments().size() ? "," : "" );
			}
		}
		return itemString;
	}

	public int getPrice() {
		return price*amouts.get(0);
	}
	public int getPrice(int i) {
		if ( i < amouts.size() )
			return price*amouts.get(i);
		return 0;
	}
	public boolean hasMultipleAmouts() {
		return ( amouts.size() > 1 ? true : false );
	}
	
	public List<Integer> getAmouts() {
		return amouts;
	}
}

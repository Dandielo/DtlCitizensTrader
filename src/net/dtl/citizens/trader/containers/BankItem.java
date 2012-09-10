package net.dtl.citizens.trader.containers;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class BankItem {
	
	private ItemStack item = null;
	//private List<Integer> amouts = new ArrayList<Integer>();
	//private boolean stackPrice = false;
	//private double price = 0;
	private int slot = -1;
	//private LimitSystem limit;
	
	public BankItem(String data) {
		//limit = new LimitSystem(this);
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
				//	amouts.add(1);
				} else {
					item = new ItemStack(Integer.parseInt(value),1);
				//	amouts.add(1);
				}
			} else {
				if ( value.length() > 2 ) 
				{
				
					if ( value.startsWith("s:") && !value.contains("/") && !value.contains(";") )
					{
						slot = Integer.parseInt(value.substring(2));
					}
					if ( value.startsWith("d:") && !value.contains("/") && !value.contains(";") ) 
					{
						item.setDurability(Short.parseShort(value.substring(2)));
					}
					if ( value.startsWith("a:") && !value.contains("/") && !value.contains(";") )
					{
						item.setAmount(Integer.parseInt(value.substring(2)));
					}
					if ( value.startsWith("e:") && !value.contains(";")  )
					{
						for ( String ench : value.substring(2).split(",") ) 
						{
							String[] enchData = ench.split("/");
							item.addEnchantment(Enchantment.getById(Integer.parseInt(enchData[0])), Integer.parseInt(enchData[1]));
						}
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
		
		//saving the item slot
		itemString += " s:" + slot;
		
		//saving the item slot
		itemString += " d:" + item.getDurability();
		
		//saving the item amounts
		itemString += " a:" + item.getAmount();
		
		//saving enchantment's
		if ( !item.getEnchantments().isEmpty() ) {
			itemString += " e:";
			for ( int i = 0 ; i < item.getEnchantments().size() ; ++i ) {
				Enchantment e = (Enchantment) item.getEnchantments().keySet().toArray()[i];
				itemString += e.getId() + "/" + item.getEnchantmentLevel(e) + ( i + 1 < item.getEnchantments().size() ? "," : "" );
			}
		}
		
		return itemString;
	}
	
	public int getSlot() {
		return slot;
	}
	public void setSlot(int s) {
		slot = s;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if ( !( o instanceof BankItem ) )
			return false;
		
		if ( ((BankItem)o).getSlot() == this.getSlot() )
			return true;
		return false;
	}
	
}

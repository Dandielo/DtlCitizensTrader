package net.dtl.citizens.trader.objects;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class BankItem
{
	private ItemStack item = null;
	private int slot = -1;
	
	//Support for anvil named items
	private String name = "";
	
	public BankItem(String data)
	{
		String[] values = data.split(" ");
		for ( String value : values ) 
		{
			if ( item == null )
			{
				
				if ( value.contains(":") ) 
				{
					String[] itemData = value.split(":");
					item = new ItemStack(Integer.parseInt(itemData[0]), 1, Byte.parseByte(itemData[1]));
				}
				else
				{
					item = new ItemStack(Integer.parseInt(value),1);
				}
			}
			else
			{
				if ( value.length() > 2 ) 
				{
					if ( value.startsWith("n:") && !value.contains("/") && !value.contains(";") )
					{
						setName(value.substring(2).replace("[@]", " "));
					}
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
		if ( !name.isEmpty() )
			itemString += " n:" + name.replace(" ", "[@]");
		
		return itemString;
	}
	
	public int getSlot() {
		return slot;
	}
	public void setSlot(int s) {
		slot = s;
	}
	
	public void setName(String name)
	{
		NBTTagEditor.setName(item, name);
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	/*private String getName()
	{
		net.minecraft.server.v1_4_5.ItemStack cis = ((CraftItemStack)item).getHandle();
		NBTTagCompound tag = cis.getTag();
		
		NBTTagCompound dis = tag.getCompound("display");
		if ( dis == null )
			return "";
		
		return dis.getString("Name");
	}*/
	
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

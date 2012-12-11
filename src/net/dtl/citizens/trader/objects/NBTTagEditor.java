package net.dtl.citizens.trader.objects;

import java.util.List;

import net.minecraft.server.v1_4_5.NBTTagCompound;
import net.minecraft.server.v1_4_5.NBTTagList;
import net.minecraft.server.v1_4_5.NBTTagString;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class NBTTagEditor {
	
	public static void removeDescriptions(Inventory inventory)
	{		
		for ( ItemStack item : inventory.getContents() )
		{
			if ( item != null )
			{
				net.minecraft.server.v1_4_5.ItemStack c = ((CraftItemStack)item).getHandle();
				NBTTagCompound tc = c.getTag();
				
				if ( tc != null )
				{
					if ( tc.hasKey("display") )
					{
						NBTTagCompound d = tc.getCompound("display");
						
						if ( d != null )
						{
							if ( d.hasKey("Lore") )
							{
								
								NBTTagList oldList = d.getList("Lore");
								NBTTagList newList = new NBTTagList();
								
								for ( int j = 0 ; j < oldList.size() ; ++j )
									if ( !oldList.get(j).getName().equals("dtl_trader") && !oldList.get(j).getName().isEmpty() )
										newList.add(oldList.get(j));
								
								if ( newList.size() == 0 )
									c.setTag(null);
								
								d.set("Lore", newList);
							}
						}
					}
				}
			}
		}		
	}
	
	public static void addDescription(CraftItemStack item, List<String> lore)
	{
		net.minecraft.server.v1_4_5.ItemStack c = item.getHandle();
		NBTTagCompound tag = c.getTag();

		if ( tag == null )
			tag = new NBTTagCompound();
		c.setTag(tag);
		
		if(!tag.hasKey("display")) 
			tag.set("display", new NBTTagCompound());
		
		NBTTagCompound d = tag.getCompound("display");
		
		if ( !d.hasKey("Lore") )
			d.set("Lore", new NBTTagList());
		
		NBTTagList list = d.getList("Lore");
			
		for ( String line : lore )
			list.add(new NBTTagString("dtl_trader", line.replace('^', '§')));

	}
	
	public static void removeDescription(CraftItemStack item)
	{
		net.minecraft.server.v1_4_5.ItemStack c = item.getHandle();
		NBTTagCompound tag = c.getTag();

		if ( tag == null )
			tag = new NBTTagCompound();
		c.setTag(tag);
		
		if(!tag.hasKey("display")) 
			tag.set("display", new NBTTagCompound());
		
		NBTTagCompound d = tag.getCompound("display");
		
		if ( !d.hasKey("Lore") )
			d.set("Lore", new NBTTagList());
		

		NBTTagList list = d.getList("Lore");
		NBTTagList newList = new NBTTagList();
		
		for ( int j = 0 ; j < list.size() ; ++j )
			if ( !list.get(j).getName().equals("dtl_trader") && !list.get(j).getName().isEmpty() )
				newList.add(list.get(j));
		
		d.set("Lore", newList);

	}
	public static String getName(ItemStack item)
	{
		return getName((CraftItemStack)item);
	}
	
	public static String getName(CraftItemStack item)
	{
		net.minecraft.server.v1_4_5.ItemStack c = item.getHandle();
		NBTTagCompound tag = c.getTag();

		if ( tag == null )
			return "";
		
		if(!tag.hasKey("display")) 
			return "";
		
		NBTTagCompound d = tag.getCompound("display");
		
		if ( !d.hasKey("Name") )
			return "";
		
		return d.getString("Name");
	}
	
	public static void setName(ItemStack item, String name)
	{
		setName((CraftItemStack)item, name);
	}
	public static void setName(CraftItemStack item, String name)
	{
		net.minecraft.server.v1_4_5.ItemStack cis = item.getHandle();
		NBTTagCompound tag = cis.getTag();

		if ( tag == null )
			tag = new NBTTagCompound();
		cis.setTag(tag);
		
		NBTTagCompound dis = tag.getCompound("display");
		if ( dis == null )
			dis = new NBTTagCompound();
		
		tag.set("display", dis);

		dis.setString("Name", name);
	}
}

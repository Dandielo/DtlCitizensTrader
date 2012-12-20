package net.dtl.citizens.trader.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.dtl.citizens.trader.CitizensTrader;
import org.bukkit.Bukkit;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class NBTTagEditor {
	
	public static void removeDescriptions(Inventory inventory)
	{		
		for ( ItemStack item : inventory.getContents() )
		{
			if ( item != null )
			{
				int size = 0;
				if ( CitizensTrader.getInstance().getItemConfig().getPriceLore("pbuy") != null )
				{
					size = CitizensTrader.getInstance().getItemConfig().getPriceLore("pbuy").size();
					
					Map<String, Object> map = item.serialize();
					ItemMeta meta = (ItemMeta) map.get("meta");
					
					if ( meta != null )
					{
						List<String> list = null;//new ArrayList<String>(meta.getLore()); 
						if ( meta.getLore().size() > size )
						{
							list = new ArrayList<String>(meta.getLore()); 
							for ( int i = 0 ; i < list.size() - size ; ++i )
								list.remove((meta.getLore().size()-1)-i);
						}
						meta.setLore(list);
					}
					
					map.remove("meta");
					if ( meta != null && meta.hasLore() )
						map.put("meta", meta);
					
					item.setItemMeta(ItemStack.deserialize(map).getItemMeta());
					
				}
			}
		}		
	}
	
	public static ItemStack addDescription(ItemStack item, List<String> lore)
	{
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item.getType());
		
		List<String> list = new ArrayList<String>();
		for ( String s : lore )
			list.add(s.replace('^', '§'));
		
		meta.setLore(list);
		Map<String, Object> map = item.serialize();
		
		map.put("meta", meta);
		
		item.setItemMeta(ItemStack.deserialize(map).getItemMeta());
		
		return ItemStack.deserialize(map);
	
	}
	
	public static void removeDescriptionPlayer(ItemStack item, int size)
	{
		if ( item.hasItemMeta() )
		{
			ItemMeta meta = item.getItemMeta();
			if ( meta.hasLore() )
			{
				List<String> list = null;//new ArrayList<String>(meta.getLore()); 
				if ( meta.getLore().size() > size )
				{
					list = new ArrayList<String>(meta.getLore()); 
					for ( int i = 0 ; i < list.size() - size ; ++i )
						list.remove((meta.getLore().size()-1)-i);
					
				}
				System.out.print(list);
				meta.setLore(list);
			}
		}
	
	}
	
	public static void removeDescription(ItemStack item)
	{
		if ( item.hasItemMeta() )
		{
			ItemMeta meta =  item.getItemMeta();
			if ( meta.hasLore() )
			{
				meta.setLore(null);
			}
		}
	
	}
	
	public static String getName(ItemStack item)
	{		
		String name = "";
		if ( item.hasItemMeta() )
			name = item.getItemMeta().getDisplayName();

		return name;
	}
	
	public static void setName(ItemStack item, String name)
	{
		/*WTH is going on?! why do i need to serialize it?! GUYS FOCUS!*/
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item.getType());
		meta.setDisplayName(name);
		
		Map<String, Object> map = item.serialize();
		map.put("meta", meta);
		
		item.setItemMeta(ItemStack.deserialize(map).getItemMeta());
	}
}

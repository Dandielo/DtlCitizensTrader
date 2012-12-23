package net.dtl.citizens.trader.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dtl.citizens.trader.CitizensTrader;
import org.bukkit.Bukkit;

import org.bukkit.enchantments.Enchantment;
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
				List<String> lore = CitizensTrader.getInstance().getItemConfig().getPriceLore("pbuy");
				if ( lore != null )
				{
					size = lore.size();
					
					Map<String, Object> map = item.serialize();
					ItemMeta meta = (ItemMeta) map.get("meta");
					
					if ( meta != null )
					{
						List<String> list = null;//new ArrayList<String>(meta.getLore()); 
						if ( meta.getLore() != null && meta.getLore().size() >= size )
						{
							list = new ArrayList<String>(meta.getLore()); 
							int s = list.size();
							
							
							for ( int i = 0 ; i + ( s - size /*last strings*/) < s ; ++i )
							{
								//TODO Create global matches
								String m = lore.get((size-1) - i);
								m = m.replace("^", "[\\^|§]");
								m = m.replace("{stack}", "\\d");
								m = m.replace("{unit}", "\\d");
								
								if ( Pattern.matches(m, list.get((s-1)-i) ) )
									list.remove((s-1)-i);
							}
						}
						meta.setLore(list);
					}
					
					map.remove("meta");
					if ( meta != null )
						map.put("meta", meta);
					
					item.setItemMeta(ItemStack.deserialize(map).getItemMeta());
					
				}
			}
		}		
	}
	
	public static ItemStack addDescription(ItemStack item, List<String> lore)
	{
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item.getType());
		Map<Enchantment, Integer> ench = item.getEnchantments();
		
		List<String> list = new ArrayList<String>();
		for ( String s : lore )
			list.add(s.replace('^', '§'));
		
		meta.setLore(list);
		meta.setDisplayName(item.getItemMeta().getDisplayName());
		Map<String, Object> map = item.serialize();
		
		map.put("meta", meta);
		
		item.setItemMeta(ItemStack.deserialize(map).getItemMeta());
		item.addUnsafeEnchantments(ench);
		
		return ItemStack.deserialize(map);
	
	}
	
	
	public static void removeDescription(ItemStack item)
	{
		Map<String, Object> map = item.serialize();
		if ( map.containsKey("meta") )
		{
			ItemMeta meta = (ItemMeta) map.get("meta");
			meta.setLore(null);
			
			item.setItemMeta(ItemStack.deserialize(map).getItemMeta());
		}
	
	}
	
	public static String getName(ItemStack item)
	{		
		String name = "";
		if ( item.hasItemMeta() )
			name = item.getItemMeta().getDisplayName();

		return name == null ? "" : name;
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

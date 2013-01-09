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
		int s = 0;
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
						if ( meta.getLore() != null && meta.getLore().size() > size )
						{
							list = new ArrayList<String>(meta.getLore()); 
							int listSize = list.size();
							
							int removed = 0;
							//TODO fixed?
							for ( int i = 0 ; i < listSize ; ++i )
							{
								//TODO fixed?
								for ( int j = 0 ; j < size ; ++j )
								{
									String m = lore.get(j);
									m = m.replace("^", "[\\^|§]");
									m = m.replace("{stack}", "\\d");
									m = m.replace("{unit}", "\\d");
									
									if ( Pattern.matches(m, list.get(i-removed) ) )
									{
										list.remove(i-removed);
										++removed;
									}
								}
							}
						}
						if ( list != null && list.isEmpty() )
							meta.setLore(null);
						else
							meta.setLore(list);
					}
					
					map.remove("meta");
					if ( meta != null  )
						map.put("meta", meta);
					else 
						map.put("meta", Bukkit.getItemFactory().getItemMeta(item.getType()));
					
					item.setItemMeta(ItemStack.deserialize(map).getItemMeta());
					
					inventory.setItem(s, new ItemStack(item));
				}
			}
			++s;
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
		ItemMeta oldMeta = item.getItemMeta();
		
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item.getType());
		meta.setDisplayName(name);
		
		if ( oldMeta != null )
		{
			meta.setLore(oldMeta.getLore());
			for ( Map.Entry<Enchantment, Integer> e : oldMeta.getEnchants().entrySet() )
				meta.addEnchant(e.getKey(), e.getValue(), true);
		}
		
		Map<String, Object> map = item.serialize();
		map.put("meta", meta);
		
		item.setItemMeta(ItemStack.deserialize(map).getItemMeta());
	}
}

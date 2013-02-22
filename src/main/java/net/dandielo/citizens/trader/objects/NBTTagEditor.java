package net.dandielo.citizens.trader.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.dandielo.citizens.trader.CitizensTrader;

import org.bukkit.Bukkit;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class NBTTagEditor {
	
	public static void removeDescriptions(Inventory inventory)
	{		
	//	int s = 0;
		for ( ItemStack item : inventory.getContents() )
		{
			if ( item != null )
			{
				NBTTagEditor.removeDescription(item);
			/*	int size = 0;
				List<String> lore = CitizensTrader.getLocaleManager().lore("player-inventory");
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
							
							Iterator<String> it = list.iterator();
							while(it.hasNext())
							{
								String line = it.next();
								for ( int j = 0 ; j < lore.size() ; ++j )
								{
									String m = lore.get(j);
									m = m.replace("^", "[\\^|§]");
									m = m.replace("{stack}", "\\S{1,}");
									m = m.replace("{unit}", "\\S{1,}");

									if ( Pattern.matches(m, line) )
									{
										it.remove();
										j = lore.size();
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
					
					
				}*/
			}
		//	++s;
		}		
	}
	
	public static ItemStack addDescription(ItemStack item, List<String> lore)
	{
		if ( lore == null || lore.isEmpty() )
			return item;
		
		ItemMeta meta = item.getItemMeta();
		if ( meta == null )
			meta = Bukkit.getItemFactory().getItemMeta(item.getType());
		
		List<String> list = ( item.getItemMeta().getLore() != null ? item.getItemMeta().getLore() : new ArrayList<String>() );
		for ( String s : lore )
			list.add(s.replace('^', '§'));
		
		meta.setLore(list);
		item.setItemMeta(meta);
	/*	ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item.getType());
		Map<Enchantment, Integer> ench = item.getEnchantments();
		
		List<String> list = ( item.getItemMeta().getLore() != null ? item.getItemMeta().getLore() : new ArrayList<String>() );
		for ( String s : lore )
			list.add(s.replace('^', '§'));
		
		meta.setLore(list);
		meta.setDisplayName(item.getItemMeta().getDisplayName());
		Map<String, Object> map = item.serialize();
		
		if ( item.getType().equals(Material.ENCHANTED_BOOK) )
		{
			EnchantmentStorageMeta em = (EnchantmentStorageMeta) item.getItemMeta();
			if ( em.getStoredEnchants() != null )
				for ( Map.Entry<Enchantment, Integer> e : em.getStoredEnchants().entrySet() )
					((EnchantmentStorageMeta) meta).addStoredEnchant(e.getKey(), e.getValue(), true);
		}
		
		map.put("meta", meta);
		
		item.setItemMeta(ItemStack.deserialize(map).getItemMeta());
		item.addUnsafeEnchantments(ench);
		
		return ItemStack.deserialize(map);*/
		return item;
	}
	

	public static void removeDescription(ItemStack item)
	{
		if ( !item.hasItemMeta() )
			return;
		
		ItemMeta meta = item.getItemMeta();
		if ( !meta.hasLore() )
			return;
		
		List<String> list = meta.getLore();
		
		
		List<String> lore = CitizensTrader.getLocaleManager().lore("player-inventory");
		
		if ( list.size() > lore.size() )
		{
			Iterator<String> it = list.iterator();
			while(it.hasNext())
			{
				String line = it.next();
				for ( int j = 0 ; j < lore.size() ; ++j )
				{
					String m = lore.get(j);
					m = m.replace("^", "[\\^|§]");
					m = m.replace("{stack}", "\\S{1,}");
					m = m.replace("{unit}", "\\S{1,}");

					if ( Pattern.matches(m, line) )
					{
						it.remove();
						j = lore.size();
					}
				}
			}
		}
		
		if ( list.isEmpty() )
			meta.setLore(null);
		else
			meta.setLore(list);

		item.setItemMeta(meta);
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

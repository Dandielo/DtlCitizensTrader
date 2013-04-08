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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;


public class NBTTagEditor {
	
	public static void removeDescriptions(Inventory inventory)
	{		
		int s = 0;
		for ( ItemStack item : inventory.getContents() )
		{
			if ( item != null )
			{
				NBTTagEditor.removeDescription(item, "player-inventory");

				inventory.setItem(s, cleanItem(item));
			}
			++s;
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
		return item;
	}
	
	public static List<String> cleanLore(List<String> list)
	{
		if ( list == null ) return new ArrayList<String>();
		List<String> lore = CitizensTrader.getLocaleManager().lore("player-inventory");
		List<String> newList = new ArrayList<String>(list);
		if ( list.size() >= lore.size() )
		{
			Iterator<String> it = newList.iterator();
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
		return newList;
	}

	public static void removeDescription(ItemStack item)
	{
		if ( !item.hasItemMeta() )
			return;
		
		ItemMeta meta = item.getItemMeta();
		if ( !meta.hasLore() )
			return;
		
		List<String> list = meta.getLore();
		
		list.clear();
		
		if ( list.isEmpty() )
			meta.setLore(null);
		else
			meta.setLore(list);

		item.setItemMeta(meta);
	}
	
	public static void removeDescription(ItemStack item, String toRem)
	{
		if ( !item.hasItemMeta() )
			return;
		
		ItemMeta meta = item.getItemMeta();
		if ( !meta.hasLore() )
			return;
		
		List<String> list = meta.getLore();
		
		
		List<String> lore = CitizensTrader.getLocaleManager().lore(toRem);
		
		if ( list.size() >= lore.size() )
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
	
	public static ItemStack cleanItem(ItemStack item)
	{
		Map<String, Object> ser = item.serialize();
		if ( !item.hasItemMeta() )
			ser.remove("meta");
		else
		{
			ItemMeta meta = item.getItemMeta();
			if ( !(meta.hasLore() || meta.hasDisplayName() || meta.hasEnchants() || checkMeta(meta)) )
				ser.remove("meta");
		}

		return ItemStack.deserialize(ser);
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
		ItemMeta meta = item.getItemMeta();
		
		if ( meta == null )
			meta = Bukkit.getItemFactory().getItemMeta(item.getType());
	
		meta.setDisplayName(name);
		item.setItemMeta(meta);
	}
	
	public static boolean checkMeta(ItemMeta meta)
	{
		if ( meta instanceof LeatherArmorMeta )
			if ( ((LeatherArmorMeta) meta).getColor() != null )
				return true;
		if ( meta instanceof FireworkMeta )
			return true;
		if ( meta instanceof EnchantmentStorageMeta )
			return true;
		if ( meta instanceof BookMeta )
			return true;
		if ( meta != null && meta instanceof ItemMeta && !meta.getClass().getSimpleName().equals("CraftMetaItem") )
			return true;
		return false;
	}
}

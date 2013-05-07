package net.dandielo.citizens.trader.objects;

import java.lang.reflect.Field;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NBTTools {

	public static boolean checkItem(ItemStack item)
	{
		// Get the specific craftItem 
		CraftItemStack cItem = CraftItemStack.asCraftCopy(new ItemStack(0));

		try 
		{
			// Get the Minecraft Vanila Item 
			Field handle = cItem.getClass().getDeclaredField("handle");
			// Set it accessible 
			handle.setAccessible(true);
			// Save the reference
			net.minecraft.server.v1_5_R3.ItemStack vItem = (net.minecraft.server.v1_5_R3.ItemStack) handle.get(cItem);
   
			return vItem.getTag() != null;
		} catch (Exception e) {
			e.printStackTrace();
		}
        return false;
	}
}

package net.dtl.trader;

import org.bukkit.inventory.ItemStack;

public class TraderItem {
	private int itemID;
	private int itemData;
	private int money;
	private int amout;
	
	public TraderItem(String list) {
		String[] data = list.split(" ");
		if ( data[0].contains(":") ) {
			String[] item = data[0].split(":");
			itemID = Integer.parseInt(item[0]);
			itemData = Integer.parseInt(item[1]);
		} else
			itemID = Integer.parseInt(data[0]);
		money = Integer.parseInt(data[1]);
		amout = Integer.parseInt(data[2]);
	}
	public int getCost() {
		return money;
	}
	public int getAmout() {
		return amout;
	}
	public int getID() {
		return itemID;
	}
	public int getData() {
		return itemData;
	}
	public ItemStack getItemStack() {
		return new ItemStack(itemID, amout, (short) 0, (byte) itemData);
	}
}

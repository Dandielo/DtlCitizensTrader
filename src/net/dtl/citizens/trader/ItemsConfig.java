package net.dtl.citizens.trader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemsConfig {	
	//check if we need to disable the plugin
	private boolean disablePlugin = false;
	
	//locale settings
	private String localeFile;
	private String localeFilePath;
	
	private Map<String, ItemStack> buttons;
	
	//navigation settings
	private ItemStack sellTab;
	private ItemStack buyTab;
	private ItemStack priceManaging;
	private ItemStack buyLimit;
	private ItemStack globalLimit;
	private ItemStack playerLimit;
	private ItemStack returnItem;
	private ItemStack amountsReturn;
	
	//general settings
	private long rclickInterval;
	private ItemStack manageWand;
	private ItemStack settingsWand;

	//functions
	public ItemsConfig(ConfigurationSection config) {
		ConfigurationSection traderSection = config.getConfigurationSection("trader");
		
		localeFile = config.getString("locale.file","locale.eng");
		localeFilePath = config.getString("locale.path","");
		
		manageWand = convertStringData(config.getString("manage","280"), "", null);
		settingsWand = convertStringData(config.getString("settings","340"), "", null);

	//	System.out.print("sth else 2");
		buttons = new HashMap<String, ItemStack>();
		
		for ( String key : traderSection.getConfigurationSection("inventory-navigation").getKeys(false) )
		{
		//	System.out.print(key+" " +traderSection.getString(buildPath("inventory-navigation", key, "item")));
			buttons.put(key, convertStringData( traderSection.getString(buildPath("inventory-navigation", key, "item")),
					traderSection.getString(buildPath("inventory-navigation", key, "name"), "") ,
					traderSection.getStringList(buildPath("inventory-navigation", key, "lore")) ));
		}
		
		/*
		
		this.buyTab = convertStringData(traderSection.getString("inventory-navigation.sell-tab","35:3"), 
				ChatColor.RESET + "Buy stock", 
				new String[] {ChatColor.GRAY + "Here you can sell you'r things"});
		
		this.sellTab = convertStringData(traderSection.getString("inventory-navigation.buy-tab","35:5"), 
				ChatColor.RESET + "Sell stock", 
				new String[] {ChatColor.GRAY + "Here you can buy things"});
		
		this.priceManaging = convertStringData(traderSection.getString("inventory-navigation.manage-price","35:15"),
				ChatColor.RESET + "Price managing", 
				new String[] {ChatColor.GRAY + "Here you manage prices"});
		
		this.buyLimit = convertStringData(traderSection.getString("inventory-navigation.manage-buy-limit","35:12"),
				ChatColor.RESET + "Buy limit", 
				new String[] {	ChatColor.GRAY + "Here you set how many items you want",
								ChatColor.GRAY + "to buy from other players"});
		
		this.globalLimit = convertStringData(traderSection.getString("inventory-navigation.manage-global-limit","35:11"), 
				ChatColor.RESET + "Global limit managing", 
				new String[] {ChatColor.GRAY + "Here you manage global limits"});
		
		this.playerLimit = convertStringData(traderSection.getString("inventory-navigation.manage-player-limit","35:12"), 
				ChatColor.RESET + "Player limit managing", 
				new String[] {ChatColor.GRAY + "Here you manage player limits"});
		
		this.returnItem = convertStringData(traderSection.getString("inventory-navigation.return","35"), 
				ChatColor.RESET + "Return", 
				new String[] {ChatColor.GRAY + "Returns to stock managing mode"});
		
		this.amountsReturn = convertStringData(traderSection.getString("inventory-navigation.amounts-return","35:14"), 
				ChatColor.RESET + "Return", 
				new String[] {ChatColor.GRAY + "Get back to look on other items"});*/
	}
	
	public boolean disablePlugin() {
		return this.disablePlugin;
	}
	
	public ItemStack initializeItemWithName(CraftItemStack cis, String name, List<String> lore)
	{
		//CraftItemStack cis = new CraftItemStack(item);
		net.minecraft.server.ItemStack mis = cis.getHandle();
		
		NBTTagCompound c = mis.getTag(); 
		if ( c == null )
			c = new NBTTagCompound();
		mis.setTag(c);
		
		if(!c.hasKey("display")) {
			c.set("display", new NBTTagCompound());
		}
		 
		NBTTagCompound d = c.getCompound("display");
		 

		if ( !name.isEmpty() )
			d.set("Name", new NBTTagString("", name.replace('^', '§')));

		if(!d.hasKey("Lore")) {
		  d.set("Lore", new NBTTagList());
		}
		
		NBTTagList l = d.getList("Lore");
		
		
		if ( lore != null )
			for ( String str : lore )
			//	System.out.print(str);
				if ( !str.isEmpty() )
					l.add(new NBTTagString("", str.replace('^', '§')));
		 
		d.set("Lore", l);
		return cis;
	}
	
	public void reloadConfig()
	{
		ConfigurationSection traderSection = CitizensTrader.getInstance().getConfig().getConfigurationSection("trader");
		
		this.rclickInterval = traderSection.getLong("rclick-interval");
		this.localeFile = traderSection.getString("locale.file","locale.eng");
		this.localeFilePath = traderSection.getString("locale.path","");
		
		for ( String key : traderSection.getConfigurationSection("inventory-navigation").getKeys(false) )
		{
			buttons.put(key, convertStringData( traderSection.getString(buildPath("inventory-navigation", key, "item")),
					traderSection.getString(buildPath("inventory-navigation", key, "name"), ""), 
					traderSection.getStringList(buildPath("inventory-navigation", key, "lore"))));
		}
		/*
		this.buyTab = convertStringData(traderSection.getString("inventory-navigation.sell-tab","35:3"), 
				ChatColor.RESET + "Buy stock", 
				new String[] {ChatColor.GRAY + "Here you can sell you'r things"});
		
		this.sellTab = convertStringData(traderSection.getString("inventory-navigation.buy-tab","35:5"), 
				ChatColor.RESET + "Sell stock", 
				new String[] {ChatColor.GRAY + "Here you can buy things"});
		
		this.priceManaging = convertStringData(traderSection.getString("inventory-navigation.manage-price","35:15"),
				ChatColor.RESET + "Price managing", 
				new String[] {ChatColor.GRAY + "Here you manage prices"});
		
		this.buyLimit = convertStringData(traderSection.getString("inventory-navigation.manage-buy-limit","35:12"),
				ChatColor.RESET + "Buy limit", 
				new String[] {	ChatColor.GRAY + "Here you set how many items you want",
								ChatColor.GRAY + "to buy from other players"});
		
		this.globalLimit = convertStringData(traderSection.getString("inventory-navigation.manage-global-limit","35:11"), 
				ChatColor.RESET + "Global limit managing", 
				new String[] {ChatColor.GRAY + "Here you manage global limits"});
		
		this.playerLimit = convertStringData(traderSection.getString("inventory-navigation.manage-player-limit","35:12"), 
				ChatColor.RESET + "Player limit managing", 
				new String[] {ChatColor.GRAY + "Here you manage player limits"});
		
		this.returnItem = convertStringData(traderSection.getString("inventory-navigation.return","35"), 
				ChatColor.RESET + "Return", 
				new String[] {ChatColor.GRAY + "Returns to stock managing mode"});
		
		this.amountsReturn = convertStringData(traderSection.getString("inventory-navigation.amounts-return","35:14"), 
				ChatColor.RESET + "Return", 
				new String[] {ChatColor.GRAY + "Get back to look on other items"});
	}*/
	}
	
	protected ItemStack convertStringData(String itemDataString, String name, List<String> lore)
	{
		//split the possible id/data value
		String[] itemData = itemDataString.split(":");
		//set the temporary id/data variables
		int id = 0;
		byte data = 0;

//		System.out.print(itemDataString + " " + name);
		//try to get the id, data
		try
		{
			if ( itemData.length > 0 )
				id = Integer.parseInt(itemData[0]);
			if ( itemData.length > 1 )
				data = Byte.parseByte(itemData[1]);
		} 
		catch( NumberFormatException e )
		{
			CitizensTrader.severe("Wrong number format in config file!");
			CitizensTrader.severe("Plugin will be disabled!");
			CitizensTrader.info("Error information");
			e.printStackTrace();
			disablePlugin = true;
			return null;
		}
		//		System.out.print("sth else");
		return initializeItemWithName(new CraftItemStack(id,1,(short) 0,data), name, lore);//new ItemStack(id,1,(short) 0,data);
	}
	
	public String getLocaleFile() {
		return localeFile;
	}
	
	public String getLocaleFilePath() {
		return localeFilePath;
	}
	
	public long getInterval() {
		return rclickInterval;
	}
	
	public ItemStack getManageWand() {
		return manageWand;
	}
	public ItemStack getSettingsWand() {
		return settingsWand;
	}
	
	public ItemStack getItemManagement(int item)
	{
		switch( item )
		{
		case 0:
			return buttons.get("sell-tab");
		case 1:
			return buttons.get("buy-tab");
		case 2: 
			return buttons.get("manage-price");
		case 3: 
			return buttons.get("manage-buy-limit");
		case 4:
			return buttons.get("manage-global-limit");
		case 5:
			return buttons.get("manage-player-limit");
		case 6: 
			return buttons.get("return");
		case 7:
			return buttons.get("amounts-return");
		default: 
			return new ItemStack(0);
		}
	}
	
	
	public static String buildPath(String... path) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		char separator = '.'; //permissions.options().pathSeparator();

		for ( String node : path ) 
		{
			if ( !first ) 
			{
				builder.append(separator);
			}

			builder.append(node);

			first = false;
		}

		return builder.toString();
	}
}

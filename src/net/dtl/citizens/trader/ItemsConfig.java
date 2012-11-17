package net.dtl.citizens.trader;

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
		
		manageWand = convertStringData(config.getString("manage","280"), localeFile, null);
		settingsWand = convertStringData(config.getString("settings","340"), localeFile, null);
		
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
	}
	
	public boolean disablePlugin() {
		return this.disablePlugin;
	}
	
	public ItemStack initializeItemWithName(CraftItemStack cis, String name, String[] lore)
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
		 
		if(!d.hasKey("Lore")) {
		  d.set("Lore", new NBTTagList());
		}

		d.set("Name", new NBTTagString("", name));
		
		NBTTagList l = d.getList("Lore");
		 
		if ( lore != null )
			for ( String str : lore )
				l.add(new NBTTagString("", str));
		 
		d.set("Lore", l);
		return cis;
	}
	
	public void reloadConfig()
	{
		ConfigurationSection traderSection = CitizensTrader.getInstance().getConfig().getConfigurationSection("trader");
		
		this.rclickInterval = traderSection.getLong("rclick-interval");
		this.localeFile = traderSection.getString("locale.file","locale.eng");
		this.localeFilePath = traderSection.getString("locale.path","");
		this.manageWand = convertStringData(traderSection.getString("manager-mode-toggle","280"), localeFile, null);
		this.buyTab = convertStringData(traderSection.getString("inventory-navigation.sell-tab","35:5"), localeFile, null);
		this.sellTab = convertStringData(traderSection.getString("inventory-navigation.buy-tab","35:3"), localeFile, null);
		this.priceManaging = convertStringData(traderSection.getString("inventory-navigation.manage-price","35:15"), localeFile, null);
		this.buyLimit = convertStringData(traderSection.getString("inventory-navigation.manage-buy-limit","35:12"), localeFile, null);
		this.globalLimit = convertStringData(traderSection.getString("inventory-navigation.manage-global-limit","35:11"), localeFile, null);
		this.playerLimit = convertStringData(traderSection.getString("inventory-navigation.manage-player-limit","35:12"), localeFile, null);
		this.returnItem = convertStringData(traderSection.getString("inventory-navigation.return","35"), localeFile, null);
		this.amountsReturn = convertStringData(traderSection.getString("inventory-navigation.amounts-return","35:14"), localeFile, null);
	}
	
	protected ItemStack convertStringData(String itemDataString, String name, String[] lore)
	{
		//split the possible id/data value
		String[] itemData = itemDataString.split(":");
		//set the temporary id/data variables
		int id = 0;
		byte data = 0;
		
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
			return sellTab;
		case 1:
			return buyTab;
		case 2: 
			return priceManaging;
		case 3: 
			return buyLimit;
		case 4:
			return globalLimit;
		case 5:
			return playerLimit;
		case 6: 
			return returnItem;
		case 7:
			return amountsReturn;
		default: 
			return new ItemStack(0);
		}
	}
}

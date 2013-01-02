package net.dtl.citizens.trader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.dtl.citizens.trader.objects.BankItem;
import net.dtl.citizens.trader.objects.NBTTagEditor;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ItemsConfig {	
	//check if we need to disable the plugin
	private boolean disablePlugin = false;
	
	//locale settings
	private String localeFile;
	private String localeFilePath;
	
	private Map<String, ItemStack> buttons;
	private Map<String, List<String>> pricesLore;
	
	//general settings
	private long rclickInterval;
	private ItemStack manageWand;
	private ItemStack settingsWand;
	
	//bank settings
	private ItemStack exchangeItem;

	//functions
	public ItemsConfig(ConfigurationSection config) {
		ConfigurationSection traderSection = config.getConfigurationSection("trader");
		
		localeFile = config.getString("locale.file","locale.eng");
		localeFilePath = config.getString("locale.path","");
		
		manageWand = convertStringData(config.getString("manage","280"), "", null);
		settingsWand = convertStringData(config.getString("settings","340"), "", null);

		buttons = new HashMap<String, ItemStack>();
		
		pricesLore = new HashMap<String,List<String>>();
		
		pricesLore.put("pbuy", traderSection.getConfigurationSection("prices-lore").getStringList("player-inventory"));
		pricesLore.put("sell", traderSection.getConfigurationSection("prices-lore").getStringList("trader-inventory-sell"));
		pricesLore.put("buy", traderSection.getConfigurationSection("prices-lore").getStringList("trader-inventory-buy"));
		
		for ( String key : traderSection.getConfigurationSection("inventory-navigation").getKeys(false) )
		{
		//	System.out.print(key+" " +traderSection.getString(buildPath("inventory-navigation", key, "item")));
			buttons.put(key, convertStringData( traderSection.getString(buildPath("inventory-navigation", key, "item")),
					traderSection.getString(buildPath("inventory-navigation", key, "name"), "") ,
					traderSection.getStringList(buildPath("inventory-navigation", key, "lore")) ));
		}
		
		exchangeItem = new BankItem(config.getString("bank.money-bank.exchange-item", "388")).getItemStack();
	}
	
	public boolean disablePlugin() {
		return this.disablePlugin;
	}
	
	public List<String> getPriceLore(String t)
	{
		return pricesLore.get(t);
	}
	
	public ItemStack initializeItemWithName(ItemStack itemStack, String name, List<String> lore)
	{
		NBTTagEditor.setName(itemStack, name.replace('^', '§'));
		if ( lore != null )
			NBTTagEditor.addDescription(itemStack, lore);
		
		
		return itemStack;
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
		return initializeItemWithName(new ItemStack(id,1,data), name, lore);//new ItemStack(id,1,(short) 0,data);
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

	public ItemStack getItemManagement(String stock) {
		return buttons.get(stock+"-tab");
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

	public ItemStack getExchangeItem() {
		return exchangeItem;
	}


}

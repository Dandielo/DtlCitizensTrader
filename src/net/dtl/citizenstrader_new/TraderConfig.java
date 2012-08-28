package net.dtl.citizenstrader_new;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class TraderConfig {	
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
	private ItemStack mmToggleItem;

	//functions
	public TraderConfig(ConfigurationSection config) {
		ConfigurationSection traderSection = config.getConfigurationSection("trader");
		
		this.rclickInterval = traderSection.getLong("rclick-interval");
		this.localeFile = traderSection.getString("locale.file","locale.eng");
		this.localeFilePath = traderSection.getString("locale.path","");
		this.mmToggleItem = convertStringData(traderSection.getString("manager-mode-toggle","280"));
		this.buyTab = convertStringData(traderSection.getString("inventory-navigation.sell-tab","35:5"));
		this.sellTab = convertStringData(traderSection.getString("inventory-navigation.buy-tab","35:3"));
		this.priceManaging = convertStringData(traderSection.getString("inventory-navigation.manage-price","35:15"));
		this.buyLimit = convertStringData(traderSection.getString("inventory-navigation.manage-buy-limit","35:12"));
		this.globalLimit = convertStringData(traderSection.getString("inventory-navigation.manage-global-limit","35:11"));
		this.playerLimit = convertStringData(traderSection.getString("inventory-navigation.manage-player-limit","35:12"));
		this.returnItem = convertStringData(traderSection.getString("inventory-navigation.return","35"));
		this.amountsReturn = convertStringData(traderSection.getString("inventory-navigation.amounts-return","35:14"));
	}
	
	public boolean disablePlugin() {
		return this.disablePlugin;
	}
	
	public void reloadConfig()
	{
		ConfigurationSection traderSection = CitizensTrader.plugin.getConfig().getConfigurationSection("trader");
		
		this.rclickInterval = traderSection.getLong("rclick-interval");
		this.localeFile = traderSection.getString("locale.file","locale.eng");
		this.localeFilePath = traderSection.getString("locale.path","");
		this.mmToggleItem = convertStringData(traderSection.getString("manager-mode-toggle","280"));
		this.buyTab = convertStringData(traderSection.getString("inventory-navigation.sell-tab","35:3"));
		this.sellTab = convertStringData(traderSection.getString("inventory-navigation.buy-tab","35:5"));
		this.priceManaging = convertStringData(traderSection.getString("inventory-navigation.manage-price","35:15"));
		this.buyLimit = convertStringData(traderSection.getString("inventory-navigation.manage-buy-limit","35:12"));
		this.globalLimit = convertStringData(traderSection.getString("inventory-navigation.manage-global-limit","35:11"));
		this.playerLimit = convertStringData(traderSection.getString("inventory-navigation.manage-player-limit","35:12"));
		this.returnItem = convertStringData(traderSection.getString("inventory-navigation.return","35"));
		this.amountsReturn = convertStringData(traderSection.getString("inventory-navigation.amounts-return","35:14"));
	}
	
	protected ItemStack convertStringData(String itemDataString)
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
			CitizensTrader.plugin.logger.severe("Wrong number format in configFile!");
			CitizensTrader.plugin.logger.severe("Plugin will be disabled!");
			CitizensTrader.plugin.logger.info("Error information");
			e.printStackTrace();
			disablePlugin = true;
			return null;
		}
				
		return new ItemStack(id,1,(short) 0,data);
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
	
	public ItemStack getMMToggleItem() {
		return mmToggleItem;
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

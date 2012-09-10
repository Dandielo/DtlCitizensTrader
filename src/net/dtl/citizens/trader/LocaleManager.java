package net.dtl.citizens.trader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocaleManager {
	//config
	protected ConfigurationSection config;

	//yaml path separator
	protected final static char PATH_SEPARATOR = '/';
	
	//locale file configuration
	protected FileConfiguration locale;
	protected File localeFile;
	
	//localeCache
	protected Map<String,String> localeCache;
	
	public LocaleManager() {
		//Loca config
		config = CitizensTrader.getInstance().getConfig();
		
		//initialize cache holder
		this.localeCache = new HashMap<String, String>();
		
		//initialize the locale
		initialize();
	}

	public void initialize() {
		String localeFilename = config.getString("locale.file");

		// Default settings
		if ( localeFilename == null ) 
		{
			//creating a config default
			localeFilename = "locale.eng";
			config.set("locale.file", "locale.eng");
			
			//saving the new config
			CitizensTrader.getInstance().saveConfig();
		}
		
		//getting the base dir
		String baseDir = config.getString("locale.basedir", "plugins/DtlCitizensTrader/locale" );

		if ( baseDir.contains("\\") && !"\\".equals(File.separator) ) 
		{
			baseDir = baseDir.replace("\\", File.separator);
		}

		File baseDirectory = new File(baseDir);
		if ( !baseDirectory.exists() ) 
		{
			baseDirectory.mkdirs();
		}

		this.localeFile = new File(baseDir, localeFilename);

		this.reload();

		if ( !localeFile.exists() )
		{
			
			try 
			{
				
				localeFile.createNewFile();

				// Load default permissions
				locale.set("locale", "eng");
				
				Map<String, String> localeStrings = new HashMap<String,String>();

				//all
				localeStrings.put("no-permissions", "^cYou don't have required permissions"); 
				localeStrings.put("no-permissions-type", "^cYou can't use this trader type"); 
				localeStrings.put("no-permissions-creative", "^cYou can't be in creative mode"); 
				
				//commands
				localeStrings.put("invalid-args", "^cWrong arguments wahere supplied");
				localeStrings.put("invalid-wallet", "^cThis wallet type is invalid");
				localeStrings.put("invalid-wallet-perm", "^cYou don't got permissions to use this wallet");
				localeStrings.put("invalid-wallet-bank", "^cYou can't use this bank account");
				localeStrings.put("invalid-ttype", "^cThis trader type is invalid");
				localeStrings.put("invalid-ttype-perm", "^cYou don't got permissions to use this trader type");
				localeStrings.put("invalid-entity", "^cYou can't use this entity as a trader");

				localeStrings.put("amount-unavailable", "^cThis trader cannot give you that amount");
				localeStrings.put("no-trader-selected", "^cYou haven't selected any trader");
				localeStrings.put("missing-args", "^cYou are missing arguments");
				localeStrings.put("no-defaults", "^cNo defaults found while creating a trader");
				
				localeStrings.put("wallet-changed", "^cYou changed the wallet type to ^b{wallet}");
				localeStrings.put("type-changed", "^cType changed to {type}, reset the manager mode");
				localeStrings.put("trader-created", "^cTrader was created at your position");
				localeStrings.put("owner-changed", "^cNew owner of this trader is ^b{player}");

				localeStrings.put("list-header", "^cTrader stock list ^b {curp}/{allp}");
				localeStrings.put("list-message", "{nr}. ^c{name} ({id}:{data}) ^f{amount} {price} ^e[{slot}]");
				localeStrings.put("balance-message", "^cTraders balance: ^b{balance}");
				localeStrings.put("withdraw-message", "^cYou withdrawed ^b{amount}");
				localeStrings.put("deposit-message", "^cYou deposited ^b{amount}");
				localeStrings.put("owner-message", "^b{player}^c is the owner of this trader");
				localeStrings.put("wallet-message", "^cCurrent wallet type is: ^b{wallet}^c | ^6{account}");
				localeStrings.put("type-message", "^cCurrent type is: ^b{type}");
				
				localeStrings.put("command-template", "^c/trader ^6{command} ^f{args}");
				
				//trader (simple)
				localeStrings.put("buy-message", "^6You bought {amount} for {price}");
				localeStrings.put("sell-message", "^6You sold {amount} for {price}");
				localeStrings.put("transaction-falied", "^6Transaction falied");
				localeStrings.put("price-message", "^6The items price is {price}");
				localeStrings.put("click-to-continue", "^6Now click to {transaction} it");
				localeStrings.put("amount-exception", "^6You can't sell anything when selecting amounts");
				
				//trader (manager)
				localeStrings.put("managing-changed-message", "^cSwitched to ^6{managing} ^cmanaging");
				localeStrings.put("show-limit", "^6{type} ^climit: ^b{limit}");
				localeStrings.put("change-limit", "^6{type} ^climit changed: ^b{limit}");
				localeStrings.put("show-timeout", "^6{type} ^ctimeout: ^b{timeout}");
				localeStrings.put("change-timeout", "^6{type} ^ctimeout changed: ^b{timeout}");
				localeStrings.put("show-price", "^cPrice: ^b{price}");
				localeStrings.put("change-price", "^cPrice changed: ^b{price}");
				localeStrings.put("stackprice-toggle", "^cStackprice ^b{value}");
				localeStrings.put("item-selected", "^cA stock item is selected");
				localeStrings.put("item-removed", "^cThis item was removed from stock");
				localeStrings.put("invalid-item", "^cInvalid item ^6{reason}");
				
				//server trader (simple)
				
				//server trader (manager)
				
				//player trader (simple)
				localeStrings.put("item-sold", "^b{item} ^6was bought by ^b{player}");
				localeStrings.put("item-bought", "^b{item} ^6was sold to ^b{player}");
				
				//player trader (manager)
				localeStrings.put("item-removed-pt", "^cItem removed! You got ^b{amount} ^cback");
				localeStrings.put("already-in-stock", "^cThis item is alredy in stock");
				localeStrings.put("item-added", "^cItem was added to the traders stock");
				localeStrings.put("amount-add-help", "^cShift r.click the item to add it to the stock");
				localeStrings.put("amount-added", "^cThe amount was added to the trader stock");
				localeStrings.put("not-in-stock", "^cItem wasn't found in stock");
				localeStrings.put("show-limit-pt", "^6{type} ^climit: ^b{amount}^c/^e{limit}");
				localeStrings.put("item-taken", "^cYou got ^b{amount} ^cfrom the stock");
				
				localeStrings.put("reload-config", "^eConfiguration was reloaded");
				
				//money banker
				localeStrings.put("mbanker-lost-item", "^6You have deposited ^b{item} ^6to the bank");
				localeStrings.put("mbanker-got-item", "^6You got ^b{item} ^6from the bank");
				localeStrings.put("mbanker-lost-money", "^6You have paid ^b{money} ^6for this item");
				localeStrings.put("mbanker-got-money", "^6You got ^b{money} ^6for this item");
				localeStrings.put("mbanker-wrong-item", "^6This item is not accepted");
				
				//banker
				localeStrings.put("bank-deposit-fee", "^6Deposit fee: ^b{fee}");
				localeStrings.put("bank-withdraw-fee", "^6Withdraw fee: ^b{fee}");
				localeStrings.put("bank-tab-price", "^6Tab price: ^b{price}");
				localeStrings.put("bank-tab-bought", "^6You bought a tab");
				localeStrings.put("select-tab-item", "^6Select a tab item");
				localeStrings.put("tab-item-selected", "^6Tab item: {name}");
				localeStrings.put("switch-tab", "^6Current tab: ^b{name}");

				localeStrings.put("bank-no-money", "^6Not enough money");
				localeStrings.put("bank-account-no-money", "^6Not enough money to create a account!");
				
				//set the new locale file
				locale.set("strings", localeStrings);

				//save and reload
				this.save();
				this.reload();
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	//build yaml path
	public static String buildPath(String... path) 
	{
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		char separator = PATH_SEPARATOR; 

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

	public void reload() {
		locale = new YamlConfiguration();
		locale.options().pathSeparator(PATH_SEPARATOR);
				
		try 
		{
			locale.load(localeFile);
			localeCache.clear();
			
			for ( String key : locale.getConfigurationSection("strings").getKeys(false) )
			{
				localeCache.put(key, locale.getString(buildPath("strings",key)).replace('^', '§') );
			}
			
		} 
		catch (FileNotFoundException e)
		{
			CitizensTrader.severe(e.getMessage());
		} 
		catch (Throwable e)
		{
			throw new IllegalStateException("Error loading permissions file", e);
		}
	}

	public String getLocaleString(String messageType)
	{
		if ( localeCache.containsKey(messageType) )
			return localeCache.get(messageType);
		
		return ChatColor.RED + "ERROR! Reset the locale file!";
	}
	
	public void save() {
		try 
		{
			this.locale.save(localeFile);
		} 
		catch (IOException e) 
		{
			CitizensTrader.severe("Error during saving permissions file: " + e.getMessage());
		}
	}

}

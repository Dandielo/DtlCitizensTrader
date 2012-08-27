package net.dtl.citizenstrader_new;

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
	protected final static char PATH_SEPARATOR = '/';
	public FileConfiguration locale;
	protected File localeFile;
	
	protected Map<String,String> localeCache;
	
	protected ConfigurationSection config;

	public LocaleManager(ConfigurationSection config) {
		this.config = config;
		this.localeCache = new HashMap<String,String>();
		initialize();
	}

	public void initialize() {
		// TODO check configuration
		String localeFilename = config.getString("trader.locale.file");

		// Default settings
		if ( localeFilename == null ) 
		{
			localeFilename = "locale.eng";
			config.set("trader.locale.file", "locale.eng");
		}
		
		CitizensTrader.plugin.saveConfig();

		String baseDir = config.getString("locale.basedir", "plugins/DtlCitizensTrader/locale" );// "plugins/PermissionsEx");

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
				localeStrings.put("type-changed", "^cType changed to {type}, reset the trader manager mode");
				localeStrings.put("trader-created", "^cTrader was created at your position");
				localeStrings.put("owner-changed", "^cNew owner of this trader is ^b{player}");

				localeStrings.put("list-header", "^cTrader stock list ^b {curp}/{allp}");
				localeStrings.put("list-message", "- ^c{name} ({id}:{data}) ^f{amount} {price} ^e[{slot}]");
				localeStrings.put("balance-message", "^cTraders balance: ^b{balance}");
				localeStrings.put("withdraw-message", "^cYou withdrawed ^b{amount}");
				localeStrings.put("deposit-message", "^cYou deposited ^b{amount}");
				localeStrings.put("owner-message", "^b{player}^c is the owner of this trader");
				localeStrings.put("wallet-message", "^cCurrent wallet type is: ^b{wallet}");
				localeStrings.put("type-message", "^cCurrent trader type is: ^b{type}");
				
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
				
				//player trader (manager)
				localeStrings.put("item-removed-pt", "^cItem removed! You got ^b{amount} ^cback");
				localeStrings.put("already-in-stock", "^cThis item is alredy in stock");
				localeStrings.put("item-added", "^cItem was added to the traders stock");
				localeStrings.put("amount-add-help", "^cShift r.click the item to add it to the stock");
				localeStrings.put("amount-added", "^cThe amount was added to the trader stock");
				localeStrings.put("not-in-stock", "^cItem wasn't founf in stock");
				localeStrings.put("show-limit-pt", "^6{type} ^climit: ^b{amount}^c/^e{limit}");
				localeStrings.put("item-taken", "^cYou got ^b{amount} ^cfrom the stock");
				
				this.localeCache = localeStrings;
				
				locale.set("strings", localeStrings);
			//	List<String> defaultPermissions = new LinkedList<String>();
				// Specify here default permissions
				// TODO chang this permission!
			//	defaultPermissions.add("modifyworld.*");

			//	locale.set("groups/default/permissions", defaultPermissions);

				this.save();
				this.reload();
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String buildPath(String... path) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		char separator = PATH_SEPARATOR; //permissions.options().pathSeparator();

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
		//	severe(e.getMessage());
		} 
		catch (Throwable e)
		{
			throw new IllegalStateException("Error loading permissions file", e);
		}
	}

	public String getMessage(String messageType)
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
			//severe("Error during saving permissions file: " + e.getMessage());
		}
	}

}

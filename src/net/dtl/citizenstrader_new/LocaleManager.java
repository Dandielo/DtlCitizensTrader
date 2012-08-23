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
		String permissionFilename = config.getString("backends.file.file");

		// Default settings
		if ( permissionFilename == null ) 
		{
			permissionFilename = "locale.eng";
			config.set("locale.file", "locale.eng");
		}

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

		this.localeFile = new File(baseDir, permissionFilename);

		this.reload();

		if ( !localeFile.exists() )
		{
			try 
			{
				localeFile.createNewFile();

				// Load default permissions
				locale.set("locale", "eng");
				
				Map<String, String> localeStrings = new HashMap<String,String>();

				localeStrings.put("no-permissions", ChatColor.RED + "You don't have required permissions");
				localeStrings.put("missing-args", ChatColor.RED + "You are missing arguments");
				localeStrings.put("invalid-wallet", ChatColor.RED + "This wallet type is invalid");
				localeStrings.put("invalid-wallet-perm", ChatColor.RED + "You don't got permissions to use this wallet");
				localeStrings.put("invalid-wallet-bank", ChatColor.RED + "You can't use this bank account");
				localeStrings.put("wallet-changed", ChatColor.RED + "You changed the wallet type to {wallet}");
				localeStrings.put("invalid-ttype", ChatColor.RED + "This trader type is invalid");
				localeStrings.put("invalid-ttype-perm", ChatColor.RED + "You don't got permissions to use this trader type");
				localeStrings.put("type-changed", ChatColor.RED + "Type changed to {type}, reset the trader manager mode");
				localeStrings.put("no-trader-selected", ChatColor.RED + "You haven't selected any trader");
				localeStrings.put("invalid-args", ChatColor.RED + "Wrong arguments wahere supplied");
				localeStrings.put("invalid-entity", ChatColor.RED + "You can't use this entity as a trader");
				localeStrings.put("no-defaults", ChatColor.RED + "No defaults found while creating a trader");
				localeStrings.put("trader-created", ChatColor.RED + "Trader was created at your position");
				localeStrings.put("amount-unavailable", ChatColor.RED + "This trader cannot give you that amount");
				localeStrings.put("list-header", ChatColor.RED + "Trader stock list " + ChatColor.AQUA + " {curp}/{allp}");
				localeStrings.put("list-message", "- " + ChatColor.RED + "{in} " + ChatColor.WHITE + " {a} {p} " + ChatColor.YELLOW + " [{s}]");
				localeStrings.put("balance-message", ChatColor.RED + "Traders balance: " + ChatColor.AQUA + "{balance}");
				localeStrings.put("withdraw-message", ChatColor.RED + "You withdrawed " + ChatColor.AQUA + "{amount}");
				localeStrings.put("deposit-message", ChatColor.RED + "You deposited " + ChatColor.AQUA + "{amount}");
				localeStrings.put("owner-changed", ChatColor.RED + "New owner of this trader is " + ChatColor.AQUA + "{player}");
				localeStrings.put("owner-message", ChatColor.AQUA + "{player}" + ChatColor.RED + " is the owner of this trader");
				localeStrings.put("buy-message", ChatColor.GOLD + "You bought {amount} for {price}");
				localeStrings.put("sell-message", ChatColor.GOLD + "You sold {amount} for {price}");
				localeStrings.put("transaction falied", ChatColor.GOLD + "Transaction falied");
				localeStrings.put("price-message", ChatColor.GOLD + "The items price is {price}");
				localeStrings.put("amount-exception", ChatColor.GOLD + "You can't sell anything when selecting amounts");
				localeStrings.put("click-to-continue", ChatColor.GOLD + "Now click to {transaction} it");
				
				this.localeCache = localeStrings;
				
				locale.set("strings", localeStrings);
			//	List<String> defaultPermissions = new LinkedList<String>();
				// Specify here default permissions
				// TODO chang this permission!
			//	defaultPermissions.add("modifyworld.*");

			//	locale.set("groups/default/permissions", defaultPermissions);

				this.save();
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
				localeCache.put(key, locale.getString(buildPath("strings",key)) );
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
		return "ERROR!";
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

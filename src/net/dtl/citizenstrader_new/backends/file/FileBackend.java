package net.dtl.citizenstrader_new.backends.file;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.dtl.citizenstrader_new.backends.Backend;
import net.dtl.citizenstrader_new.containers.BankAccount;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileBackend extends Backend {
	private final static char PATH_SEPARATOR = '/';
	
	protected boolean separateFiles;

	protected FileConfiguration accounts;
	protected File accountsFile;
	
	public FileBackend(ConfigurationSection config) {		
		String accountsFilename = config.getString("trader.bank.player-accounts.file");

		// Default settings
		if ( accountsFilename == null ) 
		{
			accountsFilename = "player_accounts.yml";
			config.set("trader.bank.player-accounts.file", "player_accounts.yml");
		}

		String baseDir = config.getString("trader.bank.player-accounts.path", "plugins/dtlCitizensTrader/bank" );// "plugins/PermissionsEx");

		if ( baseDir.contains("\\") && !"\\".equals(File.separator) ) 
		{
			baseDir = baseDir.replace("\\", File.separator);
		}

		File baseDirectory = new File(baseDir);
		if ( !baseDirectory.exists() ) 
		{
			baseDirectory.mkdirs();
		}

		this.accountsFile = new File(baseDir, accountsFilename);

		this.reload();

		if ( !accountsFile.exists() )
		{
			try 
			{
				accountsFile.createNewFile();

				// Load default permissions
				
				this.save();
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public void reload() {
		accounts = new YamlConfiguration();
		accounts.options().pathSeparator(PATH_SEPARATOR);
				
		try 
		{
			accounts.load(accountsFile);
		} 
		catch (FileNotFoundException e)
		{
		//	severe(e.getMessage());
		} 
		catch (Throwable e)
		{
			throw new IllegalStateException("Error loading warps file", e);
		}
	}

	public void save() {
		try 
		{
			this.accounts.save(accountsFile);
		} 
		catch (IOException e) 
		{
		//	severe("Error during saving warps file: " + e.getMessage());
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
	
	@Override
	public Map<String, BankAccount> getAccounts() {
		System.out.print("ad");
		this.reload();
		
		Map<String,BankAccount> accountList = new HashMap<String,BankAccount>();

		System.out.print("ad3");
		if ( !accounts.contains("accounts") )
			return accountList;
		System.out.print("ad2");
		if ( accounts.getConfigurationSection("accounts").getKeys(false) == null )
			return accountList;
		System.out.print("ad");
		
		for ( String accountName : accounts.getConfigurationSection("accounts").getKeys(false) ) {

			System.out.print("ac");
			BankAccount account = getAccount(accountName);
			if ( account != null )
				accountList.put(accountName, account);
		}

		return accountList;
	}
	/*
	public void addWarp(Warp warp, String warpName) {
		accounts.set(buildPath("warps",warpName,"owner"), warp.getOwner());
		accounts.set(buildPath("warps",warpName,"world"), warp.getLocation().getWorld().getName());
		accounts.set(buildPath("warps",warpName,"x"), warp.getLocation().getX());
		accounts.set(buildPath("warps",warpName,"y"), warp.getLocation().getY());
		accounts.set(buildPath("warps",warpName,"z"), warp.getLocation().getZ());
		accounts.set(buildPath("warps",warpName,"yaw"), warp.getLocation().getYaw());
		accounts.set(buildPath("warps",warpName,"pitch"), warp.getLocation().getPitch());
		
		this.save();
	}*/
	
/*	public void removeWarp(String warpName) {
		if ( accounts.contains(buildPath("warps",warpName)) ) {
			Map<String, Object> warpList = new HashMap<String, Object>();
			for ( String warp : accounts.getConfigurationSection("warps").getKeys(false) ) {
				if ( warp.equals(warpName) ) {
					continue;
				}
				warpList.put(warp, accounts.get(buildPath("warps", warp)));
			}
			accounts.set("warps", warpList);
		}
		this.save();
	}*/
	
	public FileBankAccount getAccount(String accountName) {
		return new FileBankAccount(accountName, accounts);
	}
	
}

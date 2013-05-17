package net.dandielo.citizens.trader.backends.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.dandielo.citizens.trader.DtlTraders.*;
import net.dandielo.citizens.trader.backends.Backend;
import net.dandielo.citizens.trader.objects.BankAccount;
import net.dandielo.citizens.trader.objects.BankItem;
import net.dandielo.citizens.trader.objects.BankTab;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileBackend extends Backend {
	private final static char PATH_SEPARATOR = '/';
	
	protected boolean separateFiles;

	protected FileConfiguration accounts;
	protected File accountsFile;
	
	public FileBackend(ConfigurationSection config, String accounts) {		
		super(SaveTrigger.ITEM);
		String accountsFilename = config.getString("bank." + accounts + ".file");

		// Default settings
		if ( accountsFilename == null ) 
		{
			accountsFilename = accounts + ".yml";
			config.set("bank." + accounts + ".file", accountsFilename);
		}

		String baseDir = config.getString("bank." + accounts + ".basedir", "plugins/DtlCitizensTrader/bank" );// "plugins/PermissionsEx");

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
				this.save();
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public void reload() 
	{
		accounts = new YamlConfiguration();
		accounts.options().pathSeparator(PATH_SEPARATOR);
				
		try 
		{
			accounts.load(accountsFile);
		} 
		catch (FileNotFoundException e)
		{
			severe(accountsFile.getName() + " not found!");
		} 
		catch (Throwable e)
		{
			throw new IllegalStateException("Error loading accounts", e);
		}
	}

	@Override
	public void save()
	{
		try 
		{
			accounts.save(accountsFile);
		} 
		catch (IOException e) 
		{
			severe("Error while saving accounts!");
		}
	}
	
	@Override
	public Map<String, BankAccount> getAccounts() 
	{
		reload();
		
		Map<String,BankAccount> accountList = new HashMap<String,BankAccount>();
		
		if ( !accounts.contains("accounts") )
			return accountList;
		
		if ( accounts.getConfigurationSection("accounts").getKeys(false) == null )
			return accountList;
		
		for ( String accountName : accounts.getConfigurationSection("accounts").getKeys(false) ) {

			FileBankAccount account = getFileAccount(accountName);
			if ( account != null )
				accountList.put(accountName, account.toPlayerAccount());
		}

		return accountList;
	}
	
	public FileBankAccount getFileAccount(String accountName) {
		return new FileBankAccount(accountName, accounts);
	}
	
	
	//Managing methods
	@Override
	public void addItem(String owner, int tab, BankItem item)
	{
		List<String> list = accounts.getStringList(buildPath("accounts", owner, "tabs", ""+tab, "content"));
		list.add(item.toString());
		
		accounts.set(buildPath("accounts", owner, "tabs", ""+tab, "content"), list);

		if ( trigger.itemSaving() )
			save();
	}

	@Override
	public void updateItem(String owner, int tab, BankItem oldItem, BankItem newItem)
	{
		removeItem(owner, tab, oldItem);
		addItem(owner, tab, newItem);
	}

	@Override
	public void removeItem(String owner, int tab, BankItem item)
	{
		List<String> list = accounts.getStringList(buildPath("accounts", owner, "tabs", ""+tab, "content"));
		list.remove(item.toString());
		
		accounts.set(buildPath("accounts", owner, "tabs", ""+tab, "content"), list);
		
		if ( trigger.itemSaving() )
			save();
	}

	@Override
	public void setTabSize(String owner, int tab, int tabSize) {
		accounts.set(buildPath("accounts", owner, "tabs", ""+tab, "tab-size"), tabSize);
		
		if ( trigger.tabSaving() )
			save();
	}
	
	@Override
	public void setBankTabItem(String owner, int tab, BankItem item) {
		accounts.set(buildPath("accounts", owner, "tabs", ""+tab, "tab-item"), item.toString());
		
		if ( trigger.tabSaving() )
			save();
	}
	
	@Override
	public void addBankTab(String owner, BankTab tab) {
		ConfigurationSection tabs = accounts.getConfigurationSection(buildPath("accounts", owner, "tabs"));
		
		
		tabs.set(buildPath(tab.getId()+"", "tab-item"), tab.getTabItem().toString());
		tabs.set(buildPath(tab.getId()+"", "tab-name"), tab.getName());
	//	tabs.set(buildPath(tab.toString(, "tab-size"), CitizensTrader.getInstance().getConfig().getConfigurationSection("bank").getInt("tab-size"));
		tabs.set(buildPath(tab.getId()+"", "content"), new String[0]);
		
		if ( trigger.tabSaving() )
			save();
	}
	
	@Override
	public BankAccount newAccount(String owner)
	{
	//	accounts.set(buildPath("accounts", owner, "available-tabs"), CitizensTrader.getInstance().getConfig().getConfigurationSection("bank").getInt("max-tabs"));
		accounts.set(buildPath("accounts", owner, "tabs", "0", "tab-item"), "35:0 a:1");
		accounts.set(buildPath("accounts", owner, "tabs", "0", "tab-name"), "First tab");
	//	accounts.set(buildPath("accounts", owner, "tabs", "tab1", "tab-size"), CitizensTrader.getInstance().getConfig().getConfigurationSection("bank").getInt("tab-size"));//buildPath("accounts", owner, "tabs", "tab1", "tab-size"), 1);
		accounts.set(buildPath("accounts", owner, "tabs", "0", "content"), new String[0]);
		
		if ( trigger.accountSaving() )
			save();
		
		return new FileBankAccount(owner, accounts);
	}
	
	@Override
	public void removeAccount(String owner)
	{
		ConfigurationSection accounts = this.accounts.getConfigurationSection(buildPath("accounts"));
		
		ConfigurationSection nAccounts = new YamlConfiguration();
		
		for ( String key : accounts.getKeys(false) )
		{
			if ( !key.equals(owner) )
				nAccounts.set(key, accounts.getConfigurationSection(key));
		}
		
		this.accounts.set(buildPath("accounts"), nAccounts);
	}
	/*@Override


	@Override
	public void addBankTab(String player, BankTabType tab) {
		ConfigurationSection tabs = accounts.getConfigurationSection(buildPath("accounts", player, "tabs"));
		
		
		tabs.set(buildPath(tab.toString(), "tab-item"), "35:0 a:1");
		tabs.set(buildPath(tab.toString(), "tab-name"), tab.toString());
		tabs.set(buildPath(tab.toString(), "tab-size"), CitizensTrader.getInstance().getConfig().getConfigurationSection("bank").getInt("tab-size"));
		tabs.set(buildPath(tab.toString(), "content"), new String[0]);
		
		//if ( saveTrigger.equals("item") )
		this.save();
	}
	
	*/
	
	public static String buildPath(String... path) 
	{
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

	
}

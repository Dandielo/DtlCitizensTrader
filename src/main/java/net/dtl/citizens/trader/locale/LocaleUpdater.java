package net.dtl.citizens.trader.locale;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocaleUpdater {
	private Map<LocaleEntry, String> cache;
	private Map<LocaleEntry, String> keywords;
	private Map<LocaleEntry, ItemLocale> lores;
	
	private static final String ver = LocaleManager.pver;
	
	public LocaleUpdater(Configuration configuration) 
	{
		configuration.options().pathSeparator(LocaleManager.PATH_SEPARATOR);
		
		cache = new HashMap<LocaleEntry, String>();
		keywords = new HashMap<LocaleEntry, String>();
		lores = new HashMap<LocaleEntry, ItemLocale>();
		
		ConfigurationSection section = configuration.getConfigurationSection(LocaleManager.buildPath("messages"));
		if ( section != null )
		for ( String key : section.getKeys(false) )
			cache.put(new LocaleEntry(key, section.getString(LocaleManager.buildPath(key, "new"), ""), ver), section.getString(LocaleManager.buildPath(key, "message")));
		
		section = configuration.getConfigurationSection(LocaleManager.buildPath("keywords"));
		if ( section != null )
		for ( String key : section.getKeys(false) )
			keywords.put(new LocaleEntry("#" + key, section.getString(LocaleManager.buildPath(key, "new"), ""), ver), section.getString(LocaleManager.buildPath(key, "keyword")));
		
		section = configuration.getConfigurationSection(LocaleManager.buildPath("lores"));
		if ( section != null )
		for ( String key : section.getKeys(false) )
			lores.put(new LocaleEntry(key, section.getString(LocaleManager.buildPath(key, "new"), ""), ver), new ItemLocale(section.getString(LocaleManager.buildPath(key, "name")), section.getStringList(LocaleManager.buildPath(key, "lore"))));
			
	}

	public YamlConfiguration update(Map<LocaleEntry, String> cache, Map<LocaleEntry, ItemLocale> lores, Map<LocaleEntry, String> keywords, File file) 
	{
		YamlConfiguration loc = new YamlConfiguration();
		loc.options().pathSeparator(LocaleManager.PATH_SEPARATOR);
		
		loc.set("ver", LocaleManager.pver);
		
		for ( Map.Entry<LocaleEntry, String> entry : this.cache.entrySet() )
		{
			String key = entry.getKey().key();
			if ( cache.containsKey(entry.getKey()) )
			{
				cache.put(new LocaleEntry(entry.getKey().newkey(), LocaleManager.pver), entry.getValue());
				
				loc.set(LocaleManager.buildPath("backup","messages", key), cache.get(entry.getKey()));
				cache.remove(entry.getKey());
				
				loc.set(LocaleManager.buildPath("messages", entry.getKey().newkey()), entry.getValue());
			}
			else
			{
				loc.set(LocaleManager.buildPath("messages", key), entry.getValue());
			}
		}

		for ( Entry<LocaleEntry, String> entry : this.keywords.entrySet() )
		{
			String key = entry.getKey().key();
			if ( keywords.containsKey(entry.getKey()) )
			{
				keywords.put(new LocaleEntry("#" + entry.getKey().newkey(), LocaleManager.pver), entry.getValue());
				
				loc.set(LocaleManager.buildPath("backup","keywords", key.substring(1)), keywords.get(entry.getKey()));
				keywords.remove(entry.getKey());
				
				loc.set(LocaleManager.buildPath("keywords",entry.getKey().newkey()), entry.getValue());
			}
			else
			{
				loc.set(LocaleManager.buildPath("keywords", key.substring(1)), entry.getValue());
			}
		}
		
		for ( Map.Entry<LocaleEntry, ItemLocale> entry : this.lores.entrySet() )
		{
			String key = entry.getKey().key();
			if ( lores.containsKey(entry.getKey()) )
			{
				lores.put(new LocaleEntry(entry.getKey().newkey(), LocaleManager.pver), entry.getValue());
				
				loc.set(LocaleManager.buildPath("backup","lores", key, "name"), lores.get(entry.getKey()).name());
				loc.set(LocaleManager.buildPath("backup","lores", key, "lore"), lores.get(entry.getKey()).lore());
				lores.remove(entry.getKey());
				
				loc.set(LocaleManager.buildPath("lores",entry.getKey().newkey(), "name"), entry.getValue().name());
				loc.set(LocaleManager.buildPath("lores",entry.getKey().newkey(), "lore"), entry.getValue().lore());
			}
			else
			{
				loc.set(LocaleManager.buildPath("lores",entry.getKey().key(), "name"), entry.getValue().name());
				loc.set(LocaleManager.buildPath("lores",entry.getKey().key(), "lore"), entry.getValue().lore());
			}
		}
		
		//save the new config
		try {
			loc.save(file);
			//locale.load(loc.saveToString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loc;
	}

}

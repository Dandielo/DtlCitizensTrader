package net.dandielo.citizens.trader.patterns;

import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.managers.PermissionsManager;

import org.bukkit.configuration.ConfigurationSection;

public abstract class TPattern {	
	// permissions manager
	protected static final PermissionsManager perms = CitizensTrader.getPermissionsManager();
	
	// pattern fields 
	protected final String name;
	protected final String type;
	
	public TPattern(String name, String type, boolean tier)
	{
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public abstract void load(ConfigurationSection data);
	
	
}

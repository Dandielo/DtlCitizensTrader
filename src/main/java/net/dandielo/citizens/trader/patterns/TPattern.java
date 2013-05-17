package net.dandielo.citizens.trader.patterns;

import net.dandielo.citizens.trader.DtlTraders;
import net.dandielo.citizens.trader.managers.PermissionsManager;

import org.bukkit.configuration.ConfigurationSection;

public abstract class TPattern {	
	// permissions manager
	protected static final PermissionsManager perms = DtlTraders.getPermissionsManager();
	
	// pattern fields 
	protected final String name;
	protected final String type;
	protected final boolean tier;
	
	public TPattern(String name, String type, boolean tier)
	{
		this.name = name;
		this.type = type;
		this.tier = tier;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}
	
	public boolean isTier()
	{
		return tier;
	}

	public abstract void load(ConfigurationSection data);

}

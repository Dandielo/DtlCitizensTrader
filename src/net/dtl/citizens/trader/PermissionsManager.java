package net.dtl.citizens.trader;

import java.util.logging.Logger;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.bukkit.PermissionsEx;

import net.dtl.permissions.bukkit.DtlPermissions;
public class PermissionsManager {
	private final Logger logger = Logger.getLogger("Minecraft");
	private final String pluginPrefix = "[DtlCitizensTrader] ";
	
	private DtlPermissions dtlPerms;
//	private Permission vaultPerms;
	private PermissionsEx permissionsEx;
	private GroupManager gmPerms;
	
	public PermissionsManager() {
		this.initializeDtlPermissions();
		this.initializePexPermissions();
		this.initializeGroupManager();
	}
	
	public void initializeDtlPermissions() {
		dtlPerms = (DtlPermissions) Bukkit.getPluginManager().getPlugin("DtlPermissions");
		if ( dtlPerms == null )
			return;
		logger.info(pluginPrefix + dtlPerms.getDescription().getName() + " ver" + dtlPerms.getDescription().getVersion() + " hooked!");
	}
	
	public void initializeVaultPermissions() {
        
	/*	RegisteredServiceProvider<Permission> rspPerm = CitizensTrader.getInstance().getServer().getServicesManager().getRegistration(Permission.class);
    
        if ( rspPerm != null ) {
        	vaultPerms = rspPerm.getProvider();
			this.logger.info("["+ pdfFile.getName() + "] Permissions enabled.");
        } else {
			this.logger.info("["+ pdfFile.getName() + "] Permissions not found!"); 
        }*/
	}
	
	public void initializePexPermissions() {
		permissionsEx = (PermissionsEx) Bukkit.getPluginManager().getPlugin("PermissionsEx");
		if ( permissionsEx == null )
			return;
		logger.info(pluginPrefix + permissionsEx.getDescription().getName() + " ver" + permissionsEx.getDescription().getVersion() + " hooked!");
	}
	
	public void initializeGroupManager() {
		gmPerms = (GroupManager) Bukkit.getPluginManager().getPlugin("GroupManager");
		if ( gmPerms == null )
			return;
		logger.info(pluginPrefix + gmPerms.getDescription().getName() + " ver" + gmPerms.getDescription().getVersion() + " hooked!");
	}
	
	public boolean has(final Player player, final String permission) {
		//if using dtlPermissions System
		if ( dtlPerms != null ) 
		{
			return dtlPerms.has(player, permission, player.getWorld().getName());
		}
		else
		//using essentials group manager 
		if ( gmPerms != null )
		{
			final AnjoPermissionsHandler handler = gmPerms.getWorldsHolder().getWorldPermissions(player);
			if (handler == null)
			{
				return false;
			}
			return handler.has(player, permission);
		}
		else
		//permissions ex
		if ( permissionsEx != null )
		{
			return permissionsEx.has(player, permission, player.getWorld().getName());
		}
		//if no system was found, use superperms
		else
		{
			return player.hasPermission(permission);
		}
	}
}

package net.dtl.citizens.trader.managers;

import java.util.logging.Logger;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.bananaco.bpermissions.imp.Permissions;

import ru.tehkode.permissions.bukkit.PermissionsEx;

import net.dtl.permissions.bukkit.DtlPermissions;
public class PermissionsManager {
	private final Logger logger = Logger.getLogger("Minecraft");
	private final String pluginPrefix = "[DtlCitizensTrader] ";
	
	private DtlPermissions dtlPerms;
	private Permissions bPermissions;
//	private Permission vaultPerms;
	private PermissionsEx permissionsEx;
	private GroupManager gmPerms;
	
	public PermissionsManager() {
		this.initializeDtlPermissions();
		this.initializePexPermissions();
		this.initializeBPermissions();
		this.initializeGroupManager();
	}
	
	public void initializeDtlPermissions() {
		dtlPerms = (DtlPermissions) Bukkit.getPluginManager().getPlugin("DtlPermissions");
		if ( dtlPerms == null )
			return;
		logger.info(pluginPrefix + dtlPerms.getDescription().getName() + " ver" + dtlPerms.getDescription().getVersion() + " hooked!");
	}
	
	public void initializeBPermissions() {
		bPermissions = (Permissions) Bukkit.getPluginManager().getPlugin("bPermissions");
		if ( bPermissions == null )
			return;
		logger.info(pluginPrefix + bPermissions.getDescription().getName() + " ver" + bPermissions.getDescription().getVersion() + " hooked!");
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
	
	public boolean has(final Player player, final String permission)
	{
		/*System.out.print(!hasPermission(player, permission));
		if ( !hasPermission(player, permission) )
		{
			return player.hasPermission(permission);
		}
		return true;*/
		return hasPermission(player, permission);
	}
	
	public boolean hasPermission(final Player player, final String permission) {
		//if using dtlPermissions System
		if ( dtlPerms != null ) 
		{
			return dtlPerms.has(player, permission, player.getWorld().getName());
		}
		else
		//permissions ex
		if ( permissionsEx != null )
		{
			return player.hasPermission(permission);
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
		if ( bPermissions != null )
		{
			return bPermissions.has(player, permission);
		}
			
		return player.hasPermission(permission);
	}
}

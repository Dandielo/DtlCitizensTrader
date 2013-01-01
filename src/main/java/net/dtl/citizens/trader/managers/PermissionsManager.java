package net.dtl.citizens.trader.managers;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.dtl.citizens.trader.CitizensTrader;
/*
import de.bananaco.bpermissions.imp.Permissions;

import ru.tehkode.permissions.bukkit.PermissionsEx;
*/
//import net.dtl.permissions.bukkit.DtlPermissions;
import net.milkbowl.vault.permission.Permission;

public class PermissionsManager {
	
//	private DtlPermissions dtlPerms;
//	private Permissions bPermissions;
//	private Permission vaultPerms;
//	private PermissionsEx permissionsEx;
//	private GroupManager gmPerms;
	private Permission permission;
	
	public PermissionsManager() {
	/*	this.initializeDtlPermissions();
		this.initializePexPermissions();
		this.initializeBPermissions();
		this.initializeGroupManager();*/
		setupPermissions();
	}
	
	/*public void initializeDtlPermissions() {
		dtlPerms = (DtlPermissions) Bukkit.getPluginManager().getPlugin("DtlPermissions");
		if ( dtlPerms == null )
			return;
		info(dtlPerms.getDescription().getName() + " ver" + dtlPerms.getDescription().getVersion() + " hooked!");
	}*/
	
	private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = CitizensTrader.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
/*
	public void initializeBPermissions() {
		bPermissions = (Permissions) Bukkit.getPluginManager().getPlugin("bPermissions");
		if ( bPermissions == null )
			return;
		info(bPermissions.getDescription().getName() + " ver" + bPermissions.getDescription().getVersion() + " hooked!");
	}
	
	public void initializePexPermissions() {
		permissionsEx = (PermissionsEx) Bukkit.getPluginManager().getPlugin("PermissionsEx");
		if ( permissionsEx == null )
			return;
		info(permissionsEx.getDescription().getName() + " ver" + permissionsEx.getDescription().getVersion() + " hooked!");
	}
	
	public void initializeGroupManager() {
		gmPerms = (GroupManager) Bukkit.getPluginManager().getPlugin("GroupManager");
		if ( gmPerms == null )
			return;
		info(gmPerms.getDescription().getName() + " ver" + gmPerms.getDescription().getVersion() + " hooked!");
	}*/
	
	public boolean has(final Player player, final String permission)
	{
		/*System.out.print(!hasPermission(player, permission));
		if ( !hasPermission(player, permission) )
		{
			return player.hasPermission(permission);
		}
		return true;*/
		return this.permission.has(player, permission);//hasPermission(player, permission);
	}
	
	
}

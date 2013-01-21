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
	
	private Permission permission;
	
	public PermissionsManager() {
		setupPermissions();
	}
	
	private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = CitizensTrader.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
	
	public boolean has(final Player player, final String permission)
	{
		return this.permission.has(player, permission);
	}
	
	
}

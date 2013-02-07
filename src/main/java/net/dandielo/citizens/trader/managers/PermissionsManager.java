package net.dandielo.citizens.trader.managers;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.dandielo.citizens.trader.CitizensTrader;
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
	
	public boolean has(final CommandSender sender, final String permission)
	{
		return this.permission.has(sender, permission);
	}
	
	
}

package net.dtl.citizens.trader.commands.core;

import java.util.Map;

import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.commands.Command;
import net.dtl.citizens.trader.locale.LocaleManager;
import net.dtl.citizens.trader.types.Banker;
import net.dtl.citizens.trader.types.Trader;

import org.bukkit.command.CommandSender;

public class GeneralCommands {
	
	private static LocaleManager locale = CitizensTrader.getLocaleManager();
	
	@Command(
	name = "trader",
	syntax = "",
	perm = "dtl.trader.commands",
	npc = false)
	public void trader(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		if ( npc == null )
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion());
		}
		else
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion(), "name", plugin.getName());
			locale.sendMessage(sender, "key-value", "key", "#type", "value", "#" + npc.getType().toString() + "-trader");
			locale.sendMessage(sender, "key-value", "key", "#owner", "value", npc.getConfig().getOwner());
			if ( npc.getWallet() != null )
			locale.sendMessage(sender, "key-value", "key", "#wallet", "value", npc.getWallet().getType().toString());
			if ( npc.getStock().getPattern() != null )
			locale.sendMessage(sender, "key-value", "key", "#pattern", "value", npc.getStock().getPattern().getName());
			
		}
	}
	
	@Command(
	name = "trader",
	syntax = "reload",
	perm = "dtl.trader.commands.reload",
	npc = false)
	public void traderReload(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		//reload the general config file
		plugin.reloadConfig();
		//reload the locale
		locale.load();
		//reload the item config file
		plugin.getItemConfig().reloadConfig();
	}

	//TODO help command
	@Command(
	name = "trader",
	syntax = "help",
	perm = "dtl.trader.commands.help",
	npc = false)
	public void traderHelp(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion());
	}
	
	//TODO Banker commands
	@Command(
	name = "banker",
	syntax = "",
	perm = "dtl.banker.commands",
	npc = false)
	public void banker(CitizensTrader plugin, CommandSender sender, Banker npc, Map<String, String> args)
	{
		if ( npc == null )
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion());
		}
		else
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion(), "name", plugin.getName());
			locale.sendMessage(sender, "key-value", "key", "#type", "value", "#" + npc.getType().toString() + "-banker");		
		}
	}
	
	@Command(
	name = "banker",
	syntax = "reload",
	perm = "dtl.banker.commands.reload",
	npc = false)
	public void bankerReload(CitizensTrader plugin, CommandSender sender, Banker npc, Map<String, String> args)
	{
		//reload the general config file
		plugin.reloadConfig();
		//reload the locale
		locale.load();
		//reload the item config file
		plugin.getItemConfig().reloadConfig();
	}
	
	@Command(
	name = "banker",
	syntax = "help",
	perm = "dtl.banker.commands.help",
	npc = false)
	public void bankerHelp(CitizensTrader plugin, CommandSender sender, Banker npc, Map<String, String> args)
	{
		locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion());
	}
}

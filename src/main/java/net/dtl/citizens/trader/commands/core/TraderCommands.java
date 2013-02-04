package net.dtl.citizens.trader.commands.core;

import java.util.Map;

import org.bukkit.command.CommandSender;

import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.commands.Command;
import net.dtl.citizens.trader.locale.LocaleManager;
import net.dtl.citizens.trader.objects.Wallet.WalletType;
import net.dtl.citizens.trader.types.Trader;

public class TraderCommands {
	
	private static LocaleManager locale = CitizensTrader.getLocaleManager();
	
	@Command(
	name = "trader",
	syntax = "",
	perm = "dtl.trader.commands")
	public void trader(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		if ( npc == null )
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion(), "name", plugin.getName());
		}
		else
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion(), "name", plugin.getName());
			locale.sendMessage(sender, "key-value", "key", "#type", "value", npc.getType().toString());
			locale.sendMessage(sender, "key-value", "key", "#owner", "value", npc.getConfig().getOwner());
			locale.sendMessage(sender, "key-value", "key", "#wallet", "value", npc.getWallet().getType().toString());
			locale.sendMessage(sender, "key-value", "key", "#pattern", "value", npc.getStock().getPattern().getName());
			
		}
	}
	
	@Command(
	name = "trader",
	syntax = "wallet",
	perm = "dtl.trader.commands.wallet")
	public void tradeWallet(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		if ( npc == null )
			return;
		locale.sendMessage(sender, "key-value", "key", "#wallet", "value", npc.getWallet().getType().toString());
	}
	
	@Command(
	name = "trader",
	syntax = "wallet set <wallet>",
	perm = "dtl.trader.commands.wallet")
	public void tradeSetWallet(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		if ( npc == null )
			return;
		
		WalletType type = WalletType.getTypeByName(args.get("wallet"));
		if ( type == null )
		{
			locale.sendMessage(sender, "error-argument-invalid", "argument", args.get("wallet"));
			return;
		}
		
		npc.getWallet().setType(type);
		locale.sendMessage(sender, "key-changed", "key", "#wallet", "value", type.toString());

	}
	
}

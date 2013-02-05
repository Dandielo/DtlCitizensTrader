package net.dtl.citizens.trader.commands.core;

import java.text.DecimalFormat;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.TraderCharacterTrait.EcoNpcType;
import net.dtl.citizens.trader.commands.Command;
import net.dtl.citizens.trader.locale.LocaleManager;
import net.dtl.citizens.trader.managers.PatternsManager;
import net.dtl.citizens.trader.objects.Wallet;
import net.dtl.citizens.trader.objects.Wallet.WalletType;
import net.dtl.citizens.trader.parts.TraderConfigPart;
import net.dtl.citizens.trader.parts.TraderStockPart;
import net.dtl.citizens.trader.types.Trader;

public class TraderCommands {
	
	private static LocaleManager locale = CitizensTrader.getLocaleManager();
	
	//TODO create commands
	@Command(
	name = "trader",
	syntax = "create {array}",
	perm = "dtl.trader.commands.create",
	npc = false)
	public void traderCreate(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		String name = args.get("free");
		String owner = args.get("o");
		
		EcoNpcType type = EcoNpcType.getTypeByName(args.get("t"));
		WalletType wallet = WalletType.getTypeByName(args.get("w"));
		EntityType entity = EntityType.fromName(args.get("e"));
	}
	
	@Command(
	name = "trader",
	syntax = "hire {array}",
	perm = "dtl.trader.commands.hire",
	npc = false)
	public void traderPlayerCreate(CitizensTrader plugin, Player sender, Trader trader, Map<String, String> args)
	{
		String name = args.get("free");
		WalletType wallet = WalletType.getTypeByName(args.get("w") == null ? "npc" : args.get("w"));

		if ( name == null )
		{
			locale.sendMessage(sender, "error-argument-missing");
			return;
		}
		if ( wallet == null )
			wallet = WalletType.NPC;
		
		// Create and spawn the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
		npc.addTrait(TraderCharacterTrait.class);
		npc.spawn(sender.getLocation());
		
		// Add basic settings
		npc.getTrait(TraderCharacterTrait.class).setType(EcoNpcType.PLAYER_TRADER);
		npc.getTrait(TraderCharacterTrait.class).implementTrader();

		TraderConfigPart settings = npc.getTrait(TraderCharacterTrait.class).getConfig();
		settings.setOwner(sender.getName());
		settings.getWallet().setType(wallet);
		
		locale.sendMessage(sender, "trader-created", "player", sender.getName(), "trader", name);
	}
	
	//TODO Config commands
	@Command(
	name = "trader",
	syntax = "owner",
	perm = "dtl.trader.commands.owner")
	public void tradeOwner(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		locale.sendMessage(sender, "key-value", "key", "#owner", "value", npc.getConfig().getOwner());
	}
	
	@Command(
	name = "trader",
	syntax = "owner set <player>",
	perm = "dtl.trader.commands.owner.set")
	public void tradeSetOwner(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		npc.getConfig().setOwner(args.get("player"));
		locale.sendMessage(sender, "key-change", "key", "#owner", "value", args.get("player"));
	}
	
	//TODO pattern commands
	@Command(
	name = "trader",
	syntax = "pattern",
	perm = "dtl.trader.commands.pattern")
	public void tradePattern(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		if ( npc.getStock().getPattern() != null )
			locale.sendMessage(sender, "key-value", "key", "#pattern", "value", npc.getStock().getPattern().getName());
		else
			locale.sendMessage(sender, "key-value", "key", "#pattern", "value", "#disabled");
	}
	
	@Command(
	name = "trader",
	syntax = "pattern <action> <pattern> (arg) (post)",
	perm = "dtl.trader.commands.pattern")
	public void tradePatternSet(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		PatternsManager man = CitizensTrader.getPatternsManager();
		
		String action = args.get("action");
		String pattern = args.get("pattern");

		if ( action.equals("set") )
		{
			if ( !npc.getStock().setPattern(pattern) )
				locale.sendMessage(sender, "error-argument-invalid", "argument", pattern);
			else
				locale.sendMessage(sender, "key-change", "key", "#pattern", "value", pattern);
		}
		else
		if ( action.equals("save") )
		{
			if ( man.getPattern(pattern) != null )
			{
				locale.sendMessage(sender, "pattern-save-fail-exists", "pattern", pattern);
				return;
			}
			
			String arg = args.get("arg") == null ? "all" : args.get("arg");
			String post = args.get("post") == null ? "" : args.get("post");
			
			TraderStockPart stock = npc.getStock();
			
			man.setFromList(pattern, 
					stock.getStock("sell"), 
					stock.getStock("buy"), 
					arg);
			
			// reload patterns
			tradePatternReload(plugin, sender, npc, args);
			
			if ( post.equals("clear") )
				stock.clearStock();
				
			if ( post.equals("reset") )
				stock.resetPrices();

			locale.sendMessage(sender, "pattern-save-success", "pattern", pattern);
		}
	}
	
	@Command(
	name = "trader",
	syntax = "pattern remove",
	perm = "dtl.trader.commands.pattern")
	public void tradePatternRemove(CitizensTrader plugin, CommandSender sender, Trader trader, Map<String, String> args)
	{
		trader.getStock().removePattern();
		locale.sendMessage(sender, "key-change", "key", "#pattern", "value", "#disabled");
	}
	
	@Command(
	name = "trader",
	syntax = "pattern reload",
	perm = "dtl.trader.commands.pattern",
	npc = false)
	public void tradePatternReload(CitizensTrader plugin, CommandSender sender, Trader trader, Map<String, String> args)
	{
		// reload patterns
		CitizensTrader.getPatternsManager().reload();
		
		// reload server traders
		for ( NPC npc : CitizensTrader.getNpcEcoManager().getTraders(EcoNpcType.SERVER_TRADER) )
		{
			npc.getTrait(TraderCharacterTrait.class).getStock().reloadStock();
		}
		
		// reload market traders
		for ( NPC npc : CitizensTrader.getNpcEcoManager().getTraders(EcoNpcType.MARKET_TRADER) )
		{
			npc.getTrait(TraderCharacterTrait.class).getStock().reloadStock();
		}
	}
	
	//TODO Wallet commands
	private static DecimalFormat format = new DecimalFormat("#.##");
	
	@Command(
	name = "trader",
	syntax = "wallet",
	perm = "dtl.trader.commands.wallet")
	public void traderWallet(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		locale.sendMessage(sender, "key-value", "key", "#wallet", "value", npc.getWallet().getType().toString());
	}
	
	@Command(
	name = "trader",
	syntax = "wallet set <wallet>",
	perm = "dtl.trader.commands.wallet")
	public void traderSetWallet(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		WalletType type = WalletType.getTypeByName(args.get("wallet"));
		if ( type == null )
		{
			locale.sendMessage(sender, "error-argument-invalid", "argument", args.get("wallet"));
			return;
		}
		
		npc.getWallet().setType(type);
		locale.sendMessage(sender, "key-change", "key", "#wallet", "value", type.toString());
	}
	
	@Command(
	name = "trader",
	syntax = "wallet deposit <amount>",
	perm = "dtl.trader.commands.wallet")
	public void traderWalletDeposit(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		Wallet wallet = npc.getWallet();
		double amount = Double.parseDouble(args.get("amount"));
		
		if ( CitizensTrader.getEconomy().depositPlayer(sender.getName(), amount).transactionSuccess() )
		{
			wallet.deposit(null, amount);
			locale.sendMessage(sender, "wallet-deposit", "amount", format.format(amount));
			locale.sendMessage(sender, "wallet-balance", "amount", format.format(wallet.getMoney()));
		}
		else
			locale.sendMessage(sender, "wallet-deposit-fail");
	}

	@Command(
	name = "trader",
	syntax = "wallet withdraw <amount>",
	perm = "dtl.trader.commands.wallet")
	public void traderWalletWithdraw(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		Wallet wallet = npc.getWallet();
		double amount = Double.parseDouble(args.get("amount"));
		
		if ( wallet.withdraw(null, amount) )
		{
			CitizensTrader.getEconomy().depositPlayer(sender.getName(), amount);
			locale.sendMessage(sender, "wallet-withdraw", "amount", format.format(amount));
			locale.sendMessage(sender, "wallet-balance", "amount", format.format(wallet.getMoney()));
		}
		else
			locale.sendMessage(sender, "wallet-withdraw-fail");
	}

	@Command(
	name = "trader",
	syntax = "wallet balance",
	perm = "dtl.trader.commands.wallet")
	public void traderWalletBalance(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		Wallet wallet = npc.getWallet();
		if ( !wallet.getType().equals(WalletType.NPC) )
			locale.sendMessage(sender, "wallet-invalid");
		else
			locale.sendMessage(sender, "wallet-balance", "amount", format.format(wallet.getMoney()));
	}
	
}

package net.dandielo.citizens.trader.commands.core;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.NpcManager;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.TraderTrait.EType;
import net.dandielo.citizens.trader.commands.Command;
import net.dandielo.citizens.trader.locale.LocaleManager;
import net.dandielo.citizens.trader.managers.LogManager;
import net.dandielo.citizens.trader.managers.PatternsManager;
import net.dandielo.citizens.trader.objects.NBTTagEditor;
import net.dandielo.citizens.trader.objects.Wallet;
import net.dandielo.citizens.trader.objects.Wallet.WalletType;
import net.dandielo.citizens.trader.parts.TraderConfigPart;
import net.dandielo.citizens.trader.parts.TraderStockPart;
import net.dandielo.citizens.trader.types.Trader;

public class TraderCommands {
	
	private static LocaleManager locale = CitizensTrader.getLocaleManager();
	
	//TODO management commands
	@Command(
	name = "trader",
	syntax = "create {array}",
	perm = "dtl.trader.commands.create",
	npc = false)
	public void traderCreate(CitizensTrader plugin, Player sender, Trader trader, Map<String, String> args)
	{
		String name = args.get("free");
		String owner = args.get("o");
		
		EType type = EType.fromName(args.get("t") == null ? "server" : args.get("t"));
		WalletType wallet = WalletType.getTypeByName(args.get("w") == null ? "npc" : args.get("w"));
		EntityType entity = EntityType.fromName(args.get("e") == null ? "player" : args.get("e"));
		
		if ( name == null )
		{
			locale.sendMessage(sender, "error-argument-missing");
			return;
		}
		
		if ( wallet == null )
			wallet = WalletType.NPC;
		if ( type == null )
			type = EType.SERVER_TRADER;
		if ( entity == null )
			entity = EntityType.PLAYER;
		
		// Create and spawn the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(entity, name);
		npc.addTrait(TraderTrait.class);
		npc.spawn(sender.getLocation());
		npc.addTrait(MobType.class);
		npc.getTrait(MobType.class).setType(entity);
		
		// Add basic settings
		npc.getTrait(TraderTrait.class).setType(type);
		npc.getTrait(TraderTrait.class).implementTrader();

		TraderConfigPart settings = npc.getTrait(TraderTrait.class).getConfig();
		settings.setOwner(owner == null ? "no owner" : owner);
		settings.getWallet().setType(wallet);
		
		locale.sendMessage(sender, "trader-created", "player", sender.getName(), "trader", name);
	}
	
	@Command(
	name = "trader",
	syntax = "hire {array}",
	perm = "dtl.trader.commands.hire",
	npc = false)
	public void traderHire(CitizensTrader plugin, Player sender, Trader trader, Map<String, String> args)
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
		npc.addTrait(TraderTrait.class);
		npc.spawn(sender.getLocation());
		
		// Add basic settings
		npc.getTrait(TraderTrait.class).setType(EType.PLAYER_TRADER);
		npc.getTrait(TraderTrait.class).implementTrader();

		TraderConfigPart settings = npc.getTrait(TraderTrait.class).getConfig();
		settings.setOwner(sender.getName());
		settings.getWallet().setType(wallet);
		
		locale.sendMessage(sender, "trader-created", "player", sender.getName(), "trader", name);
	}
	
	@Command(
	name = "trader",
	syntax = "manage {array}",
	perm = "dtl.trader.commands.manage",
	npc = false)
	public void traderManage(CitizensTrader plugin, Player sender, Trader trader, Map<String, String> args)
	{
		NpcManager man = CitizensTrader.getNpcEcoManager();
		
		String name = args.get("free");

		if ( name == null )
		{
			if ( trader != null )
			{
				locale.sendMessage(sender, "managermode-disabled", "npc", trader.getNpc().getName());
				man.removeInteractionNpc(sender.getName());
			}
			return;
		}
		else
		{
			String player = sender.getName();
			
			if ( trader != null )
			{
				locale.sendMessage(sender, "managermode-disabled", "npc", trader.getNpc().getName());
				man.removeInteractionNpc(player);
			}
				
			trader = man.traderByName(name, sender);
			man.addInteractionNpc(player, trader);
			
			locale.sendMessage(sender, "managermode-enabled", "npc", man.getInteractionNpc(player).getNpc().getName());
		}
	}
	
	@Command(
	name = "trader",
	syntax = "open",
	perm = "dtl.trader.commands.open")
	public void traderOpen(CitizensTrader plugin, Player player, Trader trader, Map<String, String> args)
	{
		NBTTagEditor.removeDescriptions(player.getInventory());
		if ( !trader.getTraderStatus().isManaging() )
			trader.loadDescriptions(player, player.getInventory());	
		
		player.openInventory(trader.getInventory());
	}
	
	//TODO Log commands
	@Command(
	name = "trader",
	syntax = "log <task>",
	perm = "dtl.trader.commands.log",
	npc = false)
	public void log(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		LogManager log = CitizensTrader.getLoggingManager();
		
		if ( args.get("task").equals("show") )
		{
			List<String> logs = log.getPlayerLogs(sender.getName(), npc == null ? "" : npc.getNpc().getName());
			
			if ( logs == null )	return;
			
			for ( String entry : logs )
				sender.sendMessage(entry);
		}
		if ( args.get("task").equals("clear") )
		{
			log.clearPlayerLogs(sender.getName(), npc == null ? "" : npc.getNpc().getName());
			locale.sendMessage(sender, "trader-log-cleared");
		}
	}
	
	
	//TODO Config commands
	@Command(
	name = "trader",
	syntax = "reset <target> (stock)",
	perm = "dtl.trader.commands.owner")
	public void tradeReset(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{		
		String target = args.get("target");
		
		if ( !target.equals("prices") && !target.equals("stock") )
		{
			locale.sendMessage(sender, "error-argument-invalid", "argument", target);
			return;
		}
		
		if ( args.containsKey("stock") )
		{
			String stock = args.get("stock");
			if ( stock.equals("sell") )
			{
				npc.getStock().reset(stock, target);
				
				locale.sendMessage(sender, "trader-stock-cleared", "stock", "#sell-stock");
			}
			else
			if ( stock.equals("buy") )
			{
				npc.getStock().reset(stock, target);

				locale.sendMessage(sender, "trader-stock-cleared", "stock", "#buy-stock");
			}
			else
				locale.sendMessage(sender, "error-argument-invalid", "argument", stock);
		}

		npc.getStock().reset(null, target);
		
		locale.sendMessage(sender, "trader-stock-cleared", "stock", "#sell-stock");
		locale.sendMessage(sender, "trader-stock-cleared", "stock", "#buy-stock");
	}
	
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
				stock.reset(null, "stock");
				
			if ( post.equals("reset") )
				stock.reset(null, "prices");

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
		for ( NPC npc : CitizensTrader.getNpcEcoManager().getTraders(EType.SERVER_TRADER) )
		{
			npc.getTrait(TraderTrait.class).getStock().reloadStock();
		}
		
		// reload market traders
		for ( NPC npc : CitizensTrader.getNpcEcoManager().getTraders(EType.MARKET_TRADER) )
		{
			npc.getTrait(TraderTrait.class).getStock().reloadStock();
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

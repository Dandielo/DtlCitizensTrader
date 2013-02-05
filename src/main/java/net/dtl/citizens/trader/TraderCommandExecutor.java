package net.dtl.citizens.trader;

import java.text.DecimalFormat;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dtl.citizens.trader.TraderCharacterTrait.EcoNpcType;
import net.dtl.citizens.trader.locale.LocaleManager;
import net.dtl.citizens.trader.managers.LoggingManager;
import net.dtl.citizens.trader.managers.PatternsManager;
import net.dtl.citizens.trader.managers.PermissionsManager;
import net.dtl.citizens.trader.objects.Wallet.WalletType;
import net.dtl.citizens.trader.parts.TraderConfigPart;
import net.dtl.citizens.trader.types.tNPC;
import net.dtl.citizens.trader.types.Trader;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
//import net.sacredlabyrinth.phaed.simpleclans.Clan;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * @author Dandielo
 */
public final class TraderCommandExecutor implements CommandExecutor {
	
	//managers
	private static PatternsManager patternsManager;
	private static NpcEcoManager traderManager;
	private static PermissionsManager permsManager;
	private static LoggingManager logManager;
	private static LocaleManager locale;
	
	//constructor
	public TraderCommandExecutor() {
		locale = CitizensTrader.getLocaleManager();
		permsManager = CitizensTrader.getPermissionsManager();
		traderManager = CitizensTrader.getNpcEcoManager();
		logManager = CitizensTrader.getLoggingManager();
		patternsManager = CitizensTrader.getPatternsManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		
		//is player
		if ( sender instanceof Player )
		{
			Player player = (Player) sender;

			//get the selected NPC
			tNPC economyNpc = traderManager.getInteractionNpc(player.getName());
			
			if ( args.length < 1 )
			{
				player.sendMessage(ChatColor.AQUA + "DtlTraders " + CitizensTrader.getInstance().getDescription().getVersion() + ChatColor.RED + "" );
				
				if ( economyNpc != null && economyNpc instanceof Trader )
				{
					
					Trader trader = (Trader) economyNpc;
					player.sendMessage(ChatColor.GOLD + "==== " + ChatColor.YELLOW + trader.getNpc().getName() + ChatColor.GOLD + " ====");
					locale.sendMessage(player, "key-value", "key", "#trader-type", "value", trader.getType().toString());
					locale.sendMessage(player, "key-value", "key", "#owner", "value", trader.getConfig().getOwner());
					if ( !CitizensTrader.dtlWalletsEnabled() )
						locale.sendMessage(player, "key-value", "key", "#wallet", "value", trader.getWallet().getType().toString());
					if ( trader.getStock().getPattern() != null )
						locale.sendMessage(player, "key-value", "key", "#pattern", "value", trader.getStock().getPattern().getName());

				}
				return true;
			}
			
			
			//no npc selected
			if ( economyNpc == null )
			{
				if ( args[0].equalsIgnoreCase("help") )
				{
					player.sendMessage(ChatColor.AQUA + "DtlTraders " + CitizensTrader.getInstance().getDescription().getVersion() + ChatColor.RED + " - Trader commands list" );
					return false;
				}
				//reload plugin
				if ( args[0].equalsIgnoreCase("create") )
				{
					if ( !this.generalChecks(player, "create", null) )
						return true;
					
					return createTrader(player, args);
				}
				if ( args[0].equals("log") )
				{
					if ( !this.generalChecks(player, "log", null) )
						return true;
					return log(player, "", args);
				}
				if ( args[0].equals("clearlog") )
				{
					if ( !this.generalChecks(player, "clearlog", null) )
						return true;
					
					return clearLog(player, "", args);
				}
				if ( args[0].equals("reload") )
				{
					locale.sendMessage(player, "plugin-reload");
					//sender.sendMessage( locale.getLocaleString("reload-config") );
					CitizensTrader.getLocaleManager().load();
					CitizensTrader.getInstance().getItemConfig().reloadConfig();
					CitizensTrader.getInstance().reloadConfig();
					
					return true;
				}
				if ( args[0].equalsIgnoreCase("manage") )
				{
					if ( !this.generalChecks(player, "manage", null) )
						return true;
					
					if ( args.length < 2 )
					{
						locale.sendMessage(player, "error-argument-missing");
						//TODO debug info
						//player.sendMessage( locale.getLocaleString("xxx-argument-missing", "argument:name") );
						return true;
					}
					//TODO name adding
					String name = "";
					for ( int i = 1 ; i < args.length ; ++i )
						name += args[i];
					
					Trader trader = (Trader) traderManager.getServerTraderByName(name, player);
					
					if ( trader == null )
					{
						locale.sendMessage(player, "error-argument-invalid", "argument", name);
						//player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:name") );
						return true;
					}
					traderManager.addInteractionNpc(player.getName(), trader);
					return traderManage(player, trader);
				}
				
				
				return true;
			}
			//npc has been selected
			else
			{
				
				//is trader type
				if ( !( economyNpc instanceof Trader ) )
				{
					locale.sendMessage(player, "error-plugin-invalid-command");
					//player.sendMessage( locale.getLocaleString("xxx-not-selected", "object:trader") );
					return true;
				}
				
				Trader trader = (Trader) economyNpc;
				
				if ( args[0].equalsIgnoreCase("help") )
				{
					return false;
				}
				if ( args[0].equalsIgnoreCase("manage") )
				{

					if ( !this.generalChecks(player, "manage", null) )
						return true;
					
				//	EconomyNpc trader = traderManager.getServerTraderByName(args[1]);
					
					traderManager.addInteractionNpc(player.getName(), trader);
					return traderManage(player, trader);
					//return traderManage(player, trader);
				}
				if ( args[0].equals("open") )
				{
					if ( !this.generalChecks(player, "open", null) )
						return true;
					
					return openTraderInventory(player, trader);
				}
				if ( args[0].equals("type") )
				{
					if ( !this.generalChecks(player, "type", null) )
						return true;
					
					return setType(player, trader, ( args.length > 1 ? args[1] : "" ) );
				}
				if ( args[0].equals("wallet") )
				{
					if ( !this.generalChecks(player, "wallet", null) )
						return true;
					
					return setWallet(player, trader, ( args.length > 1 ? args[1] : "" ), ( args.length > 2 ? args[2] : "" ) );
				}
				if ( args[0].equals("owner") )
				{
					if ( !this.generalChecks(player, "owner", null) )
						return true;
					
					if ( args.length > 1 )
						return setOwner(player, trader, args[1]);
					else
						return getOwner(player, trader);
				}
				if ( args[0].equals("clear") )
				{
					if ( !this.generalChecks(player, "clear", null) )
						return true;
					
					return clear(player, trader, args);
				}
				if ( args[0].equals("pattern") )
				{
					if ( !this.generalChecks(player, "pattern", null) )
						return true;
					
					return pattern(player, trader, args);
				}
				if ( args[0].equals("log") )
				{
					if ( !this.generalChecks(player, "log", null) )
						return true;
					
					return log(player, trader.getNpc().getName(), args);
				}
				if ( args[0].equals("clearlog") )
				{
					if ( !this.generalChecks(player, "clearlog", null) )
						return true;
					
					return clearLog(player, trader.getNpc().getName(), args);
				}
				if ( args[0].equals("balance") )
				{
					if ( !this.generalChecks(player, "balance", null) )
						return true;
					
					return balance(player, trader);
				}
				if ( args[0].equals("withdraw") )
				{
					if ( !this.generalChecks(player, "withdraw", null) )
						return true;
					if ( args.length < 2 )
					{
						locale.sendMessage(player, "error-argument-missing");
						return true;
					}
					
					return withdraw(player, trader, args[1]);
				}
				if ( args[0].equals("deposit") )
				{
					if ( !this.generalChecks(player, "deposit", null) )
						return true;
					if ( args.length < 2 )
					{
						locale.sendMessage(player, "error-argument-missing");
						return true;
					}
						
					return deposit(player, trader, args[1]);
				}
				//reload plugin
				if ( args[0].equalsIgnoreCase("create") )
				{
					if ( !this.generalChecks(player, "create", null) )
						return true;
					
					return createTrader(player, args);
				}
				if ( args[0].equals("reload") )
				{
					locale.sendMessage(player, "plugin-reload");
					//sender.sendMessage( locale.getLocaleString("reload-config") );
					CitizensTrader.getInstance().getItemConfig().reloadConfig();
					CitizensTrader.getInstance().reloadConfig();
					CitizensTrader.getLocaleManager().load();
					
					return true;
				}
				
				return false;
			}
				
		}		
		else
		{
			if ( args.length < 1 )
				return false;
			
			if ( args[0].equals("reload") )
			{
				locale.sendMessage(sender, "plugin-reload");
//				sender.sendMessage( locale.getLocaleString("reload-config") );
				CitizensTrader.getInstance().getItemConfig().reloadConfig();
				CitizensTrader.getInstance().reloadConfig();
				CitizensTrader.getLocaleManager().load();
				
				return true;
			}
		}
		
		return false;
	}
	
	private boolean openTraderInventory(Player player, Trader trader) {
		player.openInventory(trader.getInventory());
		trader.switchInventory(Trader.getManageStartStatus(player));
		return true;
	}

	private boolean traderManage(Player player, Trader economyNpc) 
	{
		Trader trader = (Trader) economyNpc;
		
		if ( !permsManager.has(player, "dtl.trader.bypass.managing") &&
			!player.isOp() )
		{
			if ( !permsManager.has(player, "dtl.trader.options.manage") )
			{
				locale.sendMessage(player, "error-nopermission");
				//player.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
				return true;
			}
			if ( !trader.getConfig().getOwner().equals(player.getName()) )
			{
				locale.sendMessage(player, "error-nopermission");
				//player.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
				return true;
			}
		}
		
		
		if ( economyNpc.getTraderStatus().isManaging() )
		{
			traderManager.removeInteractionNpc(player.getName());
			//economyNpc.setTraderStatus(TraderStatus.SELL);
			trader.switchInventory( Trader.getStartStatus(player) );
			player.sendMessage(ChatColor.AQUA + trader.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
		}
		else
		{
			//economyNpc.setTraderStatus(TraderStatus.MANAGE);
			player.sendMessage(ChatColor.AQUA + trader.getNpc().getFullName() + ChatColor.RED + " entered the manager mode!");
			trader.switchInventory( Trader.getManageStartStatus(player) );
		}
		return true;
	}

	private boolean reloadPatterns(Player player, Trader trader) 
	{
		CitizensTrader.getPatternsManager().reload();
		
	//	for ( NPC npc : TraderCommandExecutor.traderManager.getAllServerTraders() )
		{
	//		npc.getTrait(TraderCharacterTrait.class).getStock().reloadStock();
		}
		
		return true;
	}

	private boolean removePattern(Player player, Trader trader) {

		locale.sendMessage(player, "pattern-removed", "pattern", trader.getStock().getPattern().getName());
		trader.getStock().removePattern();
		trader.getInventory().clear();
		//player.sendMessage( locale.getLocaleString("removed-pattern") );
		
		return true;
	}
	
	private boolean pattern(Player player, Trader trader, String[] args) {
		
		if ( args.length <= 1 )
		{
			locale.sendMessage(player, "error-argument-missing");
			//player.sendMessage( locale.getLocaleString("xxx-argument-missing", "argument:action") );
			return true;
		}
		
		if ( args[1].toLowerCase().equals("remove") )
		{
			return this.removePattern(player, trader);
		}
		if ( args[1].toLowerCase().equals("reload") )
		{
			if ( !permsManager.has(player, "dtl.trader.commands.pattern-reload") )
			{
				locale.sendMessage(player, "error-nopermission");
				//player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
				return true;
			}
			return this.reloadPatterns(player, trader);
		}
		if ( args[1].equals("save") )
		{
			if ( args.length < 3 )
			{
				locale.sendMessage(player, "error-argument-missing");
				return true;
			}
			String target = args.length > 3 ? args[3] : "all";
			String post = args.length > 3 ? args[4] : "";
			
			patternsManager.setFromList(args[2], 
										trader.getStock().getStock("sell"), 
										trader.getStock().getStock("buy"), 
										target );
			
			this.reloadPatterns(player, trader);
			
			if ( post.equals("clear") )
				trader.getStock().clearStock();
				
			if ( post.equals("reset") )
				trader.getStock().resetPrices();
			
			player.sendMessage(ChatColor.GOLD + "Pattern was saved, all patterns has been reloaded");
			return true;
		}
		if ( !args[1].equals("set") )
		{
			locale.sendMessage(player, "error-argument-invalid", "argument", args[1]);
		//	player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:action") );
			return true;
		}
		
		if ( args.length <= 2 )
		{
			locale.sendMessage(player, "error-argument-missing");
		//	player.sendMessage( locale.getLocaleString("xxx-argument-missing", "argument:pattern") );
			return true;
		}
		
		if ( trader.getStock().setPattern(args[2]) )
		{
			locale.sendMessage(player, "key-change", "key", "#pattern", "value", args[2]);
		//	player.sendMessage( locale.getLocaleString("xxx-value-changed", "", "manage:{argument}", "argument:pattern").replace("{value}", args[2].toLowerCase()) );
		}
		else
		{
			locale.sendMessage(player, "error-argument-invalid", "argument", args[2]);
		//	player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:pattern") );
		}
		return true;
	}

	private boolean clear(Player player, Trader trader, String[] args) 
	{
		if ( args.length > 1 )
		{
			if ( !args[1].toLowerCase().equals("sell") || !args[1].toLowerCase().equals("buy") )
			{
				locale.sendMessage(player, "error-argument-invalid", "argument", args[1]);
			//	player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:stock") );
				return true;
			}
			trader.getStock().clearStock(args[1]);

			locale.sendMessage(player, "trader-stock-cleared", "stock", args[1]);
			//player.sendMessage( locale.getLocaleString("xxx-stock-cleared", "manage:" + args[1], "action:cleared") );
			return true;
		}
		
		trader.getStock().clearStock("");

		locale.sendMessage(player, "trader-stock-cleared", "stock", "#sell-stock");
		locale.sendMessage(player, "trader-stock-cleared", "stock", "#buy-stock");
	//	player.sendMessage( locale.getLocaleString("xxx-stock-cleared", "manage:sell", "action:cleared") );
	//	player.sendMessage( locale.getLocaleString("xxx-stock-cleared", "manage:buy", "action:cleared") );
		return true;
	}

	private boolean clearLog(Player player, String trader, String[] args)
	{
		if ( trader.isEmpty() )
		{
			for ( int i = 0 ; i < args.length ; ++i )
			{
				if ( i + 1 < args.length )
				{
					trader += args[i+1] + " ";
				}
			}
			if ( !trader.isEmpty() )
				trader = trader.substring(0, trader.length()-1);
		}
		
		logManager.clearPlayerLogs(player.getName(), trader);
		locale.sendMessage(player, "trader-log-cleared");
	//	player.sendMessage( locale.getLocaleString("log-xxx", "log:trader", "action:cleared") );
		
		return true;
	}
	
	private boolean log(Player player, String trader, String[] args) {
		
		if ( trader.isEmpty() )
		{
			for ( int i = 0 ; i < args.length ; ++i )
			{
				if ( i + 1 < args.length )
				{
					trader += args[i+1] + " ";
				}
			}
			if ( !trader.isEmpty() )
				trader = trader.substring(0, trader.length()-1);
		}
		
		List<String> logs = logManager.getPlayerLogs(player.getName(), trader);
		
		if ( logs == null )
			return true;
		
		for ( String log : logs )
			player.sendMessage(log);
		return true;
	}

	public boolean generalChecks(Player player, String commandPermission, String optionsPermission)
	{
		
		//check permissions
		if ( !permsManager.has(player, "dtl.trader.commands." + commandPermission)  )
		{
			locale.sendMessage(player, "error-nopermission");
		//	player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
			return false;
		}
		
		if ( optionsPermission != null )
		//check permissions
		if ( !permsManager.has(player, "dtl.trader.options." + optionsPermission)  )
		{
			locale.sendMessage(player, "error-nopermission");
		//	player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
			return false;
		}
		
		return true;
	}
	

	public EcoNpcType getDefaultTraderType(Player player) {
		
		//server trader as default
		if ( permsManager.has(player, "dtl.trader.types.server") )
			return EcoNpcType.SERVER_TRADER;
		else
		//next default is player trader 
		if ( permsManager.has(player, "dtl.trader.types.player") )
			return EcoNpcType.SERVER_TRADER;
		
		//else return no default
		return null;
	}
	
	public WalletType getDefaultWalletType(Player player, EcoNpcType traderType) {
		//server default is infinite
		if ( permsManager.has(player, "dtl.trader.wallets.infinite") && !traderType.equals(EcoNpcType.PLAYER_TRADER) )
			return WalletType.INFINITE;
		else
		//next default is npc wallet
		if ( permsManager.has(player, "dtl.trader.wallets.npc") )
			return WalletType.NPC;
		else
		//next default is player wallet
		if ( permsManager.has(player, "dtl.trader.wallets.owner") )
			return WalletType.OWNER;
		else
		//next server default is custom bank
		if ( permsManager.has(player, "dtl.trader.wallets.bank") )
			return WalletType.BANK;
		
		//else return no default
		return null;
	}

	
	//set the traders wallet type
	public boolean setWallet(Player player, Trader trader, String walletString, String bankAccount)
	{
		WalletType wallet = WalletType.getTypeByName(walletString);
		
		
		if ( wallet == null )
		{
			//send message
			locale.sendMessage(player, "key-value", "key", "#wallet", "value", trader.getWallet().getType().toString());
			//player.sendMessage( locale.getLocaleString("xxx-setting-value", "setting:wallet").replace("{value}", trader.getWallet().getType().toString() + ( account.isEmpty() ? "" : "§6:§e" + account )) );
			
		}
		//change wallet
		else
		{
			
			if ( !permsManager.has(player, "dtl.trader.wallets." + walletString ) )
			{
				locale.sendMessage(player, "error-nopermission");
			//	player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:wallet") );
				return true;
			}
			
			//bank
			if ( wallet.equals(WalletType.BANK) )
			{
				if ( bankAccount.isEmpty() )
				{
					locale.sendMessage(player, "error-argument-missing");
				//	player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
				if ( !trader.getWallet().setBank(player.getName(), bankAccount) )
				{
					locale.sendMessage(player, "error-argument-invalid", "argument", bankAccount);
				//	player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
			}
			
			//set the wallet type for both trader and wallet
			trader.getWallet().setType(wallet);

			//send message
			locale.sendMessage(player, "key-change", "key", "#wallet", "value", wallet.toString());
			//player.sendMessage( locale.getLocaleString("xxx-setting-changed", "setting:wallet").replace("{value}", walletString + (bankAccount.isEmpty()?"":"§6:§e"+bankAccount)) );
		}
		
		
		return true;
	}
	
	//set the traders type
	public boolean setType(Player player, Trader trader, String typeString)
	{
		
		EcoNpcType type = EcoNpcType.getTypeByName(typeString);
		
		//show current trader type
		if ( type == null )
		{
			locale.sendMessage(player, "key-value", "key", "#trader-type", "value", trader.getType());
		//	player.sendMessage( locale.getLocaleString("xxx-setting-value", "setting:trader").replace("{value}", trader.getType().toString()) );
		}
		//change trader type
		else
		{
			
			if ( !permsManager.has(player, "dtl.trader.types." + typeString ) )
			{
				locale.sendMessage(player, "error-nopermission");
				//player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:trader") );
				return true;
			}

			trader.getNpc().getTrait(TraderCharacterTrait.class).setType(type);

			locale.sendMessage(player, "key-change", "key", "#trader-type", "value", type);
			//player.sendMessage( locale.getLocaleString("xxx-setting-changed", "setting:trader").replace("{value}", typeString) );
		}
		
		return false;
	}
	
	//show the traders balance
	public boolean balance(Player player, Trader trader)
	{
		DecimalFormat f = new DecimalFormat("#.##");
		locale.sendMessage(player, "wallet-balance", "amount", f.format(trader.getWallet().getMoney()));
		//player.sendMessage( locale.getLocaleString("xxx-wallet", "action:balance").replace("{value}", f.format(trader.getWallet().getMoney()) ) );
		
		return true;
	}
	
	//withdraw money from the trader
	public boolean withdraw(Player player, Trader trader, String withdrawString)
	{
		
		double money = trader.getWallet().getMoney();
		double withdraw = 0.0;
		try 
		{
			withdraw = Double.valueOf(withdrawString);
		} 
		catch (NumberFormatException e)
		{
			locale.sendMessage(player, "error-argument-invalid", "argument", withdrawString);
		//	player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:amount") );
			return true;
		}
		
		if ( withdraw > money )
		{
			locale.sendMessage(player, "error-argument-invalid", "argument", withdrawString);
		//	player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:amount") );
			return true;
		}
		
		trader.getWallet().setMoney(money - withdraw);
		DecimalFormat f = new DecimalFormat("#.##");

		CitizensTrader.getEconomy().depositPlayer(player.getName(), withdraw);

		locale.sendMessage(player, "wallet-withdraw", "amount", withdrawString);
		locale.sendMessage(player, "wallet-balance", "amount", f.format(trader.getWallet().getMoney()));
	//	player.sendMessage( locale.getLocaleString("xxx-wallet", "action:withdrawed").replace("{value}", withdrawString) );
	//	player.sendMessage( locale.getLocaleString("xxx-wallet", "action:balance").replace("{value}", f.format(trader.getWallet().getMoney())) );
		
		
		return true;
	}
	
	//deposit money to the trader
	public boolean deposit(Player player, Trader trader, String depositString)
	{
		
		double money = trader.getWallet().getMoney();
		double deposit = 0.0;
		try 
		{
			deposit = Double.valueOf(depositString);
		} 
		catch (NumberFormatException e)
		{
			locale.sendMessage(player, "error-argument-invalid", "argument", depositString);
		//	player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:amount") );
			return true;
		}
		
		if ( !CitizensTrader.getEconomy().withdrawPlayer(player.getName(), deposit).type.equals(ResponseType.SUCCESS) )
		{
			locale.sendMessage(player, "error-argument-invalid", "argument", depositString);
			return true;
		}

		trader.getWallet().setMoney(money + deposit);
		DecimalFormat f = new DecimalFormat("#.##");
		
		locale.sendMessage(player, "wallet-deposit", "amount", depositString);
		locale.sendMessage(player, "wallet-balance", "amount", f.format(trader.getWallet().getMoney()));	
	//	player.sendMessage( locale.getLocaleString("xxx-wallet", "action:deposited").replace("{value}", depositString) );
	//	player.sendMessage( locale.getLocaleString("xxx-wallet", "action:balance").replace("{value}", f.format(trader.getWallet().getMoney())) );
		
		return true;
	}
	
	//setting the traders owner
	private boolean setOwner(Player player, Trader trader, String owner) {
		
		trader.getConfig().setOwner(owner);
		locale.sendMessage(player, "key-change", "key", "#owner", "value", owner);
		//player.sendMessage( locale.getLocaleString("xxx-setting-changed", "setting:owner").replace("{value}", owner) );
		
		return true;
	}
	
	//getting the traders owner
	private boolean getOwner(Player player, Trader trader) {

		locale.sendMessage(player, "key-value", "key", "#owner", "value", trader.getConfig().getOwner());
		//player.sendMessage( locale.getLocaleString("xxx-setting-value", "setting:owner").replace("{value}", trader.getConfig().getOwner() ) );

		
		return true;
	}
	
	//creating a trader, its easy ;)
	public boolean createTrader(Player player, String[] args)
	{
		String traderName = "";
		String owner = player.getName();
		
		EntityType entityType = EntityType.PLAYER;
		EcoNpcType traderType = getDefaultTraderType(player);
		WalletType walletType = getDefaultWalletType(player, traderType);
		
				
		//lets fetch the argument list
		for ( String arg : args )
		{
			if ( arg.startsWith("o:") )
			{
				owner = arg.substring(2);
				walletType = WalletType.OWNER;
			}
		
			else
			//trader type set?
			if ( arg.startsWith("t:") )
			{
				//do we have permissions to set this trader type?
				if ( !permsManager.has(player, "dtl.trader.types." + arg.substring(2) ) )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:trader") );
					return true;
				}
				traderType = EcoNpcType.getTypeByName(arg.substring(2));
				if ( traderType == null || traderType.isBanker() )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:type") );
					return true;
				}
			}
			else
			//wallet type set
			if ( arg.startsWith("w:") )
			{
				//do we have permissions to set this wallet type?
				if ( !permsManager.has(player, "dtl.trader.wallets." + arg.substring(2) ) )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:wallet") );
					return true;
				}
				walletType = WalletType.getTypeByName(arg.substring(2));
			}
			else
			//entity type set
			if ( arg.startsWith("e:") )
			{
				entityType = EntityType.fromName(arg.substring(2));
			}
			else
			{
				traderName += arg + " ";
			}
		}

		if ( traderName.isEmpty() || args.length == 1 || traderName.equals("create ") )
			traderName = "NPC";
		else 
			traderName = traderName.substring(7, traderName.length()-1);
		
		if ( walletType == null || traderType == null || entityType == null )
		{
			locale.sendMessage(player, "error-nopermission");
		//	player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
			return true;
		}
		
		//creating the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(entityType, traderName);
		npc.addTrait(TraderCharacterTrait.class);
		npc.addTrait(MobType.class);
		npc.getTrait(MobType.class).setType(entityType);
		npc.spawn(player.getLocation());
		
		npc.getTrait(TraderCharacterTrait.class).implementTrader();
		//change the trader settings
		TraderConfigPart settings = npc.getTrait(TraderCharacterTrait.class).getConfig();
		npc.getTrait(TraderCharacterTrait.class).setType(traderType);
		settings.getWallet().setType(walletType);
		
	
		settings.setOwner(owner);

		locale.sendMessage(player, "trader-created", "player", player.getName(), "trader", traderName);
	//	player.sendMessage( locale.getLocaleString("xxx-created-xxx", "entity:player", "entity:trader").replace("{name}", player.getName()) );
		return true;
	}
}

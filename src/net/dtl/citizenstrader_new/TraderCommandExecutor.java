package net.dtl.citizenstrader_new;

import java.text.DecimalFormat;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dtl.citizenstrader_new.TraderCharacterTrait.TraderType;
import net.dtl.citizenstrader_new.traders.EconomyNpc;
import net.dtl.citizenstrader_new.traders.Trader;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.TraderTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait.WalletType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * 
 * @author Dandielo
 *
 */
public final class TraderCommandExecutor implements CommandExecutor {
	
	//Config values
	//	private boolean debug;
	
	//plugin instance
	public static CitizensTrader plugin;
	
	//managers
	private static NpcEcoManager traderManager;
	private static PermissionsManager permsManager;
	private static LocaleManager locale;

	
	//constructor
	public TraderCommandExecutor(CitizensTrader instance) {
		plugin = instance;

		locale = CitizensTrader.getLocaleManager();
		permsManager = CitizensTrader.getPermissionsManager();
		traderManager = CitizensTrader.getNpcEcoManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		
		//is player
		if ( sender instanceof Player )
		{
			Player player = (Player) sender;
			
			if ( args.length < 1 )
			{
				player.sendMessage(ChatColor.AQUA + "DtlTraders " + plugin.getDescription().getVersion() + ChatColor.RED + " - Trader commands list" );
				return false;
			}
			
			
			//get the selected NPC
			EconomyNpc economyNpc = traderManager.getInteractionNpc(player.getName());
			

			
			//no npc selected
			if ( economyNpc == null )
			{
				
				//reload plugin
				if ( args[0].equalsIgnoreCase("create") )
				{
					if ( !this.generalChecks(player, "create", null, args, 3) )
						return true;
					
					return createTrader(player, args);
				}
				
				
				return true;
			}
			//npc has been selected
			else
			{
				//is trader type
				if ( !( economyNpc instanceof Trader ) )
				{
					player.sendMessage( locale.getLocaleString("no-trader-selected") );
					return true;
				}
				
				Trader trader = (Trader) economyNpc;
				
				
				//list command
				if ( args[0].equals("list") )
				{
					if ( !this.generalChecks(player, "list", ( args.length > 1 ? args[1] : null ), args, 2) )
						return true;
					
					return getItemList(player, trader, args, TraderStatus.getByName(args[1]) );	
				}
				if ( args[0].equals("type") )
				{
					if ( !this.generalChecks(player, "type", null, args, 1) )
						return true;
					
					return setType(player, trader, ( args.length > 1 ? args[1] : "" ) );
				}
				if ( args[0].equals("wallet") )
				{
					if ( !this.generalChecks(player, "wallet", null, args, 1) )
						return true;
					
					return setWallet(player, trader, ( args.length > 1 ? args[1] : "" ), ( args.length > 2 ? args[2] : "" ) );
				}
				if ( args[0].equals("owner") )
				{
					if ( !this.generalChecks(player, "owner", null, args, 1) )
						return true;
					
					return setOwner(player, trader, ( args.length > 1 ? args[1] : "" ) );
				}
				if ( args[0].equals("balance") )
				{
					if ( !this.generalChecks(player, "balance", null, args, 1) )
						return true;
					
					return balance(player, trader);
				}
				if ( args[0].equals("withdraw") )
				{
					if ( !this.generalChecks(player, "withdraw", null, args, 2) )
						return true;
					
					return withdraw(player, trader, args[1]);
				}
				if ( args[0].equals("deposit") )
				{
					if ( !this.generalChecks(player, "deposit", null, args, 2) )
						return true;
					
					return withdraw(player, trader, args[1]);
				}
				
				return true;
			}
				
		}		
		else
		{
			
			if ( args[0].equals("reload") )
			{
				
				sender.sendMessage(locale.getLocaleString("reload-config"));
				CitizensTrader.getInstance().getItemConfig().reloadConfig();
				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean generalChecks(Player player, String commandPermission, String optionsPermission, String[] args, int size)
	{
		
		//check permissions
		if ( !permsManager.has(player, "dtl.trader.commands." + commandPermission)  )
		{
			player.sendMessage( locale.getLocaleString("no-permissions") );
			return false;
		}
		
		if ( optionsPermission != null )
		//check permissions
		if ( !permsManager.has(player, "dtl.trader.options." + optionsPermission)  )
		{
			player.sendMessage( locale.getLocaleString("no-permissions") );
			return false;
		}
		
		//have we got the needed args?
		if ( args.length > size )
		{
			player.sendMessage( locale.getLocaleString("missing-args") );
			return false;
		}	
		
		return true;
	}
	

	public TraderType getDefaultTraderType(Player player) {
		
		//server trader as default
		if ( permsManager.has(player, "dtl.trader.types.server") )
			return TraderType.SERVER_TRADER;
		else
		//next default is player trader 
		if ( permsManager.has(player, "dtl.trader.types.player") )
			return TraderType.SERVER_TRADER;
		
		//else return no default
		return null;
	}
	
	public WalletType getDefaultWalletType(Player player) {
		//server default is infinite
		if ( permsManager.has(player, "dtl.trader.wallets.infinite") )
			return WalletType.INFINITE;
		else
		//next default is npc wallet
		if ( permsManager.has(player, "dtl.trader.wallets.npc") )
			return WalletType.NPC_WALLET;
		else
		//next default is player wallet
		if ( permsManager.has(player, "dtl.trader.wallets.owner") )
			return WalletType.OWNER_WALLET;
		else
		//next server default is custom bank
		if ( permsManager.has(player, "dtl.trader.wallets.bank") )
			return WalletType.BANK;
		
		//else return no default
		return null;
	}

	
	
	public boolean getItemList(Player player, Trader trader, String[] args, TraderStatus status) 
	{
		//default page '0'
		int page = 0;
		
		try 
		{

			//have we maybe got a page number?
			if ( args.length > 2 )
			{
				//get the page number, My Precious... ;>
				page = Integer.parseInt(args[2]) - 1;
			}
			
		} 
		catch (NumberFormatException e)
		{
			
			//come on can;t you write a normal number... ?
			player.sendMessage( locale.getLocaleString("invalid-args") );
			player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "list").replace("{args}", "<transaction> [page]") );
			return true;
		}
		
		int size = trader.getTraderStock().getStockSize(status);
		int totalPages = ( size % 10 == 0 ? ( size / 10 ) : ( size / 10 ) + 1 );
		
		
		//we got a item list
		player.sendMessage( locale.getLocaleString("list-header").replace("{curp}", "" + page).replace("{allp}", "" + totalPages) );
		
		
		for ( String item : trader.getTraderStock().getItemList(status, locale.getLocaleString("list-message"), page) )
			player.sendMessage(item);
		return true;
	}
	
	//set the traders wallet type
	public boolean setWallet(Player player, Trader trader, String walletString, String bankAccount)
	{
		
		if ( !permsManager.has(player, "dtl.trader.wallets." + walletString ) )
		{
			player.sendMessage( locale.getLocaleString("invalid-wallet-perm") );
			return true;
		}

		
		WalletType wallet = WalletType.getTypeByName(walletString);
		
		
		//towny
		
		
		//show wallet
		if ( wallet == null )
		{
			String account = ""; 
			if ( trader.getTraderConfig().getWalletType().equals(WalletType.TOWNY) )
				account = trader.getWallet().getTown();
			if ( trader.getTraderConfig().getWalletType().equals(WalletType.SIMPLE_CLANS) )
				account = trader.getWallet().getClan();
			if ( trader.getTraderConfig().getWalletType().equals(WalletType.BANK) )
				account = trader.getWallet().getBank();
			
			//send message
			player.sendMessage( locale.getLocaleString("wallet-message").replace("{wallet}", trader.getTraderConfig().getWalletType().toString()).replace("{account}", account) );
			
		}
		//change wallet
		else
		{
			//bank
			if ( wallet.equals(WalletType.BANK) )
			{
				if ( bankAccount.isEmpty() )
					return true;
				if ( !trader.getWallet().setBank(player.getName(), bankAccount) )
				{
					player.sendMessage( locale.getLocaleString("invalid-wallet-bank") );
					return true;
				}
			}
			else
			//clan
			if ( wallet.equals(WalletType.SIMPLE_CLANS) )
			{
				if ( CitizensTrader.getSimpleClans() == null ) {
					player.sendMessage( locale.getLocaleString("invalid-wallet") );
					return true;
				}
				
				if ( bankAccount.isEmpty() )
					return true;
				trader.getWallet().setClan(bankAccount);
				
			}
			else
			//towny
			if ( wallet.equals(WalletType.TOWNY) )
			{
				if ( CitizensTrader.getTowny() == null )
				{
					player.sendMessage( locale.getLocaleString("invalid-wallet") );
					return true;
				}
				
				if ( bankAccount.isEmpty() )
					return true;
				trader.getWallet().setTown(bankAccount);
				
			}
			
			
			//set the wallet type for both trader and wallet
			trader.getTraderConfig().setWalletType(wallet);

			//send message
			player.sendMessage( locale.getLocaleString("wallet-changed").replace("{wallet}", walletString) );
		}
		
		
		return true;
	}
	
	//set the traders type
	public boolean setType(Player player, Trader trader, String typeString)
	{
		
		if ( !permsManager.has(player, "dtl.trader.types." + typeString ) )
		{
			player.sendMessage( locale.getLocaleString("invalid-ttype-perm") );
			return true;
		}
		
		TraderType type = TraderType.getTypeByName(typeString);
		
		//show current trader type
		if ( type == null )
		{
			player.sendMessage( locale.getLocaleString("type-message").replace("{type}", trader.getTraderConfig().getTraderType().toString()) );
		}
		//change trader type
		else
		{

			trader.getTraderConfig().setTraderType(type);
			trader.getNpc().getTrait(TraderCharacterTrait.class).setTraderType(type);
			
			player.sendMessage( locale.getLocaleString("type-changed").replace("{type}", typeString) );
		}
		
		return true;
	}
	
	//show the traders balance
	public boolean balance(Player player, Trader trader)
	{
		DecimalFormat f = new DecimalFormat("#.##");
		player.sendMessage( locale.getLocaleString("balance-message").replace("{balance}", f.format(trader.getWallet().getMoney()) ) );
		
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
			player.sendMessage( locale.getLocaleString("invalid-args") );
			return true;
		}
		
		if ( withdraw > money )
		{
			player.sendMessage( locale.getLocaleString("amount-unavailable") );
			return true;
		}
		
		trader.getWallet().setMoney(money - withdraw);
		DecimalFormat f = new DecimalFormat("#.##");

		plugin.getEconomy().depositPlayer(player.getName(), withdraw);
		
		player.sendMessage( locale.getLocaleString("withdraw-message").replace("{amount}", withdrawString) );
		player.sendMessage( locale.getLocaleString("balance-message").replace("{balance}", f.format(trader.getWallet().getMoney()) ) );
		
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
			player.sendMessage( locale.getLocaleString("invalid-args") );
			return true;
		}
		
		plugin.getEconomy().withdrawPlayer(player.getName(), deposit);
		
		trader.getWallet().setMoney(money + deposit);
		DecimalFormat f = new DecimalFormat("#.##");
		
		player.sendMessage( locale.getLocaleString("deposit-message").replace("{amount}", depositString) );
		player.sendMessage( locale.getLocaleString("balance-message").replace("{balance}", f.format(trader.getWallet().getMoney()) ) );
		
		return true;
	}
	
	//setting the traders owner
	private boolean setOwner(Player player, Trader trader, String owner) {
		
		trader.getTraderConfig().setOwner(owner);
		player.sendMessage( locale.getLocaleString("owner-changed").replace("{player}", owner) );
		
		return true;
	}
	
	//getting the traders owner
	private boolean getOwner(Player player, Trader trader) {

		player.sendMessage( locale.getLocaleString("owner-message").replace("{player}", trader.getTraderConfig().getOwner() ) );
		//player.sendMessage("!OWNER CHANGED!");
		
		return true;
	}
	
	//creating a trader, its easy ;)
	public boolean createTrader(Player player, String[] args)
	{
		String traderName = "";
		String owner = player.getName();
		String clanTag = "";
		String townName = "";
		
		EntityType entityType = EntityType.PLAYER;
		TraderType traderType = getDefaultTraderType(player);
		WalletType walletType = getDefaultWalletType(player);
		
		
		//lets fetch the argument list
		for ( String arg : args )
		{
			if ( arg.startsWith("o:") )
			{
				owner = arg.substring(2);
			}
			else
			if ( arg.startsWith("sc:") )
			{
				clanTag = arg.substring(3);
			}
			else
			if ( arg.startsWith("town:") )
			{
				townName = arg.substring(5);
			}
			else
			//trader type set?
			if ( arg.startsWith("t:") )
			{
				//do we have permissions to set this trader type?
				if ( !permsManager.has(player, "dtl.trader.types." + arg.substring(2) ) )
				{
					player.sendMessage( locale.getLocaleString("invalid-ttype-perm") );
					return true;
				}
				traderType = TraderType.getTypeByName(arg.substring(2));
			}
			else
			//wallet type set
			if ( arg.startsWith("w:") )
			{
				//do we have permissions to set this wallet type?
				if ( !permsManager.has(player, "dtl.trader.wallets." + arg.substring(2) ) )
				{
					player.sendMessage( locale.getLocaleString("invalid-wallet-perm") );
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

		if ( traderName.isEmpty() )
			traderName = "NPC";
		else
			traderName = traderName.substring(7, traderName.length()-1);
		
		if ( walletType == null || traderType == null || entityType == null )
		{
			player.sendMessage( locale.getLocaleString("no-defaults") );
			return true;
		}
		
		//creating the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(entityType, traderName);
		npc.addTrait(TraderCharacterTrait.class);
		npc.addTrait(MobType.class);
		npc.getTrait(MobType.class).setType(entityType);
		npc.spawn(player.getLocation());
		
		//change the trader settings
		TraderTrait settings = npc.getTrait(TraderCharacterTrait.class).getTraderTrait();
		npc.getTrait(TraderCharacterTrait.class).setTraderType(traderType);
		settings.setWalletType(walletType);
		if ( walletType.equals(WalletType.SIMPLE_CLANS) )
			settings.getWallet().setClan(clanTag);
		if ( walletType.equals(WalletType.TOWNY) )
			settings.getWallet().setTown(townName);
		settings.setOwner(owner);
		
		player.sendMessage( locale.getLocaleString("trader-created") );
		return true;
	}
	
	
	
}
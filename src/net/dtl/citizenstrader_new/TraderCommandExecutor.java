package net.dtl.citizenstrader_new;

import java.text.DecimalFormat;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dtl.citizenstrader_new.TraderCharacterTrait.TraderType;
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

public final class TraderCommandExecutor implements CommandExecutor {
	//trader config
	protected ItemsConfig config = CitizensTrader.getInstance().getItemConfig();
	
	//general
	public static CitizensTrader plugin;
	private NpcEcoManager traderManager;
	private PermissionsManager permsManager;
	
	//locale manager
	private LocaleManager locale;
	
	public TraderCommandExecutor(CitizensTrader instance) {
		plugin = instance;
		new CitizensTrader();

		locale = CitizensTrader.getLocaleManager();
		permsManager = CitizensTrader.getPermissionsManager();
		traderManager = CitizensTrader.getNpcEcoManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		//just to be sure we don't get anything more
		if ( !cmd.getName().equalsIgnoreCase("trader") )
			return false;
		
		//if the we got a player problem
		if ( sender instanceof Player )
		{
			Player player = (Player) sender;
			
			
			//if ( !( traderManager.getInteractionNpc(player.getName()) instanceof Trader ) )
			//	return true;
			
			Trader trader = (Trader) traderManager.getInteractionNpc(player.getName());
			
			
			
			//check if we can use commands
			if ( !permsManager.has(player, "dtl.trader.commands") )
			{
				
				//goodbye!
				player.sendMessage( locale.getLocaleString("no-permissions") );
				return true;
			}
			
			
			//check if we have any arguments
			if ( args.length < 1 )
			{
				
				player.sendMessage(ChatColor.AQUA + "DtlTraders " + plugin.getDescription().getVersion() + ChatColor.RED + " - Commands list" );
				return false;
			}
			
			
			//lets see what arguments we use
			//looks like we wan't to buy something
			if ( args[0].equals("reload") )
			{
				//can we edit the traders sell mode?
				if ( !permsManager.has(player, "dtl.trader.reload") )
				{
					
					//have a good flight!
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				player.sendMessage(locale.getLocaleString("reload-config"));
				
				config.reloadConfig();
				
				return true;
			}
			else
			if ( args[0].equals("sell") )
			{
				
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage( locale.getLocaleString("no-trader-selected") );
					return true;
				}
				
				
				//can we edit the traders sell mode?
				if ( !permsManager.has(player, "dtl.trader.options.sell") )
				{
					
					//have a good flight!
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				//have we got the needed args?
				if ( args.length < 2 )
				{
					player.sendMessage( locale.getLocaleString("missing-args") );
					player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "sell").replace("{args}", "<list>") );
					return true;
				}				
				
				//can we get that list plz?
				if ( permsManager.has(player, "dtl.trader.commands.list") 
						&& args[1].equals("list") ) 
				{

					return getItemList(player, trader, args, TraderStatus.SELL);
				}
				else
				//More...
				if ( permsManager.has(player, "dtl.trader.commands.add") )
				{
					player.sendMessage(ChatColor.RED + "Not supported! Sorry :<");
					
				}
				else 
				//My precious... Kill him!
				if ( permsManager.has(player, "dtl.trader.commands.remove") )
				{
					player.sendMessage(ChatColor.RED + "Not supported! Sorry :<");
					
				}
				else 	
				//Ok, i will give you that cow you will give me 4 diamonds, ok?
				if ( permsManager.has(player, "dtl.trader.commands.edit") )
				{
					player.sendMessage(ChatColor.RED + "Not supported! Sorry :<");
					
				}
				
			}
			else
			//lets sell all the junk! 
			if ( args[0].equals("buy") )
			{
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage( locale.getLocaleString("no-trader-selected") );
					return true;
				}
				
				
				if ( !permsManager.has(player, "dtl.trader.options.buy") )
				{
					
					//have a good flight! (copied...)
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				//have we got the needed args?
				if ( args.length < 2 )
				{
					player.sendMessage( locale.getLocaleString("missing-args") );
					player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "buy").replace("{args}", "<list>") );
					return true;
				}				
				
				//lets see...
				if ( permsManager.has(player, "dtl.trader.commands.list") 
						&& args[1].equals("list") ) 
				{
					
					return getItemList(player, trader, args, TraderStatus.BUY);
				}
				else
				//I want that!
				if ( permsManager.has(player, "dtl.trader.commands.add") )
				{
					
				}
				else 
				//just take it away from me...
				if ( permsManager.has(player, "dtl.trader.commands.remove") )
				{
					
				}
				else 	
				//nothing to trade with...
				if ( permsManager.has(player, "dtl.trader.commands.edit") )
				{
					
				}
				
				
				
			}
			else
			//Hmm, bank or sock?
			if ( args[0].equals("wallet") )
			{

				
				//can he change the wallet type?
				if ( !permsManager.has(player, "dtl.trader.commands.wallet") )
				{
					//have a good flight! (copied...)
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage( locale.getLocaleString("no-trader-selected") );
					return true;
				}
				
				//are all on board?
				if ( args.length < 1 )
				{
					player.sendMessage( locale.getLocaleString("missing-args") );
					player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "wallet").replace("{args}", "[wallet] [bank_account_name]") );
					return true;
				}	
				
				return setWallet(player, trader, ( args.length > 1 ? args[1] : "" ), ( args.length > 2 ? args[2] : "" ) );
			}
			else
			//i don't like my server trader :<
			if ( args[0].equals("type") )
			{
				
				if ( !permsManager.has(player, "dtl.trader.commands.type") )
				{
					//see ya with a better permission ;)
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage( locale.getLocaleString("no-trader-selected") );
					return true;
				}
				
				//are all on board?
				if ( args.length < 1 )
				{
					player.sendMessage( locale.getLocaleString("missing-args") );
					player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "type").replace("{args}", "[type]") );
					return true;
				}	
				
				return setType(player, trader, ( args.length > 1 ? args[1] : "" ) );
			}
			else
			//lets create a trader!
			if ( args[0].equals("create") )
			{
				
				if ( !permsManager.has(player, "dtl.trader.commands.create") )
				{
					//you can't create life!
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				
				//have we got the needed args?
				if ( args.length < 2 )
				{
					player.sendMessage( locale.getLocaleString("missing-args") );
					player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "create").replace("{args}", "<a name anywhere> [t:type] [w:wallet] [e:entity]") );
					return true;
				}
				
				return createTrader(player, args);
			}
			else
			//lets create a trader!
			if ( args[0].equals("dismiss") )
			{
				// TODO create a nice dismiss function 
				
				if ( !permsManager.has(player, "dtl.trader.commands.dismiss") )
				{
					//you can't create life!
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				player.sendMessage(ChatColor.RED + "Not supported! Sorry :<");
				
				return false;
				
			}
			else 
			//show me your money!
			if ( args[0].equals("balance") )
			{

				
				if ( !permsManager.has(player, "dtl.trader.commands.balance") )
				{
					//you can't create life!
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage( locale.getLocaleString("no-trader-selected") );
					return true;
				}
				
				
				//only npc wallets can be managed!
				if ( !trader.equalsWalletType(WalletType.NPC_WALLET) )
				{
					//lets inform about that mistake
					player.sendMessage( locale.getLocaleString("invalid-wallet") );
					return true;
				}
				
				
				return balance(player, trader);
			}
			else 
			//show me your money!
			if ( args[0].equals("withdraw") )
			{

				
				if ( !permsManager.has(player, "dtl.trader.commands.withdraw") )
				{
					//no permissions available
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage( locale.getLocaleString("no-trader-selected") );
					return true;
				}
				
				
				//only npc wallets can be managed!
				if ( !trader.equalsWalletType(WalletType.NPC_WALLET) )
				{
					//lets inform about that mistake
					player.sendMessage( locale.getLocaleString("invalid-wallet") );
					return true;
				}
				
				
				//we want to withdraw nothing...
				if ( args.length < 2 )
				{
					player.sendMessage( locale.getLocaleString("missing-args") );
					player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "withdraw").replace("{args}", "<amount>") );
					return true;
				}
				
				return withdraw(player, trader, args[1]);
			}
			else 
			//show me your money!
			if ( args[0].equals("deposit") )
			{

				if ( !permsManager.has(player, "dtl.trader.commands.deposit") )
				{
					//no permissions available
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage( locale.getLocaleString("no-trader-selected") );
					return true;
				}
				
				
				//only npc wallets can be managed!
				if ( !trader.equalsWalletType(WalletType.NPC_WALLET) )
				{
					//lets inform about that mistake
					player.sendMessage( locale.getLocaleString("invalid-wallet") );
					return true;
				}
				
				
				//we want to withdraw nothing...
				if ( args.length < 2 )
				{
					player.sendMessage( locale.getLocaleString("missing-args") );
					player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "deposit").replace("{args}", "<amount>") );
					return true;
				}
				
				
				//a single function is much easier to understand ;P
				return deposit(player, trader, args[1]);
			}
			else 
			//show me your money!
			if ( args[0].equals("owner") )
			{
				//can he change the wallet type?
				if ( !permsManager.has(player, "dtl.trader.commands.owner") )
				{
					//have a good flight! (copied...)
					player.sendMessage( locale.getLocaleString("no-permissions") );
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage( locale.getLocaleString("no-trader-selected") );
					return true;
				}
				
				//are all on board?
				if ( args.length < 2 )
				{
					return getOwner(player, trader);
				}	
				
				
				return setOwner(player, trader, args[1]);
			}
		}
		//is God trying to command a trader? 
		else
		{
			
			if ( args[0].equals("reload") )
			{
				//can we edit the traders sell mode?
			//	if ( !permsManager.has(s, "dtl.trader.reload") )
			//	{
					
					//have a good flight!
				//	player.sendMessage( locale.getLocaleString("no-permissions") );
				//	return true;
			//	}
				
				sender.sendMessage(locale.getLocaleString("reload-config"));
				
				config.reloadConfig();
				
				return true;
			}
		}
		
		return false;
	}
	

	public TraderType getDefaultTraderType(Player player) {
		//server trader as default
		if ( permsManager.has(player, "dtl.trader.options.type.server") )
			return TraderType.SERVER_TRADER;
		else
		//next default is player trader 
		if ( permsManager.has(player, "dtl.trader.options.type.player") )
			return TraderType.SERVER_TRADER;
		
		//else return no default
		return null;
	}
	
	public WalletType getDefaultWalletType(Player player) {
		//server default is infinite
		if ( permsManager.has(player, "dtl.trader.options.wallet.infinite") )
			return WalletType.INFINITE;
		else
		//next server default is custom bank
		if ( permsManager.has(player, "dtl.trader.options.wallet.bank") )
			return WalletType.BANK;
		else
		//next default is npc wallet
		if ( permsManager.has(player, "dtl.trader.options.wallet.npc-wallet") )
			return WalletType.NPC_WALLET;
		else
		//next default is player wallet
		if ( permsManager.has(player, "dtl.trader.options.wallet.owner-wallet") )
			return WalletType.OWNER_WALLET;
		
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
			player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "list").replace("{args}", "[page]") );
			return true;
		}
		
		//we got a item list
		player.sendMessage( locale.getLocaleString("list-header") );
		
		
		for ( String item : trader.getTraderStock().getItemList(status, locale.getLocaleString("list-message"), page) )
			player.sendMessage(item);
		return true;
	}
	
	public boolean addItem()
	{
		return true;
	}
	
	public boolean editItem()
	{
		return true;
	}
	
	public boolean removeItem()
	{
		return true;
	}
	
	//set the traders wallet type
	public boolean setWallet(Player player, Trader trader, String walletString, String bankName)
	{
		
		if ( !permsManager.has(player, "dtl.trader.options.wallet." + walletString ) )
		{
			player.sendMessage( locale.getLocaleString("invalid-wallet-perm") );
			return true;
		}

		//have w a bank we can use?
		if ( !bankName.isEmpty() )
			if ( !trader.getWallet().setBank(player.getName(), bankName) )
			{
				player.sendMessage( locale.getLocaleString("invalid-wallet-bank") );
				return true;
			}
		
		
		WalletType wallet = WalletType.getTypeByName(walletString);
		
		//show wallet
		if ( wallet == null )
		{
			//send message
			player.sendMessage( locale.getLocaleString("wallet-message").replace("{wallet}", trader.getTraderConfig().getWalletType().toString()) );
			
		}
		//change wallet
		else
		{

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
		
		if ( !permsManager.has(player, "dtl.trader.options.type." + typeString ) )
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
		EntityType entityType = EntityType.PLAYER;
		TraderType traderType = getDefaultTraderType(player);
		WalletType walletType = getDefaultWalletType(player);
		
		
		//lets fetch the argument list
		for ( String arg : args )
		{
			//trader type set?
			if ( arg.startsWith("t:") )
			{
				//do we have permissions to set this trader type?
				if ( !permsManager.has(player, "dtl.trader.options.type." + arg.substring(2) ) )
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
				if ( !permsManager.has(player, "dtl.trader.options.wallet." + arg.substring(2) ) )
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
				//do we have permissions to set this entity type?
				if ( !permsManager.has(player, "dtl.trader.options.entity." + arg.substring(2) ) )
				{
					player.sendMessage( locale.getLocaleString("invalid-entity-perm") );
					return true;
				}
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
		settings.setOwner(player.getName());
		
		player.sendMessage( locale.getLocaleString("trader-created") );
		return true;
	}
	
	
	
}
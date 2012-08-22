package net.dtl.citizenstrader_new;

import java.text.DecimalFormat;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.traders.Trader;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.TraderTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait.TraderType;
import net.dtl.citizenstrader_new.traits.TraderTrait.WalletType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public final class TraderCommandExecutor implements CommandExecutor {
	public static CitizensTrader plugin;
	private static TraderConfig config;
	//private TraderCharacterTrait trader;
	private TraderManager traderManager;
	private PermissionsManager permsManager;
	
	public TraderCommandExecutor(CitizensTrader instance) {
		plugin = instance;
		new CitizensTrader();

		this.permsManager = CitizensTrader.getPermissionsManager();
		this.traderManager = CitizensTrader.getTraderManager();
		config = CitizensTrader.getTraderConfig();
		//trader = ((TraderCharacterTrait) CitizensAPI.getTraitFactory().getTrait(TraderCharacterTrait.class));
	}
	
	public boolean argsLength(String args[],int min,int max) {
		if ( args.length < min ) {
			return false;
		}
		if ( args.length > max ) {
			return false;
		}
		return true;
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
			
			
		/*	needs to be recoded!!!
			
			//does we have anything to interact with?
			*/
			Trader trader = traderManager.getOngoingTrades(player.getName());
			
			
			
			//check if we can use commands
			if ( !permsManager.has(player, "dtl.trader.commands") )
			{
				
				//goodbye!
				player.sendMessage(ChatColor.RED + "Sorry, you don't have permission to use commands");
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
			if ( args[0].equals("sell") )
			{
				
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "!NO TRADER SELECTED!");
					return true;
				}
				
				
				//can we edit the traders sell mode?
				if ( !permsManager.has(player, "dtl.trader.options.sell") )
				{
					
					//have a good flight!
					player.sendMessage(ChatColor.RED + "!NO PERMISSIONS!");
					return true;
				}
				
				//have we got the needed args?
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "!MISSING ARGUMENTS!");
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
					
				}
				else 
				//My precious... Kill him!
				if ( permsManager.has(player, "dtl.trader.commands.remove") )
				{
					
				}
				else 	
				//Ok, i will give you that cow you will give me 4 diamonds, ok?
				if ( permsManager.has(player, "dtl.trader.commands.edit") )
				{
					
				}
				
			}
			else
			//lets sell all the junk! 
			if ( args[0].equals("buy") )
			{
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "!NO TRADER SELECTED!");
					return true;
				}
				
				
				if ( !permsManager.has(player, "dtl.trader.options.buy") )
				{
					
					//have a good flight! (copied...)
					player.sendMessage(ChatColor.RED + "!NO PERMISSIONS!");
					return true;
				}
				
				//have we got the needed args?
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "!MISSING ARGUMENTS!");
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
					player.sendMessage(ChatColor.RED + "!NO PERMISSIONS!");
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "!NO TRADER SELECTED!");
					return true;
				}
				
				//are all on board?
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "!MISSING ARGUMENTS!");
					return true;
				}	
				
				return setWallet(player, trader, args[1], ( args.length > 2 ? args[2] : "" ) );
			}
			else
			//i don't like my server trader :<
			if ( args[0].equals("type") )
			{
				
				if ( !permsManager.has(player, "dtl.trader.commands.type") )
				{
					//see ya with a better permission ;)
					player.sendMessage(ChatColor.RED + "!NO PERMISSIONS!");
					return true;
				}
				
				return setType(player, trader, args[1]);
			}
			else
			//lets create a trader!
			if ( args[0].equals("create") )
			{
				
				if ( !permsManager.has(player, "dtl.trader.commands.create") )
				{
					//you can't create life!
					player.sendMessage(ChatColor.RED + "!NO PERMISSIONS!");
					return true;
				}
				
				
				//have we got the needed args?
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "!MISSING ARGUMENTS!");
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
					player.sendMessage(ChatColor.RED + "!NO PERMISSIONS!");
					return true;
				}
				
				return false;
				
			}
			else 
			//show me your money!
			if ( args[0].equals("balance") )
			{

				
				if ( !permsManager.has(player, "dtl.trader.commands.balance") )
				{
					//you can't create life!
					player.sendMessage(ChatColor.RED + "!NO PERMISSIONS!");
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "!NO TRADER SELECTED!");
					return true;
				}
				
				
				//only npc wallets can be managed!
				if ( !trader.equalsWalletType(WalletType.NPC_WALLET) )
				{
					//lets inform about that mistake
					player.sendMessage(ChatColor.RED + "!INVALID WALLET!");
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
					player.sendMessage(ChatColor.RED + "!NO PERMISSIONS!");
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "!NO TRADER SELECTED!");
					return true;
				}
				
				
				//only npc wallets can be managed!
				if ( !trader.equalsWalletType(WalletType.NPC_WALLET) )
				{
					//lets inform about that mistake
					player.sendMessage(ChatColor.RED + "!INVALID WALLET!");
					return true;
				}
				
				
				//we want to withdraw nothing...
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "!MISSING ARGUMENTS!");
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
					player.sendMessage(ChatColor.RED + "!NO PERMISSIONS!");
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "!NO TRADER SELECTED!");
					return true;
				}
				
				
				//only npc wallets can be managed!
				if ( !trader.equalsWalletType(WalletType.NPC_WALLET) )
				{
					//lets inform about that mistake
					player.sendMessage(ChatColor.RED + "!INVALID WALLET!");
					return true;
				}
				
				
				//we want to withdraw nothing...
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "!MISSING ARGUMENTS!");
					return true;
				}
				

				//a single function is much easier to understand ;P
				return deposit(player, trader, args[1]);
			}
		}
		//is God trying to command a trader? 
		else
		{
			
			
		}
		
		return false;
	}
	
	public TraderType getDefaultTraderType(Player player) {
		//server trader as default
		if ( permsManager.has(player, "dtl.trader.options.server") )
			return TraderType.SERVER_TRADER;
		else
		//next default is player trader 
		if ( permsManager.has(player, "dtl.trader.options.player") )
			return TraderType.SERVER_TRADER;
		
		//else return no default
		return null;
	}
	
	public WalletType getDefaultWalletType(Player player) {
		//server default is infinite
		if ( permsManager.has(player, "dtl.trader.options.infinite") )
			return WalletType.INFINITE;
		else
		//next server default is custom bank
		if ( permsManager.has(player, "dtl.trader.options.bank") )
			return WalletType.BANK;
		else
		//next default is npc wallet
		if ( permsManager.has(player, "dtl.trader.options.npc-wallet") )
			return WalletType.NPC_WALLET;
		else
		//next default is player wallet
		if ( permsManager.has(player, "dtl.trader.options.owner-wallet") )
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
			player.sendMessage(ChatColor.RED + "!INVALID ARGUMENTS!");
			return true;
		}
		
		//we got a item list
		player.sendMessage(ChatColor.RED + "Trader stock list " + ChatColor.AQUA + "# page " + String.valueOf(page+1));
		
		
		for ( String item : trader.getTraderStock().getItemList(status, "- " + ChatColor.RED + "<in> " + ChatColor.WHITE + " <a> <p> " + ChatColor.YELLOW + " [<s>]", page) )
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
			player.sendMessage("!WRONG WALLET, NO PERMISSIONS!");
			return true;
		}

		//have w a bank we can use?
		if ( !bankName.isEmpty() )
			if ( !trader.getWallet().setBank(player.getName(), bankName) )
			{
				player.sendMessage("!WRONG BANK, NO PERMISSIONS!");
				return true;
			}
		
		
		WalletType wallet = WalletType.getTypeByName(walletString);
		
		//set the wallet type for both trader and wallet
		trader.getTraderConfig().setWalletType(wallet);
		
		//change the bank name or set a bank wallet
		
		
		player.sendMessage("!WALLET CHANGED!");
		return true;
	}
	
	//set the traders type
	public boolean setType(Player player, Trader trader, String typeString)
	{
		
		if ( !permsManager.has(player, "dtl.trader.options." + typeString ) )
		{
			player.sendMessage(ChatColor.RED + "!WRONG TYPE, NO PERMISSIONS!");
			return true;
		}
		
		TraderType type = TraderType.getTypeByName(typeString);
		trader.getTraderConfig().setTraderType(type);
		
		player.sendMessage("!TYPE CHANGED, RESET MANAGER MODE!");
		
		return true;
	}
	
	//show the traders balance
	public boolean balance(Player player, Trader trader)
	{
		DecimalFormat f = new DecimalFormat("#.##");
		player.sendMessage(ChatColor.RED + "Traders balance: " + ChatColor.AQUA + String.valueOf(f.format(trader.getWallet().getMoney())) );
		
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
			player.sendMessage("Wrong amount as argument");
			return true;
		}
		
		if ( withdraw > money )
		{
			player.sendMessage(ChatColor.RED + "This trader cannot give you that amount");
			return true;
		}
		
		trader.getWallet().setMoney(money - withdraw);
		DecimalFormat f = new DecimalFormat("#.##");

		plugin.getEconomy().depositPlayer(player.getName(), withdraw);
		
		player.sendMessage(ChatColor.RED + "You withdrawed " + ChatColor.AQUA + withdrawString + "");
		player.sendMessage(ChatColor.RED + "Traders balance: " + ChatColor.AQUA + f.format(trader.getWallet().getMoney()) + "");
		
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
			player.sendMessage("Wrong amount as argument");
			return true;
		}
		
		plugin.getEconomy().withdrawPlayer(player.getName(), deposit);
		
		trader.getWallet().setMoney(money + deposit);
		DecimalFormat f = new DecimalFormat("#.##");
		
		player.sendMessage(ChatColor.RED + "You deposited " + ChatColor.AQUA + depositString + "");
		player.sendMessage(ChatColor.RED + "Traders balance: " + ChatColor.AQUA + f.format(trader.getWallet().getMoney()) + "");
		
		return true;
	}
	
	//creating a trader, its easy ;)
	public boolean createTrader(Player player, String[] args)
	{
		String traderName = args[1];
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
				if ( !permsManager.has(player, "dtl.trader.options." + arg.substring(2) ) )
				{
					player.sendMessage(ChatColor.RED + "!WRONG TYPE, NO PERMISSIONS!");
					return true;
				}
				traderType = TraderType.getTypeByName(arg.substring(2));
			}
			else
			//wallet type set
			if ( arg.startsWith("w:") )
			{
				//do we have permissions to set this wallet type?
				if ( !permsManager.has(player, "dtl.trader.options." + arg.substring(2) ) )
				{
					player.sendMessage(ChatColor.RED + "!WRONG WALLET, NO PERMISSIONS!");
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
					player.sendMessage(ChatColor.RED + "!WRONG ENTITY, NO PERMISSIONS!");
					return true;
				}
				entityType = EntityType.fromName(arg.substring(2));
			}
		}
		
		
		if ( walletType == null || traderType == null )
		{
			player.sendMessage(ChatColor.RED + "!NO DEFAULTS FOUND WHILE CREATING!");
			return true;
		}
		
		//creating the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(entityType, traderName);
		npc.addTrait(TraderCharacterTrait.class);
		npc.spawn(player.getLocation());
		
		//change the trader settings
		TraderTrait settings = npc.getTrait(TraderCharacterTrait.class).getTraderTrait();
		settings.setTraderType(traderType);
		settings.setWalletType(walletType);
		
		
		player.sendMessage(ChatColor.RED + "!TRADER CREATED!");
		return true;
	}
	
	
	
	
	
}
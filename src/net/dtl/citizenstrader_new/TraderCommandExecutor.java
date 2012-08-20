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
					player.sendMessage(ChatColor.RED + "No trader selected (manager mode)");
					return true;
				}
				
				
				//can we edit the traders sell mode?
				if ( !permsManager.has(player, "dtl.trader.options.sell") )
				{
					
					//have a good flight!
					player.sendMessage(ChatColor.RED + "Sorry, you don't have permission to use 'sell' commands");
					return true;
				}
				
				//have we got the needed args?
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "Invalid arguments");
					return true;
				}				
				
				//can we get that list plz?
				if ( permsManager.has(player, "dtl.trader.commands.list") 
						&& args[1].equals("list") ) 
				{
					//defaulr page '0'
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
						player.sendMessage(ChatColor.RED + "That number doesn't exist!");
						return true;
					}
					
					//we got a item list
					player.sendMessage(ChatColor.RED + "Trader stock list " + ChatColor.AQUA + "# page " + String.valueOf(page+1));
					
					
					for ( String item : trader.getTraderStock().getItemList(TraderStatus.PLAYER_SELL, config.getListingFormat(), page) )
						player.sendMessage(item);
					
					return true;
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
				if ( !permsManager.has(player, "dtl.trader.options.buy") )
				{
					
					//have a good flight! (copied...)
					player.sendMessage(ChatColor.RED + "Sorry, you don't have permission to use 'buy' commands");
					return true;
				}
				
				//have we got the needed args?
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "Invalid arguments");
					return true;
				}				
				
				//lets see...
				if ( permsManager.has(player, "dtl.trader.commands.list") 
						&& args[1].equals("list") ) 
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
						player.sendMessage(ChatColor.RED + "That number doesn't exist!");
						return true;
					}
					
					//we got a item list
					player.sendMessage(ChatColor.RED + "Trader stock list " + ChatColor.AQUA + "# page " + String.valueOf(page+1));
					
					
					for ( String item : trader.getTraderStock().getItemList(TraderStatus.PLAYER_BUY, "- " + ChatColor.RED + "<in> " + ChatColor.WHITE + " <a> <p> " + ChatColor.YELLOW + " [<s>]", page) )
						player.sendMessage(item);
					
					return true;
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
					player.sendMessage(ChatColor.RED + "Sorry, you can't change the traders wallet type");
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "No trader selected (manager mode)");
					return true;
				}
				

				//are all on board?
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "Invalid arguments");
					return true;
				}	
				
				
				//why i don't use a switch? because i hate them!
				//my money, my money... ;>
				if ( args[1].equals("owner-wallet") 
						&& permsManager.has(player, "dtl.trader.options.wallet." + args[1]) )
				{
					
					//we changed the type!
					trader.getTraderConfig().setWalletType(WalletType.getTypeByName(args[1]));
					player.sendMessage(ChatColor.RED + "The traders wallet type changed to: " + ChatColor.AQUA + args[1]);
					
					return true;
				}
				else
				//put it all there, in one place
				if ( args[1].equals("owner-bank") 
						&& permsManager.has(player, "dtl.trader.options.wallet." + args[1]) )
				{
					
					//trader.getTraderConfig().setWalletType(WalletType.getTypeByName(args[1]));
					player.sendMessage(ChatColor.RED + "We are not supporting this atm, sorry :<");
					
					return true;
				}
				else
				//here take it and make something useful with it
				if ( args[1].equals("npc-wallet") 
						&& permsManager.has(player, "dtl.trader.options.wallet." + args[1]) )
				{

					//we changed the type!
					trader.getTraderConfig().setWalletType(WalletType.getTypeByName(args[1]));
					player.sendMessage(ChatColor.RED + "The traders wallet type changed to: " + ChatColor.AQUA + args[1]);
					
					return true;
				}
				else
				//any1 wan't a trader eating his bank account savings?
				if ( args[1].equals("bank") 
						&& permsManager.has(player, "dtl.trader.options.wallet." + args[1]) )
				{
					
					//trader.getTraderConfig().setWalletType(WalletType.getTypeByName(args[1]));
					player.sendMessage(ChatColor.RED + "We are not supporting this atm, sorry :<");
					
					return true;
				}
				else
				//be patient, every1 gets some!
				if ( args[1].equals("infinite") 
						&& permsManager.has(player, "dtl.trader.options.wallet." + args[1]) )
				{

					//we changed the type!
					trader.getTraderConfig().setWalletType(WalletType.getTypeByName(args[1]));
					player.sendMessage(ChatColor.RED + "The traders wallet type changed to: " + ChatColor.AQUA + args[1]);
					
					return true;
				}
				
				//clan-wallet and clan-bank currently not supported
				player.sendMessage(ChatColor.RED + "Wrong wallet type or insufficient permissions");
				return true;
			}
			else
			//i don't like my server trader :<
			if ( args[0].equals("type") )
			{
				
				if ( !permsManager.has(player, "dtl.trader.commands.type") )
				{
					
					//see ya with the same permission ;)
					player.sendMessage(ChatColor.RED + "Sorry, you can't change the traders type");
					return true;
				}
				
				
				//let them have fun with this trader plugin ;P
				if ( args[1].equals("player")
						&& permsManager.has(player, "dtl.trader.options." + args[1]) ) 
				{
					
				}
				else
				//administrator shops enhanced! (WTF?! server?)
				if ( args[1].equals("server")
						&& permsManager.has(player, "dtl.trader.options." + args[1]) ) 
				{
					
				}
				

				//typo!! We hate it!
				player.sendMessage(ChatColor.RED + "Wrong trader type or insufficient permissions");
				return true;
			}
			else
			//lets create a trader!
			if ( args[0].equals("create") )
			{
				
				if ( !permsManager.has(player, "dtl.trader.commands.create") )
				{
					
					//you can't create life!
					player.sendMessage(ChatColor.RED + "Sorry, you can't create a trader");
					return true;
				}
				
				
				//have we got the needed args?
				if ( args.length < 2 )
				{
					player.sendMessage(ChatColor.RED + "Invalid arguments");
					return true;
				}
				
				
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
							player.sendMessage(ChatColor.RED + "You don't have permission to use this trader type");
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
							player.sendMessage(ChatColor.RED + "You don't have permission to use this wallet type");
							return true;
						}
						walletType = WalletType.getTypeByName(arg.substring(2));
						
						
					}
					else
					//entity type set
					if ( arg.startsWith("e:") )
					{
						
						
						//do we have permissions to set this wallet type?
						if ( !permsManager.has(player, "dtl.trader.options.entity.*") )
						{
							player.sendMessage(ChatColor.RED + "You don't have permission to use this wallet type");
							return true;
						}
						entityType = EntityType.fromName(arg.substring(2));
						
						
					}
					
					
				}
				
				
				
				if ( walletType == null || traderType == null )
				{
					player.sendMessage(ChatColor.RED + "No default available, maybe you don't have permissions to create a trader?");
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
				
				
				player.sendMessage(ChatColor.RED + "You created a trader at your position");
				
				return true;
			}
			else
			//lets create a trader!
			if ( args[0].equals("dismiss") )
			{
				
				if ( !permsManager.has(player, "dtl.trader.commands.dismiss") )
				{
					
					//you can't create life!
					player.sendMessage(ChatColor.RED + "Sorry, you can't dismiss this trader");
					return true;
				}
				
				
				
			}
			else 
			//show me your money!
			if ( args[0].equals("balance") )
			{

				
				if ( !permsManager.has(player, "dtl.trader.commands.balance") )
				{
					
					//you can't create life!
					player.sendMessage(ChatColor.RED + "Sorry, you can't dismiss this trader");
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "No trader selected (manager mode)");
					return true;
				}
				
				
				//only npc wallets can be managed!
				if ( !trader.equalsWalletType(WalletType.NPC_WALLET) )
				{
					
					
					//lets inform about that mistake
					player.sendMessage(ChatColor.RED + "Only traders with 'npc-wallet' got a balance");
					return true;
				}

				DecimalFormat f = new DecimalFormat("#.##");
				player.sendMessage(ChatColor.RED + "Traders balance: " + ChatColor.AQUA + String.valueOf(f.format(trader.getWallet().getMoney())) );
				
				return true;
			}
			else 
			//show me your money!
			if ( args[0].equals("withdraw") )
			{

				
				if ( !permsManager.has(player, "dtl.trader.commands.withdraw") )
				{
					
					//you can't create life!
					player.sendMessage(ChatColor.RED + "Sorry, you can't dismiss this trader");
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "No trader selected (manager mode)");
					return true;
				}
				
				
				//only npc wallets can be managed!
				if ( !trader.equalsWalletType(WalletType.NPC_WALLET) )
				{

					//lets inform about that mistake
					player.sendMessage(ChatColor.RED + "Only traders with 'npc-wallet' got a balance");
					return true;
				}
				
				
				//we want to withdraw nothing...
				if ( args.length < 2 )
				{
					
					player.sendMessage(ChatColor.RED + "Invalid arguments");
					return true;
				}
				
				
				double money = trader.getWallet().getMoney();
				double withdraw = 0.0;
				try 
				{
					withdraw = Double.valueOf(args[1]);
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
				
				player.sendMessage(ChatColor.RED + "You withdrawed " + ChatColor.AQUA + args[1] + "");
				player.sendMessage(ChatColor.RED + "Traders balance: " + ChatColor.AQUA + f.format(trader.getWallet().getMoney()) + "");
				
				
				return true;
			}
			else 
			//show me your money!
			if ( args[0].equals("deposit") )
			{

				
				if ( !permsManager.has(player, "dtl.trader.commands.deposit") )
				{
					
					//you can't create life!
					player.sendMessage(ChatColor.RED + "Sorry, you can't dismiss this trader");
					return true;
				}
				
				//check if we are editing a valid trader
				if ( trader == null )
				{
					player.sendMessage(ChatColor.RED + "No trader selected (manager mode)");
					return true;
				}
				
				
				//only npc wallets can be managed!
				if ( !trader.equalsWalletType(WalletType.NPC_WALLET) )
				{

					//lets inform about that mistake
					player.sendMessage(ChatColor.RED + "Only traders with 'npc-wallet' got a balance");
					return true;
				}
				
				
				//we want to withdraw nothing...
				if ( args.length < 2 )
				{
					
					player.sendMessage(ChatColor.RED + "Invalid arguments");
					return true;
				}
				
				
				double money = trader.getWallet().getMoney();
				double deposit = 0.0;
				try 
				{
					deposit = Double.valueOf(args[1]);
				} 
				catch (NumberFormatException e)
				{
					player.sendMessage("Wrong amount as argument");
					return true;
				}
				
				plugin.getEconomy().withdrawPlayer(player.getName(), deposit);
				
				trader.getWallet().setMoney(money + deposit);
				DecimalFormat f = new DecimalFormat("#.##");
				
				player.sendMessage(ChatColor.RED + "You deposited " + ChatColor.AQUA + args[1] + "");
				player.sendMessage(ChatColor.RED + "Traders balance: " + ChatColor.AQUA + f.format(trader.getWallet().getMoney()) + "");
				
				
				return true;
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
			return WalletType.SERVER_INFINITE;
		else
		//next server default is custom bank
		if ( permsManager.has(player, "dtl.trader.options.bank") )
			return WalletType.SERVER_BANK;
		else
		//next default is npc wallet
		if ( permsManager.has(player, "dtl.trader.options.npc-wallet") )
			return WalletType.NPC_WALLET;
		else
		//next default is player wallet
		if ( permsManager.has(player, "dtl.trader.options.owner-wallet") )
			return WalletType.PLAYER_WALLET;
		else
		//next default is player bank account
		if ( permsManager.has(player, "dtl.trader.options.owner-bank") )
			return WalletType.PLAYER_BANK;
		
		//else return no default
		return null;
	}

}
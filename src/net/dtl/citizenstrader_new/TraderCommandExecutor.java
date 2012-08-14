package net.dtl.citizenstrader_new;

import java.text.DecimalFormat;

import net.dtl.citizenstrader_new.traders.Trader;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.TraderTrait.TraderType;
import net.dtl.citizenstrader_new.traits.TraderTrait.WalletType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TraderCommandExecutor implements CommandExecutor {
	public static CitizensTrader plugin;
	//private TraderCharacterTrait trader;
	private TraderManager traderManager;
	private PermissionsManager permsManager;
	
	public TraderCommandExecutor(CitizensTrader instance) {
		plugin = instance;
		new CitizensTrader();
		
		this.traderManager = CitizensTrader.getTraderManager();
		this.permsManager = new PermissionsManager();
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
			
			Trader trader = traderManager.getOngoingTrades(player.getName());
			
			//does we have anything to interact with?
			if ( trader == null )
			{
				player.sendMessage(ChatColor.RED + "No trader selected (manager mode)");
				return true;
			}
			
			//check if we can use commands
			if ( !permsManager.has(player, "dtl.trader.commands") )
			{
				
				//goodbye!
				player.sendMessage(ChatColor.RED + "Sorry, you don't have permission to use commands");
				return true;
			}
			
			
			//lets see what arguments we use
			//looks like we wan't to buy something
			if ( args[0].equals("sell") )
			{
				if ( !permsManager.has(player, "dtl.trader.options.sell") )
				{
					
					//have a good flight!
					player.sendMessage(ChatColor.RED + "Sorry, you don't have permission to use 'sell' commands");
					return true;
				}
				
				//can we get that list plz?
				if ( permsManager.has(player, "dtl.trader.commands.list") ) 
				{
					
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
					player.sendMessage(ChatColor.RED + "Sorry, you don't have permission to use 'sell' commands");
					return true;
				}
				
				//Let's see
				if ( permsManager.has(player, "dtl.trader.commands.list") ) 
				{
					
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
				
				if ( !permsManager.has(player, "dtl.trader.commands.wallet") )
				{
					
					//have a good flight! (copied...)
					player.sendMessage(ChatColor.RED + "Sorry, you can't change the traders wallet type");
					return true;
				}
				
				
				//why i don't use a switch? because i hate them!
				//my money, my money... ;>
				if ( args[1].equals("owner-wallet") 
						&& permsManager.has(player, "dtl.trader.options." + args[1]) )
				{
					
				}
				else
				//put it all there, in one place
				if ( args[1].equals("owner-bank") 
						&& permsManager.has(player, "dtl.trader.options." + args[1]) )
				{
					
				}
				else
				//here take it and make something useful with it
				if ( args[1].equals("npc-wallet") 
						&& permsManager.has(player, "dtl.trader.options." + args[1]) )
				{
					
				}
				else
				//any1 wan't a trader eating his bank account savings?
				if ( args[1].equals("bank") 
						&& permsManager.has(player, "dtl.trader.options." + args[1]) )
				{
					
				}
				else
				//be patient, every1 gets some!
				if ( args[1].equals("infinite") 
						&& permsManager.has(player, "dtl.trader.options." + args[1]) )
				{
					
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
		}
		//is God trying to command a trader? 
		else
		{
			
			
		}
		
		/*if(cmd.getName().equalsIgnoreCase("trader")||cmd.getName().equalsIgnoreCase("q")) {
			if ((sender instanceof Player)) {
				Player p = (Player) sender;
				if ( argsLength(args,0,0) ) {
				} else if ( argsLength(args,2,6) ) {
					if ( args[0].equals("buy") ) {
						if ( args[1].equals("list") ) {
						} else if ( args[1].equals("add")  ) {
							addBuyItem(p, args);
						} else if ( args[1].equals("remove") ) {
							removeBuyItem(p, args);
						} else if ( args[1].equals("edit") ) {
						} 
					} else if ( args[0].equals("sell") ) {
						if ( args[1].equals("list") ) {
						} else if ( args[1].equals("add") ) {
							addSellItem(p, args);
						} else if ( args[1].equals("remove") ) {
							removeSellItem(p, args);
						} else if ( args[1].equals("edit") ) {
						} 
					} else if ( args[0].equals("mode") ) {
						setTraderMode(p, args[1]);
					} else if ( args[0].equals("type") ) {
						setTraderType(p, args[1]);
					} else if ( args[0].equals("wallet") ) {
						setWalletType(p, args[1]);
					} else if ( args[0].equals("deposit") ) {
						depositTrader(p, args[1]);
					} else if ( args[0].equals("withdraw") ) {
						withdrawTrader(p, args[1]);
					}
				} else if ( argsLength(args,1,1) ) {
					if ( args[0].equals("type") ) {
						setTraderType(p, args[1]);
					} else if ( args[0].equals("balance") ) {
						showTraderBalance(p);
					} else if ( args[0].equals("choose") ) {

					} 
				}
				
				
				return true;
			}
		}
		return false;*/
		return false;
	}
	
	private void withdrawTrader(Player p, String amount) {
		Trader trader = this.traderManager.getOngoingTrades(p.getName());
		if ( trader == null )
			return;
		
		if ( trader.getWallet().getWalletType().equals(WalletType.NPC_WALLET) ) 
		{	
			double money = trader.getWallet().getMoney();
			double withdraw = 0.0;
			try 
			{
				withdraw = Double.valueOf(amount);
			} 
			catch (NumberFormatException e)
			{
				p.sendMessage("Wrong amount as argument");
				return;
			}
			
			if ( withdraw > money )
			{
				p.sendMessage(ChatColor.RED + "This trader cannot give you that amount");
				return;
			}
			trader.getWallet().setMoney(money - withdraw);
			DecimalFormat f = new DecimalFormat("#.##");

			plugin.getEconomy().depositPlayer(p.getName(), withdraw);
			
			p.sendMessage(ChatColor.RED + "You withdrawed " + withdraw + "");
			p.sendMessage(ChatColor.RED + "Traders balance: " + ChatColor.WHITE + f.format(trader.getWallet().getMoney()) + "");
		} 
		else 
		{
			p.sendMessage(ChatColor.RED + "This npc does not have his own wallet");
		}
	}

	private void depositTrader(Player p, String amount) {
		Trader trader = this.traderManager.getOngoingTrades(p.getName());
		if ( trader == null )
			return;
		
		if ( trader.getWallet().getWalletType().equals(WalletType.NPC_WALLET) ) 
		{	
			double money = trader.getWallet().getMoney();
			double deposit = 0.0;
			try 
			{
				deposit = Double.valueOf(amount);
			} 
			catch (NumberFormatException e)
			{
				p.sendMessage("Wrong amount as argument");
				return;
			}
			
			plugin.getEconomy().withdrawPlayer(p.getName(), deposit);
			
			trader.getWallet().setMoney(money + deposit);
			DecimalFormat f = new DecimalFormat("#.##");
			
			p.sendMessage(ChatColor.RED + "You deposited " + amount + "");
			p.sendMessage(ChatColor.RED + "Traders balance: " + ChatColor.WHITE + f.format(trader.getWallet().getMoney()) + "");
		} 
		else 
		{
			p.sendMessage(ChatColor.RED + "This npc does not have his own wallet");
		}
	}

	private void showTraderBalance(Player p) {
		//if ( type != null && ( type.equals("server-infinite") || type.equals("player-wallet") || type.equals("npc-wallet") ) ) {
		Trader trader = this.traderManager.getOngoingTrades(p.getName());
		if ( trader == null )
			return;
		
		if ( trader.getWallet().getWalletType().equals(WalletType.NPC_WALLET) ) 
		{	
			
			DecimalFormat f = new DecimalFormat("#.##");
			
			p.sendMessage(ChatColor.RED + "Traders balance: " + ChatColor.WHITE + f.format(trader.getWallet().getMoney()) + "");
		} 
		else 
		{
			p.sendMessage(ChatColor.RED + "This npc does not have his own wallet");
		}
		
	}

	private void setWalletType(Player p, String type) {
		if ( type != null && ( type.equals("server-infinite") || type.equals("player-wallet") || type.equals("npc-wallet") ) ) {
			Trader trader = this.traderManager.getOngoingTrades(p.getName());
			if ( trader == null )
				return;
			
			if ( trader.getTraderConfig().getTraderType().toString().equals(type) )
				return;
			
			trader.getTraderConfig().setWalletType(WalletType.getTypeByName(type));
			p.sendMessage(ChatColor.RED + "Wallet type changed to " + type);
		} else {
			p.sendMessage(ChatColor.RED + "Invalid wallet type!");
		}
	}

	private void setTraderType(Player p, String type) {
		if ( type != null && ( type.equals("server") || type.equals("player") ) ) {
			Trader trader = this.traderManager.getOngoingTrades(p.getName());
			if ( trader == null )
				return;
			
			if ( trader.getTraderConfig().getTraderType().toString().equals(type) )
				return;
			
			trader.getTraderConfig().setTraderType(TraderType.getTypeByName(type));
			p.sendMessage(ChatColor.RED + "Trader type changed to " + type);
		} else {
			p.sendMessage(ChatColor.RED + "Invalid trader type!");
		}
		
	}

	private void setTraderMode(Player p, String mode) {
		if ( mode != null && ( mode.equals("secure") || mode.equals("simple") ) ) {
			CitizensTrader.config.setMode(mode);
			p.sendMessage(ChatColor.RED + "Trader mode set to: " + mode);
		} else {
			p.sendMessage(ChatColor.RED + "Invalid trader mode!");
		}
		
	}
	
	private void removeSellItem(Player p,String[] args) {
		if ( args.length != 3 )
			return;
		
		Trader trade = this.traderManager.getOngoingTrades(p.getName());
		if ( trade != null && trade.equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) ) {
			trade.getTraderStock().removeItem(true, Integer.parseInt(args[2]));
			p.sendMessage(ChatColor.RED + " Item succesfuly removed.");
			return;
		}
		p.sendMessage(ChatColor.RED + " Item could not be removed.");
	}

	private void addSellItem(Player p,String[] args) {
		if ( args.length < 3 )
			return;
		

		String itemDesc = "";
		for ( int i = 2 ; i < args.length ; ++i )
			itemDesc += args[i] + " ";

		Trader trade = this.traderManager.getOngoingTrades(p.getName());
		if ( trade != null && trade.equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) ) {
			trade.getTraderStock().addItem(true, itemDesc);
			p.sendMessage(ChatColor.RED + " Item succesfuly added.");
			return;
		}
		p.sendMessage(ChatColor.RED + " Item could not be added.");
		
	}
	
	//Buying options

	private void removeBuyItem(Player p,String[] args) {
		if ( args.length != 3 )
			return;

		Trader trade = this.traderManager.getOngoingTrades(p.getName());
		if ( trade != null && trade.equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) ) {
			trade.getTraderStock().removeItem(false, Integer.parseInt(args[2]));
			p.sendMessage(ChatColor.RED + " Item succesfuly removed.");
			return;
		}
		p.sendMessage(ChatColor.RED + " Item could not be removed.");
	}

	private void addBuyItem(Player p,String[] args) {
		if ( args.length < 3 )
			return;
		
		String itemDesc = "";
		for ( int i = 2 ; i < args.length ; ++i )
			itemDesc += args[i] + " ";

		Trader trade = this.traderManager.getOngoingTrades(p.getName());
		if ( trade != null && trade.equalsTraderStatus(TraderStatus.PLAYER_MANAGE_SELL) ) {
			trade.getTraderStock().addItem(false,itemDesc);
			p.sendMessage(ChatColor.RED + " Item succesfuly added.");
			return;
		}
		p.sendMessage(ChatColor.RED + " Item could not be added.");
		
	}
}
	

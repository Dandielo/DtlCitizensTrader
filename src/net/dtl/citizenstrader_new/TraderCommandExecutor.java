package net.dtl.citizenstrader_new;

import net.dtl.citizenstrader_new.traders.Trader;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.TraderTrait.TraderType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TraderCommandExecutor implements CommandExecutor {
	public static CitizensTrader plugin;
	//private TraderCharacterTrait trader;
	private TraderManager traderManager;
	
	public TraderCommandExecutor(CitizensTrader instance) {
		plugin = instance;
		new CitizensTrader();
		this.traderManager = CitizensTrader.getTraderManager();
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
		if(cmd.getName().equalsIgnoreCase("trader")||cmd.getName().equalsIgnoreCase("q")) {
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
					}
				} else if ( argsLength(args,2,2) ) {
					if ( args[0].equals("type") ) {
						setTraderType(p, args[1]);
					} else if ( args[0].equals("choose") ) {

					} 
				}
				
				
				return true;
			}
		}
		return false;
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
	

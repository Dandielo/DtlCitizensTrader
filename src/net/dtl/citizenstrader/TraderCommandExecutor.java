package net.dtl.citizenstrader;

import net.citizensnpcs.api.CitizensAPI;
import net.dtl.citizenstrader.TraderStatus.Status;
import net.dtl.citizenstrader.traits.InventoryTrait;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TraderCommandExecutor implements CommandExecutor {
	private TraderNpc trader;

	public TraderCommandExecutor() {
		trader = ((TraderNpc) CitizensAPI.getCharacterManager().getCharacter("trader"));
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
					}
				} else if ( argsLength(args,2,2) ) {
					if ( args[0].equals("main") ) {

					} else if ( args[0].equals("choose") ) {

					}
				}
				
				
				return true;
			}
		}
		return false;
	}
	private void removeSellItem(Player p,String[] args) {
		if ( args.length != 3 )
			return;
		
		TraderStatus stat = trader.getStatus(p.getName());
		if ( stat != null && stat.getStatus().equals(Status.PLAYER_MANAGE) ) {
			stat.getTrader().getTrait(InventoryTrait.class).removeItem(true, Integer.parseInt(args[2]));
			p.sendMessage(ChatColor.RED + " Item succesfuly removed.");
			return;
		}
		p.sendMessage(ChatColor.RED + " Item could not be removed.");
	}

	private void addSellItem(Player p,String[] args) {
		if ( args.length < 5 )
			return;
		
		TraderStatus stat = trader.getStatus(p.getName());
		if ( stat != null && stat.getStatus().equals(Status.PLAYER_MANAGE) ) {
			stat.getTrader().getTrait(InventoryTrait.class).addItem(true, args[2]+" "+args[3]+" "+args[4]);
			p.sendMessage(ChatColor.RED + " Item succesfuly added.");
			return;
		}
		p.sendMessage(ChatColor.RED + " Item could not be added.");
		
	}

	private void removeBuyItem(Player p,String[] args) {
		if ( args.length != 3 )
			return;
		
		TraderStatus stat = trader.getStatus(p.getName());
		if ( stat != null && stat.getStatus().equals(Status.PLAYER_MANAGE) ) {
			stat.getTrader().getTrait(InventoryTrait.class).removeItem(false, Integer.parseInt(args[2]));
			p.sendMessage(ChatColor.RED + " Item succesfuly removed.");
			return;
		}
		p.sendMessage(ChatColor.RED + " Item could not be removed.");
	}

	private void addBuyItem(Player p,String[] args) {
		if ( args.length < 5 )
			return;
		
		TraderStatus stat = trader.getStatus(p.getName());
		if ( stat != null && stat.getStatus().equals(Status.PLAYER_MANAGE) ) {
			stat.getTrader().getTrait(InventoryTrait.class).addItem(false, args[2]+" "+args[3]+" "+args[4]);
			p.sendMessage(ChatColor.RED + " Item succesfuly added.");
			return;
		}
		p.sendMessage(ChatColor.RED + " Item could not be added.");
		
	}
}
	

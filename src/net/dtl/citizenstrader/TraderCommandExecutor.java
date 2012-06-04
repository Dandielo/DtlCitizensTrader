package net.dtl.citizenstrader;

import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TraderCommandExecutor implements CommandExecutor {

	private CitizensTrader plugin;

	public TraderCommandExecutor(CitizensTrader instance) {
		plugin = instance;
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
			//		traderCommands( p );
				} else if ( argsLength(args,2,6) ) {
					if ( args[0].equals("buy") ) {
						if ( args[1].equals("list") ) {
				//			showBuyList(p);
						} else if ( args[1].equals("add") && plugin.dtlProject.getPermissions().has(p, "dtl.citizens.characters.trader") ) {
			//				addBuyItem(p, args);
						} else if ( args[1].equals("remove") && plugin.dtlProject.getPermissions().has(p, "dtl.citizens.characters.trader") ) {
			//				removeBuyItem(p, args);
						} else if ( args[1].equals("edit") && plugin.dtlProject.getPermissions().has(p, "dtl.citizens.characters.trader") ) {
			//				editBuyItem(p, args);
						} 
					} else if ( args[0].equals("sell") ) {
						if ( args[1].equals("list") ) {
			//				showSellList(p);
						} else if ( args[1].equals("add") && plugin.dtlProject.getPermissions().has(p, "dtl.citizens.characters.trader") ) {
			//				addSellList(p, args);
						} else if ( args[1].equals("remove") && plugin.dtlProject.getPermissions().has(p, "dtl.citizens.characters.trader") ) {
			//				removeSellList(p, args);
						} else if ( args[1].equals("edit") && plugin.dtlProject.getPermissions().has(p, "dtl.citizens.characters.trader") ) {
			//				editSellList(p, args);
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
/*
	private void editSellList(Player p,String[] args) {
		if ( args.length < 6 )
			return;
		NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		((TraderNpc)n.getCharacter()).editItem(Integer.parseInt(args[2]),args[3]+" "+args[4]+" "+args[5], n.getId(), true);

		p.sendMessage(ChatColor.RED + " Item succesfuly edited.");
	}

	private void removeSellList(Player p,String[] args) {
		if ( args.length != 3 )
			return;
		NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		((TraderNpc)n.getCharacter()).removeItem(args[2], n.getId(), true);
		
		p.sendMessage(ChatColor.RED + " Item succesfuly removed");
	}

	private void addSellList(Player p,String[] args) {
		if ( args.length < 5 )
			return;
		NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		((TraderNpc)n.getCharacter()).addItem(args[2]+" "+args[3]+" "+args[4], n.getId(), true);
		
		p.sendMessage(ChatColor.RED + " Item succesfuly added.");
	}

	private void showSellList(Player p) {
		NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		List<TraderItem> items = ((TraderNpc)n.getCharacter()).getList(n.getId(), true);
		p.sendMessage(ChatColor.RED + " --- Trader sales items --- ");
		for ( int i = 0 ; i < items.size() ; ++i ) {
			p.sendMessage(ChatColor.GOLD + " #" + i + " " + ChatColor.GREEN + items.get(i).getItemStack().getType().name() + " " + items.get(i).getCost() + "$ " + items.get(i).getAmout());
		}
	}

	private void editBuyItem(Player p,String[] args) {
		if ( args.length < 6 )
			return;
		NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		((TraderNpc)n.getCharacter()).editItem(Integer.parseInt(args[2]),args[3]+" "+args[4]+" "+args[5], n.getId(), false);
		
		p.sendMessage(ChatColor.RED + " Item succesfuly edited.");
	}

	private void removeBuyItem(Player p,String[] args) {
		if ( args.length != 3 )
			return;
		NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		((TraderNpc)n.getCharacter()).removeItem(args[2], n.getId(), false);
		
		p.sendMessage(ChatColor.RED + " Item succesfuly removed.");
	}

	private void addBuyItem(Player p,String[] args) {
		if ( args.length < 5 )
			return;
		NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		((TraderNpc)n.getCharacter()).addItem(args[2]+" "+args[3]+" "+args[4], n.getId(), false);
		
		p.sendMessage(ChatColor.RED + " Item succesfuly added.");
	}

	private void showBuyList(Player p) {
		NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		List<TraderItem> items = ((TraderNpc)n.getCharacter()).getList(n.getId(), false);
		p.sendMessage(ChatColor.RED + " --- Trader purchase items --- ");
		for ( int i = 0 ; i < items.size() ; ++i ) {
			p.sendMessage(ChatColor.GOLD + " #" + i + " " + ChatColor.GREEN + items.get(i).getItemStack().getType().name() + " " + items.get(i).getCost() + "$ " + items.get(i).getAmout());
		}
	}

	public void traderCommands( Player p ) {
		
	}
	*/
}
	

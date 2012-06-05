package net.dtl.citizenstrader;

import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader.TraderStatus.Status;
import net.dtl.citizenstrader.traits.InventoryTrait;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TraderCommandExecutor implements CommandExecutor {

//	private CitizensTrader plugin;
	private TraderNpc trader;

	public TraderCommandExecutor() {
		trader = ((TraderNpc) CitizensAPI.getCharacterManager().getCharacter("trader"));
	//	plugin = instance;
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
						} else if ( args[1].equals("add")  ) {
							addBuyItem(p, args);
						} else if ( args[1].equals("remove") ) {
							removeBuyItem(p, args);
						} else if ( args[1].equals("edit") ) {
			//				editBuyItem(p, args);
						} 
					} else if ( args[0].equals("sell") ) {
						if ( args[1].equals("list") ) {
			//				showSellList(p);
						} else if ( args[1].equals("add") ) {
							addSellItem(p, args);
						} else if ( args[1].equals("remove") ) {
							removeSellItem(p, args);
						} else if ( args[1].equals("edit") ) {
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
*/
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
		//NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		//((TraderNpc)n.getCharacter()).addItem(args[2]+" "+args[3]+" "+args[4], n.getId(), true);
		
	}
/*
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

*/
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
		//NPC n = CitizensAPI.getNPCRegistry().getNPC(plugin.getSelected());
		//((TraderNpc)n.getCharacter()).addItem(args[2]+" "+args[3]+" "+args[4], n.getId(), true);
		
	}
/*

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
	

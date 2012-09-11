package net.dtl.citizens.trader;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dtl.citizens.trader.TraderCharacterTrait.TraderType;
import net.dtl.citizens.trader.traders.Banker;
import net.dtl.citizens.trader.traders.EconomyNpc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class BankerCommandExecutor implements CommandExecutor {

	//plugin instance
	public static CitizensTrader plugin;
	
	//managers
	private static NpcEcoManager bankerManager;
	private static PermissionsManager permsManager;
	private static LocaleManager locale;

	//constructor
	public BankerCommandExecutor(CitizensTrader instance) {
		plugin = instance;

		locale = CitizensTrader.getLocaleManager();
		permsManager = CitizensTrader.getPermissionsManager();
		bankerManager = CitizensTrader.getNpcEcoManager();
	}
	
	//commands
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		
		//is player
		if ( sender instanceof Player )
		{
			Player player = (Player) sender;
			
			if ( args.length < 1 )
			{
				player.sendMessage(ChatColor.AQUA + "DtlTraders " + plugin.getDescription().getVersion() + ChatColor.RED + " - Banker commands list" );
				return false;
			}
			
			
			//get the selected NPC
			EconomyNpc economyNpc = bankerManager.getInteractionNpc(player.getName());
			
			if ( args[0].equals("reload") )
			{
				
				sender.sendMessage( locale.getLocaleString("reload-config") );
				CitizensTrader.getInstance().getItemConfig().reloadConfig();
				CitizensTrader.getInstance().reloadConfig();
				CitizensTrader.getLocaleManager().reload();
				
				return true;
			}
			
			//no npc selected
			if ( economyNpc == null )
			{
				
				//reload plugin
				if ( args[0].equalsIgnoreCase("create") )
				{
					if ( !this.generalChecks(player, "create", null, args, 3) )
						return true;
					
					return createBanker(player, args);
				}
				
				
				
				return true;
			}
			//npc has been selected
			else
			{
				//is trader type
				if ( !( economyNpc instanceof Banker ) )
				{
					player.sendMessage( locale.getLocaleString("xxx-not-selected", "object:banker") );
					return true;
				}
				
				Banker banker = (Banker) economyNpc;
				
				
				if ( args[0].equals("type") )
				{
					if ( !this.generalChecks(player, "type", null, args, 1) )
						return true;
					
					return setType(player, banker, ( args.length > 1 ? args[1] : "" ) );
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
				CitizensTrader.getInstance().reloadConfig();
				CitizensTrader.getLocaleManager().reload();
				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean generalChecks(Player player, String commandPermission, String optionsPermission, String[] args, int size)
	{
		
		//check permissions
		if ( !permsManager.has(player, "dtl.banker.commands." + commandPermission)  )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
			return false;
		}
		
		if ( optionsPermission != null )
		//check permissions
		if ( !permsManager.has(player, "dtl.banker.options." + optionsPermission)  )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
			return false;
		}
		
		//have we got the needed args?
	/*	if ( args.length > size )
		{
			player.sendMessage( locale.getLocaleString("missing-args") );
			return false;
		}	*/
		
		return true;
	}
	

	public TraderType getDefaultBankerType(Player player) {
		
		//server trader as default
		if ( permsManager.has(player, "dtl.banker.types.player") )
			return TraderType.PLAYER_BANK;
		else
		//next default is player trader 
		if ( permsManager.has(player, "dtl.banker.types.money") )
			return TraderType.MONEY_BANK;
		
		//else return no default
		return null;
	}
	
	
	//set the traders type
	public boolean setType(Player player, Banker banker, String typeString)
	{
		
		if ( !permsManager.has(player, "dtl.banker.types." + typeString + "-bank" ) )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:banker") );
			return true;
		}
		
		TraderType type = TraderType.getTypeByName(typeString+"-bank");
		
		//show current trader type
		if ( type == null )
		{			
			player.sendMessage( locale.getLocaleString("xxx-setting-value", "setting:banker").replace("{value}", banker.getNpc().getTrait(TraderCharacterTrait.class).getTraderType().toString().split("-")[0]) );
		}
		//change trader type
		else
		{

		//	banker.getTraderConfig().setTraderType(type);
			banker.getNpc().getTrait(TraderCharacterTrait.class).setTraderType(type);
			
			player.sendMessage( locale.getLocaleString("xxx-setting-changed", "setting:banker").replace("{value}", typeString) );
		}
		
		return true;
	}
	
	
	//creating a trader, its easy ;)
	public boolean createBanker(Player player, String[] args)
	{
		String traderName = "";
		
		EntityType entityType = EntityType.PLAYER;
		TraderType traderType = getDefaultBankerType(player);
		
		
		//lets fetch the argument list
		for ( String arg : args )
		{
			//trader type set?
			if ( arg.startsWith("t:") )
			{
				//do we have permissions to set this trader type?
				if ( !permsManager.has(player, "dtl.trader.types." + arg.substring(2) + "-bank" ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:banker") );
					return true;
				}
				traderType = TraderType.getTypeByName(arg.substring(2)+ "-bank");
				if ( traderType == null || traderType.isTrader() )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:type") );
					return true;
				}
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
		
		if ( traderType == null || entityType == null )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
			return true;
		}
		
		//creating the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(entityType, traderName);
		npc.addTrait(TraderCharacterTrait.class);
		npc.addTrait(MobType.class);
		npc.getTrait(MobType.class).setType(entityType);
		npc.spawn(player.getLocation());
		
		//change the trader settings
		npc.getTrait(TraderCharacterTrait.class).setTraderType(traderType);
		
		player.sendMessage( locale.getLocaleString("xxx-created-xxx", "entity:player", "entity:banker") );
		return true;
	}
}

package net.dtl.citizens.trader;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dtl.citizens.trader.TraderCharacterTrait.EcoNpcType;
import net.dtl.citizens.trader.managers.LocaleManager;
import net.dtl.citizens.trader.managers.PermissionsManager;
import net.dtl.citizens.trader.traders.Banker;
import net.dtl.citizens.trader.traders.EconomyNpc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class BankerCommandExecutor implements CommandExecutor {

	//TODO Total redo!
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
				return true;
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
			if ( args[0].equals("help") )
			{
				player.sendMessage(ChatColor.AQUA + "DtlTraders " + plugin.getDescription().getVersion() + ChatColor.RED + " - Banker commands list" );
				return false;
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
				if ( args[0].equals("fee") )
				{
					if ( !this.generalChecks(player, "fee", null, args, 1) )
						return true;
					
					return setFee(player, banker, args );
				}
				if ( args[0].equals("settings") )
				{
					if ( !this.generalChecks(player, "settings", null, args, 1) )
						return true;
					
					return toggleSettings(player, banker, ( args.length > 1 ? args[1] : "" ) );
				}
				return false;
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
	
	private boolean toggleSettings(Player player, Banker banker, String toggle) {
		if ( toggle.isEmpty() )
		{
			player.sendMessage( locale.getLocaleString("xxx-value", "manage:settings").replace("{value}", ""+banker.getbankTrait().hasSettingsPage()) );
			return true;
		}
		if ( toggle.equals("toggle") )
		{
			banker.getbankTrait().toggleSettingsPage();
			player.sendMessage( locale.getLocaleString("xxx-value-changed", "manage:settings").replace("{value}", ""+banker.getbankTrait().hasSettingsPage()) );
			return true;
		}
		player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:action") );
		return true;
	}

	private boolean setFee(Player player, Banker banker, String[] args) {
		if ( args.length > 2 )
		{
			double fee = 0.0;
			try
			{
				fee = Double.valueOf(args[2]);
			}
			catch( NumberFormatException exception )
			{
				player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:amount") );
				//exception.printStackTrace();
				return true;
			}
			
			if ( args[1].toLowerCase().equals("withdraw") )
			{
				banker.getbankTrait().setWithdrawFee(fee);
				player.sendMessage( locale.getLocaleString("xxx-value-changed", "manage:withdraw-fee").replace("{value}", ""+banker.getbankTrait().getWithdrawFee()) );
			}
			else
			if ( args[1].toLowerCase().equals("deposit") )
			{
				banker.getbankTrait().setDepositFee(fee);
				player.sendMessage( locale.getLocaleString("xxx-value-changed", "manage:deposit-fee").replace("{value}", ""+banker.getbankTrait().getDepositFee()) );
			}
			else
				player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:fee") );
				
			return true;
		}
		if ( args.length > 1 )
		{
			if ( args[1].toLowerCase().equals("withdraw") )
				player.sendMessage( locale.getLocaleString("xxx-value", "manage:withdraw-fee").replace("{value}", ""+banker.getbankTrait().getWithdrawFee()) );
			else
			if ( args[1].toLowerCase().equals("deposit") )
				player.sendMessage( locale.getLocaleString("xxx-value", "manage:deposit-fee").replace("{value}", ""+banker.getbankTrait().getDepositFee()) );
			else
				player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:fee") );
				
			return true;
			
		}
		player.sendMessage( locale.getLocaleString("xxx-value", "manage:withdraw-fee").replace("{value}", ""+banker.getbankTrait().getWithdrawFee()) );
		player.sendMessage( locale.getLocaleString("xxx-value", "manage:deposit-fee").replace("{value}", ""+banker.getbankTrait().getDepositFee()) );
		return true;
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
	

	public EcoNpcType getDefaultBankerType(Player player) {
		
		//server trader as default
		if ( permsManager.has(player, "dtl.banker.types.player") )
			return EcoNpcType.PRIVATE_BANKER;
		else
		//next default is player trader 
		if ( permsManager.has(player, "dtl.banker.types.money") )
			return EcoNpcType.MONEY_BANKER;
		
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
		
		EcoNpcType type = EcoNpcType.getTypeByName(typeString+"-bank");
		
		//show current trader type
		if ( type == null )
		{			
			player.sendMessage( locale.getLocaleString("xxx-setting-value", "setting:banker").replace("{value}", banker.getNpc().getTrait(TraderCharacterTrait.class).getType().toString().split("-")[0]) );
		}
		//change trader type
		else
		{

		//	banker.getTraderConfig().setTraderType(type);
			banker.getNpc().getTrait(TraderCharacterTrait.class).setType(type);
			
			player.sendMessage( locale.getLocaleString("xxx-setting-changed", "setting:banker").replace("{value}", typeString) );
		}
		
		return true;
	}
	
	
	//creating a trader, its easy ;)
	public boolean createBanker(Player player, String[] args)
	{
		String traderName = "";
		
		EntityType entityType = EntityType.PLAYER;
		EcoNpcType traderType = getDefaultBankerType(player);
		
		
		//lets fetch the argument list
		for ( String arg : args )
		{
			//trader type set?
			if ( arg.startsWith("t:") )
			{
				//do we have permissions to set this trader type?
				if ( !permsManager.has(player, "dtl.banker.types." + arg.substring(2) + "-bank" ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:banker") );
					return true;
				}
				traderType = EcoNpcType.getTypeByName(arg.substring(2)+ "-bank");
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
		npc.getTrait(TraderCharacterTrait.class).setType(traderType);
		
		player.sendMessage( locale.getLocaleString("xxx-created-xxx", "entity:player", "entity:banker") );
		return true;
	}
}

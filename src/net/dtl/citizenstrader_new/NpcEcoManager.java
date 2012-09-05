package net.dtl.citizenstrader_new;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.TraderCharacterTrait.TraderType;
import net.dtl.citizenstrader_new.traders.Banker;
import net.dtl.citizenstrader_new.traders.EconomyNpc;
import net.dtl.citizenstrader_new.traders.PlayerBanker;
import net.dtl.citizenstrader_new.traders.PlayerTrader;
import net.dtl.citizenstrader_new.traders.ServerTrader;
import net.dtl.citizenstrader_new.traders.Trader;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

public class NpcEcoManager implements Listener {
	//trader configs
	protected static ItemsConfig config = CitizensTrader.getInstance().getItemConfig();
	
	//managers
	protected static LocaleManager locale = CitizensTrader.getLocaleManager();
	private PermissionsManager permManager = CitizensTrader.getPermissionsManager();
	
	
	//for now we don't need it
	
	//for laggy servers...
//	private List<String> playersAntiDclick;
//	private Timer antiDClickTimer;
//	private long timerDelay;
	
	//EconomyNpc
	private HashMap<String,EconomyNpc> playerInteraction;
	private List<NPC> isEconomyNpc;
	
	public NpcEcoManager() 
	{
		//initialize playerInteraction
		playerInteraction = new HashMap<String, EconomyNpc>();
		
		//initialize the economyNpcList
		isEconomyNpc = new ArrayList<NPC>();
	}

	//check Npc
	public boolean isEconomyNpc(NPC npc) {
		return this.isEconomyNpc.contains(npc);
	}
	
	//Interaction
	public EconomyNpc getInteractionNpc(String player) {
		if ( playerInteraction.containsKey(player) )
			return playerInteraction.get(player);
		return null;
	}
	protected void addEconomyNpc(NPC npc) {
		if ( !isEconomyNpc(npc) ) {
			this.isEconomyNpc.add(npc);
		}
	}	
	
	//cancel opening all other inventories when in mm
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event)
	{
		EconomyNpc economyNpc = playerInteraction.get(event.getPlayer().getName());
		
		if ( economyNpc == null )
			return;
		
		if ( !TraderStatus.hasManageMode(economyNpc.getTraderStatus()) )
			return;

		if ( economyNpc.getInventory().getTitle().equals(event.getInventory().getTitle()) )
			return;
			
		if ( event.getInventory().getType().equals(InventoryType.PLAYER)
				|| event.getInventory().getType().equals(InventoryType.CRAFTING) )
			return;
		
		event.setCancelled(true);
		((Player)event.getPlayer()).sendMessage(ChatColor.RED + "You can't open this in manager mode");
	}
	
	
	//Events Handling!
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		//player licked the inventory
		if ( !( event.getWhoClicked() instanceof Player ) )
			return;
		
		//get the player
		Player p = (Player) event.getWhoClicked();
		
		//if creative inventory is open ignore this event
		if ( p.getGameMode().equals(GameMode.CREATIVE) 
				&&  event.getView().getType().equals(InventoryType.PLAYER) )
			return;
		
		//if normal inventory is open ignore this event
		if ( (p.getGameMode().equals(GameMode.SURVIVAL) 
				|| p.getGameMode().equals(GameMode.SURVIVAL) )
				&& event.getView().getType().equals(InventoryType.CRAFTING) )
			return;
		
		//Get the economy npc
		EconomyNpc economyNpc = playerInteraction.get(p.getName());
		
		if ( economyNpc == null )
			return;
		
		
		//Npc Manager-mode
		if ( TraderStatus.hasManageMode(economyNpc.getTraderStatus()) )
		{ 
			economyNpc.managerMode(event);
			return;
		}
		if ( TraderStatus.hasSettingsMode(economyNpc.getTraderStatus()) )
		{
			economyNpc.settingsMode(event);
		}
		
		economyNpc.simpleMode(event);
	}
	
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		
		
		EconomyNpc economyNpc = playerInteraction.get(player.getName());
		
		if ( economyNpc == null )
			return;
		
		
		if ( TraderStatus.hasManageMode(economyNpc.getTraderStatus()) )
		{
			
			if ( economyNpc.getTraderStatus().equals(TraderStatus.MANAGE_SELL_AMOUNT) )
			{
				//cast to trader type, (it's save)
				((Trader)economyNpc).saveManagedAmouts();
				((Trader)economyNpc).switchInventory(TraderStatus.MANAGE_SELL);
				
				//reset the traders status
				((Trader)economyNpc).reset(TraderStatus.MANAGE);
			}
			
			return;
		}
		
		//remove the interaction
		playerInteraction.remove(player.getName());
		
			
	}
	
	//block item dropping while managing
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		Player player = (Player) event.getPlayer();
		
		//get the trader we are trading with
		EconomyNpc economyNpc = playerInteraction.get(player.getName());
		
		
		if ( economyNpc == null )
			return;
		
		
		if ( !TraderStatus.hasManageMode(economyNpc.getTraderStatus()) )
			return;
		
		
		//got permission?
		if ( this.permManager.has(player, "dtl.trader.bypass.drop") )
			return;


		event.setCancelled(true);
	}
	
	
	//On npc-despawn
	@EventHandler
	public void onNPCDespawn(NPCDespawnEvent event)
	{
		
		if ( this.isEconomyNpc.contains(event.getNPC()) )
			this.isEconomyNpc.remove(event.getNPC());
		
	}
	
	@EventHandler
	public void onNPCRightCLick(NPCRightClickEvent event) {		
		if ( !isEconomyNpc(event.getNPC()) )
			return;

		
		//used variables
		Player player = event.getClicker();
		final String playerName = player.getName();
		NPC npc = event.getNPC();


		//EconomyNpc
		EconomyNpc economyNpc = playerInteraction.get(playerName);
		
		//trader character
		TraderCharacterTrait characterTrait = npc.getTrait(TraderCharacterTrait.class);
		
		
		//Manage manager mode r.click
		if ( economyNpc != null )
		{
			
			if ( player.getItemInHand().getTypeId() == config.getMMToggleItem().getTypeId() )
			{
				if ( characterTrait.getTraderType().equals(TraderType.PLAYER_BANK) )
					return;
				
				if ( characterTrait.getTraderType().isTrader()
						&& ( ( permManager.has(player, "dtl.trader.options.manager-mode") 
								&& characterTrait.getTraderTrait().getOwner().equals(player.getName()) )
								|| permManager.has(player, "dtl.trader.bypass.manager-mode") 
								|| player.isOp() ) )
				{
					
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						//exit the manager mode
						playerInteraction.remove(playerName);
						player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
						return;
					}
					
					if ( !permManager.has(player, "dtl.trader.options.type." + characterTrait.getTraderType().toString() ) )
					{
						player.sendMessage( locale.getLocaleString("no-permissions-type") );
						return;
					}
					
					if ( player.getGameMode().equals(GameMode.CREATIVE) 
							&& !permManager.has(player, "dtl.trader.bypass.creative") )
					{
						player.sendMessage( locale.getLocaleString("no-permissions-creative") );
						return;
					}
					
					if ( characterTrait.getTraderType().equals(TraderType.PLAYER_TRADER) )
					{
						playerInteraction.put(playerName, new PlayerTrader(npc, characterTrait.getTraderTrait()));
					}
					if ( characterTrait.getTraderType().equals(TraderType.SERVER_TRADER) )
					{
						playerInteraction.put(playerName, new ServerTrader(npc, characterTrait.getTraderTrait()));
					}
					
					playerInteraction.get(playerName).setTraderStatus(TraderStatus.MANAGE);
					player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
					return;
				}
				
				return;
			}
				
			//open inventory in mm mode
			if ( economyNpc.getNpcId() == npc.getId() )
			{

				if ( !TraderStatus.hasManageMode(playerInteraction.get(playerName).getTraderStatus()) )
					return;
				
				((Trader)playerInteraction.get(playerName)).switchInventory(TraderStatus.MANAGE_SELL);
				
				player.openInventory(playerInteraction.get(playerName).getInventory());
				return;
			}

			
			//exit mm mode and open the normal inventory
			
			player.sendMessage(ChatColor.AQUA + CitizensAPI.getNPCRegistry().getById(economyNpc.getNpcId()).getFullName() + ChatColor.RED + " exited the manager mode");
	//		playerInteraction.get(playerName).setTraderStatus(TraderStatus.SELL);
			
			if ( characterTrait.getTraderType().equals(TraderType.PLAYER_TRADER) )
			{
				playerInteraction.put(playerName, new PlayerTrader(npc, characterTrait.getTraderTrait()));
			}
			if ( characterTrait.getTraderType().equals(TraderType.SERVER_TRADER) )
			{
				playerInteraction.put(playerName, new ServerTrader(npc, characterTrait.getTraderTrait()));
			}
			if ( characterTrait.getTraderType().equals(TraderType.PLAYER_BANK) )
			{
				playerInteraction.put(playerName, new PlayerBanker(npc, characterTrait.getBankTrait(), playerName));
			//	Banker banker = (Banker) playerInteraction.get(playerName);
			//	banker.switchInventory(playerName, TraderStatus.BANK);
			}
			
		//	((Trader)playerInteraction.get(playerName)).switchInventory(TraderStatus.SELL);
			player.openInventory(playerInteraction.get(playerName).getInventory());
			return;
		}
		
		//no mode r.click
		
		if ( player.getItemInHand().getTypeId() == config.getMMToggleItem().getTypeId() )
		{
			
			if ( characterTrait.getTraderType().isTrader()
					&& ( ( permManager.has(player, "dtl.trader.options.manager-mode") 
					&& characterTrait.getTraderTrait().getOwner().equals(player.getName()) )
					|| permManager.has(player, "dtl.trader.bypass.manager-mode") 
					|| player.isOp() ) )
			{
				
				if ( !permManager.has(player, "dtl.trader.options.type." + characterTrait.getTraderType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("no-permissions-type") );
					return;
				}
				
				if ( player.getGameMode().equals(GameMode.CREATIVE) 
						&& !permManager.has(player, "dtl.trader.bypass.creative") )
				{
					player.sendMessage( locale.getLocaleString("no-permissions-creative") );
					return;
				}
				
				if ( characterTrait.getTraderType().equals(TraderType.PLAYER_TRADER) )
				{
					playerInteraction.put(playerName, new PlayerTrader(npc, characterTrait.getTraderTrait()));
				}
				if ( characterTrait.getTraderType().equals(TraderType.SERVER_TRADER) )
				{
					playerInteraction.put(playerName, new ServerTrader(npc, characterTrait.getTraderTrait()));
				}
				
				playerInteraction.get(playerName).setTraderStatus(TraderStatus.MANAGE);
				player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
				return;
			}
			else
			if ( permManager.has(player, "dtl.trader.options.settings-mode") 
					&& characterTrait.getTraderType().isBanker() )
			{
				if ( !permManager.has(player, "dtl.trader.options.type." + characterTrait.getTraderType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("no-permissions-type") );
					return;
				}
				
				if ( characterTrait.getTraderType().equals(TraderType.PLAYER_BANK) )
				{
					playerInteraction.put(playerName, new PlayerBanker(npc, characterTrait.getBankTrait(), playerName));
					playerInteraction.get(playerName).setTraderStatus(TraderStatus.BANK_SETTINGS);
				//	playerInteraction.get(playerName).s
					Banker banker = (Banker) playerInteraction.get(playerName);
					banker.settingsInventory();
					
					player.openInventory(banker.getInventory());
				}
				
			}
			return;
		
		//exit mm mode and open the normal inventory
		}
		
		if ( !permManager.has(player, "dtl.trader.options.type." + characterTrait.getTraderType().toString() ) )
		{
			player.sendMessage( locale.getLocaleString("no-permissions-type") );
			return;
		}
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& !permManager.has(player, "dtl.trader.bypass.creative") )
		{
			player.sendMessage( locale.getLocaleString("no-permissions-creative") );
			return;
		}
		
		if ( characterTrait.getTraderType().equals(TraderType.PLAYER_TRADER) )
		{
			playerInteraction.put(playerName, new PlayerTrader(npc, characterTrait.getTraderTrait()));
		}
		if ( characterTrait.getTraderType().equals(TraderType.SERVER_TRADER) )
		{
			playerInteraction.put(playerName, new ServerTrader(npc, characterTrait.getTraderTrait()));
		}
		if ( characterTrait.getTraderType().equals(TraderType.PLAYER_BANK) )
		{
			playerInteraction.put(playerName, new PlayerBanker(npc, characterTrait.getBankTrait(), playerName));
		//	Banker banker = (Banker) playerInteraction.get(playerName);
		//	banker.switchInventory(playerName, TraderStatus.BANK);
		}
		
		player.openInventory(playerInteraction.get(playerName).getInventory());
		
		
	}
	
	
/*	private void manageBankerRClick(NPC npc, Player player, Banker trader) {
		
		if ( !isBankerNpc(npc) )
			return;
		//get the desired trait 
		BankTrait trait = npc.getTrait(TraderCharacterTrait.class).getBankTrait();
		
		
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& !permManager.has(player, "dtl.trader.bypass.creative") )
		{
			player.sendMessage(locale.getLocaleMessage("no-permissions"));
			return;
		}
		
		if ( !permManager.has(player, "dtl.trader.options.simple-mode") )
		{
			player.sendMessage(locale.getLocaleMessage("no-permissions"));
			return;
		}
		
		if ( !permManager.has(player, "dtl.trader.options.type.player-bank") )
		{
			player.sendMessage(locale.getLocaleMessage("no-permissions"));
			return;
		}
		
		bankManagers.put(player.getName(), new PlayerBanker(npc,trait));

		bankManagers.get(player.getName()).setInventory(player.getName());
		player.openInventory(bankManagers.get(player.getName()).getInventory());
	
	}

	public void manageTraderRClick(NPC npc, Player player, Trader trader)
	{
		
		if ( !isTraderNpc(npc) )
			return;
		
		//get the desired trait 
		TraderTrait trait = npc.getTrait(TraderCharacterTrait.class).getTraderTrait();

		//System.out.print("ba");

		//if we got the magic stick!
		if ( player.getItemInHand().getTypeId() == config.getMMToggleItem().getTypeId() ) 
		{
			if ( ( permManager.has(player, "dtl.trader.options.manager-mode") 
					&& trait.getOwner().equals(player.getName()) )
					|| player.isOp()
					|| permManager.has(player, "dtl.trader.bypass.manager-mode") )
			{
				
				//sth is already managed!
				if ( trader != null )
				{
					//its the same! lets kill it ;>
					if ( trader.getNpcId() == npc.getId() )
					{

						//remove the old one
						ongoingTrades.remove(player.getName());
						
						
						return;
					}
					
					if ( !permManager.has(player, "dtl.trader.options.type." + trait.getTraderType().toString()) )
					{
						player.sendMessage( locale.getLocaleMessage("no-permissions") );
						return;
					}
					
					//is it a server trader?
					if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
					{
						
						ongoingTrades.put(player.getName(), new ServerTrader(npc,trait));
					}
					//nah it's a player trader
					else 
					if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) 
					{
						
						if ( player.getGameMode().equals(GameMode.CREATIVE) 
								&& !permManager.has(player, locale.getLocaleMessage("no-permissions")) )
						{
							player.sendMessage("!NO PERMISSIONS, CREATIVE!");
							return;
						}
						
						ongoingTrades.put(player.getName(), new PlayerTrader(npc,trait));
					}

					
					//we are managing!
					ongoingTrades.get(player.getName()).setTraderStatus(TraderStatus.MANAGE);
					player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
					
					//we are done ;)
					return;
				}
				else
				//nothing exists
				{
					if ( !permManager.has(player, "dtl.trader.options.type." + trait.getTraderType().toString()) )
					{
						player.sendMessage( locale.getLocaleMessage("no-permissions") );
						return;
					}
					
					
					//is it a server trader?
					if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
					{
						
						ongoingTrades.put(player.getName(), new ServerTrader(npc,trait));
					}
					//nah it's a player trader
					else 
					if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) 
					{
						
						if ( player.getGameMode().equals(GameMode.CREATIVE) 
								&& !permManager.has(player, "dtl.trader.bypass.creative") )
						{
							player.sendMessage( locale.getLocaleMessage("no-permissions") );
							return;
						}
						
						ongoingTrades.put(player.getName(), new PlayerTrader(npc,trait));
					}/*
					
					//is it a server trader?
					if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
						ongoingTrades.put(player.getName(), new ServerTrader(npc,trait));
					//nah it's a player trader
					else if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) ) {
						ongoingTrades.put(player.getName(), new PlayerTrader(npc,trait));
					}
					
					//we are managing!
					ongoingTrades.get(player.getName()).setTraderStatus(TraderStatus.MANAGE);
					player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
					
					//return lets end this! :D
					return;
				}
				
				
			}
			else
			{
				player.sendMessage(ChatColor.RED + locale.getLocaleMessage("no-permissions"));
			}
			//nothing to do...
			return;
		}
		else
		//no stick? :< 
		{
			
			//is some1 managing?
			if ( trader != null 
					&& TraderStatus.hasManageMode(trader.getTraderStatus()) ) 
			{ 
				ongoingTrades.get(player.getName()).switchInventory(TraderStatus.MANAGE_SELL);
				
			} 
			else
			//only void ;<
			{

				if ( player.getGameMode().equals(GameMode.CREATIVE) 
						&& !permManager.has(player, "dtl.trader.bypass.creative") )
				{
					player.sendMessage(locale.getLocaleMessage("no-permissions"));
					return;
				}
				
				if ( !permManager.has(player, "dtl.trader.options.simple-mode") )
				{
					player.sendMessage(locale.getLocaleMessage("no-permissions"));
					return;
				}
				
				if ( !permManager.has(player, "dtl.trader.options.type." + trait.getTraderType().toString()) )
				{
					player.sendMessage(locale.getLocaleMessage("no-permissions"));
					return;
				}
				
				if ( trait.getTraderType().equals(TraderType.SERVER_TRADER) )
					ongoingTrades.put(player.getName(), new ServerTrader(npc,trait));
				else 
				if ( trait.getTraderType().equals(TraderType.PLAYER_TRADER) )
					ongoingTrades.put(player.getName(), new PlayerTrader(npc,trait));
		
				//	((CraftServer)Bukkit.getServer()).getServer().serverConfigurationManager.sendAll(new Packet15Place()); 
			}
			player.openInventory(ongoingTrades.get(player.getName()).getInventory());
			
		}
	}*/
	
}

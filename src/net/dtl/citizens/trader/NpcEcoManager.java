package net.dtl.citizens.trader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.dtl.citizens.trader.TraderCharacterTrait.TraderType;
import net.dtl.citizens.trader.traders.Banker;
import net.dtl.citizens.trader.traders.EconomyNpc;
import net.dtl.citizens.trader.traders.MoneyBanker;
import net.dtl.citizens.trader.traders.PlayerBanker;
import net.dtl.citizens.trader.traders.PlayerTrader;
import net.dtl.citizens.trader.traders.ServerTrader;
import net.dtl.citizens.trader.traders.Trader;
import net.dtl.citizens.trader.traders.Banker.BankStatus;
import net.dtl.citizens.trader.traders.Trader.TraderStatus;

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
	
	public List<NPC> getAllServerTraders()
	{
		List<NPC> traders=  new ArrayList<NPC>();
		for ( NPC npc : isEconomyNpc )
		{
			if ( npc.getTrait(TraderCharacterTrait.class).getTraderType().equals(TraderType.SERVER_TRADER) )
				traders.add(npc);
		}
		return traders;
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
			return;
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
		if ( this.permManager.has(player, "dtl.trader.bypass.drop-managing") )
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
		
		switch( characterTrait.getTraderType() )
		{
			case SERVER_TRADER:
			{
				if ( economyNpc != null )
				{

					if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) )
					{
						player.sendMessage( locale.getLocaleString("lacks-permissions") );
						return;
					}
					
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						economyNpc.onRightClick(player, characterTrait, npc);
						
						if ( !TraderStatus.hasManageMode(economyNpc.getTraderStatus()) )
							playerInteraction.remove(playerName);
					}
					else
					{
						player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
						
						EconomyNpc newNpc = new ServerTrader(npc, characterTrait.getTraderTrait());
						((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
						playerInteraction.put(playerName, newNpc);
						
						newNpc.onRightClick(player, characterTrait, npc);
						
					}
				}
				else
				{
					EconomyNpc newNpc = new ServerTrader(npc, characterTrait.getTraderTrait());
					((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
					playerInteraction.put(playerName, newNpc);
					
					newNpc.onRightClick(player, characterTrait, npc);
					
				}
				return;
			}
			case PLAYER_TRADER:
			{

				if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						economyNpc.onRightClick(player, characterTrait, npc);
						
						if ( !TraderStatus.hasManageMode(economyNpc.getTraderStatus()) )
							playerInteraction.remove(playerName);
					}
					else
					{
						player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
						
						EconomyNpc newNpc = new PlayerTrader(npc, characterTrait.getTraderTrait());
						((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
						playerInteraction.put(playerName, newNpc);
						
						newNpc.onRightClick(player, characterTrait, npc);
					}
				}
				else
				{
					EconomyNpc newNpc = new PlayerTrader(npc, characterTrait.getTraderTrait());
					((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
					playerInteraction.put(playerName, newNpc);
					
					newNpc.onRightClick(player, characterTrait, npc);
				}
				return;
			}
			case PLAYER_BANK:
			{

				if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getTraderType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						economyNpc.onRightClick(player, characterTrait, npc);
						
						if ( !TraderStatus.hasManageMode(economyNpc.getTraderStatus()) )
							playerInteraction.remove(playerName);
					}
					else
					{
						player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
						
						EconomyNpc newNpc = new PlayerBanker(npc, characterTrait.getBankTrait(), playerName);
						Banker banker = (Banker) playerInteraction.get(playerName);
						if ( !Banker.hasAccount(player) ) {
							playerInteraction.remove(playerName);
							return;
						}
						
						playerInteraction.put(playerName, newNpc);
						player.sendMessage( locale.getLocaleString("bank-deposit-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getDepositFee())) );
						player.sendMessage( locale.getLocaleString("bank-withdraw-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getWithdrawFee())) );
						
						newNpc.onRightClick(player, characterTrait, npc);
						
					}
				}
				else
				{

					if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getTraderType().toString() ) )
					{
						player.sendMessage( locale.getLocaleString("lacks-permissions") );
						return;
					}
					
					EconomyNpc newNpc = new PlayerBanker(npc, characterTrait.getBankTrait(), playerName);
					
				//	Banker banker = (Banker) newNpc;
					if ( !Banker.hasAccount(player) ) {
						playerInteraction.remove(playerName);
						return;
					}
					
					playerInteraction.put(playerName, newNpc);
					newNpc.onRightClick(player, characterTrait, npc);
					
				}
				return;
			}
			case MONEY_BANK:
			{
				if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getTraderType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					{
						player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
						
						EconomyNpc newNpc = new MoneyBanker(npc, characterTrait.getBankTrait(), playerName);
						playerInteraction.put(playerName, newNpc);
						
						newNpc.onRightClick(player, characterTrait, npc);
						
					}
				}
				else
				{
					EconomyNpc newNpc = new MoneyBanker(npc, characterTrait.getBankTrait(), playerName);
					playerInteraction.put(playerName, newNpc);
					
					newNpc.onRightClick(player, characterTrait, npc);
					
				}
				return;
			}
			default:
			{
				
			}
					
		}
		
		
		
		
		
		
		/*
		
		
		
		
		
		
		
		//Manage manager mode r.click
		if ( economyNpc != null )
		{
			
			if ( player.getItemInHand().getTypeId() == config.getManageWand().getTypeId() )
			{
				
				if ( characterTrait.getTraderType().isBanker() )
					return;
				
				if ( characterTrait.getTraderType().isTrader()
						&& ( ( permManager.has(player, "dtl.trader.options.manage") 
								&& characterTrait.getTraderTrait().getOwner().equals(player.getName()) )
								|| permManager.has(player, "dtl.trader.bypass.managing") 
								|| player.isOp() ) )
				{
					
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						//exit the manager mode
						playerInteraction.remove(playerName);
						player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
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
						if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) 
								|| getStartStatus(player) == null )
						{
							player.sendMessage( locale.getLocaleString("no-permissions-type") );
							return;
						}
						playerInteraction.put(playerName, new PlayerTrader(npc, characterTrait.getTraderTrait()));
						playerInteraction.get(playerName).setTraderStatus(getStartStatus(player));
					}
					if ( characterTrait.getTraderType().equals(TraderType.SERVER_TRADER) )
					{
						if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) 
								|| getStartStatus(player) == null )
						{
							player.sendMessage( locale.getLocaleString("no-permissions-type") );
							return;
						}
						playerInteraction.put(playerName, new ServerTrader(npc, characterTrait.getTraderTrait()));
						playerInteraction.get(playerName).setTraderStatus(getStartStatus(player));
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
				
				if ( this.getManageStartStatus(player) == null )
				{
					player.sendMessage( locale.getLocaleString("no-permissions-type") );
					return;
				}
				((Trader)playerInteraction.get(playerName)).switchInventory(getManageStartStatus(player));
				
				
				player.openInventory(playerInteraction.get(playerName).getInventory());
				return;
			}

			
			//exit mm mode and open the normal inventory
			player.sendMessage(ChatColor.AQUA + CitizensAPI.getNPCRegistry().getById(economyNpc.getNpcId()).getFullName() + ChatColor.RED + " exited the manager mode");

			if ( characterTrait.getTraderType().equals(TraderType.PLAYER_TRADER) )
			{
				if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) 
						|| getStartStatus(player) == null )
				{
					player.sendMessage( locale.getLocaleString("no-permissions-type") );
					return;
				}
				playerInteraction.put(playerName, new PlayerTrader(npc, characterTrait.getTraderTrait()));
				((Trader)playerInteraction.get(playerName)).switchInventory(getStartStatus(player));
			}
			if ( characterTrait.getTraderType().equals(TraderType.SERVER_TRADER) )
			{
				if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) 
						|| getStartStatus(player) == null )
				{
					player.sendMessage( locale.getLocaleString("no-permissions-type") );
					return;
				}
				playerInteraction.put(playerName, new ServerTrader(npc, characterTrait.getTraderTrait()));
				((Trader)playerInteraction.get(playerName)).switchInventory(getStartStatus(player));
			}
			
			if ( config.getSettingsWand().getTypeId() == event.getClicker().getItemInHand().getTypeId() )
			{
				if ( permManager.has(player, "dtl.banker.options.settings") 
						&& characterTrait.getTraderType().isBanker() )
				{
					if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getTraderType().toString() ) )
					{
						player.sendMessage( locale.getLocaleString("no-permissions-type") );
						return;
					}
					playerInteraction.put(playerName, new PlayerBanker(npc, characterTrait.getBankTrait(), playerName));
					playerInteraction.get(playerName).setTraderStatus(TraderStatus.BANK_SETTINGS);
				//	playerInteraction.get(playerName).s
					Banker banker = (Banker) playerInteraction.get(playerName);
					banker.useSettingsInv();
					banker.settingsInventory();
					banker.setBankStatus(BankStatus.SETTINGS);
					
					player.openInventory(banker.getInventory());
					return;
				}
			}
			

			if ( player.getGameMode().equals(GameMode.CREATIVE) 
					&& !permManager.has(player, "dtl.banker.bypass.creative") )
			{
				player.sendMessage( locale.getLocaleString("no-permissions-creative") );
				return;
			}

			if ( characterTrait.getTraderType().equals(TraderType.PLAYER_BANK) )
			{
				if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getTraderType().toString() ) 
						|| !permManager.has(player, "dtl.banker.options.account") )
				{
					player.sendMessage( locale.getLocaleString("no-permissions-type") );
					return;
				}
				
				playerInteraction.put(playerName, new PlayerBanker(npc, characterTrait.getBankTrait(), playerName));
				Banker banker = (Banker) playerInteraction.get(playerName);
				if ( !Banker.hasAccount(player) ) {
					playerInteraction.remove(playerName);
					return;
				}
				player.sendMessage( locale.getLocaleString("bank-deposit-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getDepositFee())) );
				player.sendMessage( locale.getLocaleString("bank-withdraw-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getWithdrawFee())) );
			}
			if ( characterTrait.getTraderType().equals(TraderType.MONEY_BANK) )
			{
				if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getTraderType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("no-permissions-type") );
					return;
				}
				playerInteraction.put(playerName, new PlayerBanker(npc, characterTrait.getBankTrait(), playerName));
			}
			
			
			player.openInventory(playerInteraction.get(playerName).getInventory());
			return;
		}
		
		//no mode r.click
		
		if ( player.getItemInHand().getTypeId() == config.getManageWand().getTypeId() )
		{
			
			if ( characterTrait.getTraderType().isTrader()
					&& ( ( permManager.has(player, "dtl.trader.options.manage") 
					&& characterTrait.getTraderTrait().getOwner().equals(player.getName()) )
					|| permManager.has(player, "dtl.trader.bypass.managing") 
					|| player.isOp() ) )
			{
				
				if ( player.getGameMode().equals(GameMode.CREATIVE) 
						&& !permManager.has(player, "dtl.trader.bypass.creative") )
				{
					player.sendMessage( locale.getLocaleString("no-permissions-creative") );
					return;
				}
				
				if ( characterTrait.getTraderType().equals(TraderType.PLAYER_TRADER) )
				{
					if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) 
							|| getStartStatus(player) == null )
					{
						player.sendMessage( locale.getLocaleString("no-permissions-type") );
						return;
					}
					playerInteraction.put(playerName, new PlayerTrader(npc, characterTrait.getTraderTrait()));
					((Trader)playerInteraction.get(playerName)).switchInventory(getStartStatus(player));
				}
				if ( characterTrait.getTraderType().equals(TraderType.SERVER_TRADER) )
				{
					if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) 
							|| getStartStatus(player) == null )
					{
						player.sendMessage( locale.getLocaleString("no-permissions-type") );
						return;
					}
					playerInteraction.put(playerName, new ServerTrader(npc, characterTrait.getTraderTrait()));
					((Trader)playerInteraction.get(playerName)).switchInventory(getStartStatus(player));
				}
				
				playerInteraction.get(playerName).setTraderStatus(TraderStatus.MANAGE);
				player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
				return;
			}
		}
		if ( config.getSettingsWand().getTypeId() == event.getClicker().getItemInHand().getTypeId() )
		{
			if ( permManager.has(player, "dtl.banker.options.settings") 
					&& characterTrait.getTraderType().isBanker() )
			{
				if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getTraderType().toString() ) )
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
					banker.useSettingsInv();
					banker.settingsInventory();
					banker.setBankStatus(BankStatus.SETTINGS);
					
					player.openInventory(banker.getInventory());
				}
				
			}
			return;
		
		//exit mm mode and open the normal inventory
		}
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& !permManager.has(player, "dtl.trader.bypass.creative") )
		{
			player.sendMessage( locale.getLocaleString("no-permissions-creative") );
			return;
		}
		
		if ( characterTrait.getTraderType().equals(TraderType.PLAYER_TRADER) )
		{
			if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) 
					|| getStartStatus(player) == null )
			{
				player.sendMessage( locale.getLocaleString("no-permissions-type") );
				return;
			}
			playerInteraction.put(playerName, new PlayerTrader(npc, characterTrait.getTraderTrait()));
			((Trader)playerInteraction.get(playerName)).switchInventory(getStartStatus(player));
		}
		if ( characterTrait.getTraderType().equals(TraderType.SERVER_TRADER) )
		{
			if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getTraderType().toString() ) 
					|| getStartStatus(player) == null )
			{
				player.sendMessage( locale.getLocaleString("no-permissions-type") );
				return;
			}
			playerInteraction.put(playerName, new ServerTrader(npc, characterTrait.getTraderTrait()));
			((Trader)playerInteraction.get(playerName)).switchInventory(getStartStatus(player));
		}
		if ( characterTrait.getTraderType().equals(TraderType.PLAYER_BANK) )
		{
			
			if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getTraderType().toString() ) 
					|| !permManager.has(player, "dtl.banker.options.account") )
			{
				player.sendMessage( locale.getLocaleString("no-permissions-type") );
				return;
			}
			
			playerInteraction.put(playerName, new PlayerBanker(npc, characterTrait.getBankTrait(), playerName));
			Banker banker = (Banker) playerInteraction.get(playerName);
			if ( !Banker.hasAccount(player) ) {
				playerInteraction.remove(playerName);
				return;
			}
			player.sendMessage( locale.getLocaleString("bank-deposit-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getDepositFee())) );
			player.sendMessage( locale.getLocaleString("bank-withdraw-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getWithdrawFee())) );
		
		}
		if ( characterTrait.getTraderType().equals(TraderType.MONEY_BANK) )
		{
			
			if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getTraderType().toString() ) )
			{
				player.sendMessage( locale.getLocaleString("no-permissions-type") );
				return;
			}
			
			playerInteraction.put(playerName, new MoneyBanker(npc, characterTrait.getBankTrait(), playerName));
		}
		
		player.openInventory(playerInteraction.get(playerName).getInventory());
		
		*/
	}
	
/*	@EventHandler
	public void onNpcSpawn(net.citizensnpcs.api.event.NPCSpawnEvent event)
	{
		for ( Trait trait : event.getNPC().getTraits() )
			System.out.print(trait.getName());
	}*/
	
	
	
	
	
}

package net.dtl.citizenstrader_new;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader_new.TraderCharacterTrait.TraderType;
import net.dtl.citizenstrader_new.traders.Banker;
import net.dtl.citizenstrader_new.traders.PlayerBanker;
import net.dtl.citizenstrader_new.traders.PlayerTrader;
import net.dtl.citizenstrader_new.traders.ServerTrader;
import net.dtl.citizenstrader_new.traders.Trader;
import net.dtl.citizenstrader_new.traders.Trader.TraderStatus;
import net.dtl.citizenstrader_new.traits.BankTrait;
import net.dtl.citizenstrader_new.traits.TraderTrait;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class TraderManager implements Listener {
	//trader config
	protected static TraderConfig config = CitizensTrader.config;
	protected static LocaleManager locale = CitizensTrader.getLocale();
	
	//private static TraderConfig config;
	private PermissionsManager permManager = CitizensTrader.getPermissionsManager();
	
	//for laggy servers...
	private List<String> playersAntiDclick;
	private Timer antiDClickTimer;
	private long timerDelay;
	
	//traders
	private HashMap<String,Trader> ongoingTrades = new HashMap<String,Trader>();	
	private List<NPC> isTraderNpc;
	
	//bankers
	private HashMap<String,Banker> bankManagers = new HashMap<String,Banker>();
	private List<NPC> isBankerNpc;
	
	public TraderManager(ConfigurationSection config) {
		this.isTraderNpc = new ArrayList<NPC>();
		this.isBankerNpc = new ArrayList<NPC>();
		playersAntiDclick = new ArrayList<String>();
		antiDClickTimer = new Timer("anti-dclick");
		
		
		if ( !config.contains("trader.rclick-interval") )
		{
			config.set("trader.rclick-interval", 1000);
			CitizensTrader.plugin.saveConfig();
		}
		else
			timerDelay = config.getLong("trader.rclick-interval");
		//config = CitizensTrader.getTraderConfig();
	}
	
	//banks
	public Banker getManagedBanker(String player)
	{
		if ( bankManagers.containsKey(player) )
			return bankManagers.get(player);
		return null;
	}
	public boolean isBankerNpc(NPC npc) {
		return this.isBankerNpc.contains(npc);
	}
	protected void addBankerNpc(NPC npc) {
		if ( !isBankerNpc(npc) ) {
			this.isBankerNpc.add(npc);
		}
	}
	
	//traders
	public Trader getOngoingTrades(String player) {
		if ( ongoingTrades.containsKey(player) )
			return ongoingTrades.get(player);
		return null;
	}
	public boolean isTraderNpc(NPC npc) {
		return this.isTraderNpc.contains(npc);
	}
	protected void addTraderNpc(NPC npc) {
		if ( !isTraderNpc(npc) ) {
			this.isTraderNpc.add(npc);
		}
	}	
	
	//Events handling
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if ( !( event.getWhoClicked() instanceof Player ) )
			return;
		
		
		if ( event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE) 
				&&  event.getView().getType().equals(InventoryType.PLAYER) )
			return;
		

		if ( ( event.getWhoClicked().getGameMode().equals(GameMode.SURVIVAL) 
				|| event.getWhoClicked().getGameMode().equals(GameMode.SURVIVAL) )
				&& event.getView().getType().equals(InventoryType.CRAFTING) )
			return;
		
		Player p = (Player) event.getWhoClicked();

		if ( ongoingTrades.containsKey(p.getName())) {
			
			if ( event.getRawSlot() < 0 ) {
				//event.setCancelled(true);
				if ( ongoingTrades.get(p.getName()).hasSelectedItem() )
					event.setCursor(new ItemStack(Material.AIR));
				return;
			}

			if ( TraderStatus.hasManageMode(ongoingTrades.get(p.getName()).getTraderStatus()) ) {
				
				
				if ( ongoingTrades.get(p.getName()).equalsTraderStatus(TraderStatus.MANAGE) )
					ongoingTrades.get(p.getName()).setTraderStatus(TraderStatus.MANAGE_SELL);
				
				
				ongoingTrades.get(p.getName()).managerMode(event);

			}
			else 
			{
				
				ongoingTrades.get(p.getName()).simpleMode(event);
				
			}
		}
		else
		if ( bankManagers.containsKey(p.getName()) )
		{
			if ( event.getRawSlot() < 0 ) {
				return;
			}

			bankManagers.get(p.getName()).simpleMode(event);
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		
		
		//get the trader we are trading with
		Trader trader = ongoingTrades.get(player.getName());
		
		
		//ups, no transaction sorry :<
		if( trader == null )
		{
			return;
		}
		
		
		//are we managing?
		if ( TraderStatus.hasManageMode(trader.getTraderStatus()) )
		{
			
			//have we managed amounts?
			if (  trader.equalsTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT) ) 
			{
				//save amounts and set the basic managing page
				trader.saveManagedAmouts();
				trader.switchInventory(TraderStatus.MANAGE_SELL);
			}
			
			//reset the traders status
			trader.reset(TraderStatus.MANAGE);
		}
		else
		//no managers here, what a pity 
		{
			
			
			//lets end this transaction
			ongoingTrades.remove(player.getName());
		}
		
		
		Banker banker = bankManagers.get(player.getName());
		
		if ( banker == null )
			return;
		
		bankManagers.remove(player.getName());
			
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		
		//come on... you thought i've forgot to prevent item dropping ;P
		Player player = (Player) event.getPlayer();
		
		//get the trader we are trading with
		Trader trader = ongoingTrades.get(player.getName());
		Banker banker = bankManagers.get(player.getName());
		
		
		//ups, no transaction sorry :<
		if( trader == null && banker == null )
		{
			return;
		}
				
		//are we managing?
		if ( this.permManager.has(player, "dtl.trader.bypass.drop-item") )
			return;
		
		//sorry we can't allow drop you that item :P
		event.setCancelled(true);
		
	}
	
	@EventHandler
	public void onNPCDespawn(NPCDespawnEvent event) {
		
		//despawning an npc? DESTROY IT Buahahahah! xD
		if ( this.isTraderNpc.contains(event.getNPC()) )
			this.isTraderNpc.remove(event.getNPC());

		if ( this.isBankerNpc.contains(event.getNPC()) )
			this.isBankerNpc.remove(event.getNPC());
		
	}
	
	@EventHandler
	public void onNPCRightCLick(NPCRightClickEvent event) {		
		//bad touch hurts forever...
		//System.out.print("a");
		if ( !isTraderNpc(event.getNPC()) 
				&& !isBankerNpc(event.getNPC()) ) 
			return;

		//System.out.print("aa");
		Player player = event.getClicker();

		
		final String playerName = player.getName();
		
		if ( playersAntiDclick.contains(playerName)
				&& !permManager.has(player, "dtl.trader.bypass.interval"))
			return;
		
		playersAntiDclick.add(playerName);
		
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				playersAntiDclick.remove(playerName);
			}
		};
		//System.out.print("aaa");
		
		antiDClickTimer.schedule(task, timerDelay);
		
		//get the touched and the toucher
		NPC npc = event.getNPC();

		//System.out.print("ab");
		//get the current assigned manager :P (exists one?)
		Trader trader = ongoingTrades.get(playerName);
		Banker banker = bankManagers.get(playerName);
		
	//	if ( trader != null )
		manageTraderRClick(npc, player, trader);
	//	lse if ( banker != null )
		manageBankerRClick(npc, player, banker);

		//System.out.print("aaab");
	}
	
	
	private void manageBankerRClick(NPC npc, Player player, Banker trader) {
		
		if ( !isBankerNpc(npc) )
			return;
		//get the desired trait 
		BankTrait trait = npc.getTrait(TraderCharacterTrait.class).getBankTrait();
		
		
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& !permManager.has(player, "dtl.trader.bypass.creative") )
		{
			player.sendMessage(locale.getMessage("no-permissions"));
			return;
		}
		
		if ( !permManager.has(player, "dtl.trader.options.simple-mode") )
		{
			player.sendMessage(locale.getMessage("no-permissions"));
			return;
		}
		
		if ( !permManager.has(player, "dtl.trader.options.type.player-bank") )
		{
			player.sendMessage(locale.getMessage("no-permissions"));
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
						
						player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
						return;
					}
					
					if ( !permManager.has(player, "dtl.trader.options.type." + trait.getTraderType().toString()) )
					{
						player.sendMessage( locale.getMessage("no-permissions") );
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
								&& !permManager.has(player, locale.getMessage("no-permissions")) )
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
						player.sendMessage( locale.getMessage("no-permissions") );
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
							player.sendMessage( locale.getMessage("no-permissions") );
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
					}*/
					
					//we are managing!
					ongoingTrades.get(player.getName()).setTraderStatus(TraderStatus.MANAGE);
					player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
					
					//return lets end this! :D
					return;
				}
				
				
			}
			else
			{
				player.sendMessage(ChatColor.RED + locale.getMessage("no-permissions"));
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
					player.sendMessage(locale.getMessage("no-permissions"));
					return;
				}
				
				if ( !permManager.has(player, "dtl.trader.options.simple-mode") )
				{
					player.sendMessage(locale.getMessage("no-permissions"));
					return;
				}
				
				if ( !permManager.has(player, "dtl.trader.options.type." + trait.getTraderType().toString()) )
				{
					player.sendMessage(locale.getMessage("no-permissions"));
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
	}
	
}

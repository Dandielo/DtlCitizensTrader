package net.dtl.citizens.trader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.api.PacketsAPI;
import net.dtl.api.exceptions.InvalidWorldsException;
import net.dtl.citizens.trader.TraderCharacterTrait.EcoNpcType;
import net.dtl.citizens.trader.managers.LocaleManager;
import net.dtl.citizens.trader.managers.PermissionsManager;
import net.dtl.citizens.trader.objects.NBTTagEditor;
import net.dtl.citizens.trader.types.Banker;
import net.dtl.citizens.trader.types.EconomyNpc;
import net.dtl.citizens.trader.types.MarketTrader;
import net.dtl.citizens.trader.types.MoneyBanker;
import net.dtl.citizens.trader.types.PlayerTrader;
import net.dtl.citizens.trader.types.PrivateBanker;
import net.dtl.citizens.trader.types.ServerTrader;
import net.dtl.citizens.trader.types.Trader;
import net.dtl.citizens.trader.types.Trader.TraderStatus;
import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.Packet52MultiBlockChange;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class NpcEcoManager implements Listener {
	//trader configs
	protected static ItemsConfig config = CitizensTrader.getInstance().getItemConfig();
	
	//managers
	protected static LocaleManager locale = CitizensTrader.getLocaleManager();
	private PermissionsManager permManager = CitizensTrader.getPermissionsManager();
	private Timer timer;
	
	//EconomyNpc
	private HashMap<String,EconomyNpc> playerInteraction;
	private List<NPC> isEconomyNpc;
	
	public NpcEcoManager() 
	{
		//initialize playerInteraction
		playerInteraction = new HashMap<String, EconomyNpc>();
		//initialize the economyNpcList
		isEconomyNpc = new ArrayList<NPC>();
		initTimer();
	}
	
	public void end() {
		timer.cancel();
	}
	
	public void initTimer() {
		if ( timer != null ) 
		{
			timer.cancel();
		}
		
		timer = new Timer("DtlDescription-Cleaner");
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
			if ( npc.getTrait(TraderCharacterTrait.class).getType().equals(EcoNpcType.SERVER_TRADER) )
				traders.add(npc);
		}
		return traders;
	}
	
	public EconomyNpc getServerTraderByName(String name, Player player)
	{
		for ( NPC npc : isEconomyNpc )
		{
			if ( npc.getName().equals(name) )
				return new ServerTrader(npc.getTrait(TraderCharacterTrait.class), npc, player);
		}
		return null;
	}
	//Interaction
	public EconomyNpc getInteractionNpc(String player) {
		if ( playerInteraction.containsKey(player) )
			return playerInteraction.get(player);
		return null;
	}
	//Interaction
	public void addInteractionNpc(String player, EconomyNpc npc) {
		playerInteraction.put(player, npc);
	}
	//Interaction
	public void removeInteractionNpc(String player) {
		playerInteraction.remove(player);
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
		
		//TODO IMPORTANT, change this
		if ( !economyNpc.locked() )
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
		if ( economyNpc.locked() )
		{ 
			economyNpc.managerMode(event);
			return;
		}
		if ( economyNpc instanceof Banker )
		{
			if ( ((Banker)economyNpc).getStatus().settings() ) 
			{
				economyNpc.settingsMode(event);
				return;			
			}
		}
		economyNpc.simpleMode(event);
	}
	
	
	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent event) {
		final Player player = (Player) event.getPlayer();
		
		
		EconomyNpc economyNpc = playerInteraction.get(player.getName());
		//System.out.print(event.getPlayer().getInventory().getSize());
		
		if ( economyNpc == null )
			return;
		
		
		if ( economyNpc.locked() )
		{
			
			if ( economyNpc instanceof Trader )
				if ( ((Trader)economyNpc).getTraderStatus().equals(TraderStatus.MANAGE_SELL_AMOUNT) )
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
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (  playerInteraction.get(player.getName()) == null )
					NBTTagEditor.removeDescriptions(event.getPlayer().getInventory());
			}
		};
		timer.schedule(task, 1000);
			
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
		
		
		if ( !economyNpc.locked() )
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
		
		Player player = event.getClicker();
		//used variables
		final String playerName = player.getName();
		NPC npc = event.getNPC();
		
		//EconomyNpc
		EconomyNpc economyNpc = playerInteraction.get(playerName);


		//trader character
		TraderCharacterTrait characterTrait = npc.getTrait(TraderCharacterTrait.class);
		
		switch( characterTrait.getType() )
		{
			case SERVER_TRADER:
			{
				if ( economyNpc != null )
				{
					if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getType().toString() ) )
					{
						player.sendMessage( locale.getLocaleString("lacks-permissions") );
						return;
					}
					
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						economyNpc.onRightClick(player, characterTrait, npc);
						
						if ( !economyNpc.locked() )
							playerInteraction.remove(playerName);
					}
					else
					{
						player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");

						EconomyNpc newNpc = new ServerTrader(characterTrait, npc, player);
						((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
						playerInteraction.put(playerName, newNpc);
						
						if ( !newNpc.onRightClick(player, characterTrait, npc) )
							playerInteraction.remove(playerName);
					
					}
				}
				else
				{
					EconomyNpc newNpc = new ServerTrader(characterTrait, npc, player);
					playerInteraction.put(playerName, newNpc);
					
					if ( !newNpc.onRightClick(player, characterTrait, npc) )
						playerInteraction.remove(playerName);
					
				}
				return;
			}
			case PLAYER_TRADER:
			{
				if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						economyNpc.onRightClick(player, characterTrait, npc);
						
						if ( !economyNpc.locked() )
							playerInteraction.remove(playerName);
					}
					else
					{
						player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
						
						EconomyNpc newNpc = new PlayerTrader(characterTrait, npc, player);
						((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
						playerInteraction.put(playerName, newNpc);
						
						if ( !newNpc.onRightClick(player, characterTrait, npc) )
							playerInteraction.remove(playerName);
					}
				}
				else
				{
					EconomyNpc newNpc = new PlayerTrader(characterTrait, npc, player);
					((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
					playerInteraction.put(playerName, newNpc);
					
					if ( !newNpc.onRightClick(player, characterTrait, npc) )
						playerInteraction.remove(playerName);
				}
				return;
			}
			case MARKET_TRADER:
			{
				if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						economyNpc.onRightClick(player, characterTrait, npc);
						
						if ( !economyNpc.locked() )
							playerInteraction.remove(playerName);
					}
					else
					{
						player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
						
						EconomyNpc newNpc = new MarketTrader(characterTrait, npc, player);
						((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
						playerInteraction.put(playerName, newNpc);
						
						if ( !newNpc.onRightClick(player, characterTrait, npc) )
							playerInteraction.remove(playerName);
					}
				}
				else
				{
					EconomyNpc newNpc = new MarketTrader(characterTrait, npc, player);
					((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
					playerInteraction.put(playerName, newNpc);
					
					if ( !newNpc.onRightClick(player, characterTrait, npc) )
						playerInteraction.remove(playerName);
				}
				return;
			}
			case PRIVATE_BANKER:
			{

				if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					
					player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
					
					EconomyNpc newNpc = new PrivateBanker(npc, characterTrait.getBankTrait(), playerName);
					playerInteraction.put(playerName, newNpc);
				//	player.sendMessage( locale.getLocaleString("bank-deposit-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getDepositFee())) );
				//	player.sendMessage( locale.getLocaleString("bank-withdraw-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getWithdrawFee())) );
					
					newNpc.onRightClick(player, characterTrait, npc);
					
				}
				else
				{

					if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getType().toString() ) )
					{
						player.sendMessage( locale.getLocaleString("lacks-permissions") );
						return;
					}
					
					EconomyNpc newNpc = new PrivateBanker(npc, characterTrait.getBankTrait(), playerName);
					
					playerInteraction.put(playerName, newNpc);
					newNpc.onRightClick(player, characterTrait, npc);
					
				}
				return;
			}
			case MONEY_BANKER:
			{
				if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getType().toString() ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					{
						player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
						
						EconomyNpc newNpc = new MoneyBanker(npc, characterTrait, playerName);
						playerInteraction.put(playerName, newNpc);
						
						newNpc.onRightClick(player, characterTrait, npc);
						
					}
				}
				else
				{
					EconomyNpc newNpc = new MoneyBanker(npc, characterTrait, playerName);
					playerInteraction.put(playerName, newNpc);
					
					newNpc.onRightClick(player, characterTrait, npc);
					
				}
				return;
			}
			default:
			{
				
			}
					
		}
		
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event)
	{
		NBTTagEditor.removeDescriptions(event.getPlayer().getInventory());
	}
	

}

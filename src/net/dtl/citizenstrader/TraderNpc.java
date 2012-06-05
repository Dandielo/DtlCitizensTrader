package net.dtl.citizenstrader;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.NPCSelectEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizenstrader.TraderStatus.Status;
import net.dtl.citizenstrader.traits.InventoryTrait;
import net.dtl.citizenstrader.traits.StockItem;
import net.milkbowl.vault.economy.Economy;


public class TraderNpc extends Character implements Listener {
//	private CitizensTrader plugin;
	private Economy econ;
	
	private HashMap<String,TraderStatus> state = new HashMap<String,TraderStatus>();
	
	public TraderNpc() {
	}
	
	public void setEcon(Economy e) {
		econ = e;
	}
	
	public TraderStatus getStatus(String name) {
		if ( state.containsKey(name) )
			return state.get(name);
		return null;
	}
//	private HashMap<Integer,List<TraderItem>> SellItems = new HashMap<Integer,List<TraderItem>>(); 
//	private HashMap<Integer,List<TraderItem>> BuyItems = new HashMap<Integer,List<TraderItem>>(); 
//	private List<TraderItem> SellItems = new ArrayList<TraderItem>(); 
//	private List<TraderItem> BuyItems = new ArrayList<TraderItem>(); 
//	private List<Integer> traderID = new ArrayList<Integer>();
	
	/*public TraderNpc(TraderNpc character) {
		SellItems = character.getSellItems();
		BuyItems = character.getBuyItems();
	}*/

	@Override
	public void load(DataKey arg0) throws NPCLoadException {		
/*		List<String> list = new ArrayList<String>();
		if ( arg0.keyExists("items") && arg0.getRelative("items").keyExists("sell") )
			list = (List<String>) arg0.getRelative("items").getRaw("sell");

	//	System.out.print(list);
		List<TraderItem> items = new ArrayList<TraderItem>();
		for ( int i = 0 ; i < list.size() ; ++i ) {
			items.add(new TraderItem(list.get(i)));
		}
		SellItems.put(-1, new ArrayList<TraderItem>(items));
		items.clear();
		list.clear();
		
		if ( arg0.keyExists("items") && arg0.getRelative("items").keyExists("buy") )
			list = (List<String>) arg0.getRelative("items").getRaw("buy");
	//	System.out.print(list);
		for ( int i = 0 ; i < list.size() ; ++i ) {
			items.add(new TraderItem(list.get(i)));
		}
		BuyItems.put(-1, new ArrayList<TraderItem>(items));*/
		
	}

	@Override
	public void save(DataKey arg0) {
	/*	List<TraderItem> buy = new ArrayList<TraderItem>();
		List<TraderItem> sell = new ArrayList<TraderItem>();
		if ( !SellItems.isEmpty() )
			sell = SellItems.get(traderID.get(0));
		if ( !BuyItems.isEmpty() )
			buy = BuyItems.get(traderID.get(0));
		//if ( !traderID.isEmpty() )
			traderID.remove(0);
		
		List<String> strSell = new ArrayList<String>();
		for ( int i = 0 ; i < sell.size() ; ++i ) {
			strSell.add(sell.get(i).getItemStack().getTypeId()+(sell.get(i).getItemStack().getData().getData() != 0 ? ":" + sell.get(i).getItemStack().getData().getData() + " " : " " ) + sell.get(i).getCost() + " " + sell.get(i).getAmout() );
		}
		List<String> strBuy = new ArrayList<String>();
		for ( int i = 0 ; i < buy.size() ; ++i ) {
			strBuy.add(buy.get(i).getItemStack().getTypeId()+(buy.get(i).getItemStack().getData().getData() != 0 ? ":" + buy.get(i).getItemStack().getData().getData() + " " : " " ) + buy.get(i).getCost() + " " + buy.get(i).getAmout() );
		}
		
		*/
	//	HashMap<String,List<TraderItem>> map = new HashMap<String,List<TraderItem>>();
	//	map.put("sell", sell);
	//	map.put("buy", buy);

//		arg0.setRaw("items", "sell");
//		arg0.getRelative("items").setRaw("sell", strSell);
//		arg0.getRelative("items").setRaw("buy", strBuy);
	} 
	
	@Override
	public void onRightClick(NPC npc, Player p) {
		
	//	System.out.println("Customer inventory!");
	//	by.setMetadata("npc-talking-with", new FixedMetadataValue(CitizensTrader.plugin, npc));
		if ( p.getItemInHand().getTypeId() != 280 ) {
			if ( !state.containsKey(p.getName()) )
				state.put(p.getName(),new TraderStatus(npc));
			state.get(p.getName()).setInventory(npc.getTrait(InventoryTrait.class).inventoryView(54,npc.getName()));
			p.openInventory(state.get(p.getName()).getInventory());
			
		} else {
			if ( state.containsKey(p.getName()) && state.get(p.getName()).getTrader().getId() == npc.getId() ) {
				if ( !state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE) ) {
					state.get(p.getName()).setStatus(Status.PLAYER_MANAGE);
					p.sendMessage(ChatColor.RED + "Trader manager enabled");
				} else if ( state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE) ) { 
					state.get(p.getName()).setStatus(Status.PLAYER_SELL);
					p.sendMessage(ChatColor.RED + "Trader manager disabled");
				}
			} else {
				state.put(p.getName(),new TraderStatus(npc,Status.PLAYER_MANAGE));
				p.sendMessage(ChatColor.RED + "Trader manager enabled");
			}
		}
			
		
	}
	
	@Override
    public void onSet(NPC npc) {
        if( !npc.hasTrait(InventoryTrait.class) ){
            npc.addTrait( new InventoryTrait() );
        }
    }
	
	
	
	
	@EventHandler
	public void npcSelect(NPCSelectEvent event) {
		if ( event.getSelector() instanceof Player ) {
			Player p = (Player) event.getSelector();
		//	if ( plugin.dtlProject.getPermissions().has(p, "dtl.citizens.characters.trader.manage") ) {
				
			}
		//}
					
		//		plugin.setSelected(event.getNPC().getId());
		//		event.getPlayer().sendMessage("you have selected " + CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()).getFullName() + ".");
		
	}
	
	@EventHandler 
	public void npcSpawn(NPCSpawnEvent event) {
		/*if ( event.getNPC().getCharacter() instanceof TraderNpc ) {
			((TraderNpc)event.getNPC().getCharacter()).setTraderID(event.getNPC().getId());
		}*/
	}
/*	@EventHandler 
	public void npcSpawn(NPCLeftClickEvent event) {
		event.setCancelled(true);
	}*/
	/*
	@EventHandler
	public void playerInteractEntity(PlayerInteractEntityEvent event) {
		if ( !event.getPlayer().getItemInHand().getType().equals(Material.STICK) ) {
			Collection<NPC> npcs = CitizensAPI.getNPCRegistry().getNPCs(TraderNpc.class);
			if ( npcs.contains(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())) ) {
				Inventory inv = CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()).getInventory();
				((TraderNpc)CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()).getCharacter()).setInventory(inv,CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()).getId(),event.getPlayer(),dProject);
				event.getPlayer().openInventory(inv);
			} 
		}
	}*///boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();

	@SuppressWarnings("unused")
	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {
		if ( event.getRawSlot() < 0 )
			return;
		if ( event.getWhoClicked() instanceof Player ) {
			Player p = (Player) event.getWhoClicked();
			if ( state.containsKey(p.getName()) ) {
				TraderStatus trader = state.get(p.getName());
				InventoryTrait sr = trader.getTrader().getTrait(InventoryTrait.class);
				boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
				
				if ( !trader.getStatus().equals(Status.PLAYER_MANAGE) && top ) {
					StockItem si = null;
					
					if ( trader.getStatus().equals(Status.PLAYER_SELL) || trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) ) {
						if ( trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) )
							si = trader.getStockItem();
						else
							si = sr.itemForSell(event.getSlot());
						if ( si != null ) {
							if ( event.isShiftClick() ) {
								if ( si.hasMultipleAmouts() ) {
									if ( econ.has(p.getName(), si.getPrice(event.getSlot())) ) {
										if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) &&
											 !event.getCurrentItem().getType().equals(Material.AIR)) {
											econ.withdrawPlayer(p.getName(), si.getPrice(event.getSlot()));
											p.getInventory().addItem(event.getCurrentItem());
											p.sendMessage(ChatColor.GOLD + "You bought " + event.getCurrentItem().getAmount() + " for " + si.getPrice(event.getSlot()) + ".");
										}
									} else {
										p.sendMessage(ChatColor.GOLD + "You don't have enough money.");
									}
								} else {
									if ( econ.has(p.getName(), si.getPrice()) ) {
										econ.withdrawPlayer(p.getName(), si.getPrice());
										p.getInventory().addItem(si.getItemStack());
										p.sendMessage(ChatColor.GOLD + "You bought " + si.getItemStack().getAmount() + " for " + si.getPrice() + ".");
									} else {
										p.sendMessage(ChatColor.GOLD + "You don't have enough money.");
									}
								}
							} else {
								if ( trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) ) {
									if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
										trader.getInventory().clear();
										sr.inventoryView(trader.getInventory(),Status.PLAYER_SELL);
										trader.setStatus(Status.PLAYER_SELL);
										trader.setStockItem(null);
									} else {
										if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) &&
											 !event.getCurrentItem().getType().equals(Material.AIR) ) 
											p.sendMessage(ChatColor.GOLD + "This item costs " + si.getPrice(event.getSlot()) + ".");
									}
								} else if ( trader.getStatus().equals(Status.PLAYER_SELL) ) {
									if ( si.hasMultipleAmouts() ) {
										if ( trader.getStatus().equals(Status.PLAYER_SELL) ) {
											trader.getInventory().clear();
											InventoryTrait.setInventoryWith(trader.getInventory(), si);
											trader.setStatus(Status.PLAYER_SELL_AMOUT);
											trader.setStockItem(si);
										}
									} else {
										
											p.sendMessage(ChatColor.GOLD + "This item costs " + si.getPrice() + ".");
									}
								}
							} 
						} else if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)5)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
							trader.getInventory().clear();
							sr.inventoryView(trader.getInventory(),Status.PLAYER_BUY);
							trader.setStatus(Status.PLAYER_BUY);
							trader.setStockItem(null);
						} 
					} else if ( trader.getStatus().equals(Status.PLAYER_BUY) ) {
						si = sr.wantItemBuy(event.getSlot());
						if ( si != null ) {
							if ( si.getItemStack().getType().equals(event.getCursor().getType()) ) {
								econ.depositPlayer(p.getName(), si.getPrice()*event.getCursor().getAmount());
								p.sendMessage(ChatColor.GOLD + "You sold " + event.getCursor().getAmount() + " for " + si.getPrice(event.getSlot()) + ".");
								event.setCursor(new ItemStack(Material.AIR));
							} else {
								if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) &&
									 !event.getCurrentItem().getType().equals(Material.AIR) ) 
								p.sendMessage(ChatColor.GOLD + "You can get for this item " + si.getPrice(event.getSlot()) + " money.");
							}
						} else {
							if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
								trader.getInventory().clear();
								sr.inventoryView(trader.getInventory(),Status.PLAYER_SELL);
								trader.setStatus(Status.PLAYER_SELL);
								trader.setStockItem(null);
							}
						}
					}					
					event.setCancelled(true);
				} else {
					
				}
			} 
		}
	}
	



	@EventHandler
	public void inventoryClose(InventoryCloseEvent event){
	    if(state.containsKey(event.getPlayer().getName())){
	        state.remove(event.getPlayer().getName());
	    }
	}
/*
			}
			}
			
		//	Collection<NPC> npcs = CitizensAPI.getNPCRegistry().getNPCs(TraderNpc.class);

			for ( int i = 0 ; i < npcs.size() ; ++i ) {
				if ( ((NPC)npcs.toArray()[i]).getName().equals(event.getInventory().getName()) ) {
					TraderNpc trader = (TraderNpc)((NPC)npcs.toArray()[i]).getCharacter();/*
					if ( ( !event.getCursor().getType().equals(Material.AIR) && event.getCurrentItem().getType().equals(Material.FIRE) ) || event.getCurrentItem().getType().equals(Material.FIRE) ) {
						if ( ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).canBuy(event.getCursor(),((NPC)npcs.toArray()[i]).getId()) )
							if ( ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).bought(event.getCursor(),((NPC)npcs.toArray()[i]).getId(),dProject.getEconomy(),p) ) {
								event.setCursor(new ItemStack(0,0));
								event.setCancelled(true);
								return;
							}
						event.setCancelled(true);
					} else
					if ( trader.checkSlot(((NPC)npcs.toArray()[i]).getId(),event.getSlot(),event.getCurrentItem()) && ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).canSell(event.getCurrentItem(),((NPC)npcs.toArray()[i]).getId()) || event.getCurrentItem().equals(Material.FIRE) ) {
						if ( event.isShiftClick() && ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).sold(event.getCurrentItem(),((NPC)npcs.toArray()[i]).getId(),dProject.getEconomy(),p) )
							p.getInventory().addItem(event.getCurrentItem());
						else
							p.sendMessage(ChatColor.GOLD + event.getCurrentItem().getType().name() + " kosztuje " + ((TraderNpc)((NPC)npcs.toArray()[i]).getCharacter()).getCost(((NPC)npcs.toArray()[i]).getId(),event.getSlot()) + "$ za sztuke.");
						event.setCancelled(true);
					} 
					if ( event.isShiftClick() )
						event.setCancelled(true);
				}
			}
		}*/
}
	
	/*
	
	public List<TraderItem> getList(int id, boolean sell) {
		System.out.print(id+ " " +sell);
		if ( sell && SellItems.get(id) != null )
			return SellItems.get(id);
		else if ( BuyItems.get(id) != null )
			return BuyItems.get(id);
		return new ArrayList<TraderItem>();
	}
	
	public void addItem(String itemInfo, int id, boolean sell) {
		if ( sell ) {
			List<TraderItem> items;
			if ( SellItems.get(id) == null )
				items = new ArrayList<TraderItem>();
			else
				items = SellItems.get(id);
			items.add(new TraderItem(itemInfo));
			SellItems.put(id, new ArrayList<TraderItem>(items));
		} else {
			List<TraderItem> items;
			if ( BuyItems.get(id) == null )
				items = new ArrayList<TraderItem>();
			else
				items = BuyItems.get(id);
			items.add(new TraderItem(itemInfo));
			BuyItems.put(id, new ArrayList<TraderItem>(items));
		}
	}
	
	public void removeItem(String itemInfo, int id, boolean sell) {
		if ( sell ) {
			if ( SellItems.get(id) == null )
				return;
			List<TraderItem> items = SellItems.get(id);
			if ( items.size() <= Integer.parseInt(itemInfo) )
				return;
			items.remove(Integer.parseInt(itemInfo));
			SellItems.put(id, new ArrayList<TraderItem>(items));
		} else {
			if ( BuyItems.get(id) == null )
				return;
			List<TraderItem> items = BuyItems.get(id);
			if ( items.size() <= Integer.parseInt(itemInfo) )
				return;
			items.remove(Integer.parseInt(itemInfo));
			BuyItems.put(id, new ArrayList<TraderItem>(items));
		}
	}
	
	public void editItem(int index, String itemInfo, int id, boolean sell) {
		if ( sell ) {
			if ( SellItems.get(id) == null )
				return;
			List<TraderItem> items = SellItems.get(id);
			if ( items.size() <= index )
				return;
			items.remove(index);
			items.add(index, new TraderItem(itemInfo));
			SellItems.put(id, new ArrayList<TraderItem>(items));
		} else {
			if ( BuyItems.get(id) == null )
				return;
			List<TraderItem> items = BuyItems.get(id);
			if ( items.size() <= index )
				return;
			items.remove(index);
			items.add(index, new TraderItem(itemInfo));
			BuyItems.put(id, new ArrayList<TraderItem>(items));
		}
	}
	
	public void setTraderID(int id) {
		if ( SellItems.get(-1) != null ) {
			SellItems.put(id, SellItems.get(-1));
			SellItems.remove(-1);
		} else {
			SellItems.put(id, new ArrayList<TraderItem>());
		}
		if ( BuyItems.get(-1) != null ) {
			BuyItems.put(id, BuyItems.get(-1));
			BuyItems.remove(-1);
		} else {
			BuyItems.put(id, new ArrayList<TraderItem>());
		}
		traderID.add(id);
	}
	
	public String getCost( int npcID , int slot ) {
		return String.valueOf(SellItems.get(npcID).get(slot).getCost());
	}
	
	public boolean checkSlot( int npcID , int slot , ItemStack is ) {
		if ( SellItems.get(npcID).size() > slot )		
			if ( SellItems.get(npcID).get(slot).getAmout() == is.getAmount() &&
				 SellItems.get(npcID).get(slot).getID() == is.getTypeId() &&
				 SellItems.get(npcID).get(slot).getData() == is.getData().getData() )
				return false;
		return true;
	}
	
	public boolean canSell(ItemStack is,int id) {
		if ( SellItems.get(id) != null )
			for ( int i = 0 ; i < SellItems.get(id).size() ; ++i ) {
				if ( SellItems.get(id).get(i).getItemStack().equals(is) )
					return true;
			}
		return false;
	}
	public boolean sold(ItemStack is, int id, DtlEconomy dtlEconomy, Player p) {
		for ( int i = 0 ; i < SellItems.get(id).size() ; ++i ) {
			if ( SellItems.get(id).get(i).getItemStack().equals(is) && dtlEconomy.getBalance(p.getName()) >= SellItems.get(id).get(i).getCost()*SellItems.get(id).get(i).getAmout() ) {
				dtlEconomy.withdrawPlayer(p.getName(), SellItems.get(id).get(i).getCost()*SellItems.get(id).get(i).getAmout());
				return true;
			}
		}
		return false;
	}
	public void setInventory(Inventory inv,int id,Player p,DtlProject dtl) {
		if ( SellItems.get(id) != null )
			for ( int i = 0 ; i < SellItems.get(id).size() ; ++i ) {
	//			if ( dtl.getPermissions().has(p, "dtl.characters.trader."+ id + ".sell." + SellItems.get(id).get(i).getItemStack().getTypeId() ) )
					inv.addItem(SellItems.get(id).get(i).getItemStack());
			}
		if ( BuyItems.get(id) != null )
			if ( !BuyItems.get(id).isEmpty() ) {
				inv.setItem(35,new ItemStack(Material.FIRE, 1));
			}
	}

	public boolean canBuy(ItemStack is, int id) {
		if ( BuyItems.get(id) != null )
			for ( int i = 0 ; i < BuyItems.get(id).size() ; ++i ) {
				if ( BuyItems.get(id).get(i).getItemStack().getType().equals(is.getType()) )
					return true;
			}
		return false;
	}

	public boolean bought(ItemStack is, int id, DtlEconomy economy, Player p) {
		for ( int i = 0 ; i < BuyItems.get(id).size() ; ++i ) {
			if ( BuyItems.get(id).get(i).getItemStack().getType().equals(is.getType()) ) {
				economy.depositPlayer(p.getName(), BuyItems.get(id).get(i).getCost()*is.getAmount());
				return true;
			}
		}
		return false;
	}*/
	

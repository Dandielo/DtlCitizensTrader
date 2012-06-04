package net.dtl.citizenstrader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.DtlProject;
import net.dtl.citizenstrader.traits.StockRoomTrait;
import net.dtl.economy.DtlEconomy;


public class TraderNpc extends Character {

//	private HashMap<Integer,List<TraderItem>> SellItems = new HashMap<Integer,List<TraderItem>>(); 
//	private HashMap<Integer,List<TraderItem>> BuyItems = new HashMap<Integer,List<TraderItem>>(); 
//	private List<TraderItem> SellItems = new ArrayList<TraderItem>(); 
//	private List<TraderItem> BuyItems = new ArrayList<TraderItem>(); 
//	private List<Integer> traderID = new ArrayList<Integer>();
	
	/*public TraderNpc(TraderNpc character) {
		SellItems = character.getSellItems();
		BuyItems = character.getBuyItems();
	}*/

	@SuppressWarnings("unchecked")
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
	public void onRightClick(NPC npc, Player by) {
		
		System.out.println("Customer inventory!");
		by.setMetadata("npc-talking-with", new FixedMetadataValue(CitizensTrader.plugin, npc));
		by.openInventory(npc.getTrait(StockRoomTrait.class).inventoryView(54));
		
	}
	
	@Override
    public void onSet(NPC npc) {
        if( !npc.hasTrait(StockRoomTrait.class) ){
            npc.addTrait( new StockRoomTrait() );
        }
    }
	
	
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
	}
	
}
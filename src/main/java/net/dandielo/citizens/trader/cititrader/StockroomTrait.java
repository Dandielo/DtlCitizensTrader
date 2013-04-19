package net.dandielo.citizens.trader.cititrader;

import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.parts.TraderStockPart;
import net.dandielo.citizens.trader.types.Trader;
import net.dandielo.citizens.trader.types.Trader.TraderStatus;

public class StockroomTrait extends Trait {
	private TraderTrait trait;
	
	public StockroomTrait() {
		super("stockroom");
	}
	
	@Override
	public void onAttach()
	{
		if ( !npc.hasTrait(TraderTrait.class) )
			npc.addTrait(TraderTrait.class);
		trait = npc.getTrait(TraderTrait.class);
	}
	
	@Override
	public void load(DataKey data) throws NPCLoadException 
	{
		TraderStockPart stock = trait.getStock();
		
		//load selling prices
        for (DataKey priceKey : data.getRelative("prices").getIntegerSubKeys())
        {
            ItemStack k = ItemStorage.loadItemStack(priceKey.getRelative("item"));

            double price = priceKey.getDouble("price");
            int stacksize = priceKey.getInt("stack", 1);

            StockItem item = Trader.toStockItem(k);
            if ( item != null )
            {
            	item.setPatternPrice(false);
            	item.setPrice(price);
            	if ( stacksize > 1 )
            	{
            		item.setStackPrice(true);
            		item.setAmount(stacksize);
            	}
    			stock.addItem("sell", item);
            }
            
        }
        
        //load buy prices
        for (DataKey priceKey : data.getRelative("buyprices").getIntegerSubKeys()) 
        {
            ItemStack k = ItemStorage.loadItemStack(priceKey.getRelative("item"));

            // Assume once that if there is an item, that it is real.
            if (k == null) {
                int test = priceKey.getInt("item.id");
                Material mat = Material.getMaterial(test);
                priceKey.setString("item.id", mat.name());
                k = ItemStorage.loadItemStack((priceKey.getRelative("item")));
            }

            double price = priceKey.getDouble("price");
            int stacksize = priceKey.getInt("stack", 1);

            StockItem item = Trader.toStockItem(k);
            if ( item != null )
            {
            	item.setPatternPrice(false);
            	item.setPrice(price);
            	if ( stacksize > 1 )
            	{
            		item.setStackPrice(true);
            		item.setAmount(stacksize);
            	}
    			stock.addItem("buy", item);
            }
        }
	
		for ( DataKey key : data.getRelative("inv").getIntegerSubKeys() )
		{
			ItemStack is = ItemStorage.loadItemStack(key);
			StockItem item = stock.getItem(is, TraderStatus.SELL, false, false);
			if ( item != null )
			{
				item.setAmount(1);
				item.addAmount(2);
				item.addAmount(4);
				item.addAmount(8);
				item.addAmount(16);
				item.addAmount(32);
				item.addAmount(64);
			}
			
			StockItem bitem = stock.getItem(is, TraderStatus.BUY, false, false);
			if ( bitem != null )
			{
				item.setAmount(1);
				item.addAmount(2);
				item.addAmount(4);
				item.addAmount(8);
				item.addAmount(16);
				item.addAmount(32);
				item.addAmount(64);
			}
		}
		npc.removeTrait(StockroomTrait.class);
	}
}

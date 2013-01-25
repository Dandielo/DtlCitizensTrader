package net.dtl.citizens.trader.cititrader;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.parts.TraderStockPart;
import net.dtl.citizens.trader.types.Trader;

public class ShopTrait extends Trait {
	private TraderCharacterTrait trait;

	public ShopTrait() {
		super("shop"); 
	}

	@Override
	public void onAttach()
	{
		if ( !npc.hasTrait(TraderCharacterTrait.class) )
			npc.addTrait(TraderCharacterTrait.class);
		trait = npc.getTrait(TraderCharacterTrait.class);
	}
	
	@Override
	public void load(DataKey data)
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
            	item.setPetternListening(false);
            	item.setRawPrice(price);
            	if ( stacksize > 1 )
            	{
            		item.setRawPrice(price);
            		item.resetAmounts(stacksize);
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
            	item.setPetternListening(false);
            	item.setRawPrice(price);
            	if ( stacksize > 1 )
            	{
            		item.setStackPrice(true);
            		item.resetAmounts(stacksize);
            	}
    			stock.addItem("buy", item);
            }
        }
		npc.removeTrait(ShopTrait.class);
	}
	
}

package net.dtl.citizens.trader.cititrader;

import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import net.dtl.citizens.trader.TraderTrait;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.parts.TraderStockPart;
import net.dtl.citizens.trader.types.Trader.TraderStatus;

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
		
		for ( DataKey key : data.getRelative("inv").getIntegerSubKeys() )
		{
			ItemStack is = ItemStorage.loadItemStack(key);
			StockItem item = stock.getItem(is, TraderStatus.SELL, false, false);
			if ( item != null )
				item.resetAmounts(is.getAmount());
			
			StockItem bitem = stock.getItem(is, TraderStatus.BUY, false, false);
			if ( bitem != null )
				bitem.resetAmounts(is.getAmount());
		}
		npc.removeTrait(StockroomTrait.class);
	}
	
	
}

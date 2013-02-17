package net.dandielo.citizens.trader.limits;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.types.Trader;

public class LimitManager {

	public LimitManager()
	{
		
	}
	
	private Map<String, Limits> players = new HashMap<String, Limits>();
	
	public boolean checkLimit(Trader trader, Player player, StockItem item)
	{
		return false;
	}
	
}

package net.dandielo.citizens.trader.limits;

import java.util.HashMap;
import java.util.Map;

import net.dandielo.citizens.trader.objects.StockItem;

public class Limits {
	private final StockItem item;

	private Map<String, Limit> limits = new HashMap<String, Limit>();

	public Limits(StockItem item)
	{
		this.item = item;
	}
	
	public StockItem getItem()
	{
		return item;
	}
	
	public Limit getLimit(String limit)
	{
		return limits.get(limit);
	}
	
	public void setLimit(String limit, Limit value)
	{
		limits.put(limit, value);
	}
	
	public static class Limit
	{
		private int limit;
		
		private int timeout;
		
		public Limit(int limit, int timeout)
		{
			this.limit = limit;
			this.timeout = timeout;
		}
		
		public int getLimit()
		{
			return limit;
		}
		
		public int getTimeout()
		{
			return timeout;
		}
		
		@Override
		public String toString()
		{
			return String.valueOf(limit) + "/" + String.valueOf(timeout);
		}
	}
	
}
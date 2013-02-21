package net.dandielo.citizens.trader.limits;

import java.util.HashMap;
import java.util.Map;

import net.dandielo.citizens.trader.objects.StockItem;

public class Limits {
	private final StockItem item;
	
	private StockItem linked = null;

	private Map<String, Limit> limits = new HashMap<String, Limit>();

	public void linkWith(StockItem item)
	{
		linked = item;
	}
	
	public StockItem getLinked()
	{
		return linked;
	}
	
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
	
	public String timeout(String limit)
	{
		if ( limits.get(limit) == null )
			return "no timeout";
		
		long sTime = limits.get(limit).getTimeout()/1000;

		String sec = ( sTime % 60 > 0 ? sTime % 60 + "s " : "" );
		sTime /= 60;
		String min = ( sTime % 60 > 0 ? sTime % 60 + "m " : "" );
		sTime /= 60;
		String hou = ( sTime % 24 > 0 ? sTime % 24 + "h " : "" );
		sTime /= 24;
		String day = ( sTime > 0 ? sTime + "d " : "" );

		return day + hou + min + sec;
	}
	
	public String limit(String limit)
	{
		return limits.get(limit) == null ? "no limit" : String.valueOf(limits.get(limit).getLimit());
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
		
		public void changeLimit(int n)
		{
			limit -= n;
			if ( limit < 0 )
				limit = -1;
		}
		
		public void changeTimeout(int n)
		{
			timeout -= n;
			if ( timeout < 0 )
				timeout = -1;
		}
		
		public void setTimeout(int n)
		{
			timeout = n;
		}
		
		public void setLimit(int n)
		{
			limit = n;
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
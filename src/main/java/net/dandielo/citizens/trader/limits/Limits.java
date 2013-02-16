package net.dandielo.citizens.trader.limits;

import java.util.Date;
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
		private Date date; 

		private boolean useTime;
		
		public Limit(int limit, int timeout)
		{
			useTime = false;
			this.limit = limit;
			this.timeout = timeout;
		}
		
		public Limit(int limit, Date date, int days)
		{
			useTime = true;
			this.limit = limit;
			this.date = date;
			this.timeout = days;
		}
		
		public int getLimit()
		{
			return limit;
		}
		
		public int getTimeout()
		{
			return timeout;
		}
		
		public Date getDate()
		{
			return date;
		}
		
		public int getDays()
		{
			return timeout;
		}
		
		public boolean usesTime()
		{
			return useTime;
		}
	}
	
	/*private final StockItem thisItem;
	private StockItem linked;

	//global limit for server traders, or amount saving for player traders
	private Limit limit;
	//a player based limit, a player can only buy a set amount of items until the timeout (atm not supported)
	private Limit playerLimit;

	//players with a ongoing timeout (need to add a gile to save this)
	private HashMap<String,Integer> players;
	
	public Limits(StockItem item) {
		thisItem = item;
		linked = null;
		
		limit = new Limit();
		playerLimit = new Limit();
		players = new HashMap<String,Integer>();
		players.clear();
	}

	public boolean hasLimit() {
		return limit.hasLimit();
	}
	
	public boolean hasPlayerLimit() {
		return playerLimit.hasLimit();
	}
	
	public void linkWith(StockItem item) {
		linked = item;
	}
	
	// TODO redo this function
	public boolean checkLimit(String p, int slot, int scale) {
		if ( limit.timeoutReached(new Date()) )
			limit.reset();
		
		if ( !limit.reachedLimit() ) 
		{
			if ( playerLimit.hasLimit() ) 
			{
				if ( players.containsKey(p) )
				{
					return !playerLimit.reachedLimitWith((thisItem.getAmount(slot)*scale)-1);
				}
				else
				{
					players.put(p, 0);
				}
			}
			return !limit.reachedLimitWith((thisItem.getAmount(slot)*scale)-1);
		}
		return false;
	}
	
	// TODO redo this function
	public boolean checkLimit(String p, int slot) {
		if ( limit.timeoutReached(new Date()) )
			limit.reset();
		
		if ( !limit.reachedLimit() ) 
		{
			if ( playerLimit.hasLimit() )
			{
				if ( players.containsKey(p) )
				{
					return !playerLimit.reachedLimitWith(thisItem.getAmount(slot)-1);
				}
				else
				{
					players.put(p, 0);
				}
			}
			return !limit.reachedLimitWith(thisItem.getAmount(slot)-1);
		}
		return false;
	}
	
	//for buy update
	public boolean updateLimit(int slot, int scale, String p) {
		if ( !limit.reachedLimit() ) 
		{
			if ( playerLimit.hasLimit() ) 
			{
				if ( players.containsKey(p) )
				{
					if ( !playerLimit.reachedLimitAs(players.get(p) + thisItem.getAmount(slot)) )
						return false;
					players.put(p, players.get(p) + thisItem.getAmount(slot));
				} 
				else
				{
					if ( playerLimit.reachedLimitAs(thisItem.getAmount(slot)) )
						return false;
					players.put(p, thisItem.getAmount(slot));
				}
			}

			if ( linked != null )
				linked.getLimitSystem().limit.amount -= thisItem.getAmount(slot)*scale;
			limit.changeAmount(thisItem.getAmount(slot)*scale);
			
			return true;
		}
		return false;
	}
	
	//for sell limit update
	public boolean updateLimit(int slot, String p) {
		if ( !limit.reachedLimit() ) 
		{
			if ( playerLimit.hasLimit() ) 
			{
				if ( players.containsKey(p) )
				{
					if ( !playerLimit.reachedLimitAs(players.get(p) + thisItem.getAmount(slot)) )
						return false;
					players.put(p, players.get(p) + thisItem.getAmount(slot));
				}
				else
				{
					if ( playerLimit.reachedLimitAs(thisItem.getAmount(slot)) )
						return false;
					players.put(p, thisItem.getAmount(slot));
				}
			}
			
			if ( linked != null )
				linked.getLimitSystem().limit.amount -= thisItem.getAmount(slot);
			limit.changeAmount(thisItem.getAmount(slot));
			
			return true;
		}
		return false;
	}
	
	
	public void setItemGlobalLimit(int amount, int limit, long time) {
		this.limit.setLimit(limit);
		this.limit.setAmount(amount);
		this.limit.setTimeout(time);
	}
	public void setItemPlayerLimit(int amount, int limit, long time) {
		this.playerLimit.setLimit(limit);
		this.playerLimit.setAmount(amount);
		this.playerLimit.setTimeout(time);
	}
	
	@Override 
	public String toString() {
		return limit.getAmount() + "/" + limit.getLimit() + "/" + ( limit.getTimeout() / 1000 );// + ( playerLimit.hasLimit() ? "pl" : "" );
	}

	public String playerLimitToString() {
		return playerLimit.getAmount() + "/" + playerLimit.getLimit() + "/" + ( playerLimit.getTimeout() / 1000 );// + ( playerLimit.hasLimit() ? "pl" : "" );
	}
	
	public String getStackAmount()
	{
		return limit.limit < 0 ? "unlimited" : String.valueOf(limit.getAvaiableAmount());
	}
	
	public void setGlobalAmount(int amount) {
		limit.setAmount(amount);
	}
	
	public String getGlobalTimeout() {
		return limit.timeoutString();
	}
	public String getPlayerTimeout() {
		return playerLimit.timeoutString();
	}
	
	public void setGlobalTimeout(long t) {
		limit.setTimeout(t);
	}
	public void changeGlobalTimeout(long t) {
		limit.changeTimeout(t*1000);
	}
	public void changePlayerTimeout(long t) {
		playerLimit.changeTimeout(t*1000);
	}
	
	public int getGlobalAvailable()
	{
		return limit.getLimit() - limit.getAmount();
	}
	public int getGlobalLimit() {
		return limit.getLimit();
	}
	public int getPlayerLimit() {
		return playerLimit.getLimit();
	}
	public void setGlobalLimit(int l) {
		limit.setLimit(l);
	}
	public void changeGlobalLimit(int l) {
		limit.changeLimit(l);
	}
	public void changePlayerLimit(int l) {
		playerLimit.changeLimit(l);
	}
	
	public int getGlobalAmount() {
		return limit.getAmount();
	}
	
	public int getUnusedLimit() {
		return limit.getLimit() - limit.getAmount();
	}

	public class Limit {

		private int limit = -1;
		private int amount;
		
		private Date timer = new Date();
		private long timeout = 0;
		
		public Limit() {
			this(0);
		}
		public Limit(int a) {
			amount = a;
		}
		
		public boolean hasLimit() {
			return limit > -1;
		}
		public int getLimit() {
			return limit;
		}
		public void changeLimit(int l) {
			limit += l;
			if ( limit < 0 )
				limit = -1;
		}
		public void setLimit(int l) {
			limit = l;
		}
		
		public int getAvaiableAmount()
		{
			return limit - amount;
		}
		
		public int getAmount() {
			return amount;
		}
		public void changeAmount(int a) {
			amount += a;
		}
		public void setAmount(int a) {
			amount = a;
		}
		public void resetAmount() {
			amount = 0;
		}
		
		//less than = no limit, 0 = always unavailable, 
		public boolean reachedLimit() {
			if ( limit < 0 )
				return false;
			return amount >= limit;
		}
		
		//less than = no limit, 0 = always unavailable, 
		public boolean reachedLimitWith(int a) {
			if ( limit < 0 )
				return false;
			return amount + a >= limit;
		}
		
		public boolean reachedLimitAs(int a) {
			return a >= limit;
		}
		
		public long getTimeout() {
			return timeout;
		}
		public void setTimeout(long t) {
			timeout = t;
		}
		public void changeTimeout(long t) {
			timeout += t;
			if ( timeout < 0 )
				timeout = 0;
		}
		public void resetTimer() {
			timer = new Date();
		}
		public boolean timeoutReached(Date d) {
			if ( timeout == -2000 )
				return false;
			return ( d.getTime() - timer.getTime() > timeout );
		}
		
		
		public String timeoutString() {			
			long sTime = timeout/1000;

			String sec = ( sTime % 60 > 0 ? sTime % 60 + "s " : "" );
			sTime /= 60;
			String min = ( sTime % 60 > 0 ? sTime % 60 + "m " : "" );
			sTime /= 60;
			String hou = ( sTime % 24 > 0 ? sTime % 24 + "h " : "" );
			sTime /= 24;
			String day = ( sTime > 0 ? sTime + "d " : "" );
			
			return day + hou + min + sec;
		}
		
		

		public void reset() {
			resetAmount();
			resetTimer();
		}
	}*/
	
}
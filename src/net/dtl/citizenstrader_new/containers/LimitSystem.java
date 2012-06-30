package net.dtl.citizenstrader_new.containers;

import java.util.Date;
import java.util.HashMap;

import org.bukkit.entity.Player;

/* *
 * New LimitClass (the old one will be removed after this one is complete)
 * 
 */
public class LimitSystem {
	/* *
	 * Players variable used by the perPlayerLimit system
	 * 
	 */
	private final StockItem thisItem;
	private StockItem linked;

	private Limit limit;
	private Limit playerLimit;

	private HashMap<String,Integer> players;
	
	/* *
	 * Constructor
	 *
	 */
	public LimitSystem(StockItem item) {
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
	
	public boolean checkLimit(String p, int slot) {
		if ( limit.timeoutReached(new Date()) )
			limit.reset();
		
		if ( !limit.reachedLimit() ) {
			if ( playerLimit.hasLimit() ) {
				if ( players.containsKey(p) ) {
					if ( playerLimit.reachedLimitWith(players.get(p)+thisItem.getAmount(slot)) )
						return false;
				} else {
					players.put(p, 0);
				}
			}
			return !limit.reachedLimitWith(thisItem.getAmount(slot)-1);
		}
		return false;
	}
	
	/* *
	 * Lol, why I've coded that?
	 * 
	
	public boolean checkLimitWith(int l, String p) {
		if ( !limit.reachedLimit() ) {
			if ( playerLimit.hasLimit() ) {
				if ( players.containsKey(p) ) {
					if ( playerLimit.reachedLimitAs(players.get(p)-1) )
						return false;
					if (  playerLimit.reachedLimitAs((players.get(p)+l)-1) )
						return false;
				} else {
					if ( playerLimit.reachedLimitAs(l-1) )
						return false;
				}
			}
			if ( limit.reachedLimitWith(l-1) )
				return false;
			
			return true;
		}
		return false;
	}*/
	
	public boolean updateLimit(int slot, String p) {
		if ( !limit.reachedLimit() ) {
			if ( playerLimit.hasLimit() ) {
				if ( players.containsKey(p) ) {
					if ( !playerLimit.reachedLimitAs(players.get(p) + thisItem.getAmount(slot)) )
						return false;
					players.put(p, players.get(p) + thisItem.getAmount(slot));
				} else {
					if ( playerLimit.reachedLimitAs(thisItem.getAmount(slot)) )
						return false;
					players.put(p, players.get(p) + thisItem.getAmount(slot));
				}
			}
			limit.changeAmount(thisItem.getAmount(slot));
			
			return true;
		}
		return false;
	}
	
	
	
	public void setItemLimit(int limit, int amount, long time) {
		this.limit.setLimit(limit);
		this.limit.setAmountt(amount);
		this.limit.setTimeout(time);
	}
	
	@Override
	public String toString() {
		return limit.getAmount() + "/" + limit.getLimit() + "/" + ( limit.getTimeout() / 1000 );// + ( playerLimit.hasLimit() ? "pl" : "" );
	}
	
	
	/* *
	 * global limit
	 */
	
	public String getGlobalTimeout() {
		return limit.timeoutString();
	}
	public String getPlayerTimeout() {
		return playerLimit.timeoutString();
	}
	
	public void changeGlobalTimeout(long t) {
		limit.changeTimeout(t*1000);
	}
	
	public int getGlobalLimit() {
		return limit.getLimit();
	}
	public void changeGlobalLimit(int l) {
		limit.changeLimit(l);
	}
	
	/* *
	 * The Limit information of an item
	 * 
	 */
	public class Limit {
		/* *
		 * Limit Variables
		 */
		private int limit = -1;
		private int amount;
		
		/* *
		 * TimeOut variables
		 */
		private Date timer = new Date();
		private long timeout = 0;
		
		/* *
		 * Constructors
		 * 
		 */
		public Limit() {
			this(0);
		}
		public Limit(int a) {
			amount = a;
		}
		
		/* *
		 * Limit management functions
		 * 
		 */
		public boolean hasLimit() {
			return limit > 0;
		}
		public int getLimit() {
			return limit;
		}
		public void changeLimit(int l) {
			limit += l;
			if ( limit < 0 )
				limit = 0;
		}
		public void setLimit(int l) {
			limit = l;
		}
		
		public int getAmount() {
			return amount;
		}
		public void changeAmount(int a) {
			amount += a;
		}
		public void setAmountt(int a) {
			amount = a;
		}
		public void resetAmount() {
			amount = 0;
		}
		
		public boolean reachedLimit() {
			return amount >= limit;
		}
		public boolean reachedLimitWith(int a) {
			return amount + a >= limit;
		}
		public boolean reachedLimitAs(int a) {
			return a >= limit;
		}
		
		/* *
		 * TimeOut management functions
		 * 
		 */
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
		
		/* *
		 * Limit and amount management
		 *//*
		public void setLimit(int l) {
			limit = l;
		}
		public int getLimit() {
			return limit;
		}
		public void changeTimeout(int t) {
			timeout += t*1000;
			if ( timeout < 0 )
				timeout = 0;
		}
		public void changeLimit(int l) {
			limit += l;
			if ( limit < 0 )
				limit = -1;
		}
		public void setAmount(int a) {
			amount = a;
		}
		public void changeAmount(int a) {
			amount += a;
		}
		public void resetAmount() {
			amount = 0;
		}
		public boolean hasAmount(int a) {
			return ( limit - amount ) >= a ;
		}
		
		public boolean reachedLimit() {
			if ( limit < 1 )
				return false;
			return limit <= amount;
		}
		public boolean hasLimit() {
			if ( limit < 0 )
				return false;
			return true;
		}
		
		public Limit setTimeout(long t) {
			timeout = t;
			return this;
		}
		public Limit resetTimer() {
			timer = new Date();
			return this;
		}
		public Limit checkTimer(Date d) {
			if ( limit < 0 )
				return this;
			if ( d.getTime() - timer.getTime() > timeout ) {
				reset();
			}
			return this;
		}
		public String getNextReset() {
			Date d = new Date();
			d.setTime(new Date().getTime() - timer.getTime());
			return new SimpleDateFormat("HH mm ss").format(d);
		}
		public String getTimeout() {
			Date d = new Date();
			d.setTime(timeout);
			return new SimpleDateFormat("dd HH mm ss").format(d);
		}
		
		public void reset() {
			resetTimer();
			resetAmount();
		}
		
		@Override
		public String toString() {
			return limit + "/" + amount + "/" + ( timeout / 1000 );
		}*/
	}
	
	
	/* *
	 * does the limit applies to the player or to the item
	 * 
	 */
	
	
}
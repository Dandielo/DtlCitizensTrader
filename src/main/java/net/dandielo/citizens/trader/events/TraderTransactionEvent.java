package net.dandielo.citizens.trader.events;

import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.types.Trader;
import net.dandielo.citizens.trader.types.Trader.TraderStatus;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TraderTransactionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
	
	public enum TransactionResult { SUCCESS_BUY, SUCCESS_SELL, FAIL_MONEY, FAIL_SPACE, FAIL_TRADER_MONEY, FAIL_LIMIT, FAIL_ITEM;
	
		public Boolean succeeded()
		{
			switch( this )
			{
			case SUCCESS_BUY:
			case SUCCESS_SELL:
				return true;
			default:
				return false;
			}
		}

		public String stringResult() {
			switch( this )
			{
			case SUCCESS_BUY:
			case SUCCESS_SELL:
				return "SUCCEEDED";
			default:
				return "FALIED";
			}
		}
		
		public String stringResultInfo() {
			switch( this )
			{
			case SUCCESS_BUY:
				return "BUY";
			case SUCCESS_SELL:
				return "SELL";
			case FAIL_MONEY:
				return "MONEY";
			case FAIL_TRADER_MONEY:
				return "MONEY_TRADER";
			case FAIL_LIMIT:
				return "LIMIT";
			case FAIL_SPACE:
				return "SPACE";
			case FAIL_ITEM:
				return "ITEM";
			default: return "UNDEFINED";
			}
		}
	};
	
	//trader variables
	private StockItem item;
	private int amount;
	private int left;
	private double price;
	private Trader trader;
	private NPC npc;
	private TraderStatus status;
	private TransactionResult result;
	private Player player;
	
	public TraderTransactionEvent(Trader trader, NPC npc, HumanEntity humanEntity, TraderStatus status, StockItem item, double price, TransactionResult result)
	{
		this.trader = trader;
		this.npc = npc;
		this.status = status;
		this.item = item;
		this.amount = item.getAmount();
		//TODO Limits
		//this.left = item.getLimitSystem().getGlobalAvailable();
		this.result = result;
		this.player = (Player) humanEntity;
	}
	
	public TraderTransactionEvent(Trader trader, NPC npc, HumanEntity humanEntity, TraderStatus status, StockItem item, double price, int amount, TransactionResult result)
	{
		this.trader = trader;
		this.npc = npc;
		this.status = status;
		this.item = item;
		this.amount = amount;
		//TODO Limits
		//this.left = item.getLimitSystem().getGlobalAvailable();
		this.result = result;
		this.player = (Player) humanEntity;
	}
	
	public Player getParticipant()
	{
		return player;
	}
	
	public Trader getTrader()
	{
		return this.trader;
	}
	
	public double getEndPrice()
	{
		return price;
	}
	
	public NPC getNPC()
	{
		return this.npc;
	}
	
	public TraderStatus getStatus()
	{
		return this.status;
	}
	
	public StockItem getItem()
	{
		return this.item;
	}
	
	public TransactionResult getResult()
	{
		return this.result;
	}
	
	public int getAmount()
	{
		return amount;
	}
	
	public int getLeft()
	{
		return left;
	}
/*	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	} */

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}

}

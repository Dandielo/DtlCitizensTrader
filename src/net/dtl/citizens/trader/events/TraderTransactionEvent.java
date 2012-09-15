package net.dtl.citizens.trader.events;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.objects.StockItem;
import net.dtl.citizens.trader.traders.Trader;
import net.dtl.citizens.trader.traders.Trader.TraderStatus;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TraderTransactionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
	
	public enum TransactionResult { SUCCESS_BUY, SUCCESS_SELL, FAIL_MONEY, FAIL_SPACE, FAIL_TRADER_MONEY, FAIL_LIMIT };
	
	//trader variables
	private StockItem item;
	private Trader trader;
	private NPC npc;
	private TraderStatus status;
	private TransactionResult result;
	private HumanEntity player;
	
	public TraderTransactionEvent(Trader trader, NPC npc, HumanEntity humanEntity, TraderStatus status, StockItem item, TransactionResult result)
	{
		this.trader = trader;
		this.npc = npc;
		this.status = status;
		this.item = item;
		this.result = result;
		this.player = humanEntity;
	}
	
	public HumanEntity getParticipant()
	{
		return player;
	}
	
	public Trader getTrader()
	{
		return this.trader;
	}
	
	public NPC getNpc()
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

package net.dtl.citizens.trader.events;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderTrait.EType;
import net.dtl.citizens.trader.types.Trader;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class TraderCloseEvent implements Cancellable {
	
	private boolean cancelled;
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
	
	public TraderCloseEvent(Player player, Trader trader, NPC npc)
	{
		this.player = player;
		this.trader = trader;
		this.npc = npc;
		type = trader.getType();
	}
	
	private Player player;
	private Trader trader;
	private NPC npc;
	
	private EType type;

	public Player getPlayer()
	{
		return player;
	}
	
	public Trader getTrader()
	{
		return trader;
	}
	
	public NPC getNPC()
	{
		return npc;
	}
	
	public EType getTraderType()
	{
		return type;
	}
}

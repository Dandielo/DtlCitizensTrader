package net.dtl.citizens.trader.events;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.TraderCharacterTrait.EcoNpcType;
import net.dtl.citizens.trader.types.Trader;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TraderOpenEvent extends Event implements Cancellable {
	
	private boolean cancelled;
	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
	
	public TraderOpenEvent(Player player, Trader trader, NPC npc)
	{
		this.player = player;
		this.trader = trader;
		this.npc = npc;
		type = trader.getType();
	}
	
	private Player player;
	private Trader trader;
	private NPC npc;
	
	private EcoNpcType type;

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
	
	public EcoNpcType getTraderType()
	{
		return type;
	}
}

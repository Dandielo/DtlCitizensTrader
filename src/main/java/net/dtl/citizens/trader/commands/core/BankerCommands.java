package net.dtl.citizens.trader.commands.core;

import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.TraderTrait;
import net.dtl.citizens.trader.TraderTrait.EType;
import net.dtl.citizens.trader.commands.Command;
import net.dtl.citizens.trader.locale.LocaleManager;
import net.dtl.citizens.trader.types.Banker;

public class BankerCommands {
	private static LocaleManager locale = CitizensTrader.getLocaleManager();
	
	@Command(
	name = "banker",
	syntax = "create {array}",
	perm = "dtl.banker.commands.create",
	npc = false)
	public void traderCreate(CitizensTrader plugin, Player sender, Banker trader, Map<String, String> args)
	{
		String name = args.get("free");
		
		EType type = EType.fromName(args.get("t") == null ? "server" : args.get("t"));
		EntityType entity = EntityType.fromName(args.get("e") == null ? "player" : args.get("e"));
		
		if ( name == null )
		{
			locale.sendMessage(sender, "error-argument-missing");
			return;
		}
		
		if ( type == null )
			type = EType.PRIVATE_BANKER;
		if ( entity == null )
			entity = EntityType.PLAYER;
		
		// Create and spawn the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(entity, name);
		npc.addTrait(TraderTrait.class);
		npc.spawn(sender.getLocation());
		npc.addTrait(MobType.class);
		npc.getTrait(MobType.class).setType(entity);
		
		// Add basic settings
		npc.getTrait(TraderTrait.class).setType(type);
		npc.getTrait(TraderTrait.class).implementBanker();
		
		locale.sendMessage(sender, "banker-created", "player", sender.getName(), "banker", name);
	}
}

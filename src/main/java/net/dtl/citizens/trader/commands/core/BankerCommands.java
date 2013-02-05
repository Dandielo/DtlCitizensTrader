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
import net.dtl.citizens.trader.objects.Wallet.WalletType;
import net.dtl.citizens.trader.parts.BankerPart;
import net.dtl.citizens.trader.parts.TraderConfigPart;
import net.dtl.citizens.trader.types.Banker;
import net.dtl.citizens.trader.types.Trader;

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
	//	String owner = args.get("o");
		
		EType type = EType.fromName(args.get("t") == null ? "server" : args.get("t"));
	//	WalletType wallet = WalletType.getTypeByName(args.get("w") == null ? "npc" : args.get("w"));
		EntityType entity = EntityType.fromName(args.get("e") == null ? "player" : args.get("e"));
		
		if ( name == null )
		{
			locale.sendMessage(sender, "error-argument-missing");
			return;
		}
		
	//	if ( wallet == null )
	//		wallet = WalletType.NPC;
		if ( type == null )
			type = EType.SERVER_TRADER;
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

		BankerPart settings = npc.getTrait(TraderTrait.class).getBankTrait();
	//	settings.setOwner(owner == null ? "no owner" : owner);
	//	settings.getWallet().setType(wallet);
		
		locale.sendMessage(sender, "banker-created", "player", sender.getName(), "trader", name);
	}
}

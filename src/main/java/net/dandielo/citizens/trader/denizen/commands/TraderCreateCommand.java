package net.dandielo.citizens.trader.denizen.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.dandielo.citizens.trader.DtlTraders;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.TraderTrait.EType;
import net.dandielo.citizens.trader.denizen.AbstractDenizenCommand;
import net.dandielo.citizens.trader.objects.Wallet.WalletType;

public class TraderCreateCommand extends AbstractDenizenCommand {

	/**
	 * <p>Sends a block animation sequence using .schematic files and an 'ANIMATION script'</p>
	 * 
	 * 
	 * <br><b>dScript Usage:</b><br>
	 * <pre>TRADERCREATE 	({SERVER}|MARKET|PLAYER) [NAME:trader_name]
	 * 						(WALLET:SERVER|{NPC}|OWNER) (PATTERN:pattern_name)
	 * 						(LOC:location) (OWNER:owner) (ENTITY:entity)</pre>
	 * 
	 * <ol><tt>Arguments: [] - Required, () - Optional, {} - Default</ol></tt>
	 * 
	 * @author Dandielo
	 *
	 */
	
	public TraderCreateCommand() {
		this.activate().as("TRADERCREATE").withOptions("({SERVER}|MARKET|PLAYER) [NAME:trader_name] (WALLET:SERVER|{NPC}|OWNER) (PATTERN:pattern_name) (LOC:location) (OWNER:owner) (ENTITY:entity)", 1);
		DtlTraders.info("Registered denizen " + ChatColor.YELLOW + TraderCreateCommand.class.getSimpleName());
	}
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		// Initialize fields used
		EType type = EType.SERVER_TRADER;
		EntityType entity = EntityType.PLAYER;
		String name = null;
		String pattern = DtlTraders.getInstance().getConfig().getString("trader.patterns.default", "");
		String owner = "no owner";
		Location location = scriptEntry.getPlayer().getLocation();
		WalletType wallet = WalletType.NPC;

		// Iterate through arguments
		for (String arg : scriptEntry.getArguments()) {
			// matchesScript will ensure there is an actual script with this name loaded
			if (aH.matchesLocation(arg)) {
				// All script names for denizen are upper-case to avoid case sensitivity
				location = aH.getLocationFrom(arg);
				dB.echoDebug("...set LOCATION: '%s'", location.toString());
				continue;
				// mathesDuration will make sure the argument is a positive integer
			} else if (arg.startsWith("NAME:")) {
				name = aH.getStringFrom(arg.substring(5));
				dB.echoDebug("...set NAME: '%s'", name);
				continue;
				// matches the same values as the AnimationAction enum
			} else if (arg.startsWith("PATTERN:")) {
				pattern = aH.getStringFrom(arg.substring(8));
				dB.echoDebug("...set PATTERN: '%s'", pattern);
				continue;
				// matches the same values as the AnimationAction enum
			} else if (arg.startsWith("WALLET:")) {
				wallet = WalletType.getTypeByName(aH.getStringFrom(arg.substring(7)).toLowerCase());
				dB.echoDebug("...set WALLET: '%s'", wallet.toString());
				continue;
				// matches the same values as the AnimationAction enum
			} else if (arg.startsWith("ENTITY:")) {
				entity = EntityType.fromName(aH.getStringFrom(arg.substring(7)).toLowerCase());
				dB.echoDebug("...set ENTITY: '%s'", entity.getName());
				continue;
				// matches the same values as the AnimationAction enum
			} else if (aH.matchesArg("SERVER, PLAYER, MARKET", arg)) {
				type = EType.fromName(aH.getStringFrom(arg).toLowerCase());
				dB.echoDebug("...set TraderType: '%s'", type.toString());
				continue;
				// Unknown argument should be caught to avoid unwanted behavior.
			} else if (arg.startsWith("OWNER:")) {
				owner = aH.getStringFrom(arg).toUpperCase();
				dB.echoDebug("...set OWNER: '%s'", owner);
				continue;
				// Unknown argument should be caught to avoid unwanted behavior.
			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);

		}

		// Check for null fields from 'required' arguments
		if ( name == null ) 
			throw new InvalidArgumentsException("Must specify a valid 'Animation SCRIPT'.");

		// Stash objects in scriptEntry for use in execute()
		scriptEntry.addObject("type", type);
		scriptEntry.addObject("name", name);
		scriptEntry.addObject("pattern", pattern);
		scriptEntry.addObject("owner", owner);
		scriptEntry.addObject("location", location);
		scriptEntry.addObject("wallet", wallet);
		scriptEntry.addObject("entity", entity);
	}
	
	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		
		EntityType entity = (EntityType) scriptEntry.getObject("entity");
		//creating the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(entity, (String) scriptEntry.getObject("name"));
		npc.addTrait(TraderTrait.class);
		npc.addTrait(MobType.class);
		npc.getTrait(MobType.class).setType(entity);
		npc.spawn((Location) scriptEntry.getObject("location"));
		npc.getTrait(Owner.class).setOwner((String) scriptEntry.getObject("owner"));
		
		TraderTrait trait = npc.getTrait(TraderTrait.class);
		trait.implementTrader();
		trait.setType((EType) scriptEntry.getObject("type"));
		trait.getStock().addPattern((String) scriptEntry.getObject("pattern"), 0);
		trait.getConfig().getWallet().setType((WalletType) scriptEntry.getObject("wallet"));
		trait.getConfig().setOwner((String) scriptEntry.getObject("owner"));
	}

}
 
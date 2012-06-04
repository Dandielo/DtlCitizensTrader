package net.dtl.trader;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.citizensnpcs.api.ai.AI;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.trait.Trait;

public class Npc extends AbstractNPC {

	protected Npc(int id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void chat(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void chat(Player arg0, String arg1) {
		arg0.sendMessage("msg");
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean despawn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AI getAI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LivingEntity getBukkitEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSpawned() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean spawn(Location arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Inventory getInventory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Trait getTraitFor(Class<? extends Trait> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

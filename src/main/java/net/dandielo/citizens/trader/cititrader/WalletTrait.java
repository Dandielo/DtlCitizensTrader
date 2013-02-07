package net.dandielo.citizens.trader.cititrader;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.objects.Wallet;
import net.dandielo.citizens.trader.parts.TraderConfigPart;

public class WalletTrait extends Trait {
	private TraderTrait trait;
	
	public WalletTrait() {
		super("wallet");
	}
	
	WalletType type = WalletType.PRIVATE;
    double amount = 0;
    String account = "";
    
	public enum WalletType {

        PRIVATE,
        OWNER,
        BANK,
        ADMIN,
        TOWN_BANK;
    }

	@Override
	public void onAttach()
	{
		if ( !npc.hasTrait(TraderTrait.class) )
			npc.addTrait(TraderTrait.class);
		trait = npc.getTrait(TraderTrait.class);
	}
	
	@Override
    public void load(DataKey key) throws NPCLoadException {
        type = WalletType.valueOf(key.getString("type"));
        amount = key.getDouble("amount");
        account = key.getString("account");

        TraderConfigPart config = trait.getConfig();
        Wallet wallet = config.getWallet();
        
        switch(type)
        {
        case PRIVATE:
        	wallet.setType(Wallet.WalletType.NPC);
        	wallet.setMoney(amount);
        	return;
        case OWNER:
            wallet.setType(Wallet.WalletType.OWNER);
            config.setOwner(npc.getTrait(Owner.class).getOwner());
            return;
        case BANK:
        	wallet.setType(Wallet.WalletType.BANK);
        	wallet.setBank(npc.getTrait(Owner.class).getOwner(), account);
        	return;
        case ADMIN:
        	wallet.setType(Wallet.WalletType.INFINITE);
        	return;
        case TOWN_BANK:
        	wallet.setType(Wallet.WalletType.NPC);
        	wallet.setMoney(0.0);
        	return;
        }
		npc.removeTrait(WalletTrait.class);
    }
}

package net.dtl.citizens.trader;


import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.parts.BankerPart;
import net.dtl.citizens.trader.parts.TraderConfigPart;
import net.dtl.citizens.trader.parts.TraderStockPart;

public class TraderTrait extends Trait {
	private EType type = EType.SERVER_TRADER;
	private String defPattern;
	
	private TraderConfigPart config;
	private TraderStockPart stock;
	private BankerPart banker;

	public TraderTrait() {
		super("trader");
	}
	
	@Override
	public void onSpawn() {
		CitizensTrader.getNpcEcoManager().addEconomyNpc(npc);
	}
	
	@Override
	public void onAttach()
	{
		type = EType.SERVER_TRADER;
		implementTrader();
		
		if ( CitizensTrader.dtlWalletsEnabled() )
			config.loadDtlWallet(npc);
		
		CitizensTrader.getNpcEcoManager().addEconomyNpc(npc);
		
		defPattern = CitizensTrader.getInstance().getConfig().getString("trader.patterns.default","");
	}
	
	public TraderStockPart getStock() {
		return stock;
	}
	public TraderConfigPart getConfig() {
		return config;
	}
	
	public void implementTrader()
	{
		config = new TraderConfigPart();
		stock = new TraderStockPart("Stock");
	}
	public void implementBanker()
	{
		banker = new BankerPart();
	}
	
	//The EcoNpc's type
	public EType getType()
	{
		return type;
	}
	public void setType(EType type)
	{
		this.type = type;
	}
	
	@Override
	public void load(DataKey data) throws NPCLoadException {
		String type = data.getString("type", "trader");
		
		if ( type.equals("trader") )
		{
			this.type = EType.fromName( data.getString("trader") );
			
			if ( config == null )
			{
				config = new TraderConfigPart();
				stock = new TraderStockPart(getNPC().getFullName() + "'s stock");
			}
			
			config.load(data);
			stock.load(data);
			
			if ( CitizensTrader.dtlWalletsEnabled() )
				config.loadDtlWallet(npc);
			
			if ( this.type.equals(EType.SERVER_TRADER) && !defPattern.isEmpty() )
				stock.setPattern(defPattern);
			
			if ( this.type.equals(EType.MARKET_TRADER) )
				stock.linkItems();
		}
		else
		if ( type.equals("banker") )
		{
			this.type = EType.fromName( data.getString("banker") );
			
			if ( banker == null )
				banker = new BankerPart();
			

			if ( CitizensTrader.dtlWalletsEnabled() )
				banker.loadDtlWallet(npc);
			
			banker.load(data);
		}
		//old version loading
		else
		{
			this.type = EType.fromName( data.getString("trader-type", data.getString("type")) );
			
			if ( config == null )
			{
				config = new TraderConfigPart();
				stock = new TraderStockPart(getNPC().getFullName() + "'s stock");
			}
			
			config.load(data);
			stock.load(data);
			
			if ( this.type.equals(EType.MARKET_TRADER) )
				stock.linkItems();
		}
	}

	@Override
	public void save(DataKey data) 
	{
		if ( type.isBanker() )
		{
			data.setString("type", "banker");
			data.setString("banker", type.toString());

			banker.save(data);
		}
		else if ( type.isTrader() )
		{
			data.setString("type", "trader");
			data.setString("trader", type.toString());
			
			if ( config != null )
				config.save(data);
			if ( stock != null )
				stock.save(data);
		}
	}
	
	
	public enum EType {
		PLAYER_TRADER, SERVER_TRADER, MARKET_TRADER, PRIVATE_BANKER, MONEY_BANKER;
		
		public boolean isTrader()
		{
			if ( this.equals(PLAYER_TRADER) 
					|| this.equals(SERVER_TRADER) 
					|| this.equals(MARKET_TRADER) )
				return true;
			return false;
		}
		public boolean isBanker()
		{
			if ( this.equals(PRIVATE_BANKER) 
					|| this.equals(MONEY_BANKER) )
				return true;
			return false;
		}
		
		public static EType fromName(String n) {
			if ( n.equalsIgnoreCase("server") || n.equalsIgnoreCase("s") ) 
				return EType.SERVER_TRADER;
			else if ( n.equalsIgnoreCase("player") || n.equalsIgnoreCase("p") )
				return EType.PLAYER_TRADER;
			else if ( n.equalsIgnoreCase("market") || n.equalsIgnoreCase("m") )
				return EType.MARKET_TRADER;
			else if ( n.equalsIgnoreCase("private") )
				return EType.PRIVATE_BANKER;
			else if ( n.equalsIgnoreCase("money") )
				return EType.MONEY_BANKER;
			return null;
		}
		
		@Override
		public String toString() {
			switch( this ) {
			case PLAYER_TRADER:
				return "player";
			case SERVER_TRADER:
				return "server";
			case MARKET_TRADER:
				return "market";
			case PRIVATE_BANKER:
				return "private";
			case MONEY_BANKER:
				return "money";
			default:
				break;
			}
			return "";
		}
	}

	//TODO CHANGE THIS!
	public BankerPart getBankTrait() {
		return banker;
	}
	
	
}

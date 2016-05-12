package tera.gameserver.model.inventory;

import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.Config;
import tera.gameserver.model.Guild;
import tera.gameserver.model.items.ItemLocation;

/**
 * Модель личного банка игрока.
 *
 * @author Ronn
 *
 */
public class GuildBank extends AbstractBank<Guild>
{
	private static final FoldablePool<GuildBank> pool = Pools.newConcurrentFoldablePool(GuildBank.class);

	public static GuildBank newInstance(Guild owner)
	{
		GuildBank bank = pool.take();

		if(bank == null)
			bank = new GuildBank();

		bank.owner = owner;

		return bank;
	}

	@Override
	public void fold()
	{
		pool.put(this);
	}

	@Override
	public ItemLocation getLocation()
	{
		return ItemLocation.GUILD_BANK;
	}

	@Override
	public int getMaxSize()
	{
		return Config.WORLD_GUILD_BANK_MAX_SIZE;
	}

	@Override
	protected int getOwnerId()
	{
		return owner.getObjectId();
	}
}

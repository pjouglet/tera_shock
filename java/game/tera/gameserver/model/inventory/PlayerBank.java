package tera.gameserver.model.inventory;

import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.Config;
import tera.gameserver.model.Account;
import tera.gameserver.model.items.ItemLocation;
import tera.gameserver.model.playable.Player;

/**
 * Модель личного банка игрока.
 *
 * @author Ronn
 *
 */
public class PlayerBank extends AbstractBank<Player>
{
	private static final FoldablePool<PlayerBank> pool = Pools.newConcurrentFoldablePool(PlayerBank.class);

	public static PlayerBank newInstance(Player owner)
	{
		PlayerBank bank = pool.take();

		if(bank == null)
			bank = new PlayerBank();

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
		return ItemLocation.BANK;
	}

	@Override
	public int getMaxSize()
	{
		return Config.WORLD_BANK_MAX_SIZE;
	}

	@Override
	protected int getOwnerId()
	{
		// получаем владельца
		Player owner = getOwner();

		// если егонет, возвращаем ноль
		if(owner == null)
		{
			log.warning(this, "not found owner.");
			return 0;
		}

		// получаем аккаунт владельца
		Account account = owner.getAccount();

		// если его нет, возвращаем ноль
		if(account == null)
		{
			log.warning(this, "not found accaunt.");
			return 0;
		}

		// возвращаем ид банка аккаунта
		return account.getBankId();
	}
}

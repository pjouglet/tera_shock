package tera.gameserver.manager;

import rlib.util.SafeTask;
import rlib.util.array.Array;

import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;

/**
 * Менеджер по авто сохранению данных игроков.
 *
 * @author Ronn
 */
public final class AutoSaveManager extends SafeTask
{
	private static AutoSaveManager instance;

	public static AutoSaveManager getInstance()
	{
		if(instance == null)
			instance = new AutoSaveManager();

		return instance;
	}

	private AutoSaveManager()
	{
		ExecutorManager executor = ExecutorManager.getInstance();

		executor.scheduleGeneralAtFixedRate(this, 900000, 900000);
	}

	@Override
	protected void runImpl()
	{
		// получаем список всех онлаин игроков
		Array<Player> players = World.getPlayers();

		players.readLock();
		try
		{
			// получаем их массив
			Player[] array = players.array();

			// перебираем и сохраняем
			for(int i = 0, length = players.size(); i < length; i++)
				array[i].store(false);
		}
		finally
		{
			players.readUnlock();
		}
	}
}

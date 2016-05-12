package tera.gameserver.events;

import rlib.util.Strings;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.playable.Player;

/**
 * Модель команды на ивенте.
 *
 * @author Ronn
 */
public class EventTeam implements Foldable
{
	private static final FoldablePool<EventTeam> pool = Pools.newConcurrentFoldablePool(EventTeam.class);

	public static EventTeam newInstance()
	{
		EventTeam team = pool.take();

		if(team == null)
			team = new EventTeam();

		return team;
	}

	/** список игроков в команде */
	private final Array<EventPlayer> players;

	/** название команды */
	private String name;

	/** уровень команды */
	private int level;

	private EventTeam()
	{
		this.name = Strings.EMPTY;
		this.players = Arrays.toConcurrentArray(EventPlayer.class);
	}

	/**
	 * @param eventPlayer добавляемый игрок.
	 */
	public final void addPlayer(EventPlayer eventPlayer)
	{
		players.add(eventPlayer);

		if(players.isEmpty())
			setName(Strings.EMPTY);

		players.readLock();
		try
		{
			EventPlayer[] array = players.array();

			StringBuilder builder = new StringBuilder("{");

			for(int i = 0, length = players.size(); i < length; i++)
			{
				Player player = array[i].getPlayer();

				builder.append(player.getName());

				if(i != length - 1)
					builder.append('-');
			}

			builder.append('}');

			setName(builder.toString());
		}
		finally
		{
			players.readUnlock();
		}
	}

	/**
	 * понижение уровня команды.
	 */
	public void decreaseLevel()
	{
		level--;
	}

	@Override
	public void finalyze()
	{
		if(!players.isEmpty())
		{
			EventPlayer[] array = players.array();

			for(int i = 0, length = players.size(); i < length; i++)
				array[i].fold();

			players.clear();
		}
	}

	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return уровень команды.
	 */
	public int getLevel()
	{
		return level;
	}

	/**
	 * @return название команды.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return список игроков в команде.
	 */
	public final EventPlayer[] getPlayers()
	{
		return players.array();
	}

	/**
	 * Увеличение уровня команды.
	 */
	public void increaseLevel()
	{
		level++;
	}

	/**
	 * @return мертва ли вся команда.
	 */
	public final boolean isDead()
	{
		if(players.isEmpty())
			return true;

		players.readLock();
		try
		{
			EventPlayer[] array = players.array();

			for(int i = 0, length = players.size(); i < length; i++)
			{
				Player player = array[i].getPlayer();

				if(!player.isDead())
					return false;
			}

			return true;
		}
		finally
		{
			players.readUnlock();
		}
	}

	@Override
	public void reinit()
	{
		level = 0;
	}

	public void removePlayer(EventPlayer player)
	{
		players.fastRemove(player);
	}

	/**
	 * @param name название команды.
	 */
	public final void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return кол-во игроков в команде.
	 */
	public final int size()
	{
		return players.size();
	}
}

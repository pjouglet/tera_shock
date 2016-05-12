package tera.gameserver.model;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.npc.Minion;
import tera.gameserver.model.npc.MinionLeader;
import tera.gameserver.model.npc.spawn.MinionSpawn;
import tera.gameserver.model.npc.spawn.Spawn;

/**
 * Модель данных о минионах.
 *
 * @author Ronn
 * @created 14.03.2012
 */
public final class MinionData
{
	/** набор спавнов минионов */
	public MinionSpawn[] minions;

	/** ид лидера нпс */
	private int leaderId;
	/** тип нпс */
	private int type;
	/** время респавна всех минионов */
	private int respawnDelay;
	/** итоговое кол-во минионов */
	private int total;

	/**
	 * @param spawns спавны минионов.
	 * @param leaderId ид лидера.
	 * @param type тип нпс.
	 * @param respawnDelay время респавна мининонв.
	 */
	public MinionData(MinionSpawn[] minions, int leaderId, int type, int respawnDelay)
	{
		this.minions = minions;
		this.leaderId = leaderId;
		this.type = type;
		this.respawnDelay = respawnDelay;

		for(MinionSpawn info : minions)
			this.total += info.getCount();
	}

	/**
	 * Принадлежит к этой дате минион.
	 *
	 * @param spawn спавн миниона.
	 * @param list список минионов.
	 * @return принадлежит ли.
	 */
	public boolean containsMinion(Spawn spawn, Array<Minion> list)
	{
		return Arrays.contains(minions, spawn) && list.size() < total;
	}

	/**
	 * @return ид лидера.
	 */
	public final int getLeaderId()
	{
		return leaderId;
	}

	/**
	 * @return список спавнов минионов.
	 */
	public MinionSpawn[] getMinions()
	{
		return minions;
	}

	/**
	 * @return время респавна минионов.
	 */
	public final int getRespawnDelay()
	{
		return respawnDelay;
	}

	/**
	 * @return тип нпс.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param respawnDelay время респавна минионов.
	 */
	public final void setRespawnDelay(int respawnDelay)
	{
		this.respawnDelay = respawnDelay;
	}

	/**
	 * @return всего кол-во минионов.
	 */
	public final int size()
	{
		int counter = 0;

		// получаем массив спавнов минионов
		MinionSpawn[] minions = getMinions();

		for(int i = 0, length = minions.length; i < length; i++)
			counter += minions[i].getCount();

		return counter;
	}

	/**
	 * Стартовый спавн минионов.
	 *
	 * @param leader лидер минионов.
	 * @return список отспавненых минионов.
	 */
	public Array<Minion> spawnMinions(MinionLeader leader)
	{
		Array<Minion> array = Arrays.toConcurrentArray(Minion.class, size());

		return spawnMinions(leader, array);
	}

	/**
	 * Спавн минионов.
	 *
	 * @param leader лидер минионов.
	 * @param array список минионов.
	 * @return список отспавненых минионов.
	 */
	public Array<Minion> spawnMinions(MinionLeader leader, Array<Minion> array)
	{
		// получаем массив спавнов минионов
		MinionSpawn[] minions = getMinions();

		for(int i = 0, length = minions.length; i < length; i++)
			minions[i].start(leader, array);

		return array;
	}

	@Override
	public String toString()
	{
		return "MinionData leaderId = " + leaderId + ", respawnDelay = " + respawnDelay;
	}
}

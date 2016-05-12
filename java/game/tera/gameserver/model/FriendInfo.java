package tera.gameserver.model;

import rlib.util.pools.Foldable;

/**
 * Модель контейнера информации об друге игрока.
 *
 * @author Ronn
 */
public final class FriendInfo implements Foldable
{
	/** имя друга */
	private String name;

	/** уникальный ид игрока */
	private int objectId;
	/** уровень игрока */
	private int level;
	/** раса игрока */
	private int raceId;
	/** класс игрока */
	private int classId;

	@Override
	public void finalyze()
	{
		name = null;
	}

	/**
	 * @return класс игрока.
	 */
	public int getClassId()
	{
		return classId;
	}

	/**
	 * @return уровень игрока.
	 */
	public int getLevel()
	{
		return level;
	}

	/**
	 * @return name имя игрока.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return objectId уникальный ид игрока.
	 */
	public final int getObjectId()
	{
		return objectId;
	}

	/**
	 * @return раса игрока.
	 */
	public int getRaceId()
	{
		return raceId;
	}

	@Override
	public void reinit(){}

	/**
	 * @param classId класс игрока.
	 */
	public void setClassId(int classId)
	{
		this.classId = classId;
	}

	/**
	 * @param level уровень игрока.
	 */
	public void setLevel(int level)
	{
		this.level = level;
	}

	/**
	 * @param name имя игрока.
	 */
	public final void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param objectId уникальный ид игрока.
	 */
	public final void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * @param raceId раса игрока.
	 */
	public void setRaceId(int raceId)
	{
		this.raceId = raceId;
	}
}

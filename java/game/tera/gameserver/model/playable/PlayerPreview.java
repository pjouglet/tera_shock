package tera.gameserver.model.playable;

import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.equipment.Equipment;

/**
 * Модель предосмотра игрока.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public final class PlayerPreview implements Comparable<PlayerPreview>, Foldable
{
	private static final FoldablePool<PlayerPreview> pool = Pools.newConcurrentFoldablePool(PlayerPreview.class);

	/**
	 * Новый экземпляр превьюшки игрока.
	 *
	 * @param objectId уник ид игрока.
	 * @return новый экземпляр.
	 */
	public static PlayerPreview newInstance(int objectId)
	{
		PlayerPreview preview = pool.take();

		if(preview == null)
			preview = new PlayerPreview();

		preview.objectId = objectId;

		return preview;
	}

	/** ид игрока */
	private int objectId;

	/** пол */
	private int sex;
	/** ид расы */
	private int raceId;
	/** ид класса */
	private int classId;
	/** уровень */
	private int level;

	/** время онлайна */
	private long onlineTime;

	/** имя */
	private String name;

	/** внешность */
	private PlayerAppearance appearance;

	/** экиперовка */
	private Equipment equipment;

	@Override
	public int compareTo(PlayerPreview playerPreview)
	{
		if(playerPreview == null || onlineTime < playerPreview.getOnlineTime())
			return -1;

		if(onlineTime == playerPreview.getOnlineTime())
			return 0;

		return 1;
	}

	@Override
	public void finalyze()
	{
		if(equipment != null)
			equipment.fold();

		equipment = null;

		if(appearance != null)
			appearance.fold();

		appearance = null;
		name = null;
	}

	/**
	 * Сложить в пул.
	 */
	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return ид класса игрока.
	 */
	public int getClassId()
	{
		return classId;
	}

	/**
	 * @return экиперовка игрока.
	 */
	public Equipment getEquipment()
	{
		return equipment;
	}

	/**
	 * @return внешность игрока.
	 */
	public PlayerAppearance getAppearance()
	{
		return appearance;
	}

	/**
	 * @return уровень игрока.
	 */
	public int getLevel()
	{
		return level;
	}

	/**
	 * @return имя игрока.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return уникальный ид игрока.
	 */
	public int getObjectId()
	{
		return objectId;
	}

	/**
	 * @return онлаин игрока.
	 */
	public long getOnlineTime()
	{
		return onlineTime;
	}

	/**
	 * @return ид расы игрока.
	 */
	public int getRaceId()
	{
		return raceId;
	}

	/**
	 * @return пол игрока.
	 */
	public int getSex()
	{
		return sex;
	}

	@Override
	public void reinit(){}

	/**
	 * @param classId ид класса игрока.
	 */
	public void setClassId(int classId)
	{
		this.classId = classId;
	}

	/**
	 * @param equipment экиперовка игрока.
	 * @return this.
	 */
	public PlayerPreview setEquipment(Equipment equipment)
	{
		this.equipment = equipment;

		return this;
	}

	/**
	 * @param appearance внешность игрока.
	 * @return this.
	 */
	public PlayerPreview setAppearance(PlayerAppearance appearance)
	{
		this.appearance = appearance;

		return this;
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
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param objectId уникальный ид игрока.
	 */
	public void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * @param onlineTime онлаин игрока.
	 */
	public void setOnlineTime(long onlineTime)
	{
		this.onlineTime = onlineTime;
	}

	/**
	 * @param raceId ид расы игрока.
	 */
	public void setRaceId(byte raceId)
	{
		this.raceId = raceId;
	}

	/**
	 * @param sex пол игрока.
	 */
	public void setSex(int sex)
	{
		this.sex = sex;
	}

	@Override
	public String toString()
	{
		return "PlayerPreview objectId = " + objectId + ", level = " + level + ", name = " + name;
	}
}
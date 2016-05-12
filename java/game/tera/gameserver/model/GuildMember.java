package tera.gameserver.model;

import rlib.util.Strings;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.playable.Player;

/**
 * Модель члена клана в Тера.
 *
 * @author Ronn
 */
public final class GuildMember implements Foldable
{
	private static final FoldablePool<GuildMember> pool = Pools.newConcurrentFoldablePool(GuildMember.class);

	/**
	 * @return новый клан мембер.
	 */
	public static final GuildMember newInstance()
	{
		GuildMember member = pool.take();

		if(member == null)
			return new GuildMember();

		return member;
	}

	/** ник нейм мембера */
	private String name;
	/** заметка для гильдии о игроке */
	private String note;

	/** обджект ид мембера */
	private int objectId;
	/** уровень мембера */
	private int level;
	/** пол мембера */
	private int sex;
	/** раса мембера */
	private int raceId;
	/** класс мембера */
	private int classId;
	/** ид зоны, в которой находится игрок */
	private int zoneId;
	/** время последнего входа в игру */
	private int lastOnline;

	/** ранг мембера */
	private GuildRank rank;

	/** онлаин ли он сейчас */
	private boolean online;

	public GuildMember()
	{
		this.note = Strings.EMPTY;
	}

	@Override
	public boolean equals(Object object)
	{
		if(object instanceof Player)
		{
			Player player = (Player) object;

			return objectId == player.getObjectId();
		}

		return super.equals(object);
	}

	@Override
	public void finalyze()
	{
		rank = null;
	}

	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return класс мембера.
	 */
	public final int getClassId()
	{
		return classId;
	}

	/**
	 * @return lastOnline
	 */
	public final int getLastOnline()
	{
		return lastOnline;
	}

	/**
	 * @return ранг мембера.
	 */
	public final int getLawId()
	{
		return rank.getLawId();
	}

	/**
	 * @return уровень мембера.
	 */
	public final int getLevel()
	{
		return level;
	}

	/**
	 * @return имя мембера.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return пометка об игроке.
	 */
	public final String getNote()
	{
		return note;
	}

	/**
	 * @return уникальный ид мембера.
	 */
	public final int getObjectId()
	{
		return objectId;
	}

	/**
	 * @return раса мембера.
	 */
	public final int getRaceId()
	{
		return raceId;
	}

	/**
	 * @return ранг мембера.
	 */
	public final GuildRank getRank()
	{
		return rank;
	}

	/**
	 * @return ранг мембера.
	 */
	public final int getRankId()
	{
		return rank == null? 0 : rank.getIndex();
	}

	/**
	 * @return пол мембера.
	 */
	public final int getSex()
	{
		return sex;
	}

	/**
	 * @return the zoneId
	 */
	public final int getZoneId()
	{
		return zoneId;
	}

	/**
	 * @return онлаин ли мембер.
	 */
	public final boolean isOnline()
	{
		return online;
	}

	@Override
	public void reinit(){}

	/**
	 * @param classId класс мембера.
	 */
	public final void setClassId(int classId)
	{
		this.classId = classId;
	}

	/**
	 * @param lastOnline задаваемое lastOnline
	 */
	public final void setLastOnline(int lastOnline)
	{
		this.lastOnline = lastOnline;
	}

	/**
	 * @param level уровень мембера.
	 */
	public final void setLevel(int level)
	{
		this.level = level;
	}

	/**
	 * @param name имя мембера.
	 */
	public final void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param note пометка об игроке.
	 */
	public final void setNote(String note)
	{
		this.note = note;
	}

	/**
	 * @param objectId уникальный ид мембера.
	 */
	public final void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * @param online онлаин ли мембер.
	 */
	public final void setOnline(boolean online)
	{
		this.online = online;
	}

	/**
	 * @param raceId раса мембера.
	 */
	public final void setRaceId(int raceId)
	{
		this.raceId = raceId;
	}

	/**
	 * @param rank ранг мембера.
	 */
	public final void setRank(GuildRank rank)
	{
		this.rank = rank;
	}

	/**
	 * @param sex пол мембера.
	 */
	public final void setSex(int sex)
	{
		this.sex = sex;
	}

	/**
	 * @param zoneId the zoneId to set
	 */
	public final void setZoneId(int zoneId)
	{
		this.zoneId = zoneId;
	}

	@Override
	public String toString()
	{
		return "GuildMember  name = " + name + ", note = " + note + ", objectId = " + objectId + ", level = " + level + ", sex = " + sex + ", raceId = " + raceId + ", classId = " + classId + ", zoneId = " + zoneId + ", lastOnline = " + lastOnline + ", rank = " + rank + ", online = " + online;
	}
}

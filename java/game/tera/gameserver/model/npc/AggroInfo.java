package tera.gameserver.model.npc;

import rlib.util.pools.Foldable;
import tera.gameserver.model.Character;

/**
 * Контейнер информации об колчестве аггра на персонажа.
 *
 * @author Ronn
 */
public final class AggroInfo implements Foldable, Comparable<AggroInfo>
{
	/** атакующий персонаж */
	private Character aggressor;

	/** уровень агрессии */
	private long aggro;
	/** нанесенный урон */
	private long damage;

	/**
	 * @param aggro кол-во добавляемых аггр поинтов.
	 */
	public void addAggro(long aggro)
	{
		this.aggro += aggro;
	}

	/**
	 * @param damage кол-во добавляемого урона.
	 */
	public void addDamage(long damage)
	{
		this.damage += damage;
	}

	@Override
	public int compareTo(AggroInfo info)
	{
		return (int) (aggro - info.aggro);
	}

	@Override
	public boolean equals(Object object)
	{
		if(object == null)
			return false;

		if(object == this)
			return true;

		if(object == aggressor)
			return true;

		return false;
	}

	@Override
	public void finalyze()
	{
		aggressor = null;
		aggro = 0L;
		damage = 0L;
	}

	/**
	 * @return агрессор.
	 */
	public Character getAggressor()
	{
		return aggressor;
	}

	/**
	 * @return кол-во агр поинтов.
	 */
	public long getAggro()
	{
		return aggro;
	}

	/**
	 * @return нанесенный урон.
	 */
	public final long getDamage()
	{
		return damage;
	}

	/**
	 * @return есть ли агрессор.
	 */
	public boolean hasAggressor()
	{
		return aggressor != null;
	}

	@Override
	public void reinit(){}

	/**
	 * @param aggressor агрессор.
	 */
	public void setAggressor(Character aggressor)
	{
		this.aggressor = aggressor;
	}

	/**
	 * @param aggro кол-во агр поинтов.
	 */
	public void setAggro(long aggro)
	{
		this.aggro = aggro;
	}

	/**
	 * @param damage нанесенный урон.
	 */
	public final void setDamage(long damage)
	{
		this.damage = damage;
	}

	/**
	 * @param aggro кол-во отнимаемых агр поинтов.
	 */
	public void subAggro(long aggro)
	{
		this.aggro -= aggro;
	}

	/**
	 * @param damage кол-во отнимаемого урона.
	 */
	public void subDamage(long damage)
	{
		this.damage -= damage;
	}
}

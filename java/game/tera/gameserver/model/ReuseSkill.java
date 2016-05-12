package tera.gameserver.model;

import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;

/**
 * Модель отката скилов.
 *
 * @author Ronn
 */
public final class ReuseSkill implements Foldable
{
	private static final FoldablePool<ReuseSkill> pool = Pools.newConcurrentFoldablePool(ReuseSkill.class);

	/**
	 * Срздает новый экземпляр отката скила.
	 *
	 * @param skillId ид откатываемого скила.
	 * @param reuse время отката скила.
	 * @return новый откат скила.
	 */
	public static final ReuseSkill newInstance(int skillId, long reuse)
	{
		ReuseSkill reuseSkill = pool.take();

		if(reuseSkill == null)
			reuseSkill = new ReuseSkill();

		reuseSkill.skillId = skillId;
		reuseSkill.endTime = System.currentTimeMillis() + reuse;

		return reuseSkill;
	}

	/** скил ид */
	private int skillId;
	/** ид итема */
	private int itemId;

	/** время окончания реюза */
	private long endTime;

	@Override
	public void finalyze()
	{
		itemId = 0;
		skillId = 0;
		endTime = 0;
	}

	/**
	 * Сложить в пул.
	 */
	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return возвращает кол-во милисек оставшихся для отката скила.
	 */
	public long getCurrentDelay()
	{
		long rest = endTime - System.currentTimeMillis();

		if(rest > 0)
			return rest;

		return 0;
	}

	/**
	 * @return время окончания отката.
	 */
	public long getEndTime()
	{
		return endTime;
	}

	/**
	 * @return ид откатываемого итема.
	 */
	public int getItemId()
	{
		return itemId;
	}

	/**
	 * @return ид откатываемого скила.
	 */
	public int getSkillId()
	{
		return skillId;
	}

	/**
	 * @return является ли откат откатом итема.
	 */
	public boolean isItemReuse()
	{
		return itemId > 0;
	}

	/**
	 * @return откатился ли уже скил.
	 */
	public boolean isUse()
	{
		return System.currentTimeMillis() < endTime;
	}

	@Override
	public void reinit(){}

	/**
	 * @param endTime дата завершения отката скила.
	 */
	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	/**
	 * @param itemId ид откатываемого итема.
	 */
	public ReuseSkill setItemId(int itemId)
	{
		this.itemId = itemId;

		return this;
	}

	/**
	 * @param skillId ид откатываемого скила.
	 */
	public void setSkillId(int skillId)
	{
		this.skillId = skillId;
	}

	@Override
	public String toString()
	{
		return "ReuseSkill  skillId = " + skillId + ", itemId = " + itemId + ", endTime = " + endTime;
	}
}

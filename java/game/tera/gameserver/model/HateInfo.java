package tera.gameserver.model;

import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.npc.Npc;

/**
 * Контейнер информации о хейте персонажа на нпс.
 * 
 * @author Ronn
 */
public final class HateInfo implements Foldable
{
	private static final FoldablePool<HateInfo> pool = Pools.newConcurrentFoldablePool(HateInfo.class);
	
	/**
	 * Новый контейнер инфы об аггресии.
	 * 
	 * @param npc нпс.
	 * @return новый контейнер.
	 */
	public static HateInfo newInstance(Npc npc)
	{
		HateInfo info = pool.take();
		
		if(info == null)
			info = new HateInfo(npc);
		else
			info.npc = npc;
		
		return info;
	}
	
	/** нпс для которого инфа */
	private Npc npc;
	/** сколько агр поинтов получил */
	private long hate;
	
	/** сколько нанесено было урона */
	private long damage;
	
	/**
	 * @param npc нпс, на который имеем хейт.
	 */
	private HateInfo(Npc npc)
	{
		super();
		
		this.npc = npc;
		
		hate = 0L;
		damage = 0L;
	}

	/**
	 * Прибавка к урону.
	 * 
	 * @param damage добавочный урон.
	 */
	public void addDamage(long damage)
	{
		this.damage += damage;
	}
	
	/**
	 * Прибавка к хейту.
	 * 
	 * @param hate добавочный хейт.
	 */
	public void addHate(long hate)
	{
		this.hate += hate;
	}

	@Override
	public void finalyze()
	{
		this.npc = null;
		this.damage = 0;
		this.hate = 0;
	}

	/**
	 * Сохранить в пуле.
	 */
	public void fold()
	{
		pool.put(this);
	}
	
	/**
	 * @return нанесенный урон.
	 */
	public long getDamage()
	{
		return damage;
	}

	/**
	 * @return набранный хейт.
	 */
	public long getHate()
	{
		return hate;
	}

	/**
	 * @return нпс.
	 */
	public Npc getNpc()
	{
		return npc;
	}
	
	@Override
	public void reinit(){}
	
	/**
	 * @param damage нанесенный урон.
	 */
	public void setDamage(long damage)
	{
		this.damage = damage;
	}

	/**
	 * @param hate набранный хейт.
	 */
	public void setHate(long hate)
	{
		this.hate = hate;
	}

	/**
	 * @param npc нпс.
	 */
	public void setNpc(Npc npc)
	{
		this.npc = npc;
	}

	@Override
	public String toString()
	{
		return "HateInfo  hate = " + hate + ", damage = " + damage;
	}
}

package tera.gameserver.model.skillengine.shots;

import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель быстрого выстрела с авто наводкой.
 * 
 * @author Ronn
 */
public final class FastAutoShot extends AbstractAutoShot
{
	private static final FoldablePool<FastAutoShot> pool = Pools.newConcurrentFoldablePool(FastAutoShot.class);
	
	/**
	 * Создание экземпляра выстрела по цели.
	 * 
	 * @param caster стреляющий.
	 * @param target цель.
	 * @param skill скил.
	 */
	public static void startShot(Character caster, Character target, Skill skill)
	{
		FastAutoShot shot = pool.take();
		
		if(shot == null)
			shot = new FastAutoShot();
		
		shot.caster = caster;
		shot.target = target;
		shot.skill = skill;
		
		shot.start();
	}
	
	@Override
	public ShotType getType()
	{
		return ShotType.FAST_SHOT;
	}

	@Override
	public void stop()
	{
		super.stop();
		
		pool.put(this);
	}
}

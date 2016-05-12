package tera.gameserver.model.skillengine.shots;

import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель быстрых выстрелов.
 *
 * @author Ronn
 */
public class FastShot extends AbstractShot
{
	/** пул выстрелов */
	private static FoldablePool<FastShot> shotPool = Pools.newConcurrentFoldablePool(FastShot.class);

	/**
	 * @param caster стреляющий персонаж.
	 * @param skill стреляющий скил.
	 * @param targetX координата точки полета.
	 * @param targetY координата точки полета.
	 * @param targetZ координата точки полета.
	 */
	public static void startShot(Character caster, Skill skill, float targetX, float targetY, float targetZ)
	{
		// извлекаем из пула выстрел
		FastShot shot = shotPool.take();

		// если его нет
		if(shot == null)
			// создаем
			shot = new FastShot();

		// подготавливаем
		shot.prepare(caster, skill, targetX, targetY, targetZ);

		// запускаем
		shot.start();
	}

	public FastShot()
	{
		setType(ShotType.FAST_SHOT);
	}

	@Override
	public synchronized void stop()
	{
		super.stop();

		// складываем в пул
		shotPool.put(this);
	}
}

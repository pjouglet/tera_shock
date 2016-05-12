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
public class NpcFastShot extends AbstractShot
{
	/** пул выстрелов */
	private static FoldablePool<NpcFastShot> shotPool = Pools.newConcurrentFoldablePool(NpcFastShot.class);

	/**
	 * @param caster стреляющий персонаж.
	 * @param skill стреляющий скил.
	 * @param impactX координата точки полета.
	 * @param impactY координата точки полета.
	 * @param impactZ координата точки полета.
	 */
	public static void startShot(Character caster, Skill skill, Character target)
	{
		// вытаскиваем из пула
		NpcFastShot shot = shotPool.take();

		// если нет
		if(shot == null)
			// созадем новый
			shot = new NpcFastShot();

		// подготавливаем
		shot.prepare(caster, skill, target);

		// запускаем
		shot.start();
	}

	/** цель выстрела */
	private Character target;

	public NpcFastShot()
	{
		setType(ShotType.FAST_SHOT);
	}

	@Override
	public void finalyze()
	{
		target = null;

		super.finalyze();
	}

	/**
	 * @param caster
	 * @param skill
	 * @param target
	 */
	public void prepare(Character caster, Skill skill, Character target)
	{
		this.target = target;

		super.prepare(caster, skill, target.getX(), target.getY(), target.getZ());
	}

	@Override
	public void run()
	{
		this.targetX = target.getX();
		this.targetY = target.getY();
		this.targetZ = target.getZ();

		super.run();
	}

	@Override
	public void stop()
	{
		super.stop();

		// ложим в пул
		shotPool.put(this);
	}
}

package tera.gameserver.model.skillengine.shots;

import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.IdFactory;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.DeleteShot;
import tera.gameserver.network.serverpackets.StartSlowShot;

/**
 * Модель медленных выстрелов.
 *
 * @author Ronn
 */
public class SlowShot extends AbstractShot
{
	/** пул выстрелов */
	private static FoldablePool<SlowShot> shotPool = Pools.newConcurrentFoldablePool(SlowShot.class);

	/**
	 * @param caster стреляющий персонаж.
	 * @param skill стреляющий скил.
	 * @param targetX координата точки полета.
	 * @param targetY координата точки полета.
	 * @param targetZ координата точки полета.
	 */
	public static void startShot(Character caster, Skill skill, float targetX, float targetY, float targetZ)
	{
		// получаем объект из пула
		SlowShot shot = shotPool.take();

		// если его нет
		if(shot == null)
			// создаем новый
			shot = new SlowShot();

		// подготавливаем
		shot.prepare(caster, skill, targetX, targetY, targetZ);

		// запускаем
		shot.start();
	}

	/** уникальный ид выстрела */
	protected int objectId;

	public SlowShot()
	{
		setType(ShotType.SLOW_SHOT);
	}

	@Override
	public int getObjectId()
	{
		return objectId;
	}

	@Override
	public void start()
	{
		super.start();

		// получаем фабрику ИД
		IdFactory idFactory = IdFactory.getInstance();

		// получаем новый ид
		objectId = idFactory.getNextShotId();

		// отправляем пакет
		caster.broadcastPacket(StartSlowShot.getInstance(caster, skill, objectId, getSubId(), targetX, targetY, targetZ));
	}

	@Override
	public void stop()
	{
		super.stop();

		// отправляем пакет
		caster.broadcastPacket(DeleteShot.getInstance(objectId, getSubId()));
		// ложим в пул
		shotPool.put(this);
	}
}

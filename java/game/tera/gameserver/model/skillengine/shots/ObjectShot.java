package tera.gameserver.model.skillengine.shots;

import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.IdFactory;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.StartObjectShot;
import tera.gameserver.network.serverpackets.CharObjectDelete;

/**
 * Модель объектных выстрелов.
 *
 * @author Ronn
 */
public class ObjectShot extends AbstractShot
{
	/** пул выстрелов */
	private static FoldablePool<ObjectShot> pool = Pools.newConcurrentFoldablePool(ObjectShot.class);

	/**
	 * @param caster стреляющий персонаж.
	 * @param skill стреляющий скил.
	 * @param targetX координата точки полета.
	 * @param targetY координата точки полета.
	 * @param targetZ координата точки полета.
	 */
	public static void startShot(Character caster, Skill skill, float targetX, float targetY, float targetZ)
	{
		// вытаскиваем с пула
		ObjectShot shot = pool.take();

		// если нету
		if(shot == null)
			// создаем
			shot = new ObjectShot();

		// подготавливаем
		shot.prepare(caster, skill, targetX, targetY, targetZ);

		// запускаем
		shot.start();
	}

	/** уникальный ид выстрела */
	protected int objectId;

	public ObjectShot()
	{
		setType(ShotType.SLOW_SHOT);
	}

	@Override
	public int getObjectId()
	{
		return objectId;
	}

	@Override
	public synchronized void start()
	{
		super.start();

		// получаем фабрику ИД
		IdFactory idFactory = IdFactory.getInstance();

		// получаем новый ид
		setObjectId(idFactory.getNextShotId());

		// отправляем пакет
		caster.broadcastPacket(StartObjectShot.getInstance(caster, skill, this));
	}

	@Override
	public synchronized void stop()
	{
		super.stop();

		// отправляем пакет удаления
		caster.broadcastPacket(CharObjectDelete.getInstance(objectId, getSubId()));

		// ложим в пул
		pool.put(this);
	}

	/**
	 * @param objectId уникальный ид скила.
	 */
	public void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}
}

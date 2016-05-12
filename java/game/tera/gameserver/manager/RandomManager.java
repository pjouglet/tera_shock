package tera.gameserver.manager;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.random.Random;
import rlib.util.random.Randoms;
import tera.Config;

/**
 * Менеджер рандоминайзеров для разных целей.
 *
 * @author Ronn
 */
public final class RandomManager
{
	private static final Logger log = Loggers.getLogger(RandomManager.class);

	private static RandomManager instance;

	public static RandomManager getInstance()
	{
		if(instance == null)
			instance = new RandomManager();

		return instance;
	}

	/** рандоминайзер для дропа */
	private Random dropRandom;

	/** рандоминайзер для критов */
	private Random critRandom;

	/** рандоминайзер для урона */
	private Random damageRandom;

	/** рандоминайзер для опрокидывания */
	private Random owerturnRandom;

	/** рандоминайзер для эффектов */
	private Random effectRandom;

	/** рандоминайзер для функций */
	private Random funcRandom;

	/** рандоминайзер точек дропа итемов */
	private Random dropItemPointRandom;

	/** рандоминайзер для спавна НПС */
	private Random npcSpawnRandom;

	private RandomManager()
	{
		dropRandom = Config.SERVER_DROP_REAL_RANDOM? Randoms.newRealRandom() : Randoms.newFastRandom();
		critRandom = Config.SERVER_CRIT_REAL_RANDOM? Randoms.newRealRandom() : Randoms.newFastRandom();
		effectRandom = Config.SERVER_EFFECT_REAL_RANDOM? Randoms.newRealRandom() : Randoms.newFastRandom();
		funcRandom = Config.SERVER_FUNC_REAL_RANDOM? Randoms.newRealRandom() : Randoms.newFastRandom();
		damageRandom = Config.SERVER_DAMAGE_REAL_RANDOM? Randoms.newRealRandom() : Randoms.newFastRandom();
		owerturnRandom = Config.SERVER_OWERTURN_REAL_RANDOM? Randoms.newRealRandom() : Randoms.newFastRandom();

		dropItemPointRandom = Randoms.newFastRandom();
		npcSpawnRandom = Randoms.newFastRandom();

		log.info("initialized.");
	}

	/**
	 * @return рандоминайзер для критов.
	 */
	public Random getCritRandom()
	{
		return critRandom;
	}

	/**
	 * @return рандоминайзер для урона.
	 */
	public Random getDamageRandom()
	{
		return damageRandom;
	}

	/**
	 * @return рандоминайзер точек дропа итемов.
	 */
	public Random getDropItemPointRandom()
	{
		return dropItemPointRandom;
	}

	/**
	 * @return рандоминайзер для дропа.
	 */
	public Random getDropRandom()
	{
		return dropRandom;
	}

	/**
	 * @return рандоминайзер для эффектов.
	 */
	public Random getEffectRandom()
	{
		return effectRandom;
	}

	/**
	 * @return рандоминайзер для функций.
	 */
	public Random getFuncRandom()
	{
		return funcRandom;
	}

	/**
	 * @return рандоминайзер для опрокидывания.
	 */
	public Random getOwerturnRandom()
	{
		return owerturnRandom;
	}

	/**
	 * @return рандоминайзер для спавна НПС.
	 */
	public Random getNpcSpawnRandom()
	{
		return npcSpawnRandom;
	}
}

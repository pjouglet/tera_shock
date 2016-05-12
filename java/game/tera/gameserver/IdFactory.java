package tera.gameserver;

import java.util.concurrent.ScheduledExecutorService;

import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ExecutorManager;

import rlib.database.ConnectFactory;
import rlib.idfactory.IdGenerator;
import rlib.idfactory.IdGenerators;
import rlib.logging.Logger;
import rlib.logging.Loggers;

/**
 * Глобальная фабрика ид.
 *
 * @author Ronn
 */
public final class IdFactory
{
	private static final Logger log = Loggers.getLogger(IdFactory.class);

	private static final String[][] itemTable =
	{
		{"items", "object_id"},
	};

	private static final String[][] playerTable =
	{
		{"characters", "object_id"},
	};

	private static final String[][] clanTable =
	{
		{"guilds", "id"},
	};

	private static IdFactory instance;

	public static IdFactory getInstance()
	{
		if(instance == null)
			instance = new IdFactory();

		return instance;
	}

	/** фабрика ид для нпс */
	private IdGenerator npcIds;
	/** фабрика ид для игроков */
	private IdGenerator playerIds;
	/** фабрика ид для итемов */
	private IdGenerator itemIds;
	/** фабрика ид для ресурсов */
	private IdGenerator objectIds;
	/** фабрика ид для кланов */
	private IdGenerator guildIds;

	/** фабрика ид для акшенов */
	private IdGenerator actionIds;
	/** фабрика ид для выстрелов */
	private IdGenerator shotIds;
	/** фабрика ид для квестов */
	private IdGenerator questIds;
	/** фабрика ид для ловушек */
	private IdGenerator trapIds;
	/** фабрика ид для ресурсов */
	private IdGenerator resourseIds;

	public IdFactory()
	{
		// получаем менеджера БД
		DataBaseManager manager = DataBaseManager.getInstance();

		// получаем фабрику подключений
		ConnectFactory connectFactory = manager.getConnectFactory();

		// получаем исполнительного менеджера
		ExecutorManager executorManager = ExecutorManager.getInstance();

		// получаем исполнителя заданий для фабрики ИД
		ScheduledExecutorService executor = executorManager.getIdFactoryExecutor();

		log.info("prepare npc ids...");
		npcIds = IdGenerators.newBitSetIdGeneratoe(connectFactory, executor, null);
		npcIds.prepare();

		log.info("prepare objects ids...");
		objectIds = IdGenerators.newBitSetIdGeneratoe(connectFactory, executor, null);
		objectIds.prepare();

		log.info("prepare players ids...");
		playerIds = IdGenerators.newBitSetIdGeneratoe(connectFactory, executor, playerTable);
		playerIds.prepare();

		log.info("prepare guilds ids...");
		guildIds = IdGenerators.newBitSetIdGeneratoe(connectFactory, executor, clanTable);
		guildIds.prepare();

		log.info("prepare item ids...");
		itemIds = IdGenerators.newBitSetIdGeneratoe(connectFactory, executor, itemTable);
		itemIds.prepare();

		actionIds = IdGenerators.newSimpleIdGenerator(1, Integer.MAX_VALUE);
		shotIds = IdGenerators.newSimpleIdGenerator(1, Integer.MAX_VALUE);
		questIds = IdGenerators.newSimpleIdGenerator(1, Integer.MAX_VALUE);
		trapIds = IdGenerators.newSimpleIdGenerator(1, Integer.MAX_VALUE);
		resourseIds = IdGenerators.newSimpleIdGenerator(1, Integer.MAX_VALUE);
	}

	/**
	 * @return новый ид для акшена.
	 */
	public final int getNextActionId()
	{
		return actionIds.getNextId();
	}

	/**
	 * @return новый ид для клана.
	 */
	public final int getNextGuildId()
	{
		return guildIds.getNextId();
	}

	/**
	 * @return новый ид для итема.
	 */
	public final int getNextItemId()
	{
		return itemIds.getNextId();
	}

	/**
	 * @return новый ид для нпс.
	 */
	public final int getNextNpcId()
	{
		return npcIds.getNextId();
	}

	/**
	 * @return новый ид для объекта.
	 */
	public final int getNextObjectId()
	{
		return objectIds.getNextId();
	}

	/**
	 * @return новый ид для игрока.
	 */
	public final int getNextPlayerId()
	{
		return playerIds.getNextId();
	}

	/**
	 * @return новый ид для квеста.
	 */
	public final int getNextQuestId()
	{
		return questIds.getNextId();
	}

	/**
	 * @return новый ид для ресурсов.
	 */
	public final int getNextResourseId()
	{
		return resourseIds.getNextId();
	}

	/**
	 * @return новый ид для выстрела.
	 */
	public final int getNextShotId()
	{
		return shotIds.getNextId();
	}

	/**
	 * @return новый ид для ловушки.
	 */
	public final int getNextTrapId()
	{
		return trapIds.getNextId();
	}
}

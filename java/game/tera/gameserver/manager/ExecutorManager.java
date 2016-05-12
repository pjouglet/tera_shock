package tera.gameserver.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import rlib.concurrent.GroupThreadFactory;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.Config;
import tera.gameserver.ServerThread;

/**
 * Менеджер Исполнения заданий.
 *
 * @author Ronn
 * @created 25.03.2012
 */
public final class ExecutorManager
{
	private static final Logger log = Loggers.getLogger(ExecutorManager.class);

	private static ExecutorManager instance;

	public static ExecutorManager getInstance()
	{
		if(instance == null)
			instance = new ExecutorManager();

		return instance;
	}

	/** исполнитель основных заданий */
	private ScheduledExecutorService generalExecutor;
	/** исполнитель заданий перемещений */
	private ScheduledExecutorService moveExecutor;
	/** исполнитель заданий АИ */
	private ScheduledExecutorService aiExecutor;
	/** исполнитель заданий приминения скилов */
	private ScheduledExecutorService skillUseExecutor;
	/** исполнитель заданий каста скилов */
	private ScheduledExecutorService skillCastExecutor;
	/** исполнитель заданий перемещение скилов */
	private ScheduledExecutorService skillMoveExecutor;
	/** исполнитель заданий по фабрике ид */
	private ScheduledExecutorService idFactoryExecutor;

	/** обработчик клиентских пакетов */
	private ExecutorService synchPacketExecutor;
	/** обработчик клиентских пакетов */
	private ExecutorService asynchPacketExecutor;
	/** обработчик клиентских пакетов */
	private ExecutorService serverPacketExecutor;

	/** обработчик параллельных дейтсвий */
	private ExecutorService executor;

	private ExecutorManager()
	{
		generalExecutor = Executors.newScheduledThreadPool(Config.THREAD_POOL_SIZE_GENERAL, new GroupThreadFactory("GeneralThreadExecutor", ServerThread.class, Thread.NORM_PRIORITY + 1));
		moveExecutor = Executors.newScheduledThreadPool(Config.THREAD_POOL_SIZE_MOVE, new GroupThreadFactory("MoveThreadExecutor", ServerThread.class, Thread.NORM_PRIORITY + 3));
		aiExecutor = Executors.newScheduledThreadPool(Config.THREAD_POOL_SIZE_AI, new GroupThreadFactory("AIThreadExecutor", ServerThread.class, Thread.MIN_PRIORITY));

		skillUseExecutor = Executors.newScheduledThreadPool(Config.THREAD_POOL_SIZE_SKILL_USE, new GroupThreadFactory("SkillUseExecutor", ServerThread.class, Thread.NORM_PRIORITY));
		skillCastExecutor = Executors.newScheduledThreadPool(Config.THREAD_POOL_SIZE_SKILL_CAST, new GroupThreadFactory("SkillCastExecutor", ServerThread.class, Thread.NORM_PRIORITY));
		skillMoveExecutor = Executors.newScheduledThreadPool(Config.THREAD_POOL_SIZE_SKILL_MOVE, new GroupThreadFactory("SkillMoveExecutor", ServerThread.class, Thread.NORM_PRIORITY + 3));

		idFactoryExecutor = Executors.newSingleThreadScheduledExecutor();

		synchPacketExecutor = Executors.newSingleThreadExecutor(new GroupThreadFactory("SynchPacketExecutor", ServerThread.class, Thread.MAX_PRIORITY));
		asynchPacketExecutor = Executors.newFixedThreadPool(Config.THREAD_POOL_PACKET_RUNNER, new GroupThreadFactory("AsynchPacketExecutor", ServerThread.class, Thread.NORM_PRIORITY));

		serverPacketExecutor = Executors.newSingleThreadExecutor(new GroupThreadFactory("ServerPacketExecutor", ServerThread.class, Thread.MAX_PRIORITY));

		executor = Executors.newFixedThreadPool(2, new GroupThreadFactory("AsynThreadExecutor", ServerThread.class, Thread.MAX_PRIORITY));

		log.info("initialized.");
	}

	/**
	 * Выполнение задания в параллельном потоке.
	 *
	 * @param runnable задание, которое надо выполнить.
	 */
	public void execute(Runnable runnable)
	{
		executor.execute(runnable);
	}

	/**
	 * @return исполнитель заданий для фабрики ИД.
	 */
	public ScheduledExecutorService getIdFactoryExecutor()
	{
		return idFactoryExecutor;
	}

	/**
	 * Отправка на асинхронное выполнение пакет.
	 *
	 * @param packet пакет, который нужно отправить.
	 */
	public void runAsynchPacket(Runnable packet)
	{
		asynchPacketExecutor.execute(packet);
	}


	/**
	 * Отправка на выполнение серверный пакет.
	 *
	 * @param packet пакет, который нужно отправить.
	 */
	public void runServerPacket(Runnable packet)
	{
		serverPacketExecutor.execute(packet);
	}

	/**
	 * Отправка на синхронное выполнение пакет.
	 *
	 * @param packet пакет, который нужно отправить.
	 */
	public void runSynchPacket(Runnable packet)
	{
		synchPacketExecutor.execute(packet);
	}

	/**
	 * Создание периодического отложенного таска для АИ.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед первым запуском.
	 * @param interval интервал между выполнением таска.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleAiAtFixedRate(T runnable, long delay, long interval)
	{
		try
		{
			if(interval < 0)
				interval = 0;

			if(delay < 0)
				delay = 0;

			return (ScheduledFuture<T>) aiExecutor.scheduleAtFixedRate(runnable, delay, interval, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание отложенного общего таска.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед выполнением.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleGeneral(T runnable, long delay)
	{
		try
		{
			if(delay < 0)
				delay = 0;

			return (ScheduledFuture<T>) generalExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание периодического отложенного общего таска.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед первым запуском.
	 * @param interval интервал между выполнением таска.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleGeneralAtFixedRate(T runnable, long delay, long interval)
	{
		try
		{
			if(interval <= 0)
				interval = 1;

			return (ScheduledFuture<T>) generalExecutor.scheduleAtFixedRate(runnable, delay, interval, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание отложенного таска перемещения.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед выполнением.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleMove(T runnable, long delay)
	{
		try
		{
			if(delay < 1)
				delay = 1;

			return (ScheduledFuture<T>) moveExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание периодического отложенного таска движения.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед первым запуском.
	 * @param interval интервал между выполнением таска.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleMoveAtFixedRate(T runnable, long delay, long interval)
	{
		try
		{
			if(delay < 1)
				delay = 1;

			return (ScheduledFuture<T>) moveExecutor.scheduleAtFixedRate(runnable, delay, interval, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание отложенного таска для каста скилла.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед выполнением.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleSkillCast(T runnable, long delay)
	{
		try
		{
			if(delay < 0)
				delay = 0;

			return (ScheduledFuture<T>) skillCastExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание периодического отложенного таска для каста скила.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед первым запуском.
	 * @param interval интервал между выполнением таска.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleSkillCastAtFixedRate(T runnable, long delay, long interval)
	{
		try
		{
			if(interval < 0)
				interval = 0;

			if(delay < 0)
				delay = 0;

			return (ScheduledFuture<T>) skillCastExecutor.scheduleAtFixedRate(runnable, delay, interval, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание отложенного таска для движения скилла.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед выполнением.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleSkillMove(T runnable, long delay, boolean isPlayer)
	{
		try
		{
			if(delay < 0)
				delay = 0;

			return (ScheduledFuture<T>) skillMoveExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание периодического отложенного таска для движения скила.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед первым запуском.
	 * @param interval интервал между выполнением таска.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleSkillMoveAtFixedRate(T runnable, long delay, long interval)
	{
		try
		{
			if(interval < 0)
				interval = 0;

			if(delay < 0)
				delay = 0;

			return (ScheduledFuture<T>) skillMoveExecutor.scheduleAtFixedRate(runnable, delay, interval, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание отложенного таска для приминения скилла.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед выполнением.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleSkillUse(T runnable, long delay)
	{
		try
		{
			if(delay < 0)
				delay = 0;

			return (ScheduledFuture<T>) skillUseExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}

	/**
	 * Создание периодического отложенного таска для приминения скила.
	 *
	 * @param runnable содержание таска.
	 * @param delay задержка перед первым запуском.
	 * @param interval интервал между выполнением таска.
	 * @return ссылка на таск.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Runnable> ScheduledFuture<T> scheduleSkillUseAtFixedRate(T runnable, long delay, long interval)
	{
		try
		{
			if(interval < 0)
				interval = 0;

			if(delay < 0)
				delay = 1;

			return (ScheduledFuture<T>) skillUseExecutor.scheduleAtFixedRate(runnable, delay, interval, TimeUnit.MILLISECONDS);
		}
		catch(RejectedExecutionException e)
		{
			log.warning(e);
		}

		return null;
	}
}
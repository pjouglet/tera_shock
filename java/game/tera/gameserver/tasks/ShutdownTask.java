package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;

import rlib.logging.GameLoggers;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.OnlineManager;
import tera.gameserver.manager.ServerVarManager;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.tables.SpawnTable;

/**
 * Таск для ребута/выключения
 *
 * @author Ronn
 * @created 25.04.2012
 */
public class ShutdownTask extends SafeTask
{
	private static final Logger log = Loggers.getLogger(ShutdownTask.class);

	/** перезапускать ли сервер */
	private boolean restart;

	/** задержка */
	private long delay;

	/** ссылка на таск */
	private volatile ScheduledFuture<ShutdownTask> schedule;

	/**
	 * Отмена таска.
	 */
	public synchronized boolean cancel()
	{
		// если времени еще больше чем минутаи таск есть
		if(delay > 60000 && schedule != null)
		{
			// останавливаем таск
			schedule.cancel(true);
			// зануляем
			schedule = null;
			// выводим в консоль
			log.info("cancel shutdown task.");

			return true;
		}

		// возвращаем флаг остановки
		return schedule == null;
	}

	/**
	 * @return ссылка натаск.
	 */
	public final ScheduledFuture<ShutdownTask> getSchedule()
	{
		return schedule;
	}

	/**
	 * Выполнен ли таск.
	 */
	public boolean isComplete()
	{
		return schedule == null;
	}

	/**
	 * @return запущен ли.
	 */
	public boolean isRunning()
	{
		return schedule != null;
	}

	/**
	 * @param restart флаг рестарт/Выключение.
	 * @param delay время до выключения.
	 */
	public void next(boolean restart, long delay)
	{
		// если отменено предыдущее выключение
		if(cancel())
		{
			// запоминаем новые данные
			this.restart = restart;
			this.delay = delay;

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			// создаем таск
			schedule = executor.scheduleGeneralAtFixedRate(this, 60000, 60000);

			// выводим сообщение в анонс
			if(restart)
			{
				World.sendAnnounce("Рестарт сервера произойдет через " + (delay / 1000 / 60) + " минут.");
				World.sendAnnounce("Restart the server will happen in " + (delay / 1000 / 60) + " minutes.");
			}
			else
			{
				World.sendAnnounce("Выключение сервера произойдет через " + (delay / 1000 / 60) + " минут.");
				World.sendAnnounce("Server shutdown will happen in " + (delay / 1000 / 60) + " minutes.");
			}

			// пишем в консоль
			log.info("will shutdown in " + (delay / 1000 / 60) + " minutes.");
		}
	}

	@Override
	protected synchronized void runImpl()
	{
		// уменьшаем оставшееся время
		delay -= 60000;

		// если еще больше минуты
		if(delay > 60000)
		{
			// выводим кол-во оставшихся минут
			if(restart)
			{
				World.sendAnnounce("Рестарт сервера произойдет через " + (delay / 1000 / 60) + " минут.");
				World.sendAnnounce("Restart the server will happen in " + (delay / 1000 / 60) + " minutes.");
			}
			else
			{
				World.sendAnnounce("Выключение сервера произойдет через " + (delay / 1000 / 60) + " минут.");
				World.sendAnnounce("Server shutdown will happen in " + (delay / 1000 / 60) + " minutes.");
			}
		}
		// иначе говорим, что вырубится через минуту
		else
		{
			World.sendAnnounce("Через 1 минуту сервер выключится.");
			World.sendAnnounce ("After 1 minute, turn off the server.");

			// получаем таблицу спавна
			SpawnTable spawnTable = SpawnTable.getInstance();

			// удаляем спавн нпс
			spawnTable.stopSpawns();
		}

		// сообщаем в консоль
		log.info("will shutdown in " + (delay / 1000 / 60) + " minutes.");

		// если время истекло
		if(delay < 1)
		{
			// выводим в консоль процесс выключения
			log.info("start save players...");

			// сохраняем всех кто онлаин
			for(Player player : World.getPlayers())
			{
				log.info("store " + player.getName());
				player.store(false);
			}

			log.info("all players saved.");

			// сохраняем гейм логи
			GameLoggers.finish();

			// получаем менеджера серверных переменных
			ServerVarManager varManager = ServerVarManager.getInstance();

			// завершаем работу менеджера серверных переменных
			varManager.finish();

			log.info("all game loggers writed.");

			// получаем менеджер онлайна
			OnlineManager onlineManager = OnlineManager.getInstance();

			// зануляем онлаин
			onlineManager.stop();

			// завершаем процесс
			System.exit(restart? 2 : 0);
		}
	}

	/**
	 * @param schedule ссылка на таск.
	 */
	public final void setSchedule(ScheduledFuture<ShutdownTask> schedule)
	{
		this.schedule = schedule;
	}
}

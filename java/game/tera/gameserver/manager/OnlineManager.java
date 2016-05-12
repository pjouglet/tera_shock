package tera.gameserver.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ScheduledFuture;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import tera.Config;
import tera.gameserver.model.World;

/**
 * Менеджер онлайна сервера.
 *
 * @author Ronn
 */
public final class OnlineManager
{
	private static final Logger log = Loggers.getLogger(OnlineManager.class);

	private static OnlineManager instance;

	public static OnlineManager getInstance()
	{
		if(instance == null)
			instance = new OnlineManager();

		return instance;
	}

	/** файл для записи онлайна */
	private File file;

	/** ссылка на таск обновления онлайна */
	private ScheduledFuture<SafeTask> schedule;

	/** текущий онлаин сервера */
	private volatile int currentOnline;

	private OnlineManager()
	{
		// если указан фаил экспорта онлайна
		if(!Config.SERVER_ONLINE_FILE.isEmpty())
			// создаем файл для экспорта
			file = new File(Config.SERVER_ONLINE_FILE);

		// создаем таск периодичной записи
		SafeTask task = new SafeTask()
		{
			@Override
			protected void runImpl()
			{
				// обновляем значение онлайна
				currentOnline = (int) (World.online() * Config.SERVER_ONLINE_FAKE);

				// если есть записчик
				if(file != null && file.canWrite())
				{
					try(PrintWriter writer = new PrintWriter(file))
					{
						// записываем онлайн
						writer.print(currentOnline);
					}
					catch(FileNotFoundException e)
					{
						Loggers.warning(OnlineManager.class, e);
					}
				}
			}
		};

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// ставим на выполнение задание
		schedule = executor.scheduleGeneralAtFixedRate(task, 60000, 60000);

		log.info("initialized.");
	}

	/**
	 * @return текущий онлаин.
	 */
	public int getCurrentOnline()
	{
		return currentOnline;
	}

	/**
	 * Остановка онлайн менеджера.
	 */
	public synchronized void stop()
	{
		// если есть таск
		if(schedule != null)
		{
			// останавливаем
			schedule.cancel(false);
			schedule = null;
		}

		// если есть записчик
		if(file != null && file.canWrite())
		{
			try(PrintWriter writer = new PrintWriter(file))
			{
				// зануляем онлаин
				writer.print(0);
			}
			catch(FileNotFoundException e)
			{
				Loggers.warning(OnlineManager.class, e);
			}
		}
	}
}
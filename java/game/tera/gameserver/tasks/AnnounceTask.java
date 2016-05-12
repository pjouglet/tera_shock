package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;

import rlib.util.SafeTask;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.World;

/**
 * Модель периодического аннонса.
 *
 * @author Ronn
 * @created 29.03.2012
 */
public class AnnounceTask extends SafeTask
{
	/** отправляемый текст */
	private final String text;

	/** интервал */
	private final int interval;

	/** ссылка на таск */
	private ScheduledFuture<AnnounceTask> schedule;

	public AnnounceTask(String text, int interval)
	{
		this.text = text;
		this.interval = interval;

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		this.schedule = executor.scheduleGeneralAtFixedRate(this, interval, interval);
	}

	/**
	 * Остановка аннонса.
	 */
	public synchronized void cancel()
	{
		if(schedule != null)
			schedule.cancel(true);
	}

	/**
	 * @return интервал аннонса.
	 */
	public final int getInterval()
	{
		return interval;
	}

	/**
	 * @return текст аннонса.
	 */
	public final String getText()
	{
		return text;
	}

	@Override
	protected void runImpl()
	{
		World.sendAnnounce(text);
	}
}

package tera.gameserver.manager;

import tera.gameserver.model.World;
import tera.gameserver.tasks.ShutdownTask;

/**
 * Менеджер выключения и перезагрузки сервера
 *
 * @author Ronn
 * @created 25.04.2012
 */
public final class ShutdownManager
{
	/** таск рестарта/выключения */
	private static final ShutdownTask task = new ShutdownTask();

	/**
	 * остановка выключения сервера
	 */
	public static final void cancel()
	{
		if(task.cancel())
			World.sendAnnounce("Выключение сервера прервано.");
	}

	/**
	 * Рестарт сервера.
	 *
	 * @param delay через сколько мс. перезапустить сервер.
	 */
	public static final void restart(long delay)
	{
		task.next(true, Math.max(delay, 120000));
	}

	/**
	 * Выключение сервера.
	 *
	 * @param delay через сколько мс. выключить сервер.
	 */
	public static final void shutdown(long delay)
	{
		task.next(false, Math.max(delay, 120000));
	}

	private ShutdownManager()
	{
		throw new IllegalArgumentException();
	}
}

package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;

import rlib.util.SafeTask;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;

/**
 * Модель обработчика опрокидывания.
 *
 * @author Ronn
 */
public class OwerturnTask extends SafeTask
{
	/** персонаж */
	private final Character character;

	/** ссылка на исполняемое задание */
	private volatile ScheduledFuture<OwerturnTask> schedule;

	public OwerturnTask(Character character)
	{
		this.character = character;
	}

	/**
	 * Запустить опрокидывание на указанное время.
	 *
	 * @param time время опрокидывания.
	 */
	public synchronized void nextOwerturn(int time)
	{
		if(schedule != null)
			schedule.cancel(true);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем задачу
		schedule = executor.scheduleGeneral(this, time);
	}

	@Override
	protected synchronized void runImpl()
	{
		// отменяем опрокидывание
		character.cancelOwerturn();

		// зануляем ссылку
		schedule = null;
	}
}

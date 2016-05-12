package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import rlib.util.SafeTask;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.templates.CharTemplate;

/**
 * Модель обработки разворота нпс.
 *
 * @author Ronn
 */
public class TurnTask extends SafeTask
{
	private static final int HALF = Short.MAX_VALUE;

	/** обрабатываемый нпс */
	private final Character actor;

	/** скорость разворота */
	private final int turnSpeed;

	/** ссылка на таск */
	private volatile ScheduledFuture<TurnTask> schedule;

	/** стартовый разворот */
	private int startHeading;
	/** конечный разворот */
	private int endHeading;
	/** время данное для разворота */
	private int time;
	/** разница в разворотах */
	private int diff;

	public TurnTask(Character actor)
	{
		this.actor = actor;

		CharTemplate template = actor.getTemplate();

		this.turnSpeed = template.getTurnSpeed() * 4;
	}

	/**
	 * Отмена разворота.
	 */
	public void cancel()
	{
		// получаем ссылку на задачу разворота
		ScheduledFuture<TurnTask> schedule = getSchedule();

		// если такая есть
		if(schedule != null)
		{
			synchronized(this)
			{
				// повторно получаем
				schedule = getSchedule();

				// если всеже такая есть
				if(schedule != null)
				{
					// получаем оставшееся время
					int res = (int) schedule.getDelay(TimeUnit.MILLISECONDS);

					// останавливаем задачу
					schedule.cancel(false);

					// зануляем ссылку
					setSchedule(null);

					// получвем отведенное время
					int time = getTime();

					// получаем процент выполненности разворота
					float done = (time - res) * 1F / time;

					// получаем сколько разворота прошли
					int result = (int) (getDiff() * done);

					// рассчитываем итоговый текущий разворот
					result = isPositive()? getStartHeading() + result : getStartHeading() - result;

					// применяем его
					actor.setHeading(result);
				}
			}
		}
	}

	/**
	 * @return позетивный ли разворот.
	 */
	public boolean isPositive()
	{
		return startHeading < endHeading;
	}

	/**
	 * @return разница в разворотах.
	 */
	public int getDiff()
	{
		return diff;
	}

	/**
	 * @return стартовый разворот.
	 */
	public int getStartHeading()
	{
		return startHeading;
	}

	/**
	 * @return время необоходимое для разворота.
	 */
	public int getTime()
	{
		return time;
	}

	/**
	 * @param diff разница в разворотах.
	 */
	public void setDiff(int diff)
	{
		this.diff = diff;
	}

	/**
	 * @return ссылка на задачу разворота.
	 */
	public ScheduledFuture<TurnTask> getSchedule()
	{
		return schedule;
	}

	/**
	 * @param schedule ссылка на задачу разворота.
	 */
	public void setSchedule(ScheduledFuture<TurnTask> schedule)
	{
		this.schedule = schedule;
	}

	/**
	 * @return конечный разворот.
	 */
	public final int getEndHeading()
	{
		if(endHeading > 65536)
			endHeading -= 65536;

		return endHeading;
	}

	/**
	 * @return находится ли нпс в процессе разворота.
	 */
	public boolean isTurner()
	{
		return schedule != null;
	}

	/**
	 * @return разворачиваемый персонаж.
	 */
	public Character getActor()
	{
		return actor;
	}

	/**
	 * Развернуть нпс до указанного направления.
	 *
	 * @param newHeading новое направление.
	 */
	public void nextTurn(int newHeading)
	{
		// отменяем предыдущий равороот
		cancel();

		// получаем разворачиваемого персонажа
		Character actor = getActor();

		// получаем стартовый разворот
		int startHeading = actor.getHeading();

		// получаем целевой разворот
		int endHeading = newHeading;

		synchronized(this)
		{
			// определяем разницу в развороте
			int diff = Math.abs(startHeading - endHeading);

			// вносим поправки в разницу
			if(diff > HALF)
			{
				if(startHeading < endHeading)
					startHeading += 65536;
				else
					endHeading += 65536;

				diff = Math.abs(startHeading - endHeading);
			}

			// обновляем разницу
			setDiff(diff);

			// увеличиваем разницу для рассчета требуемого времени в милисекундах
			diff *= 1000;

			// определяем требуемое время
			int time = diff / turnSpeed;

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			// ставим на исполнеие
			setSchedule(executor.scheduleGeneral(this, time + 700));

			// обновляем время
			setTime(time);

			// отображаем разворот
			PacketManager.showTurnCharacter(actor, endHeading, time * 80 / 100);
		}

		// обновляем стартовый разворот
		setStartHeading(startHeading);
		// обновляем конечный разворот
		setEndHeading(endHeading);
	}

	/**
	 * @param time время отведенное на разворот.
	 */
	public void setTime(int time)
	{
		this.time = time;
	}

	/**
	 * @param startHeading стартовый разворот.
	 */
	public void setStartHeading(int startHeading)
	{
		this.startHeading = startHeading;
	}

	/**
	 * @param endHeading конечный разворот.
	 */
	public void setEndHeading(int endHeading)
	{
		this.endHeading = endHeading;
	}

	@Override
	protected void runImpl()
	{
		// применяем целевой разворот
		actor.setHeading(endHeading);

		// зануляем ссылку на задачу
		setSchedule(null);
	}
}

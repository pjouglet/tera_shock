package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;

import rlib.util.Rnd;
import rlib.util.SafeTask;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.EmotionType;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.network.serverpackets.Emotion;
import tera.gameserver.network.serverpackets.ResourseCollectProgress;
import tera.gameserver.network.serverpackets.ResourseEndCollect;
import tera.gameserver.network.serverpackets.ResourseStartCollect;

/**
 * Модель обработки сбора ресурсов.
 *
 * @author Ronn
 */
public final class ResourseCollectTask extends SafeTask
{
	/** кастер скила */
	private Player collector;

	/** собираемый ресурс */
	private ResourseInstance resourse;

	/** текущий шанс сбора */
	private int chance;
	/** кол-во проходов до завершени сбора */
	private int counter;

	/** ссылка на таск */
	private volatile ScheduledFuture<ResourseCollectTask> schedule;

	public ResourseCollectTask(Player collector)
	{
		this.collector = collector;
	}

	/**
	 * Отмена каста скила.
	 */
	public synchronized void cancel(boolean force)
	{
		// получаем собераемый ресурс
		ResourseInstance resourse = getResourse();

		synchronized(this)
		{
			// если работает таск
			if(schedule != null)
			{
				// отстанавливаем
				schedule.cancel(false);
				// зануляем
				schedule = null;
			}

			// если есть скил
			if(resourse != null)
			{
				// зануляем скил
				setResourse(null);
			}
		}

		// если ресурс был
		if(resourse != null)
		{
			// если отмена формирована
			if(force)
				// отправляем пакет остановки сбора
				collector.broadcastPacket(ResourseEndCollect.getInstance(collector, resourse, ResourseEndCollect.INTERRUPTED));
			// иначе это фейл
			else
				// отправляем пакет остановки сбора
				collector.broadcastPacket(ResourseEndCollect.getInstance(collector, resourse, ResourseEndCollect.FAILED));

			// выполняем отмену сбора
			resourse.onCollected(collector, true);
		}
	}

	/**
	 * @return собираемый ресурс.
	 */
	protected final ResourseInstance getResourse()
	{
		return resourse;
	}

	/**
	 * @return запущен ли.
	 */
	public boolean isRunning()
	{
		return schedule != null && !schedule.isDone();
	}


	/**
	 * @param resourse собираемый ресурс.
	 */
	public void nextTask(ResourseInstance resourse)
	{
		// отменяем предыдущий таск
		cancel(true);

		synchronized(this)
		{
			this.counter = 3;
			// запоминаем ресурс
			this.resourse = resourse;
			// рассчитываем шанс сбора
			this.chance = resourse.getChanceFor(collector);

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			// запускаем новый
			this.schedule = executor.scheduleGeneralAtFixedRate(this, 1000, 1000);
		}

		// отсылаем пакет со стартом анимации
		collector.broadcastPacket(ResourseStartCollect.getInstance(collector, resourse));
	}

	@Override
	protected void runImpl()
	{
		// отправляем пакет с погрессом
		collector.sendPacket(ResourseCollectProgress.getInstance((100 - 25 * counter)), true);

		ResourseInstance resourse = null;

		boolean cancel = false;

		synchronized(this)
		{
			//TODO обновление прогресса

			// если еще не завершен сбор
			if(counter > 0)
			{
				// рассчитываем не сфейлился ли он
				if(Rnd.chance(chance))
				{
					// уменьшаем счетчик проходов и ыходим
					counter -= 1;
					return;
				}

				// ставим флаг прерывания
				cancel = true;
			}
			else
			{
				// если есть ссылка на таск
				if(schedule != null)
				{
					// авершаем
					schedule.cancel(false);
					// зануляем ссылку на таск
					schedule = null;
				}

				// вытаскиваем ресурс
				resourse = getResourse();
				// зануляем ресурс
				setResourse(null);
			}
		}

		// если фейл
		if(cancel)
		{
			// отменяем сбор
			cancel(false);
			// отправляем пакет с эмоцией
			collector.broadcastPacket(Emotion.getInstance(collector, EmotionType.FAIL));
		}
		else if(resourse != null)
		{
			// отправляем пакет остановки сбора
			collector.broadcastPacket(ResourseEndCollect.getInstance(collector, resourse, ResourseEndCollect.SUCCESSFUL));

			// завершаем сбор
			resourse.onCollected(collector, false);

			// отправляем пакет с эмоцией
			collector.broadcastPacket(Emotion.getInstance(collector, EmotionType.BOASTING));
		}
	}

	/**
	 * @param resourse собираемый ресурс.
	 */
	protected final void setResourse(ResourseInstance resourse)
	{
		this.resourse = resourse;
	}
}

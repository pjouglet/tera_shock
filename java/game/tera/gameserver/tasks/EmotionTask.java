package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;

import rlib.util.Rnd;
import rlib.util.SafeTask;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.EmotionType;

/**
 * Модель таска автоэмоций персонажей.
 *
 * @author Ronn
 */
public class EmotionTask extends SafeTask
{
	// набор эмоций у монстра
	public static final EmotionType[] MONSTER_TYPES =
	{
		EmotionType.INSPECTION,
		EmotionType.FAST_INSPECTION,
	};

	// набор эмоций у нпс
	public static final EmotionType[] NPC_TYPES =
	{
		EmotionType.INSPECTION,
		EmotionType.FAST_INSPECTION,
		EmotionType.CAST,
		EmotionType.MUSE,
		EmotionType.KNEAD_FISTS,
	};

	// набор эмоций у игрока
	public static final EmotionType[] PLAYER_TYPES =
	{
		EmotionType.INSPECTION,
		EmotionType.FAST_INSPECTION,
		EmotionType.BUMPING,
		EmotionType.MUSE,
		EmotionType.KNEAD_FISTS,
	};

	/** персонаж */
	private final Character actor;
	/** набор эмоций */
	private final EmotionType[] types;
	/** кол-во эмоций */
	private final int max;

	/** таск эмоций */
	private volatile ScheduledFuture<EmotionTask> schedule;

	/**
	 * @param actor персонаж.
	 * @param types набор эмоций.
	 */
	public EmotionTask(Character actor, EmotionType[] types)
	{
		this.actor = actor;
		this.types = types;
		this.max = types.length - 1;
	}

	/**
	 * @return ссылка на таск.
	 */
	public final ScheduledFuture<EmotionTask> getSchedule()
	{
		return schedule;
	}

	@Override
	protected void runImpl()
	{
		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// создаем следующий таск
		schedule = executor.scheduleGeneral(this, Rnd.nextInt(30000, 120000));

		// запускам рандомную эмоцию
		actor.getAI().startEmotion(types[Rnd.nextInt(0, max)]);
	}

	/**
	 * @param schedule ссылка на таск.
	 */
	public final void setSchedule(ScheduledFuture<EmotionTask> schedule)
	{
		this.schedule = schedule;
	}

	/**
	 * Запуск таска.
	 */
	public synchronized void start()
	{
		if(schedule != null)
			return;

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// создаем таск автоэмоций
		schedule = executor.scheduleGeneral(this, Rnd.nextInt(30000, 120000));
	}

	/**
	 * Остановка таска.
	 */
	public synchronized void stop()
	{
		// получаем текущую ссылку на таск
		ScheduledFuture<EmotionTask> schedule = getSchedule();

		// если он есть
		if(schedule != null)
		{
			// отменяем
			schedule.cancel(false);
			// зануляем
			setSchedule(null);
		}
	}
}

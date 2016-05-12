package tera.gameserver.taskmanager;

import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.skillengine.Effect;

/**
 * Менеджер по обиспечению работы эффектов.
 *
 * @author Ronn
 */
@SuppressWarnings("unchecked")
public final class EffectTaskManager extends SafeTask
{
	private static final Logger log = Loggers.getLogger(EffectTaskManager.class);

	private static EffectTaskManager instance;

	public static EffectTaskManager getInstance()
	{
		if(instance == null)
			instance = new EffectTaskManager();

		return instance;
	}

	/** блокировщик контейнеров */
	private Lock lock;

	/** доступные контейнеры для эффектов */
	private Array<Effect>[] containers;

	/** пул контейнеров */
	private FoldablePool<Array<Effect>> pool;

	/** текущая позиция в массиве контейнеров */
	private volatile int ordinal;

	private EffectTaskManager()
	{
		// создаем массив контейнеров
		containers = new Array[178000];

		// синхронизатор
		lock = Locks.newLock();

		// пул контейнеров
		pool = Pools.newFoldablePool(Array.class, 1000);

		// индекс текущего контейнера
		ordinal = 0;

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// добавляем задание на исполнение
		executor.scheduleGeneralAtFixedRate(this, 1000, 1000);

		log.info("initialized.");
	}

	/**
	 * Добавление в очередь эффект.
	 *
	 * @param effect эффект, который нужно добавить.
	 * @param interval через сколько времени он должен обновится.
	 */
	public final void addTask(Effect effect, int interval)
	{
		// минимальный интервал
		if(interval < 1)
			interval = 1;

		// максимальный интервал
		if(interval >= containers.length)
			interval = containers.length - 1;

		// определяем целевую ячейку
		int cell = ordinal + interval;
		// смещаем, если превышает предел
		if(containers.length <= cell)
			cell -= containers.length;

		// контейнер ожидающих эффектов
		Array<Effect> container = null;

		lock.lock();
		try
		{
			// получаем контейнер
			container = containers[cell];

			// если его нету
			if(container == null)
			{
				// если нету в пуле
				if(pool.isEmpty())
					container = Arrays.toArray(Effect.class);
				else
					// если есть, вынимаем
					container = pool.take();

				// добавляем в массив контейнеров
				containers[cell] = container;
			}

			// добавляем эффект в контейнер
			container.add(effect);
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	protected void runImpl()
	{
		// контейнер эффектов
		Array<Effect> container;

		lock.lock();
		try
		{
			// забираем с массива контейнер
			container = containers[ordinal];

			// зануляем его в массиве
			containers[ordinal] = null;
		}
		finally
		{
			lock.unlock();
		}

		// если контейнера нету
		if(container == null)
		{
			// увеличиваем позицию
			ordinal += 1;

			// если предельная позиция - обнуляем
			if(ordinal >= containers.length)
				ordinal = 0;

			// выходим
			return;
		}

		// если в контейнере есть эффекты
		if(!container.isEmpty())
		{
			// получаем массив эффектов
			Effect[] array = container.array();

			// перебераем  эффекты
			for(int i = 0, length = container.size(); i < length; i++)
			{
				// получаем эффект
				Effect effect = array[i];

				// если эффекта нету
				if(effect == null)
				{
					log.warning(new Exception("not found effect"));
					continue;
				}

				// если эффект уже завершен
				if(effect.isFinished())
				{
					// складываем в пул
					effect.fold();
					continue;
				}

				// получаем эффект лист
				EffectList effectList = effect.getEffectList();

				// если его нет, пропускаем обработку эффекта
				if(effectList == null)
				{
					log.warning("not found effect list to " + effect);
					continue;
				}

				effectList.lock();
				try
				{
					effect.scheduleEffect();
				}
				finally
				{
					effectList.unlock();
				}

				// если эффект завершен
				if(effect.isFinished())
				{
					// ложим в пул
					effect.fold();
					continue;
				}

				// добавляем эффект на очередь обработки
				addTask(effect, effect.getPeriod());
			}
		}

		// смещаем ячейку
		ordinal += 1;

		// если ячейка превысила лимит, обнуляем
		if(ordinal >= containers.length)
			ordinal = 0;

		// очищаем контейнер
		container.clear();

		// ложим контейнер в пул
		pool.put(container);
	}
}

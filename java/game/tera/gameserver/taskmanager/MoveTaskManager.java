package tera.gameserver.taskmanager;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.tasks.MoveNextTask;

/**
 * Модель менеджера по задачам перемещения персонажей.
 *
 * @author Ronn
 */
public class MoveTaskManager
{
	private static final Logger log = Loggers.getLogger(MoveTaskManager.class);

	private static final int ARRAY_LIMIT = 100;

	private static MoveTaskManager instance;

	public static MoveTaskManager getInstance()
	{
		if(instance == null)
			instance = new MoveTaskManager();

		return instance;
	}

	/** таблица обрабатываемых задач */
	private Table<Class<?>, Array<Array<MoveNextTask>>> taskTable;

	private MoveTaskManager()
	{
		this.taskTable = Tables.newObjectTable();

		log.info("initialized.");
	}

	/**
	 * Добавлние персонажа на обработку движения.
	 */
	public void addMoveTask(Array<Array<MoveNextTask>> tasks, MoveNextTask moveTask)
	{
		tasks.readLock();
		try
		{
			Array<MoveNextTask>[] array = tasks.array();

			// перебираем списки заданий
			for(int i = 0, length = tasks.size(); i < length; i++)
			{
				// получаем список заданий
				Array<MoveNextTask> container = array[i];

				if(container.size() < ARRAY_LIMIT)
				{
					// добавляем задание на обработку
					addMoveTask(moveTask, container);

					// выходим
					return;
				}
			}
		}
		finally
		{
			tasks.readUnlock();
		}

		// создаем новый контейнер заданий
		final Array<MoveNextTask> container = Arrays.toConcurrentArray(MoveNextTask.class);

		// создаем задачу по обработке списка задач
		SafeTask task = new SafeTask()
		{
			@Override
			protected void runImpl()
			{
				if(container.isEmpty())
					return;

				// получаем текущее время
				long currentTime = System.currentTimeMillis();

				container.readLock();
				try
				{
					// получаем массив задача
					MoveNextTask[] array = container.array();

					for(int i = 0, length = container.size(); i < length; i++)
						array[i].update(currentTime);
				}
				finally
				{
					container.readUnlock();
				}
			}
		};

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// ставим задачу на выполнение
		executor.scheduleMoveAtFixedRate(task, MoveNextTask.INTERVAL, MoveNextTask.INTERVAL);

		// добавляем задание на обработку
		addMoveTask(moveTask, container);

		// вносим в список
		tasks.add(container);
	}

	/**
	 * Добавление задания движения на обработку.
	 *
	 * @param task модель перемещения.
	 */
	public void addMoveTask(MoveNextTask moveTask)
	{
		// получаем владельца
		Character owner = moveTask.getOwner();

		// получаем таблицу обрабатываемых персонажей
		Table<Class<?>, Array<Array<MoveNextTask>>> taskTable = getTaskTable();

		// получаем список скписков обработки
		Array<Array<MoveNextTask>> array = taskTable.get(owner.getClass());

		// если его нет
		if(array == null)
		{
			// синхронизируем табилцу
			synchronized(taskTable)
			{
				// получаем еще раз
				array = taskTable.get(owner.getClass());

				if(array == null)
				{
					// создаем новый список
					array = Arrays.toConcurrentArray(Array.class);

					// вносим в таблицу
					taskTable.put(owner.getClass(), array);
				}
			}
		}

		// добавляем персонажа в массив
		addMoveTask(array, moveTask);
	}

	/**
	 * Внесение задачи в контейнер на исполнение.
	 */
	public void addMoveTask(MoveNextTask task, Array<MoveNextTask> container)
	{
		// запоминаем контейнер
		task.setContainer(container);

		// вносимз адачу в контейнер
		container.add(task);
	}

	/**
	 * @return таблица задач.
	 */
	public Table<Class<?>, Array<Array<MoveNextTask>>> getTaskTable()
	{
		return taskTable;
	}

	/**
	 * Удаление из обработки задачи движения.
	 *
	 * @param task удаляемая задача.
	 */
	public void removeTask(MoveNextTask task)
	{
		// получаем контейнер, в котром эта задача
		Array<MoveNextTask> container = task.getContainer();

		// если такой есть
		if(container != null)
		{
			// удаляем задачу из контейнера
			container.fastRemove(task);

			// зануляем контейнер
			task.setContainer(null);
		}
	}
}

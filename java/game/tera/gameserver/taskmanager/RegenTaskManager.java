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

/**
 * Менеджер для обработки регена персонажей.
 *
 * @author Ronn
 */
public final class RegenTaskManager
{
	private static final Logger log = Loggers.getLogger(RegenTaskManager.class);

	private static RegenTaskManager instance;

	public static RegenTaskManager getInstance()
	{
		if(instance == null)
			instance = new RegenTaskManager();

		return instance;
	}

	/** таблица регенируемых персонажей */
	private Table<Class<?>, Array<Character>> table;

	private RegenTaskManager()
	{
		this.table = Tables.newObjectTable();

		log.info("initializable.");
	}

	/**
	 * Добавление персонажа на обработку.
	 *
	 * @param character добалвяемый персонаж.
	 */
	public void addCharacter(Character character)
	{
		Array<Character> array = table.get(character.getClass());

		if(array == null)
		{
			synchronized(table)
			{
				array = table.get(character.getClass());

				if(array == null)
				{
					// создаем список
					array = Arrays.toConcurrentArray(Character.class);

					// создаем ссылку на список
					final Array<Character> characters = array;

					// создаем задание
					SafeTask task = new SafeTask()
					{
						@Override
						protected void runImpl()
						{
							characters.readLock();
							try
							{
								// получаем массив персонажей
								Character[] array = characters.array();

								// перебираем и выполняем реген персонажам
								for(int i = 0, length = characters.size(); i < length; i++)
									array[i].doRegen();
							}
							finally
							{
								characters.readUnlock();
							}
						}
					};

					// получаем менеджер исполнения
					ExecutorManager executor = ExecutorManager.getInstance();

					// добавляем на выполнение задание
					executor.scheduleGeneralAtFixedRate(task, 1000, 1000);
				}
			}
		}

		// вносим нового персонажа
		array.add(character);
	}

	/**
	 * Удаление персонажа из обработки.
	 *
	 * @param character удаляемый персонаж.
	 */
	public void removeCharacter(Character character)
	{
		// получаем список обрабатываемых персонажей
		Array<Character> array = table.get(character.getClass());

		// если список есть
		if(array != null)
			// удаляем персонаж оттуда
			array.fastRemove(character);
	}
}

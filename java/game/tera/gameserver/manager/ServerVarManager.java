package tera.gameserver.manager;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.Table;
import rlib.util.table.Tables;

/**
 * Менеджер серверных переменных.
 *
 * @author Ronn
 */
public final class ServerVarManager
{
	private static final Logger log = Loggers.getLogger(ServerVarManager.class);

	private static ServerVarManager instance;

	public static ServerVarManager getInstance()
	{
		if(instance == null)
			instance = new ServerVarManager();

		return instance;
	}

	/**
	 * Конвектор переменных в строку.
	 *
	 * @param value значение переменной.
	 * @return строковый эквивалент.
	 */
	private static String toString(Object value)
	{
		if(value == null)
			return null;

		return value.toString();
	}

	/** синзронизатор */
	private final Lock lock = Locks.newLock();
	/** таблица всех серверных переменных */
	private final Table<String, String> variables;

	/** кешированный вариант всех переменных */
	private final Table<String, Object> cache;

	/** список названий переменных */
	private final Array<String> varNames;

	/** ссылка на задание по обновлению переменных в БД */
	private final ScheduledFuture<SafeTask> saveTask;

	private ServerVarManager()
	{
		// создаем таблицу переменных
        variables = Tables.newObjectTable();

        // создаем таблицу кэшеированных данных
        cache = Tables.newObjectTable();

        // список имен переменных
        varNames = Arrays.toArray(String.class);

        // получаем менеджера БД
        DataBaseManager manager = DataBaseManager.getInstance();

        // загружаем с БД переменные
        manager.loadServerVars(variables);

        // создаем задание для обновления в БД переменных
        SafeTask task = new SafeTask()
        {
        	@Override
        	protected void runImpl()
        	{
        		lock.lock();
        		try
        		{
        			updateVars();
        		}
        		finally
        		{
        			lock.unlock();
        		}
        	}
        };

        // получаем исполнительного менеджера
        ExecutorManager executor = ExecutorManager.getInstance();

        // запускаем задание
        saveTask = executor.scheduleGeneralAtFixedRate(task, 300000, 300000);

        log.info("loaded " + variables.size() + " variables.");
	}

	/**
	 * Завершение менеджера.
	 */
	public void finish()
	{
		lock.lock();
		try
		{
			if(saveTask != null)
				saveTask.cancel(true);

			updateVars();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return таблица кеша.
	 */
	public Table<String, Object> getCache()
	{
		return cache;
	}

	/**
	 * @param name название переменной.
	 * @return значение переменной.
	 */
	public String getString(String name)
	{
		if(name == null)
			return null;

		lock.lock();
		try
		{
			// извлекаем переменную из кэша
			Object value = cache.get(name);

			// если в кеше ее нету
			if(value == null)
			{
				// извлекаем из таблицы
				value = variables.get(name);

				// если в таблицу оказалась
				if(value != null)
					// вносим в кеш
					cache.put(name, value);
			}

			// возвращаем найденное значение
			return value == null? null : (String) value;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @param name название переменной.
	 * @param def значение по умолчанию.
	 * @return значение переменной.
	 */
	public String getString(String name, String def)
	{
		if(name == null)
			return null;

		// получаем значение переменной
		String value = getString(name);

		// если переменной нету, возвращаем по умолчанию
		return value == null? def : value;
	}

	/**
	 * @return таблица переменных.
	 */
	public Table<String, String> getVariables()
	{
		return variables;
	}

	/**
	 * @return список названий переменных.
	 */
	public Array<String> getVarNames()
	{
		return varNames;
	}

	/**
	 * Удаление переменной с указаным названием.
	 *
	 * @param name название переменной.
	 */
	public void remove(String name)
	{
		if(name == null)
			return;

		lock.lock();
		try
		{
			// получаем значение в кеше
			Object valCache = cache.remove(name);

			// получаем значение в переменных
			String valVar = variables.remove(name);

			// если она хоть где-то была
			if(valCache != null || valVar != null)
			{
				// получаем менеджер по работе с БД
				DataBaseManager manager = DataBaseManager.getInstance();

				// удаляем из БД
				manager.removeServerVar(name);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @param name название переменной.
	 * @param value новое значение переменной.
	 */
	public void setString(String name, String value)
	{
		if(value == null || name == null)
			return;

		lock.lock();
		try
		{
			// вносим в кеш
			cache.put(name, value);

			// если в переменных отсутствует такая
			if(!variables.containsKey(name))
			{
				// вносим
				variables.put(name, value);

				// прлучаем менеджера БД
				DataBaseManager manager = DataBaseManager.getInstance();

				// вносим в БД
				manager.insertServerVar(name, value);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Обновление переменных.
	 */
	private void updateVars()
	{
		// получаем таблицу переменных
		Table<String, String> variables = getVariables();

		// получаем кеш
		Table<String, Object> cache = getCache();

		// получаем список имен переменных
		Array<String> varNames = getVarNames();

		// получаем менеджер БД
		DataBaseManager manager = DataBaseManager.getInstance();

		// получаем список названий
		variables.keyArray(varNames);

		// получаем массив
		String[] array = varNames.array();

		// перебираем названия переменных
		for(int i = 0, length = varNames.size(); i < length; i++)
		{
			String name = array[i];

			// получаем текущее значение
			String current = variables.get(name);

			// получаем последнее
			String last = toString(cache.get(name));

			// если оно не изменилось, пропускаем
			if(last == null || current.equals(last))
				continue;

			// обновляем в БД
			manager.updateServerVar(name, last);

			// обновляем в таблице
			variables.put(name, last);
		}
	}
}

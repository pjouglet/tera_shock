package tera.gameserver.manager;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.scripts.items.ItemExecutor;
import tera.gameserver.scripts.items.ItemExecutorType;

/**
 * Менеджер обработки активных итемов.
 *
 * @author Ronn
 */
public final class ItemExecutorManager
{
	private static final Logger log = Loggers.getLogger(ItemExecutorManager.class);

	private static ItemExecutorManager instance;

	public static ItemExecutorManager getInstance()
	{
		if(instance == null)
			instance = new ItemExecutorManager();

		return instance;
	}

	/** таблица обработчиков активных итемов */
	private Table<IntKey, ItemExecutor> executors;

	private ItemExecutorManager()
	{
		executors = Tables.newIntegerTable();

		int counter = 0;

		for(ItemExecutorType use : ItemExecutorType.values())
			register(use.newInstance());

		log.info("loaded " + counter + " executor items.");
	}

	/**
	 * Обработка активного итема.
	 *
	 * @param itemId ид шаблона итема.
	 * @param player игрок, который активировал итем.
	 * @return обработался ли итем.
	 */
	public final boolean execute(ItemInstance item, Player player)
	{
		// если итема нет, выходим
		if(item == null)
			return false;

		// пробуем получить обработчик для итема
		ItemExecutor executor = executors.get(item.getItemId());

		// если обработчика нет, выходим
		if(executor == null)
			return false;

		// выполняем обработку итема
		executor.execution(item, player);

		return true;
	}

	/**
	 * Добавление обработчика активного итема.
	 *
	 * @param executor обработчик итема.
	 */
	public final void register(ItemExecutor executor)
	{
		int ids[] = executor.getItemIds();

		for(int id : ids)
		{
			if(executors.containsKey(id))
				throw new IllegalArgumentException();

			executors.put(id, executor);
		}
	}

	/**
	 * @return кол-во видов обрабатываемых итемов.
	 */
	public final int size()
	{
		return executors.size();
	}
}

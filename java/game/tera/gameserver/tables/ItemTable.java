package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.document.DocumentItem;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.templates.ItemTemplate;

/**
 * Таблица шаблонов итемов.
 *
 * @author Ronn
 */
public final class ItemTable
{
	private static final Logger log = Loggers.getLogger(ItemTable.class);

	private static ItemTable instance;

	/**
	 * Создает массив итемов с указанным кол-вом и указаным итем ид.
	 *
	 * @param templateId ид шаблона итема.
	 * @param count кол-во итемов.
	 * @return итоговый массив итемов.
	 */
	public static final ItemInstance[] createItem(int templateId, int count)
	{
		// проверяем отношение итема к донату
		if(Arrays.contains(Config.WORLD_DONATE_ITEMS, templateId))
		{
			log.warning(new Exception("not create donate item for id " + templateId));
			return null;
		}

		if(count < 1)
			return new ItemInstance[0];

		// получаем таблицу итемов
		ItemTable table = getInstance();

		// получаем шаблон
		ItemTemplate template = table.getItem(templateId);

		// если шаблона нет
		if(template == null)
			// возавращаем пустой массив
			return new ItemInstance[0];

		// создаем массив итемов
		ItemInstance[] items = new ItemInstance[template.isStackable()? 1 : count];

		// заполняем экземплярами
		for(int i = 0; i < items.length; i++)
		{
			items[i] = template.newInstance();

			if(items[i] == null)
				return new ItemInstance[0];

			if(template.isStackable())
				items[i].setItemCount(count);
		}

		// возвращаем массив итемов
		return items;
	}

	/**
	 * Создает итем с указанным кол-вом и указаным итем ид.
	 *
	 * @param templateId ид шаблона итема.
	 * @param count кол-во итемов.
	 * @return итоговый массив итемов.
	 */
	public static final ItemInstance createItem(int templateId, long count)
	{
		// проверяем отношение итема к донату
		if(Arrays.contains(Config.WORLD_DONATE_ITEMS, templateId))
		{
			log.warning(new Exception("not create donate item for id " + templateId));
			return null;
		}

		if(count < 1)
			return null;

		// получаем таблицу итемов
		ItemTable table = getInstance();

		// получаем шаблон
		ItemTemplate template = table.getItem(templateId);

		// если шаблона нет, выходим
		if(template == null)
			return null;

		// создаем новый экземпляр итема
		ItemInstance item = template.newInstance();

		// если не получилось
		if(item == null)
			return null;

		// устанавливаем по возможностит нужное кол-во
		item.setItemCount(item.isStackable()? count : 1);

		// возвращаем
		return item;
	}

	public static ItemTable getInstance()
	{
		if(instance == null)
			instance = new ItemTable();

		return instance;
	}

	/**
	 * Извлекает ид шаблона из итема.
	 *
	 * @param item итем.
	 * @return итем ид итема.
	 */
	public static final int templateId(ItemInstance item)
	{
		if(item == null)
			return 0;

		return item.getItemId();
	}

	/** таблица всех темплейтв */
	private Table<IntKey, ItemTemplate> items;

	private ItemTable()
	{
		// созадем таблицу скилов
		items = Tables.newIntegerTable();

		// получаем файлы с итемами
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/items"));

		// перебираем файлы
		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			if(file.getName().startsWith("example"))
				continue;

			// перебираем отпарсенные шаблоны файла
			for(ItemTemplate item : new DocumentItem(file).parse())
			{
				// если стоимость продажи выше стоимости покупки
				if(item.getSellPrice() != 0 && item.getSellPrice() > item.getBuyPrice())
				{
					// уведомляем
					log.warning("found incorrect price for item " + item + " in file " + file);

					// зануляем стоимость продажи
					item.setSellPrice(0);
				}

				if(items.containsKey(item.getItemId()))
					log.warning("found duplicate item " + item);

				// вносим в таблицу
				items.put(item.getItemId(), item);
			}
		}

		log.info("loaded " + items.size() + " items.");
	}

	/**
	 * Получение шаблона темплейта.
	 *
	 * @param type тип шаблона.
	 * @param id ид шаблона итема.
	 * @return соответствующий шаблон.
	 */
	public final <T extends ItemTemplate> T getItem(Class<T> type, int id)
	{
		ItemTemplate item = items.get(id);

		if(item == null || !type.isInstance(item))
			return null;

		return type.cast(item);
	}

	/**
	 * Получение шаблона темплейта.
	 *
	 * @param id ид шаблона итема.
	 * @return соотвествующий шаблон.
	 */
	public final ItemTemplate getItem(int id)
	{
		return items.get(id);
	}

	/**
	 * Обновляет текуще итемы и добавляет новые.
	 */
	public synchronized void reload()
	{
		// создаем новую таблицу шаблонов
		Table<IntKey, ItemTemplate> newTemplates = Tables.newIntegerTable();

		// получаем файлы для парса
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/items"));

		// перебираем файлы
		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			if(file.getName().startsWith("example"))
				continue;

			// парсим файл
			for(ItemTemplate template : new DocumentItem(file).parse())
				// вносим в таблицу новые шаблоны
				newTemplates.put(template.getItemId(), template);
		}

		// перебираем новые шаблоны
		for(ItemTemplate template : newTemplates)
		{
			// поопускаем пустые
			if(template == null)
				continue;

			// получаем старый шаблон
			ItemTemplate old = items.get(template.getItemId());

			// если его нет
			if(old == null)
			{
				// вносим новый
				items.put(template.getItemId(), template);
				continue;
			}

			// обновляем старый
			old.reload(template);
		}

		log.info("reloaded items.");
	}
}

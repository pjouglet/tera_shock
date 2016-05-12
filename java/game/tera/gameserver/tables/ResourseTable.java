package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.Objects;
import rlib.util.array.Array;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.document.DocumentResourse;
import tera.gameserver.model.drop.ResourseDrop;
import tera.gameserver.templates.ResourseTemplate;

/**
 * Таблица шаблонов ресурсов.
 *
 * @author Ronn
 * @created 05.03.2012
 */
public final class ResourseTable
{
	private static final Logger log = Loggers.getLogger(ResourseTable.class);

	private static ResourseTable instance;

	public static ResourseTable getInstance()
	{
		if(instance == null)
			instance = new ResourseTable();

		return instance;
	}

	/** таблица шаблонов */
	private Table<IntKey, ResourseTemplate> templates;

	private ResourseTable()
	{
		// созадем таблицу
		templates = Tables.newIntegerTable();

		// получаем все нужные хмлки
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/resourses"));

		// получаем таблицу дропа
		DropTable dropTable = DropTable.getInstance();

		// перебираем файлики
		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			// парсим файл
			Array<ResourseTemplate> parsed = new DocumentResourse(file).parse();

			// перебираем отпарсенные шаблоны
			for(ResourseTemplate template : parsed)
			{
				// получаем дроп для этого ресурса
				ResourseDrop drop = dropTable.getResourseDrop(template.getId());

				// заносим дроп
				template.setDrop(drop);

				// заносим в таблицу
				templates.put(template.getId(), template);
			}
		}

		log.info("loaded  " + templates.size() + " resourses.");
	}

	/**
	 * @return шаблон ресурса.
	 */
	public ResourseTemplate getTemplate(int templateId)
	{
		return templates.get(templateId);
	}

	/**
	 * Перезагрузка шаблонов ресурсов.
	 */
	public synchronized void reload()
	{
		// получаем все нужные хмлки
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/resourses"));

		// получаем таблицу дропа
		DropTable dropTable = DropTable.getInstance();

		// перебираем файлики
		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			// парсим файл
			Array<ResourseTemplate> parsed = new DocumentResourse(file).parse();

			// перебираем отпарсенные шаблоны
			for(ResourseTemplate template : parsed)
			{
				// получаем дроп для этого ресурса
				ResourseDrop drop = dropTable.getResourseDrop(template.getId());

				// заносим дроп
				template.setDrop(drop);

				// получаем прошлый вариант темплейта
				ResourseTemplate old = templates.get(template.getId());

				// если уже такой есть
				if(old != null)
					// обновляем
					Objects.reload(old, template);
				else
					// заносим в таблицу
					templates.put(template.getId(), template);
			}
		}

		log.info("resourses reloaded.");
	}
}
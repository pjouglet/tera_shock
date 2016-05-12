package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.array.Array;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.Config;
import tera.gameserver.document.DocumentMinion;
import tera.gameserver.model.MinionData;
import tera.gameserver.templates.NpcTemplate;

/**
 * Таблица данных о минионах.
 *
 * @author Ronn
 * @created 14.03.2012
 */
public final class MinionTable
{
	private static final Logger log = Loggers.getLogger(MinionTable.class);

	private static MinionTable instance;

	public static MinionTable getInstance()
	{
		if(instance == null)
			instance = new MinionTable();

		return instance;
	}

	/** таблица минионов */
	private Table<IntKey, Table<IntKey, MinionData>> minions;

	private MinionTable()
	{
		minions = Tables.newIntegerTable();

		// получаем таблицу НПС
		NpcTable npcTable = NpcTable.getInstance();

		int counter = 0;

		// получаем файлы для парса
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/minions"));

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
			Array<MinionData> parsed = new DocumentMinion(file).parse();

			// перебираем результат
			for(MinionData minion : parsed)
			{
				// получаем подтаблицу
				Table<IntKey, MinionData> table = minions.get(minion.getLeaderId());

				// если ее нету
				if(table == null)
				{
					// создаем новую
					table = Tables.newIntegerTable();

					// вносим в основную таблицу
					minions.put(minion.getLeaderId(), table);
				}

				// вносим в таблицу информацию о минионах
				table.put(minion.getType(), minion);

				// квеличиваем счетчик
				counter += minion.size();

				// получаем темплейт лидера
				NpcTemplate template = npcTable.getTemplate(minion.getLeaderId(), minion.getType());

				if(template == null)
				{
					log.warning("not found npc template for " + minion);
					continue;
				}

				// применяем минионов
				template.setMinions(minion);
			}
		}

		log.info("loaded " + counter + " minions for " + minions.size() + " npcs.");
	}
}

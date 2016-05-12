package tera.gameserver.tables;

import java.io.File;
import java.util.Set;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.array.Array;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.document.DocumentDrop;
import tera.gameserver.model.drop.Drop;
import tera.gameserver.model.drop.NpcDrop;
import tera.gameserver.model.drop.ResourseDrop;

/**
 * Таблица дропа с нпс.
 *
 * @author Ronn
 */
public final class DropTable
{
	private static final Logger log = Loggers.getLogger(DropTable.class);

	private static DropTable instance;

	public static DropTable getInstance()
	{
		if(instance == null)
			instance = new DropTable();

		return instance;
	}

	/** таблица дропа нпс */
	private Table<IntKey, Table<IntKey, NpcDrop>> npcDrop;
	/** таблица дропа ресурсов */
	private Table<IntKey, ResourseDrop> resourseDrop;

	private DropTable()
	{
		npcDrop = Tables.newIntegerTable();
		resourseDrop = Tables.newIntegerTable();

		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/drops"));

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
			Array<Drop> parsed = new DocumentDrop(file).parse();

			// перебираем дроп
			for(Drop drop : parsed)
			{
				// если это дроп ресурсов
				if(drop.getTemplateType() < 0)
					// добавляем в таблицу ресурсов
					resourseDrop.put(drop.getTemplateId(), (ResourseDrop) drop);
				else
				{
					// получаем под-таблицу дропа нпс
					Table<IntKey, NpcDrop> table = npcDrop.get(drop.getTemplateId());

					// если ее нет
					if(table == null)
					{
						// создаем новую
						table = Tables.newIntegerTable();
						// вставляем в таблицу дропа с НП
						npcDrop.put(drop.getTemplateId(), table);
					}

					// вставляем дроп
					table.put(drop.getTemplateType(), (NpcDrop) drop);
				}

			}
		}

		int counter = 0;

		for(Table<IntKey, NpcDrop> table : npcDrop)
			counter += table.size();

		Set<Integer> filter = DocumentDrop.filter;

		if(!filter.isEmpty())
			log.warning("not found items " + filter);

		log.info("load drop for " + counter + " npcs and " + resourseDrop.size() + " for resourse.");
	}

	/**
	 * Получение дропа для НПС.
	 *
	 * @param templateId ид темплейта НПС.
	 * @param templateType тип темплейта НПС.
	 * @return дроп для этого темплейта.
	 */
	public NpcDrop getNpcDrop(int templateId, int templateType)
	{
		// получаем подтаблицу дропа нпс
		Table<IntKey, NpcDrop> table = npcDrop.get(templateId);

		// смотрим дроп дял конкретного нпс
		return table == null? null : table.get(templateType);
	}

	/**
	 * Получние дропа для ресурса указанного ид.
	 *
	 * @param templateId ид темплейта ресурса.
	 * @return дроп для этого темплейта.
	 */
	public ResourseDrop getResourseDrop(int templateId)
	{
		return resourseDrop.get(templateId);
	}
}

package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.array.Array;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.document.DocumentNpcConfigAI;
import tera.gameserver.model.ai.npc.ConfigAI;

/**
 * Таблица конфигов АИ.
 *
 * @author Ronn
 */
public final class ConfigAITable
{
	private static final Logger log = Loggers.getLogger(ConfigAITable.class);

	private static ConfigAITable instance;

	public static ConfigAITable getInstance()
	{
		if(instance == null)
			instance = new ConfigAITable();

		return instance;
	}

	/** таблица конфигов */
	private Table<String, ConfigAI> configs;

	private ConfigAITable()
	{
		configs = Tables.newObjectTable();

		for(File file : Files.getFiles(new File(Config.SERVER_DIR + "/data/config_ai"), "xml"))
		{
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file);
				continue;
			}

			// парсим файл
			Array<ConfigAI> result = new DocumentNpcConfigAI(file).parse();

			// перебираем результат парса
			for(ConfigAI config : result)
			{
				if(configs.containsKey(config.getName()))
				{
					log.warning(new Exception("found duplicate config " + config.getName()));
					continue;
				}

				// вносим конфиг АИ в таблицу
				configs.put(config.getName(), config);
			}
		}


		log.info("loaded " + configs.size() + " npc ai configs.");
	}

	/**
	 * Получение конфига АИ по его имени.
	 *
	 * @param name название конфига.
	 * @return конфиг.
	 */
	public ConfigAI getConfig(String name)
	{
		return configs.get(name);
	}
}

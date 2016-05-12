package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.array.Array;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.document.DocumentMessagePackage;
import tera.gameserver.model.ai.npc.MessagePackage;

/**
 * Таблица пакетов сообщений для АИ НПС.
 *
 * @author Ronn
 */
public final class MessagePackageTable
{
	private static final Logger log = Loggers.getLogger(MessagePackageTable.class);

	private static MessagePackageTable instance;

	public static MessagePackageTable getInstance()
	{
		if(instance == null)
			instance = new MessagePackageTable();

		return instance;
	}

	/** таблицв конфигов */
	private Table<String, MessagePackage> table;

	private MessagePackageTable()
	{
		table = Tables.newObjectTable();

		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/messages_ai"), "xml");
		
		for(File file : files)
		{
			// парсим хмлку пакетов сообщений
			Array<MessagePackage> result = new DocumentMessagePackage(file).parse();

			for(MessagePackage pckg : result)
			{
				if(table.containsKey(pckg.getName()))
				{
					log.warning(new Exception("found duplicate message package " + pckg.getName()));
					continue;
				}

				table.put(pckg.getName(), pckg);
			}
		}

		log.info("loaded " + table.size() + " message packages.");
	}

	/**
	 * @return пакет сообщений указанного имени.
	 */
	public MessagePackage getPackage(String name)
	{
		return table.get(name);
	}
}

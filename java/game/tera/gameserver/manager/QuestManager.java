package tera.gameserver.manager;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.array.Array;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.document.DocumentQuest;
import tera.gameserver.model.quests.Quest;

/**
 * Менеджер обработки и работы с квестами.
 *
 * @author Ronn
 */
public final class QuestManager
{
	/** логгер квестов */
	protected static final Logger log = Loggers.getLogger(QuestManager.class);

	private static QuestManager instance;

	public static QuestManager getInstance()
	{
		if(instance == null)
			instance = new QuestManager();

		return instance;
	}

	/** список всех квестов */
	private final Table<IntKey, Quest> allquests;

	private QuestManager()
	{
		allquests = Tables.newIntegerTable();

		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/quests"));

		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
				continue;

			Array<Quest> quests = new DocumentQuest(file).parse();

			for(Quest quest : quests)
				allquests.put(quest.getId(), quest);
		}

		log.info("loaded " + allquests.size() + " quests.");
	}

	/**
	 * @param id ид квеста.
	 * @return квест.
	 */
	public Quest getQuest(int id)
	{
		return allquests.get(id);
	}

	/**
	 * Перезагрузка квестов.
	 */
	public void reload()
	{
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/quests"));

		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
				continue;

			Array<Quest> quests = new DocumentQuest(file).parse();

			for(Quest quest : quests)
			{
				Quest old = allquests.get(quest.getId());

				if(old == null)
					allquests.put(quest.getId(), quest);
				else
					old.reload(quest);
			}
		}

		log.info("reloaded.");
	}
}

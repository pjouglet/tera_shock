package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.Config;
import tera.gameserver.document.DocumentNpcAppearance;
import tera.gameserver.model.npc.playable.NpcAppearance;

/**
 * Таблица готовых внешностей НПС.
 *
 * @author Ronn
 */
public final class NpcAppearanceTable
{
	private static final Logger log = Loggers.getLogger(NpcAppearanceTable.class);

	private static NpcAppearanceTable instance;

	public static NpcAppearanceTable getInstance()
	{
		if(instance == null)
			instance = new NpcAppearanceTable();

		return instance;
	}

	/** таблица внешностей */
	private Table<String, NpcAppearance> table;

	private NpcAppearanceTable()
	{
		table = Tables.newObjectTable();

		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/npc_appearance"), "xml");

		for(File file : files)
		{
			DocumentNpcAppearance document = new DocumentNpcAppearance(file, table);

			document.parse();
		}

		log.info("initialized. Loaded " + table.size() + " appearances.");
	}

	/**
	 * @param name название внешности НПС.
	 * @return внешность НПс.
	 */
	public NpcAppearance getAppearance(String name)
	{
		return table.get(name);
	}
}

package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.document.DocumentTown;
import tera.gameserver.model.TownInfo;

/**
 * Таблица городов в мире теры.
 *
 * @author Ronn
 */
public final class TownTable
{
	private static final Logger log = Loggers.getLogger(TownTable.class);

	private static TownTable instance;

	public static TownTable getInstance()
	{
		if(instance == null)
			instance = new TownTable();

		return instance;
	}

	/** таблица городов по ид */
	private final Table<IntKey, TownInfo> townIds;
	/** таблица городов по названию */
	private final Table<String, TownInfo> townNames;

	private TownTable()
	{
		townIds = Tables.newIntegerTable();
		townNames = Tables.newObjectTable();

		Array<TownInfo> towns = new DocumentTown(new File(Config.SERVER_DIR + "/data/towns.xml")).parse();

		for(TownInfo town : towns)
		{
			townIds.put(town.getId(), town);
			townNames.put(town.getName(), town);
		}

		log.info("initialized.");
	}

	/**
	 * Получение города по его ид.
	 *
	 * @param id ид города.
	 * @return город.
	 */
	public TownInfo getTown(int id)
	{
		return townIds.get(id);
	}

	/**
	 * Получение города по его имени.
	 *
	 * @param name имя города.
	 * @return город.
	 */
	public TownInfo getTown(String name)
	{
		return townNames.get(name);
	}
}

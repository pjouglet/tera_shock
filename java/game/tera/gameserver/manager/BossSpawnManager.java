package tera.gameserver.manager;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import rlib.util.wraps.Wrap;
import rlib.util.wraps.Wraps;

import tera.gameserver.model.npc.spawn.BossSpawn;
import tera.gameserver.model.npc.spawn.Spawn;
import tera.gameserver.templates.NpcTemplate;

/**
 * Менеджер за контролем респавна РБ.
 *
 * @author Ronn
 */
public final class BossSpawnManager
{
	private static final Logger log = Loggers.getLogger(BossSpawnManager.class);

	private static BossSpawnManager instance;

	public static BossSpawnManager getInstance()
	{
		if(instance == null)
			instance = new BossSpawnManager();

		return instance;
	}

	/** таблица спавнов РБ */
	private final Table<IntKey, Table<IntKey, Wrap>> spawnTable;

	/** список спавнов с боссами */
	private final Array<Spawn> spawns;

	private BossSpawnManager()
	{
		// создаем таблицу спавна
		spawnTable = Tables.newIntegerTable();

		// получаем менеджера БД
		DataBaseManager manager = DataBaseManager.getInstance();

		// загружаем спавны с БД
		manager.loadBossSpawns(spawnTable);

		// содаем список спавнов боссов
		spawns = Arrays.toArray(BossSpawn.class);

		int count = 0;

		for(Table<IntKey, Wrap> table : spawnTable)
			count += table.size();

		log.info("loaded " + count + " boss spawns.");
	}
	/**
	 * Внесение спавна босса в список спавнов.
	 *
	 * @param spawn спавн босса.
	 * @return успешно ли внесен.
	 */
	public synchronized final boolean addSpawn(Spawn spawn)
	{
		if(spawns.contains(spawn))
			return false;

		spawns.add(spawn);

		return true;
	}

	/**
	 * Получение времени спавна босса.
	 *
	 * @param template темплейт босса.
	 * @return когда он отспавнится.
	 */
	public synchronized final long getSpawn(NpcTemplate template)
	{
		// получаем таблицу по типу
		Table<IntKey, Wrap> table = spawnTable.get(template.getTemplateId());

		// если такой нет, спавна нет
		if(table == null)
			return -1;

		// получаем время респавна
		Wrap wrap = table.get(template.getTemplateType());

		// если оно есть, отдаем иначе спавна нет
		return wrap == null? -1 : wrap.getLong();
	}

	/**
	 * Обновление спавна босса в таблице.
	 *
	 * @param template темплейт босса.
	 * @param spawn время респавна.
	 */
	public synchronized final void updateSpawn(NpcTemplate template, long spawn)
	{
		// получаем таблицу спавнов по типу
		Table<IntKey, Wrap> table = spawnTable.get(template.getTemplateId());

		// получаем менеджер работы с БД
		DataBaseManager manager = DataBaseManager.getInstance();

		// если ее нету
		if(table == null)
		{
			// создаем
			table = Tables.newIntegerTable();

			// вносим в таблицу спавнов
			spawnTable.put(template.getTemplateId(), table);

			// вносим время респавна этого босса
			table.put(template.getTemplateType(), Wraps.newLongWrap(spawn, true));

			// вносим в БД
			manager.insertBossSpawns(template, spawn);
		}
		else
		{
			// получаем время респавна
			Wrap wrap = table.get(template.getTemplateType());

			// если такое есть
			if(wrap != null)
			{
				// то обновляем
				wrap.setLong(spawn);

				// обновляем в БД
				manager.updateBossSpawns(template, spawn);
			}
			// иначе
			else
			{
				// вносим время респавна в таблицу
				table.put(template.getTemplateType(), Wraps.newLongWrap(spawn, true));

				// вносим в БД
				manager.insertBossSpawns(template, spawn);
			}
		}
	}
}

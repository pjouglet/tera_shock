package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.Config;
import tera.gameserver.document.DocumentNpcSpawn;
import tera.gameserver.document.DocumentResourseSpawn;
import tera.gameserver.model.npc.spawn.NpcSpawn;
import tera.gameserver.model.npc.spawn.Spawn;
import tera.gameserver.model.resourse.ResourseSpawn;
import tera.util.Location;

/**
 * Таблица спавнов НПС и ресурсов.
 *
 * @author Ronn
 */
public final class SpawnTable
{
	private static final Logger log = Loggers.getLogger(SpawnTable.class);

	private static SpawnTable instance;

	public static SpawnTable getInstance()
	{
		if(instance == null)
			instance = new SpawnTable();

		return instance;
	}

	/** таблица спавнов нпс */
	private Table<IntKey, Table<IntKey, Array<Spawn>>> npcSpawnTable;
	/** таблица спавнов ресурсов */
	private Table<IntKey, Array<ResourseSpawn>> resourseSpawnTable;

	private SpawnTable()
	{
		npcSpawnTable = Tables.newIntegerTable();
		resourseSpawnTable = Tables.newIntegerTable();

		int counterNpc = 0;
		int counterResourses = 0;

		// получаем все нужные нам хмлки
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/spawns"));

		// перебираем
		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			if(file.getName().startsWith("example"))
				continue;

			// парсим спавны НПС
			Array<Spawn> spawnsNpc = new DocumentNpcSpawn(file).parse();

			// плюсуем к счетчику
			counterNpc += spawnsNpc.size();

			// перебираем спавны
			for(Spawn spawn : spawnsNpc)
			{
				// получаем под-таблицу спавнов нпс
				Table<IntKey, Array<Spawn>> table = npcSpawnTable.get(spawn.getTemplateId());

				// если ее нет
				if(table == null)
				{
					// создаем новую
					table = Tables.newIntegerTable();
					// вставялем
					npcSpawnTable.put(spawn.getTemplateId(), table);
				}

				// получаем список спавнов этого НПС
				Array<Spawn> array = table.get(spawn.getTemplateType());

				// если списка нет
				if(array == null)
				{
					// создаем новый
					array = Arrays.toArray(Spawn.class);

					// вставляем
					table.put(spawn.getTemplateType(), array);
				}

				// добавляем спавн
				array.add(spawn);
			}

			// парсим спавны ресурсов
			Array<ResourseSpawn> spawnsResourses = new DocumentResourseSpawn(file).parse();

			// плюсуем к счетчику
			counterResourses += spawnsResourses.size();

			// перебираем спавны ресурсов
			for(ResourseSpawn spawn : spawnsResourses)
			{
				// получаем список спавнов этого ресурса
				Array<ResourseSpawn> spawns = resourseSpawnTable.get(spawn.getTemplateId());

				// если списка нет
				if(spawns == null)
				{
					// создаем новый
					spawns = Arrays.toArray(ResourseSpawn.class);

					// вставляем в таблицу
					resourseSpawnTable.put(spawn.getTemplateId(), spawns);
				}

				// добавляем в список
				spawns.add(spawn);
			}
		}

		// сжимаем все списки спавнов нпс
		for(Table<IntKey, Array<Spawn>> table : npcSpawnTable)
			for(Array<Spawn> spawns : table)
				spawns.trimToSize();

		for(Array<ResourseSpawn> spawns : resourseSpawnTable)
			spawns.trimToSize();

		startSpawns();

		log.info("loaded " + counterNpc + " spawns for " + npcSpawnTable.size() + " npcs and " + counterResourses + " spawns for " + resourseSpawnTable.size() + " resourses.");
	}

	/**
	 * Получение точки спавна нужного НПС.
	 *
	 * @param templateId ид шаблона НПС.
	 * @param templateType тип шаблона НПС.
	 * @return точка спавна.
	 */
	public Location getNpcSpawnLoc(int templateId, int templateType)
	{
		// получаем подтаблицу спавнов
		Table<IntKey, Array<Spawn>> table = npcSpawnTable.get(templateId);

		// если ее нет, выходим
		if(table == null)
			return null;

		// получаем список спавнов нужных НПС
		Array<Spawn> spawns = table.get(templateType);

		// если таких нет, выходим
		if(spawns == null || spawns.isEmpty())
			return null;

		// получаем первый спавн
		Spawn spawn = spawns.first();

		// если это актуальный спавн
		if(spawn instanceof NpcSpawn)
		{
			// кастим его
			NpcSpawn npcSpawn = (NpcSpawn) spawn;

			// извлекаем точку спавна
			return npcSpawn.getLocation();
		}

		return null;
	}

	/**
	 * Перезагрузка таблицы спавнов.
	 */
	public synchronized void reload()
	{
		stopSpawns();

		npcSpawnTable.clear();
		resourseSpawnTable.clear();

		// получаем все нужные нам хмлки
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/spawns"));

		// перебираем
		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			if(file.getName().startsWith("example"))
				continue;

			// парсим спавны НПС
			Array<Spawn> spawnsNpc = new DocumentNpcSpawn(file).parse();

			// перебираем спавны
			for(Spawn spawn : spawnsNpc)
			{
				// получаем под-таблицу спавнов нпс
				Table<IntKey, Array<Spawn>> table = npcSpawnTable.get(spawn.getTemplateId());

				// если ее нет
				if(table == null)
				{
					// создаем новую
					table = Tables.newIntegerTable();
					// вставялем
					npcSpawnTable.put(spawn.getTemplateId(), table);
				}

				// получаем список спавнов этого НПС
				Array<Spawn> array = table.get(spawn.getTemplateType());

				// если списка нет
				if(array == null)
				{
					// создаем новый
					array = Arrays.toArray(Spawn.class);

					// вставляем
					table.put(spawn.getTemplateType(), array);
				}

				// добавляем спавн
				array.add(spawn);
			}

			// парсим спавны ресурсов
			Array<ResourseSpawn> spawnsResourses = new DocumentResourseSpawn(file).parse();

			// перебираем спавны ресурсов
			for(ResourseSpawn spawn : spawnsResourses)
			{
				// получаем список спавнов этого ресурса
				Array<ResourseSpawn> spawns = resourseSpawnTable.get(spawn.getTemplateId());

				// если списка нет
				if(spawns == null)
				{
					// создаем новый
					spawns = Arrays.toArray(ResourseSpawn.class);

					// вставляем в таблицу
					resourseSpawnTable.put(spawn.getTemplateId(), spawns);
				}

				// добавляем в список
				spawns.add(spawn);
			}
		}

		// сжимаем все списки спавнов нпс
		for(Table<IntKey, Array<Spawn>> table : npcSpawnTable)
			for(Array<Spawn> spawns : table)
				spawns.trimToSize();

		for(Array<ResourseSpawn> spawns : resourseSpawnTable)
			spawns.trimToSize();

		startSpawns();

		log.info("reloaded.");
	}

	/**
	 * Запуск спавна всех нпс.
	 */
	public void startSpawns()
	{
		for(Table<IntKey, Array<Spawn>> table : npcSpawnTable)
			for(Array<Spawn> spawns : table)
				for(Spawn spawn : spawns)
					spawn.start();

		for(Array<ResourseSpawn> spawns : resourseSpawnTable)
			for(ResourseSpawn spawn : spawns)
				spawn.start();
	}

	/**
	 * Остановка и деспавн всех нпс.
	 */
	public void stopSpawns()
	{
		for(Table<IntKey, Array<Spawn>> table : npcSpawnTable)
			for(Array<Spawn> spawns : table)
				for(Spawn spawn : spawns)
					spawn.stop();

		for(Array<ResourseSpawn> spawns : resourseSpawnTable)
			for(ResourseSpawn spawn : spawns)
				spawn.stop();
	}
}
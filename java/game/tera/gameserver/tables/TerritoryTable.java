package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.Config;
import tera.gameserver.document.DocumentTerritory;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.territory.Territory;

/**
 * Таблица территорий.
 *
 * @author Ronn
 */
public final class TerritoryTable
{
	private static final Logger log = Loggers.getLogger(TerritoryTable.class);

	private static TerritoryTable instance;

	public static TerritoryTable getInstance()
	{
		if(instance == null)
			instance = new TerritoryTable();

		return instance;
	}

	/** таблица территорий */
	private final Table<IntKey, Territory> tableId;
	/** таблица территорий */
	private final Table<String, Territory> tableName;

	private TerritoryTable()
	{
		tableId = Tables.newIntegerTable();
		tableName = Tables.newObjectTable();

		// создаем промежуточную карту регион-список территорий региона
		Table<WorldRegion, Array<Territory>> territories = Tables.newObjectTable();

		// создаем промежуточный массив регионов
		Array<WorldRegion> regions = Arrays.toArray(WorldRegion.class);

		// получаем список всех отпарсенных территорий
		Array<Territory> parsed = new DocumentTerritory(new File(Config.SERVER_DIR + "/data/territories.xml")).parse();

		// перебираем территории
		for(Territory territory : parsed)
		{
			int minimumX = territory.getMinimumX() / World.REGION_WIDTH + World.OFFSET_X;
			int maximumX = territory.getMaximumX() / World.REGION_WIDTH + World.OFFSET_X;
			int minimumY = territory.getMinimumY() / World.REGION_WIDTH + World.OFFSET_Y;
			int maximumY = territory.getMaximumY() / World.REGION_WIDTH + World.OFFSET_Y;
			int minimumZ = territory.getMinimumZ() / World.REGION_HEIGHT + World.OFFSET_Z;
			int maximumZ = territory.getMaximumZ() / World.REGION_HEIGHT + World.OFFSET_Z;

			for(int x = minimumX; x <= maximumX; x++)
				for(int y = minimumY; y <= maximumY; y++)
					for(int z = minimumZ; z <= maximumZ; z++)
					{
						// получаем регион, к которому относится территория
						WorldRegion region = World.region(territory.getContinentId(), x, y, z);

						// получаем список территори этого региона из промежуточной карты
						Array<Territory> array = territories.get(region);

						// если списка нетц
						if(array == null)
						{
							// создаем его
							array = Arrays.toArray(Territory.class);
							// добавляем в промежуточную карту
							territories.put(region, array);
						}

						// если региона нет в промежуточном списке регионов
						if(!regions.contains(region))
							// добавляем его
							regions.add(region);

						// если территория уже есть в списке у региона, пропускаем
						if(array.contains(territory))
							continue;

						// добавляем территорию
						array.add(territory);

						// получаем текущий список связаных регионов
						WorldRegion[] regs = territory.getRegions();

						// если это новый регион
						if(regs == null || !Arrays.contains(regs, region))
						{
							// вносим в список
							regs = Arrays.addToArray(regs, region, WorldRegion.class);
							// обновляем список регионов в территории
							territory.setRegions(regs);
						}
					}

			if(tableId.containsKey(territory.getId()))
				log.warning("found duplicate territory " + territory.getId());

			if(tableName.containsKey(territory.getName()))
				log.warning("found duplicate territory " + territory.getName());

			// добавляем территорию в основую таблицу всех территорий
			tableId.put(territory.getId(), territory);

			// добавляем территорию в таблицу по имени
			tableName.put(territory.getName(), territory);
		}

		// перебираем список всех регионов с территориями
		for(WorldRegion region : regions)
		{
			// получаем список их территорий из промежуточной таблици
			Array<Territory> array = territories.get(region);

			// если список пуст, пропускаем
			if(array.isEmpty())
				continue;

			// сжимаем список
			array.trimToSize();

			// применяем список в регион
			region.setTerritories(array.array());
		}

		log.info("loaded " + parsed.size() + " territories for " + regions.size() + " regions.");
	}
	/**
	 * @param id ид территории.
	 * @return территория.
	 */
	public Territory getTerritory(int id)
	{
		return tableId.get(id);
	}

	/**
	 * @param name название территории.
	 * @return территория.
	 */
	public Territory getTerritory(String name)
	{
		return tableName.get(name);
	}

	/**
	 * Обработка входа в мир объекта.
	 *
	 * @param object вошедший в мир объект.
	 * @return вошел ли объект в какие-нибудь территории.
	 */
	public boolean onEnterWorld(TObject object)
	{
		// получаем текущий регион объекта
		WorldRegion region = object.getCurrentRegion();

		// если региона нет, выходим
		if(region == null)
			return false;

		// получеам территории региона
		Territory[] terrs = region.getTerritories();

		// если их нету, выходим
		if(terrs == null || terrs.length == 0)
			return false;

		// получаем текущие территории объекта
		Array<Territory> territories = object.getTerritories();

		// перебираем территории региона
		for(int i = 0, length = terrs.length; i < length; i++)
		{
			Territory territory = terrs[i];

			// если в территорию объект входит
			if(territory.contains(object.getX(), object.getY(), object.getZ()))
			{
				// и территория уже есть в его списке, пропускаем
				if(territories.contains(territory))
					continue;

				// добавляем в список его территорий
				territories.add(territory);
			}
		}

		// если не в какие территории не входит, выходим
		if(territories.isEmpty())
			return true;

		// получаем весь список итоговый территорий
		Territory[] result = territories.array();

		// обрабатываем вход в каждую
		for(int i = 0, length = territories.size(); i < length; i++)
			result[i].onEnter(object);

		return true;
	}

	/**
	 * Обработка выхода из мира объекта.
	 *
	 * @param object вышедший из мира объект.
	 * @return был ли произведен выход хоть с одной территории.
	 */
	public boolean onExitWorld(TObject object)
	{
		// получаем текущий список территорий объекта
		Array<Territory> territories = object.getTerritories();

		// если их нету, выходим
		if(territories.isEmpty())
			return false;

		territories.readLock();
		try
		{
			Territory[] result = territories.array();

			// перебираем их
			for(int i = 0, length = territories.size(); i < length; i++)
				// обрабатываем выход из них
				result[i].onExit(object);
		}
		finally
		{
			territories.readUnlock();
		}

		// очищаем список
		territories.clear();

		return true;
	}
}

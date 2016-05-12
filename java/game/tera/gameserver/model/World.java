package tera.gameserver.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.util.Strings;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.CharSay;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.util.Location;

/**
 * Модель мира Tera-Online.
 *
 * @author Ronn
 */
public abstract class World
{
	/** Параметры карты */
	public static final int MAP_MIN_X = -294912; // Координаты от фанаря, нужно мерять в игре!!!
	public static final int MAP_MAX_X = 229375; // Координаты от фанаря, нужно мерять в игре!!!
	public static final int MAP_MIN_Y = -229376; // Координаты от фанаря, нужно мерять в игре!!!
	public static final int MAP_MAX_Y = 294911; // Координаты от фанаря, нужно мерять в игре!!!
	public static final int MAP_MIN_Z = -32768; // Координаты от фанаря, нужно мерять в игре!!!
	public static final int MAP_MAX_Z = 32767; // Координаты от фанаря, нужно мерять в игре!!!

	public static final int WORLD_SIZE_X = (MAP_MAX_X - MAP_MIN_X + 1) / 32768;
	public static final int WORLD_SIZE_Y = (MAP_MAX_Y - MAP_MIN_Y + 1) / 32768;

	/** размер регионов */
	public static final int REGION_WIDTH = Config.WORLD_WIDTH_REGION;
	public static final int REGION_HEIGHT = Config.WORLD_HEIGHT_REGION;

	/** рассчет смещения */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X / REGION_WIDTH);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y / REGION_WIDTH);
	public static final int OFFSET_Z = Math.abs(MAP_MIN_Z / REGION_HEIGHT);

	/** Размерность массива регионов */
	private static final int REGIONS_X = MAP_MAX_X / REGION_WIDTH + OFFSET_X;
	private static final int REGIONS_Y = MAP_MAX_Y / REGION_WIDTH + OFFSET_Y;
	private static final int REGIONS_Z = MAP_MAX_Z / REGION_HEIGHT + OFFSET_Z;

	/** массив регионов */
	private static final WorldRegion[][][][] worldRegions = new WorldRegion[Config.WORLD_CONTINENT_COUNT][REGIONS_X + 1][REGIONS_Y + 1][REGIONS_Z + 1];

	/** блокировщик */
	private static final Lock lock = Locks.newLock();

	/** список активных регионов */
	private static final Array<WorldRegion> activeRegions = Arrays.toConcurrentArray(WorldRegion.class);

	/** список онлаин игроков по никам */
	private static final Map<String, Player> playerNames = new HashMap<String, Player>();
	/** просто список онлаин игроков */
	private static final Array<Player> players = Arrays.toConcurrentArray(Player.class);
	/** таблица игроков */
	private static final Table<IntKey, Player> playerTable = Tables.newIntegerTable();

	/** счетчики */
	private static volatile long droppedItems;
	private static volatile long spawnedNpcs;
	private static volatile long killedNpcs;
	private static volatile long killedPlayers;

	/**
	 * @param region активировшийся регион.
	 */
	public static void addActiveRegion(WorldRegion region)
	{
		activeRegions.add(region);
	}

	/**
	 * Добавление к счетчику.
	 */
	public static final void addDroppedItems()
	{
		droppedItems += 1;
	}

	/**
	 * Добавление к счетчику.
	 */
	public static final void addKilledNpc()
	{
		killedNpcs += 1;
	}

	/**
	 * Добавление к счетчику.
	 */
	public static final void addKilledPlayers()
	{
		killedPlayers += 1;
	}

	/**
	 * Добавляем нового игрока в список онлаин.
	 *
	 * @param player вошедший игрок.
	 */
	public static void addNewPlayer(Player player)
	{
		players.writeLock();
		try
		{
			// получаем массив текущих онлаин игроков
			Player[] array = players.array();

			// перебираем их
			for(int i = 0, length = players.size(); i < length; i++)
			{
				// получаем игрока
				Player target = array[i];

				// получаем его список друзей
				FriendList friendList = target.getFriendList();

				// если пуст, пропускаем
				if(friendList.size() < 1)
					continue;

				// уведомляем о входе в игру этого игрока
				friendList.onEnterGame(player);
			}

			playerNames.put(player.getName(), player);
			players.add(player);
			playerTable.put(player.getObjectId(), player);
		}
		finally
		{
			players.writeUnlock();
		}
	}

	/**
	 * Добавление к счетчику.
	 */
	public static final void addSpawnedNpc()
	{
		spawnedNpcs += 1;
	}

	/**
	 * Проверяет, сменился ли регион в котором находится обьект Если сменился - удаляет обьект из старого региона и добавляет в новый.
	 *
	 * @param object обьект для проверки.
	 */
	public static void addVisibleObject(TObject object)
	{
		if(object == null || !object.isVisible())
			return;


		WorldRegion region = getRegion(object);
		WorldRegion currentRegion = object.getCurrentRegion();

		// если регион не нашли
		if(region == null)
		{
			// ставим его в центр мира
			object.setXYZ(0, 0, 0);
			return;
		}

		// активируем ловушки региона
		region.activateTrap(object);

		if(currentRegion != null && currentRegion == region)
			return;

		region.addObject(object);
		object.setCurrentRegion(region);

		// Убираем из старых регионов обьект
		if(currentRegion == null) // Новый обьект (пример - игрок вошел в мир, заспаунился моб, дропнули вещь)
		{
			// Показываем обьект в текущем и соседних регионах
			// Если обьект игрок, показываем ему все обьекты в текущем и соседних регионах
			WorldRegion[] newNeighbors = region.getNeighbors();

			for(int i = 0, length = newNeighbors.length; i < length; i++)
				newNeighbors[i].addToPlayers(object);
		}
		else
		// Обьект уже существует, перешел из одного региона в другой
		{
			// Показываем обьект, но в отличие от первого случая - только для новых соседей.
			// Убираем обьект из старых соседей.
			WorldRegion[] oldNeighbors = currentRegion.getNeighbors();
			WorldRegion[] newNeighbors = region.getNeighbors();

			for(int i = 0, length = oldNeighbors.length; i < length; i++)
			{
				WorldRegion neighbor = oldNeighbors[i];

				if(!Arrays.contains(newNeighbors, neighbor))
					neighbor.removeFromPlayers(object, DeleteCharacter.DISAPPEARS);
			}

			for(int i = 0, length = newNeighbors.length; i < length; i++)
			{
				WorldRegion neighbor = newNeighbors[i];

				if(!Arrays.contains(oldNeighbors, neighbor))
					neighbor.addToPlayers(object);
			}

			currentRegion.removeObject(object);
		}
	}

	/**
	 * Очищает мир от всего.
	 */
	public static final void clear()
	{
		// удаляем старые регионы
		for(WorldRegion[][][] regionsss : worldRegions)
			for(WorldRegion[][] regionss : regionsss)
				for(WorldRegion[] regions : regionss)
					Arrays.clear(regions);

		// очищаем активные регионы
		activeRegions.clear();

		// очищаем таблицу игроков
		playerNames.clear();
	}

	/**
	 * Проверка на нахождение онлаин игрока с указанным именем.
	 *
	 * @param name имя игрока.
	 * @return есть ли онлаин игрок с таким ником.
	 */
	public static boolean containsPlayer(String name)
	{
		players.readLock();
		try
		{
			return playerNames.containsKey(name);
		}
		finally
		{
			players.readUnlock();
		}
	}

	/**
	 * @return список активных регионов.
	 */
	public static Array<WorldRegion> getActiveRegions()
	{
		return activeRegions;
	}

	/**
	 * Добавляем в указанный список объекты указанного класса расположенных вокруг указанного объекта.
	 *
	 * @param array список объектов.
	 * @param type тип искомых объектов.
	 * @param object объект, вокруг которого исщем.
	 * @return список объектов.
	 */
	public static <T extends TObject, V extends T> Array<T> getAround(Array<T> array, Class<V> type, TObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion == null || !currentRegion.isActive())
			return array;

		WorldRegion[] regions = currentRegion.getNeighbors();

		int objectId = object.getObjectId();
		int subId = object.getSubId();

		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addObject(array, type, objectId, subId);

		return array;
	}

	/**
	 * Получаем список объектов указанного класса вокруг указанного объекта.
	 *
	 * @param type тип искомых объектов.
	 * @param array список объекто.
	 * @param object объект, вокруг которого ищем.
	 * @return список объектов.
	 */
	public static <T extends TObject> Array<T> getAround(Class<T> type, Array<T> array, TObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion == null)
			return array;

		WorldRegion[] regions = currentRegion.getNeighbors();

		int objectId = object.getObjectId();
		int subId = object.getSubId();

		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addObject(array, type, objectId, subId);

		return array;
	}

	/**
	 * Получаем список объектов указанного класса вокруг указанной точки.
	 *
	 * @param type тип искомых объектов.
	 * @param continentId ид континента.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 * @param objectId исключающийся объект.
	 * @param subId саб ид объекта.
	 * @param radius радиус поиска.
	 * @param height высота описка.
	 * @return список объектов.
	 */
	public static <T extends TObject> Array<T> getAround(Class<T> type, int continentId, float x, float y, float z, int objectId, int subId, float radius)
	{
		WorldRegion[] regions = getRegion(continentId, x, y, z).getNeighbors();

		Array<T> array = Arrays.toArray(type);

		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addObjects(array, type, objectId, subId, x, y, z, radius);

		return array;
	}

	/**
	 * Получаем список объектов указанного класса вокруг указанной точки.
	 *
	 * @param type тип искомых объектов.
	 * @param loc точка, вокруг которой поиск.
	 * @param objectId исключаемый объект.
	 * @param subId саб ид объекта.
	 * @param radius радиус поиска.
	 * @param height высота поиска.
	 * @return список объектов.
	 */
	public static <T extends TObject> Array<T> getAround(Class<T> type, Location loc, int objectId, int subId, float radius)
	{
		return getAround(type, loc.getContinentId(), loc.getX(), loc.getY(), loc.getZ(), objectId, subId, radius);
	}

	/**
	 * Получаем список объектов указанного класса вокруг указанного объекта.
	 *
	 * @param type тип искомых объектов.
	 * @param object объект, вокруг которого ищем.
	 * @return список объектов.
	 */
	public static <T extends TObject> Array<T> getAround(Class<T> type, TObject object)
	{
		WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion == null)
			return Arrays.toArray(type, 0);

		WorldRegion[] regions = currentRegion.getNeighbors();

		Array<T> array = Arrays.toArray(type);

		int objectId = object.getObjectId();
		int subId = object.getSubId();

		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addObject(array, type, objectId, subId);

		return array;
	}

	/**
	 * Получаем список объектов указанного класса вокруг указанного объекта в указанном радиусе и высоте.
	 *
	 * @param type тип искомых объектов.
	 * @param object объект, вокруг которого поиск.
	 * @param radius радиус поиска.
	 * @return список объектов.
	 */
	public static <T extends TObject> Array<T> getAround(Class<T> type, TObject object, float radius)
	{
		WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion == null)
			return Arrays.toArray(type, 0);

		WorldRegion[] regions = currentRegion.getNeighbors();

		Array<T> array = Arrays.toArray(type);

		int objectId = object.getObjectId();
		int subId = object.getSubId();

		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addObjects(array, type, objectId, subId, object.getX(), object.getY(), object.getZ(), radius);

		return array;
	}

	/**
	 * Получения кол-во искомых объектов.
	 *
	 * @param type тип искомых объектов.
	 * @param object объект, вокруг которого поиск.
	 * @param radius радиус поиска.
	 * @return кол-во объектов.
	 */
	public static <T extends TObject> int getAroundCount(Class<T> type, TObject object, float radius)
	{
		WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion == null)
			return 0;

		WorldRegion[] regions = currentRegion.getNeighbors();

		int objectId = object.getObjectId();
		int subId = object.getSubId();

		int counter = 0;

		for(int i = 0, length = regions.length; i < length; i++)
			counter += regions[i].getObjectCount( type, objectId, subId, object.getX(), object.getY(), object.getZ(), radius);

		return counter;
	}

	/**
	 * Получаем список объектов указанного класса вокруг указанной точки.
	 *
	 * @param type тип искомых объектов.
	 * @param array список персонажей.
	 * @param continentId ид континента.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 * @param objectId исключающийся объект.
	 * @param subId саб ид исключаемого.
	 * @param radius радиус поиска.
	 * @param height высота описка.
	 * @return список объектов.
	 */
	public static <T extends TObject, V extends T> Array<T> getAround(Class<V> type, Array<T> array, int continentId, float x, float y, float z, int objectId, int subId, float radius)
	{
		WorldRegion curreentRegion = getRegion(continentId, x, y, z);

		if(curreentRegion == null)
			return array;

		WorldRegion[] regions = curreentRegion.getNeighbors();

		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addObjects(array, type, objectId, subId, x, y, z, radius);

		return array;
	}

	/**
	 * Получаем список объектов указанного класса вокруг указанного объекта в указанном радиусе и высоте.
	 *
	 * @param type тип искомых объектов.
	 * @param array список объектов.
	 * @param object объект, вокруг которого поиск.
	 * @param radius радиус поиска.
	 * @param height высота описка.
	 * @return список объектов.
	 */
	public static <T extends TObject, V extends T> Array<T> getAround(Class<V> type, Array<T> array, TObject object, float radius)
	{
		WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion == null)
			return array;

		WorldRegion[] regions = currentRegion.getNeighbors();

		int objectId = object.getObjectId();
		int subId = object.getSubId();

		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addObjects(array, type, objectId, subId, object.getX(), object.getY(), object.getZ(), radius);

		return array;
	}

	/**
	 * Получение списка потенциальных препядствий для движения во время каста скила.
	 *
	 * @param array список препядствий.
	 * @param caster кастующий скил персонаж.
	 * @param distance дистанция, на которую персонаж сместится.
	 */
	public static void getAroundBarriers(Array<Character> array, Character caster, float distance)
	{
		WorldRegion currentRegion = caster.getCurrentRegion();

		if(currentRegion == null)
			return;

		WorldRegion[] regions = currentRegion.getNeighbors();

		float x = caster.getX();
		float y = caster.getY();
		float z = caster.getZ();

		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addBarriers(array, caster, x, y, z, distance);
	}

	/**
	 * Возврашает объект с указанным ид в регионе где находится object.
	 *
	 * @param type тип искомых объектов.
	 * @param object объект, вокруг которого ищем.
	 * @param targetId ид искомого объекта.
	 * @param targetSubId саб ид искомого объекта.
	 * @return список объектов.
	 */
	public static <T extends TObject> T getAroundById(Class<T> type, TObject object, int targetId, int targetSubId)
	{
		WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion == null || !currentRegion.isActive())
			return null;

		WorldRegion[] regions = currentRegion.getNeighbors();

		for(int i = 0, length = regions.length; i < length; i++)
		{
			TObject target = regions[i].getObject(targetId, targetSubId);

			if(target != null && type.isInstance(target))
				return type.cast(target);
		}

		return null;
	}

	/**
	 * Возврашает объект с указанным именем в регионе где находится object.
	 *
	 * @param type тип искомого объекта.
	 * @param object объект, вокруг которого идет поиск.
	 * @param name имя искомого объекта.
	 * @return искомый объект.
	 */
	public static <T extends TObject> T getAroundByName(Class<T> type, TObject object, String name)
	{
		WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion == null || !currentRegion.isActive())
			return null;

		WorldRegion[] regions = currentRegion.getNeighbors();

		for(WorldRegion region : regions)
		{
			TObject target = region.getObject(name);

			if(target != null && type.isInstance(target))
				return type.cast(target);
		}

		return null;
	}

	/**
	 * @return кол-во дропнутых вещей.
	 */
	public static final long getDroppedItems()
	{
		return droppedItems;
	}

	/**
	 * @return кол-во убитых нпс.
	 */
	public static final long getKilledNpcs()
	{
		return killedNpcs;
	}

	/**
	 * @return кол-во убитых игроков.
	 */
	public static final long getKilledPlayers()
	{
		return killedPlayers;
	}

	/**
	 * Список регионов вокруг координат объекта.
	 *
	 * @param continentId ид континента.
	 * @param x координата х.
	 * @param y координата у.
	 * @param minZ минимальная высота.
	 * @param maxZ максимальная высота.
	 * @return список регионов.
	 */
	public static Array<WorldRegion> getNeighbors(int continentId, float x, float y, float minZ, float maxZ)
	{
		Array<WorldRegion> array = Arrays.toArray(WorldRegion.class, 27);

		int newX = (int) (x / REGION_WIDTH + OFFSET_X);
		int newY = (int) (y / REGION_WIDTH + OFFSET_Y);
		int newMinZ = (int) (minZ / REGION_HEIGHT + OFFSET_Z);
		int newMaxZ = (int) (maxZ / REGION_HEIGHT + OFFSET_Z);

		for(int a = -1; a <= 1; a++)
			for(int b = -1; b <= 1; b++)
				for(int c = newMinZ; c <= newMaxZ; c++)
					if(validRegion(newX + a, newY + b, c))
					{
						if(worldRegions[continentId][newX + a][newY + b][c] == null)
						{
							lock.lock();
							try
							{
								if(worldRegions[continentId][newX + a][newY + b][c] == null)
									worldRegions[continentId][newX + a][newY + b][c] = new WorldRegion(continentId, newX + a, newY + b, c);
							}
							finally
							{
								lock.unlock();
							}
						}

						array.add(worldRegions[continentId][newX + a][newY + b][c]);
					}

		return array;
	}

	/**
	 * Список регионов вокруг координат объекта.
	 *
	 * @param continentId ид континента.
	 * @param x координата х.
	 * @param y координата у.
	 * @param z координата.
	 * @return список регионов.
	 */
	public static WorldRegion[] getNeighbors(int continentId, int x, int y, int z)
	{
		Array<WorldRegion> array = Arrays.toArray(WorldRegion.class, 27);

		for(int a = -1; a <= 1; a++)
			for(int b = -1; b <= 1; b++)
				for(int c = -1; c <= 1; c++)
					if(validRegion(x + a, y + b, z + c))
					{
						if(worldRegions[continentId][x + a][y + b][z + c] == null)
						{
							lock.lock();
							try
							{
								if(worldRegions[continentId][x + a][y + b][z + c] == null)
									worldRegions[continentId][x + a][y + b][z + c] = new WorldRegion(continentId, x + a, y + b, z + c);
							}
							finally
							{
								lock.unlock();
							}
						}

						array.add(worldRegions[continentId][x + a][y + b][z + c]);
					}

		array.trimToSize();

		return array.array();
	}

	/**
	 * Получение онлаин игрока по уникальному ид.
	 *
	 * @param objectId уникальный ид игрока.
	 * @return игрок.
	 */
	public static Player getPlayer(int objectId)
	{
		players.readLock();
		try
		{
			return playerTable.get(objectId);
		}
		finally
		{
			players.readUnlock();
		}
	}

	/**
	 * Получение онлаин игрока по имени.
	 *
	 * @param name имя игрока.
	 * @return онлаин игрок.
	 */
	public static Player getPlayer(String name)
	{
		players.readLock();
		try
		{
			return playerNames.get(name);
		}
		finally
		{
			players.readUnlock();
		}
	}

	/**
	 * @return список игроков онлайн.
	 */
	public static final Array<Player> getPlayers()
	{
		return players;
	}

	/**
	 * @param continentId ид континента.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 * @return регион, в котором находятся указанные координаты.
	 */
	public static WorldRegion getRegion(int continentId, float x, float y, float z)
	{
		if(continentId > 2)
			return null;

		int newX = (int) x / REGION_WIDTH + OFFSET_X;
		int newY = (int) y / REGION_WIDTH + OFFSET_Y;
		int newZ = (int) z / REGION_HEIGHT + OFFSET_Z;

		// еси коррдинаты корректные
		if(validRegion(newX, newY, newZ))
		{
			// получаем регион
			WorldRegion region = worldRegions[continentId][newX][newY][newZ];

			// если его нет
			if(region == null)
			{
				lock.lock();
				try
				{
					// получаем еще раз
					region = worldRegions[continentId][newX][newY][newZ];

					// если его всеравно нет
					if(region == null)
					{
						// создаем новый
						region = new WorldRegion(continentId, newX, newY, newZ);
						// вносим
						worldRegions[continentId][newX][newY][newZ] = region;
					}
				}
				finally
				{
					lock.unlock();
				}
			}

			// возвращаем регион
			return region;
		}

		return null;
	}

	/**
	 * @param continentId ид континента.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 * @return регион, в котором находятся координаты.
	 */
	public static WorldRegion getRegion(int continentId, int x, int y, int z)
	{
		int newX = x / REGION_WIDTH + OFFSET_X;
		int newY = y / REGION_WIDTH + OFFSET_Y;
		int newZ = z / REGION_HEIGHT + OFFSET_Z;

		// еси коррдинаты корректные
		if(validRegion(newX, newY, newZ))
		{
			// получаем регион
			WorldRegion region = worldRegions[continentId][newX][newY][newZ];

			// если его нет
			if(region == null)
			{
				lock.lock();
				try
				{
					// получаем еще раз
					region = worldRegions[continentId][newX][newY][newZ];

					// если его всеравно нет
					if(region == null)
					{
						// создаем новый
						region = new WorldRegion(continentId, newX, newY, newZ);
						// вносим
						worldRegions[continentId][newX][newY][newZ] = region;
					}
				}
				finally
				{
					lock.unlock();
				}
			}

			// возвращаем регион
			return region;
		}

		return null;
	}

	/**
	 * @return регион, в котором находится указанная точка.
	 */
	public static WorldRegion getRegion(Location location)
	{
		return getRegion(location.getContinentId(), location.getX(), location.getY(), location.getZ());
	}

	/**
	 * @param object объект.
	 * @return регион, в котором находится указанный объект.
	 */
	public static WorldRegion getRegion(TObject object)
	{
		return getRegion(object.getContinentId(), object.getX(), object.getY(), object.getZ());
	}

	/**
	 * @return массив регионов.
	 */
	public static WorldRegion[][][][] getRegions()
	{
		return worldRegions;
	}

	/**
	 * Подсчет регионов с указанной активность.
	 *
	 * @param active активность региона.
	 * @return кол-во регионов.
	 */
	public static long getRegionsCount(boolean active)
	{
		long counter = 0;

		for(WorldRegion[][][] first : worldRegions)
		{
			if(first == null)
				continue;

			for(WorldRegion[][] second : first)
			{
				if(second == null)
					continue;

				for(WorldRegion[] thrid : second)
				{
					if(thrid == null)
						continue;

					for(WorldRegion four : thrid)
					{
						if(four == null)
							continue;

						if(four.isActive() != active)
							continue;

						counter++;
					}
				}
			}
		}

		return counter;
	}

	/**
	 * @return кол-во отспавненых нпс.
	 */
	public static final long getSpawnedNpcs()
	{
		return spawnedNpcs;
	}

	/**
	 * @return текущий онлаин.
	 */
	public static int online()
	{
		return playerNames.size();
	}

	/**
	 * Возвращает регион по указанным индексам.
	 *
	 * @param continentId ид континента.
	 * @param i индекс региона.
	 * @param j индекс региона.
	 * @param k индекс региона.
	 * @return соответствующий регион.
	 */
	public static WorldRegion region(int continentId, int i, int j, int k)
	{
		WorldRegion region = worldRegions[continentId][i][j][k];

		if(region == null)
		{
			lock.lock();
			try
			{
				region = worldRegions[continentId][i][j][k];

				if(region == null)
				{
					region = new WorldRegion(continentId, i, j, k);
					worldRegions[continentId][i][j][k] = region;
				}
			}
			finally
			{
				lock.unlock();
			}
		}

		return region;
	}

	/**
	 * @param region удаляемый из активных регион.
	 */
	public static void removeActiveRegion(WorldRegion region)
	{
		activeRegions.fastRemove(region);
	}

	/**
	 * Удаляет из онлайна старого игрока.
	 *
	 * @param player удаляемый игрок.
	 */
	public static void removeOldPlayer(Player player)
	{
		players.writeLock();
		try
		{
			playerNames.remove(player.getName());
			players.fastRemove(player);
			playerTable.remove(player.getObjectId());

			// получаем массив текущих онлаин игроков
			Player[] array = players.array();

			// перебираем их
			for(int i = 0, length = players.size(); i < length; i++)
			{
				// получаем игрока
				Player target = array[i];

				// получаем его список друзей
				FriendList friendList = target.getFriendList();

				// если пуст, пропускаем
				if(friendList.size() < 1)
					continue;

				// уведомляем о выходе в игру этого игрока
				friendList.onExitGame(player);
			}
		}
		finally
		{
			players.writeUnlock();
		}
	}

	/**
	 * Удаляет обьект из текущего региона.
	 *
	 * @param object удаляемый объект.
	 */
	public static void removeVisibleObject(TObject object, int type)
	{
		if(object == null || object.isVisible())
			return;

		WorldRegion currentRegion = object.getCurrentRegion();

		if(currentRegion != null)
		{
			currentRegion.removeObject(object);

			// TODO
			for(WorldRegion neighbor : currentRegion.getNeighbors())
				neighbor.removeFromPlayers(object, type);

			object.setCurrentRegion(null);
		}
	}

	/**
	 * @param text текст аннонса.
	 */
	public static void sendAnnounce(String text)
	{
		CharSay packet = CharSay.getInstance(Strings.EMPTY, text, SayType.NOTICE_CHAT, 0, 0);

		players.readLock();
		try
		{
			Player[] array = players.array();

			for(int i = 0, length = players.size(); i < length; i++)
				packet.increaseSends();

			for(int i = 0, length = players.size(); i < length; i++)
				array[i].sendPacket(packet, false);
		}
		finally
		{
			players.readUnlock();
		}
	}

	/**
	 * Проверка на корректность индексов региона.
	 *
	 * @param x индекс региона.
	 * @param y индекс региона.
	 * @param z индекс региона.
	 * @return корректные ли индексы региона.
	 */
	public static boolean validRegion(int x, int y, int z)
	{
		return x >= 0 && x < REGIONS_X && y >= 0 && y < REGIONS_Y && z >= 0 && z < REGIONS_Z;
	}
}
package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.Config;
import tera.gameserver.document.DocumentWorldZone;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.WorldZone;
import tera.util.Location;

/**
 * Таблица мировых зон.
 *
 * @author Ronn
 * @created 08.03.2012
 */
public final class WorldZoneTable
{
	public static final Location DEFAULT_RESPAWN_POINT = new Location(66596, -79856, -2994);

	/** размер клетки */
	public static final int CELL_POWER = 15;

	private static WorldZoneTable instance;

	public static WorldZoneTable getInstance()
	{
		if(instance == null)
			instance = new WorldZoneTable();

		return instance;
	}

	/** точки воскрешения */
	private Location[] respawnPoints;

	private WorldZoneTable()
	{
		Table<WorldRegion, Array<WorldZone>> zones = Tables.newObjectTable();

		Array<WorldZone> zons = new DocumentWorldZone(new File(Config.SERVER_DIR + "/data/zones.xml")).parse();
		Array<Location> points = Arrays.toArray(Location.class);
		Array<WorldRegion> regions = Arrays.toArray(WorldRegion.class);

		for(WorldZone worldZone : zons)
		{
			int minimumX = worldZone.getMinimumX() / World.REGION_WIDTH + World.OFFSET_X;
			int maximumX = worldZone.getMaximumX() / World.REGION_WIDTH + World.OFFSET_X;
			int minimumY = worldZone.getMinimumY() / World.REGION_WIDTH + World.OFFSET_Y;
			int maximumY = worldZone.getMaximumY() / World.REGION_WIDTH + World.OFFSET_Y;
			int minimumZ = worldZone.getMinimumZ() / World.REGION_HEIGHT + World.OFFSET_Z;
			int maximumZ = worldZone.getMaximumZ() / World.REGION_HEIGHT + World.OFFSET_Z;

			points.addAll(worldZone.getRespawnPoints());

			for(int x = minimumX; x <= maximumX; x++)
				for(int y = minimumY; y <= maximumY; y++)
					for(int z = minimumZ; z <= maximumZ; z++)
					{
						WorldRegion region = World.region(worldZone.getContinentId(), x, y, z);

						Array<WorldZone> array = zones.get(region);

						if(array == null)
						{
							array = Arrays.toArray(WorldZone.class);
							zones.put(region, array);
						}

						if(!regions.contains(region))
							regions.add(region);

						if(array.contains(worldZone))
							continue;

						array.add(worldZone);
					}
		}

		for(WorldRegion region : regions)
		{
			Array<WorldZone> array = zones.get(region);

			if(array.isEmpty())
				continue;

			array.trimToSize();

			region.setZones(array.array());
		}

		points.trimToSize();

		respawnPoints = points.array();

		Loggers.info(WorldZoneTable.class, "loaded " + zons.size() + " world zones for " + regions.size() + " regions.");
	}

	/**
	 * Получаем точку респа для указанного объекта
	 *
	 * @param object объект.
	 * @return точка респавна.
	 */
	public final Location getDefaultRespawn(TObject object)
	{
		Location target = null;

		float min = Float.MAX_VALUE;

		for(int i = 0, length = respawnPoints.length; i < length; i++)
		{
			Location point = respawnPoints[i];

			if(point.getContinentId() != object.getContinentId())
				continue;

			float dist = object.getDistance(point.getX(), point.getY(), point.getZ());

			if(dist < min)
			{
				min = dist;
				target = point;
			}
		}

		if(target != null)
			return target;

		for(int i = 0, length = respawnPoints.length; i < length; i++)
		{
			Location point = respawnPoints[i];

			float dist = object.getDistance(point.getX(), point.getY(), point.getZ());

			if(dist < min)
			{
				min = dist;
				target = point;
			}
		}

		if(target == null)
			return WorldZoneTable.DEFAULT_RESPAWN_POINT;

		return target;
	}

	/**
	 * Получаем точку респа для указанного объекта
	 *
	 * @param object объект.
	 * @return точка респавна.
	 */
	public final Location getRespawn(TObject object)
	{
		WorldRegion region = object.getCurrentRegion();

		if(region == null)
			return getDefaultRespawn(object);

		WorldZone[] zones = region.getZones();

		if(zones == null)
			return getDefaultRespawn(object);

		int x = (int) object.getX();
		int y = (int) object.getY();
		int z = (int) object.getZ();

		for(int i = 0, length = zones.length; i < length; i++)
		{
			WorldZone zone = zones[i];

			if(zone.contains(x, y, z))
				return zone.getRespawn(object);
		}

		return getDefaultRespawn(object);
	}

	/**
	 * Получаем ид зоны, в которой находится объект.
	 *
	 * @param object объект.
	 * @return ид зоны.
	 */
	public final int getZoneId(TObject object)
	{
		WorldRegion region = object.getCurrentRegion();

		if(region == null)
			return -1;

		return region.getZoneId(object);
	}
}

package tera.gameserver.model;

import java.awt.Polygon;

import rlib.util.array.Arrays;
import tera.gameserver.tables.WorldZoneTable;
import tera.util.Location;

/**
 * Класс, описывающий зону с содержанем кода зоны для пакета
 *
 * @author Ronn
 * @created 08.03.2012
 */
public final class WorldZone
{
	/** долгота зоны */
	private int maximumX;
	private int minimumX;
	/** ширина зоны */
	private int maximumY;
	private int minimumY;
	/** высота зоны */
	private int maximumZ;
	private int minimumZ;

	/** ид зоны */
	private int zoneId;

	/** ид континента */
	private int continentId;

	/** описание территории */
	private Polygon zone;

	/** точки респавна */
	private Location[] respawnPoints;

	/**
	 * @param maxZ максимальная высота зоны.
	 * @param minZ минимальная высота зоны.
	 * @param zoneId ид зоны.
	 * @param continentId ид континента.
	 */
	public WorldZone(int maxZ, int minZ, int zoneId, int continentId)
	{
		this.maximumZ = maxZ;
		this.minimumZ = minZ;
		this.zoneId = zoneId;
		this.continentId = continentId;

		maximumX = Integer.MIN_VALUE;
		minimumX = Integer.MAX_VALUE;
		maximumY = Integer.MIN_VALUE;
		minimumY = Integer.MAX_VALUE;

		zone = new Polygon();

		respawnPoints = new Location[0];
	}

	/**
	 * Добавляем точку в полигон.
	 *
	 * @param x координата.
	 * @param y координата.
	 */
	public final void addPoint(int x, int y)
	{
		maximumX = Math.max(maximumX, x);
		minimumX = Math.min(minimumX, x);

		maximumY = Math.max(maximumY, y);
		minimumY = Math.min(minimumY, y);

		zone.addPoint(x, y);
	}

	/**
	 * @param point новая точка респавна.
	 */
	public final void addRespawnPoint(Location point)
	{
		respawnPoints = Arrays.addToArray(respawnPoints, point, Location.class);
	}

	/**
	 * Находятся ли эти координаты в зоне.
	 *
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 * @return находятся ли в зоне.
	 */
	public final boolean contains(int x, int y, int z)
	{
		if(z > maximumZ || z < minimumZ)
			return false;

		return zone.contains(x, y);
	}

	/**
	 * @return ид континента.
	 */
	public int getContinentId()
	{
		return continentId;
	}

	/**
	 * @return максимальный х.
	 */
	public final int getMaximumX()
	{
		return maximumX;
	}

	/**
	 * @return максимальный у.
	 */
	public final int getMaximumY()
	{
		return maximumY;
	}

	/**
	 * @return максимальный z.
	 */
	public final int getMaximumZ()
	{
		return maximumZ;
	}

	/**
	 * @return минимальный х.
	 */
	public final int getMinimumX()
	{
		return minimumX;
	}

	/**
	 * @return минимальный y.
	 */
	public final int getMinimumY()
	{
		return minimumY;
	}

	/**
	 * @return минимальный z.
	 */
	public final int getMinimumZ()
	{
		return minimumZ;
	}

	/**
	 * Получить ближайшую респавн точку.
	 *
	 * @param object умерший объект.
	 * @return point точка респавна.
	 */
	public final Location getRespawn(TObject object)
	{
		Location target = null;

		float min = Float.MAX_VALUE;

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
	 * @return точки респавна.
	 */
	public final Location[] getRespawnPoints()
	{
		return respawnPoints;
	}

	/**
	 * @return полигон зоны.
	 */
	public final Polygon getZone()
	{
		return zone;
	}

	/**
	 * @return ид зоны.
	 */
	public final int getZoneId()
	{
		return zoneId;
	}

	@Override
	public String toString()
	{
		return "WorldZone maxX = " + maximumX + ", minX = " + minimumX + ", maxY = " + maximumY + ", minY = " + minimumY + ", maxZ = " + maximumZ + ", minZ = " + minimumZ + ", zoneId = " + zoneId + ", zone = " + zone + ", respawnPoints = " + Arrays.toString(respawnPoints);
	}
}

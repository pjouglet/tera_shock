package tera.gameserver.tables;

import rlib.util.array.Arrays;
import tera.gameserver.model.TObject;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.territory.BonfireTerritory;
import tera.gameserver.model.territory.Territory;
import tera.gameserver.model.territory.TerritoryType;

/**
 * Таблица костров.
 *
 * @author Ronn
 */
public abstract class BonfireTable
{
	/** массив всех костров */
	private static BonfireTerritory[] array;

	/**
	 * Добавить костер в список.
	 *
	 * @param bonfire костер.
	 */
	public static void addBonfire(BonfireTerritory bonfire)
	{
		array = Arrays.addToArray(array, bonfire, BonfireTerritory.class);
	}

	/**
	 * Получение ближайшего костра к объекту.
	 *
	 * @param object объект.
	 * @return ближайший костер.
	 */
	public static BonfireTerritory getNearBonfire(TObject object)
	{
		// получаем текущий регион объекта
		WorldRegion region = object.getCurrentRegion();

		// ближайший костер
		BonfireTerritory near = null;

		// дистанция до костра
		float dist = 0;

		// если регион есть
		if(region != null && region.hasTerritories())
		{
			// получаем список территорий региона
			Territory[] terrs = region.getTerritories();
			// перебираем территории
			for(int i = 0, length = terrs.length; i < length; i++)
			{
				Territory terr = terrs[i];

				// если территория не костер, пропускаем
				if(terr.getType() != TerritoryType.CAMP_TERRITORY)
					continue;

				// кастим в территорию костра
				BonfireTerritory bonfire = (BonfireTerritory) terr;
				// определяем расстояние до костра
				float newDist = object.getDistance(bonfire.getCenterX(), bonfire.getCenterY(), bonfire.getCenterZ());
				// если ближайшего нету либо расстояние у него меньше, чем у текущего ближайшего
				if(near == null || newDist < dist)
				{
					// запоминаем территорию
					near = bonfire;
					// запоминаем расстояние
					dist = newDist;
				}
			}

			// если нашли ближайший костер, возвращаем
			if(near != null)
				return near;
		}

		// если в регионе не нашли костер, перебираем среди всех
		for(int i = 0, length = array.length; i < length; i++)
		{
			BonfireTerritory terr = array[i];

			// считаем расстояние до костра
			float newDist = object.getDistance(terr.getCenterX(), terr.getCenterY(), terr.getCenterZ());

			// если ближайшего нету либо расстояние у него меньше, чем у текущего ближайшего
			if(near == null || newDist < dist)
			{
				near = terr;
				dist = newDist;
			}
		}

		return near;
	}
}

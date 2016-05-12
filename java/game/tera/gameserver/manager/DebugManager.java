package tera.gameserver.manager;

import rlib.geom.Coords;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;
import tera.util.Location;

/**
 * Менеджер по отображению дебага.
 *
 * @author Ronn
 */
public abstract class DebugManager
{
	public static void showAreaDebug(int continentId, float targetX, float targetY, float targetZ, float radius)
	{
		Location[] locs = Coords.circularCoords(Location.class, targetX, targetY, targetZ, (int) radius, 10);

		ItemTable itemTable = ItemTable.getInstance();

		ItemTemplate template = itemTable.getItem(8007);

		// перебираем точки
		for(int i = 0; i < 10; i++)
		{
			// получаем точку
			Location loc = locs[i];

			// вносим континент
			loc.setContinentId(continentId);

			// спавним
			template.newInstance().spawnMe(loc);
		}


		template.newInstance().spawnMe(new Location(targetX, targetY, targetZ));
	}
}

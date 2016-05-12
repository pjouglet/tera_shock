package tera.gameserver.model.npc.spawn;

import tera.gameserver.model.npc.Npc;
import tera.util.Location;

/**
 * Интерфейс для реализации спавна нпс.
 *
 * @author Ronn
 */
public interface Spawn
{
	/**
	 * Обработка смерти нпс из этого спавна.
	 *
	 * @param npc умерший нпс.
	 */
	public void doDie(Npc npc);

	/**
	 * @return позиция спавна.
	 */
	public Location getLocation();

	/**
	 * @return маршрут патрулирования.
	 */
	public Location[] getRoute();

	/**
	 * @return ид темплейта.
	 */
	public int getTemplateId();

	/**
	 * @return тип темплейта.
	 */
	public int getTemplateType();

	/**
	 * @param location позиция спавна.
	 */
	public void setLocation(Location location);

	/**
	 * Запуск спавна.
	 */
	public void start();

	/**
	 * Остановка спавна.
	 */
	public void stop();
}

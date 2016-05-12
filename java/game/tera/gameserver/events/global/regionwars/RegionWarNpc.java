package tera.gameserver.events.global.regionwars;

import tera.gameserver.model.Guild;

/**
 * Интерфейс для реализации НПС взаимодействующего с региональтными битвами.
 *
 * @author Ronn
 */
public interface RegionWarNpc
{
	/**
	 * @param guild владеющая гильдия.
	 */
	public void setGuildOwner(Guild guild);

	/**
	 * @param region регион, в котором учавствует контрол.
	 */
	public void setRegion(Region region);

	/**
	 * @return регион, в котором учавствует контрол.
	 */
	public Region getRegion();

	/**
	 * @return владеющая гильдия.
	 */
	public Guild getGuildOwner();
}

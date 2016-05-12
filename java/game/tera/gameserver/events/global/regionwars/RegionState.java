package tera.gameserver.events.global.regionwars;

/**
 * Перечисление состояния региона.
 *
 * @author Ronn
 */
public enum RegionState
{
	WAIT_WAR,
	PREPARE_START_WAR,
	WAR,
	PREPARE_END_WAR;

	public static RegionState valueOf(int index)
	{
		return values()[index];
	}
}

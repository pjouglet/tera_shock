package tera.gameserver.events;

import tera.gameserver.tables.NpcTable;
import tera.gameserver.templates.NpcTemplate;

/**
 * Набор костант связааных с ивентами.
 *
 * @author Ronn
 */
public class EventConstant
{
	/** название переменной очков славы */
	public static final String VAR_NANE_HERO_POINT = "event_honor_point";

	/** список темплейтов НПС координаторов */
	public static final NpcTemplate MYSTEL;

	static
	{
		NpcTable npcTable = NpcTable.getInstance();

		MYSTEL = npcTable.getTemplate(10000, 402);
	}
}

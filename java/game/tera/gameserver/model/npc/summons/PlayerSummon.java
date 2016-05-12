package tera.gameserver.model.npc.summons;

import tera.gameserver.network.serverpackets.NameColor;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель сумона игрока на основе игрокоподобных суммонов.
 *
 * @author Ronn
 */
public class PlayerSummon extends PlayableSummon
{
	public PlayerSummon(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getNameColor()
	{
		return NameColor.COLOR_LIGHT_BLUE;
	}
}

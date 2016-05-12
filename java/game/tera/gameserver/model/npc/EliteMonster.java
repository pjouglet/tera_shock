package tera.gameserver.model.npc;

import tera.gameserver.templates.NpcTemplate;

/**
 * Модель элитного монстра.
 *
 * @author Ronn
 */
public class EliteMonster extends Monster
{
	public EliteMonster(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getOwerturnId()
	{
		return 0x482DE6CA;
	}

	@Override
	public int getKarmaMod()
	{
		return 4;
	}
}
package tera.gameserver.model.npc;

import tera.gameserver.templates.NpcTemplate;

/**
 * Модель НПС барьра.
 *
 * @author Ronn
 */
public class NpcBarrier extends Monster
{
	public NpcBarrier(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isOwerturnImmunity()
	{
		return true;
	}

	@Override
	public boolean isSleepImmunity()
	{
		return true;
	}

	@Override
	public boolean isStunImmunity()
	{
		return true;
	}

	@Override
	public boolean isLeashImmunity()
	{
		return true;
	}
}

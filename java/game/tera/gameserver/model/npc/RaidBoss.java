package tera.gameserver.model.npc;

import tera.gameserver.templates.NpcTemplate;

/**
 * Модель Рейдового Босса.
 *
 * @author Ronn
 */
public class RaidBoss extends Monster
{
	/**
	 * @param objectId уникальный ид.
	 * @param template темплейт нпс.
	 */
	public RaidBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isOwerturnImmunity()
	{
		return true;
	}

	@Override
	public boolean isLeashImmunity()
	{
		return true;
	}

	@Override
	public boolean isRaidBoss()
	{
		return true;
	}
}
package tera.gameserver.model.npc;

import tera.gameserver.model.Character;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель дружественного нпс.
 *
 * @author Ronn
 */
public class BattleGuard extends Guard
{
	public BattleGuard(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean checkTarget(Character target)
	{
		if(target.isPlayer() || target.isSummon())
			return false;

		if(target.isNpc())
		{
			Npc npc = target.getNpc();

			return npc.isMonster() || npc.isRaidBoss();
		}

		return false;
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}
}

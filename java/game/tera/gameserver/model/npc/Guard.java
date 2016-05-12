package tera.gameserver.model.npc;

import tera.gameserver.model.Character;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель гварда.
 *
 * @author Ronn
 */
public class Guard extends FriendNpc
{
	public Guard(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean checkTarget(Character target)
	{
		if(!target.isNpc())
			return false;

		Npc npc = target.getNpc();

		return !npc.isGuard() && !npc.isFriendNpc();
	}

	@Override
	public boolean isGuard()
	{
		return true;
	}
}

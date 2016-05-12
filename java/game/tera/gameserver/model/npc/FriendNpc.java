package tera.gameserver.model.npc;

import rlib.idfactory.IdGenerator;
import rlib.idfactory.IdGenerators;
import tera.gameserver.model.EmotionType;
import tera.gameserver.tasks.EmotionTask;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель дружественного нпс.
 *
 * @author Ronn
 */
public class FriendNpc extends Npc
{
	private static final IdGenerator ID_FACTORY = IdGenerators.newSimpleIdGenerator(1000001, 1300000);

	public FriendNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected EmotionType[] getAutoEmotions()
	{
		return EmotionTask.NPC_TYPES;
	}

	@Override
	public boolean isFriendNpc()
	{
		return true;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public int nextCastId()
	{
		return ID_FACTORY.getNextId();
	}
}

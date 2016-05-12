package tera.gameserver.model.npc;

import rlib.idfactory.IdGenerator;
import rlib.idfactory.IdGenerators;
import tera.gameserver.model.Character;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель монстра.
 *
 * @author Ronn
 */
public class Monster extends Npc
{
	private static final IdGenerator ID_FACTORY = IdGenerators.newSimpleIdGenerator(300001, 600000);

	/**
	 * @param objectId уникальный ид.
	 * @param template темплейт нпс.
	 */
	public Monster(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean checkTarget(Character target)
	{
		if(target == null)
			return false;

		if(target.isPlayer() || target.isSummon())
			return true;

		if(target.isNpc())
		{
			Npc npc = target.getNpc();

			return npc.isGuard();
		}

		return false;
	}

	@Override
	public boolean isMonster()
	{
		return true;
	}

	@Override
	public int nextCastId()
	{
		return ID_FACTORY.getNextId();
	}
}
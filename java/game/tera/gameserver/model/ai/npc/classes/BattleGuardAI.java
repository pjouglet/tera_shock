package tera.gameserver.model.ai.npc.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.npc.BattleGuard;
import tera.gameserver.model.npc.Npc;

/**
 * Базовая модель НПС АИ.
 *
 * @author Ronn
 */
public class BattleGuardAI extends AbstractNpcAI<BattleGuard>
{
	public BattleGuardAI(BattleGuard actor, ConfigAI config)
	{
		super(actor, config);
	}

	@Override
	public boolean checkAggression(Character target)
	{
		if(!target.isNpc())
			return false;

		// получаем НПС
		Npc npc = target.getNpc();

		// еслио н не является гвардом
		return !npc.isGuard() && !npc.isFriendNpc();
	}
}

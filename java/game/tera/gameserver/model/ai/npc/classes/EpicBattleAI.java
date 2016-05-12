package tera.gameserver.model.ai.npc.classes;

import rlib.util.Rnd;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.npc.playable.EventEpicBattleNpc;

/**
 * Модель АИ для ивента эпичных битв.
 *
 * @author Ronn
 */
public final class EpicBattleAI extends AbstractNpcAI<EventEpicBattleNpc>
{
	public EpicBattleAI(EventEpicBattleNpc actor, ConfigAI config)
	{
		super(actor, config);
	}

	@Override
	public void notifyClanAttacked(Character attackedMember, Character attacker, int damage)
	{
		super.notifyClanAttacked(attackedMember, attacker, 1);
	}

	@Override
	public boolean checkAggression(Character target)
	{
		// получаем НПС
		EventEpicBattleNpc actor = getActor();

		// если его нет или нет цели, или цель не в оне агра
		if(actor == null || target == null || !target.isInRange(actor, actor.getAggroRange()))
			return false;

		// если НПС может атаковать цель
		if(actor.checkTarget(target) && Rnd.chance(25))
		{
			// добавляем агр поинт
			actor.addAggro(target, (long) (actor.getMaxHp() * 0.05F), true);
			return true;
		}

		return false;
	}
}

package tera.gameserver.model.ai.npc.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.npc.RegionWarDefense;

/**
 * Модель АИ защитника региона.
 *
 * @author Ronn
 */
public class RegionWarDefenseAI extends AbstractNpcAI<RegionWarDefense>
{
	public RegionWarDefenseAI(RegionWarDefense actor, ConfigAI config)
	{
		super(actor, config);
	}

	@Override
	public boolean checkAggression(Character target)
	{
		// получаем НПС
		RegionWarDefense actor = getActor();

		// если НПС нету, выходим
		if(actor == null)
		{
			log.warning(this, "not found actor.");
			return false;
		}

		// может ли НПС атаковать эту цель
		return actor.checkTarget(target);
	}

	@Override
	public void notifyClanAttacked(Character attackedMember, Character attacker, int damage)
	{
		// срезаем урон, что бы срабатывало как уведомление, но не переагриивало
		damage = 1;

		// обрабатываем
		super.notifyClanAttacked(attackedMember, attacker, damage);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}

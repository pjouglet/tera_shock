package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.network.serverpackets.Damage;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * @author Ronn
 */
public class LockOnEffect extends LockOnStrike
{
	public LockOnEffect(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// инфа об атаке
		AttackInfo info = local.getNextAttackInfo();

		// рассчитывам блокированность дебафа
		info.setBlocked(target.isBlocked(attacker, impactX, impactY, this));

		// если дебаф не заблокирован
		if(!info.isBlocked())
		{
			// добавляем эффекты
			addEffects(attacker, target);

			// если цель нпс, агрмм его на себя
			if(target.isNpc())
			{
				Npc npc = target.getNpc();

				// добавялем агр поинты
				npc.addAggro(attacker, attacker.getLevel() * attacker.getLevel(), false);
			}
		}

		// отображаем анимацию приминения дебафа
		target.broadcastPacket(Damage.getInstance(attacker, target, template.getDamageId(), getPower(), false, false, Damage.EFFECT));

		// если цель в ПвП режиме а кастер нет
		if(target.isPvPMode() && !attacker.isPvPMode())
		{
			// включаем пвп режим
			attacker.setPvPMode(true);

			// включаем боевую стойку
			attacker.startBattleStance(target);
		}

		return info;
	}
}

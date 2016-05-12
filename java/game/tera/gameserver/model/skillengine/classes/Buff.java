package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.Damage;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила, служащего только для активирования бафа.
 *
 * @author Ronn
 */
public class Buff extends Effect
{
	/**
	 * @param template темплейт скила.
	 */
	public Buff(SkillTemplate template)
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

		// добавляем эффекты
		addEffects(attacker, target);

		// добалвяем агр на баф
		addAggroTo(attacker, target, attacker.getLevel() * attacker.getLevel());

		// отображаем анимацию вешания бафа
		target.broadcastPacket(Damage.getInstance(attacker, target, template.getDamageId(), 0, false, false, Damage.EFFECT));

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

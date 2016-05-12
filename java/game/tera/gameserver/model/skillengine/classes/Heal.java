package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель исцеляющего скила.
 *
 * @author Ronn
 */
public class Heal extends Effect
{
	/**
	 * @param template темплейт скила.
	 */
	public Heal(SkillTemplate template)
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

		//сила хила скила
		int power = getPower();

		// увеличиваем на процентный бонус хилера
		power = (int) (power * attacker.calcStat(StatType.HEAL_POWER_PERCENT, 1, null, null));
		// добавляем статичный бонус хилера
		power += attacker.calcStat(StatType.HEAL_POWER_STATIC, 0, null, null);

		// хилим цель
		target.skillHealHp(getDamageId(), power, attacker);
		// добавляем агр поинты
		addAggroTo(attacker, target, power + getAggroPoint());

		// добавляем эффекты
		addEffects(attacker, target);

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

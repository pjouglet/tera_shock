package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель заряжающегося восстановителя МП.
 *
 * @author Ronn
 */
public class ChargeManaHeal extends ChargeDam
{
	public ChargeManaHeal(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		// хилим МП
		target.effectHealMp(getPower(), attacker);

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		return local.getNextAttackInfo();
	}
}

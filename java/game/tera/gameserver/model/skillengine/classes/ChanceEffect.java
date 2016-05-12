package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила, служащего только для активирования бафа.
 *
 * @author Ronn
 */
public class ChanceEffect extends Effect
{
	/**
	 * @param template темплейт скила.
	 */
	public ChanceEffect(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		// получаем формулы
		Formulas formulas = Formulas.getInstance();

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// рассчитываем урон
		AttackInfo info = formulas.calcDamageSkill(local.getNextAttackInfo(), this, attacker, target);

		// если атака не заблокированна
		if(!info.isBlocked())
			// добавляем эффекты
			addEffects(attacker, target);

		return info;
	}
}

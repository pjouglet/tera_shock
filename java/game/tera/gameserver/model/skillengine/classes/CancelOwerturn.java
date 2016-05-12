package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила, служащего только для активирования бафа.
 *
 * @author Ronn
 */
public class CancelOwerturn extends Buff
{
	/**
	 * @param template темплейт скила.
	 */
	public CancelOwerturn(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		if(target.isOwerturned())
			target.cancelOwerturn();

		return super.applySkill(attacker, target);
	}
}

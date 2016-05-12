package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила, прерывающего каст других скилов.
 *
 * @author Ronn
 */
public class CancelCast extends Strike
{
	public CancelCast(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		if(!target.isNpc())
			// обрываем каст скила цели
			target.abortCast(true);

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		return local.getNextAttackInfo();
	}
}

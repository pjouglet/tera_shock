package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила восстанавливающего мп.
 *
 * @author Ronn
 */
public class ManaHealPercent extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public ManaHealPercent(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// сила хила скила
		int power = getPower();

		if(power < 1)
			return;

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		Array<Character> targets = local.getNextCharList();

		addTargets(targets, character, targetX, targetY, targetZ);

		Character[] array = targets.array();

		for(int i = 0, length = targets.size(); i < length; i++)
		{
			Character target = array[i];

			if(target.isDead() || target.isInvul())
				continue;

			addEffects(character, target);

			int heal = target.getMaxMp() / 100 * power;

			// хилим таргет
			target.skillHealMp(getDamageId(), heal, character);
		}
	}
}

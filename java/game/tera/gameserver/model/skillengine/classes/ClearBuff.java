package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;

import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила, снимающий все бафы с цели.
 *
 * @author Ronn
 */
public class ClearBuff extends AbstractSkill
{
	public ClearBuff(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем список персонажей
		Array<Character> targets = local.getNextCharList();

		// вносим цели
		addTargets(targets, character, targetX, targetY, targetZ);

		Character[] array = targets.array();

		for(int i = 0, length = targets.size(); i < length; i++)
    	{
			Character target = array[i];

    		if(target.isDead() || target.isInvul())
    			continue;

    		EffectList effectList = target.getEffectList();

    		if(effectList == null || effectList.size() < 1)
    			continue;

    		Array<Effect> effects = effectList.getEffects();

			Effect[] effectArray = effects.array();

			for(int g = 0, size = effects.size(); g < size; g++)
			{
				Effect effect = effectArray[g];

				if(effect == null || effect.isAura() || effect.isDebuff())
					continue;

				effect.exit();
			}
    	}
	}
}

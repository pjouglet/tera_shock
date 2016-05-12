package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель прыжковых скилов.
 *
 * @author Ronn
 */
public class Jump extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public Jump(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public boolean isIgnoreBarrier()
	{
		return true;
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
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
    	}
	}
}

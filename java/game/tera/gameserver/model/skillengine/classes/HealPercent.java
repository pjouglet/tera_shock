package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель исцеляющего скила.
 *
 * @author Ronn
 */
public class HealPercent extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public HealPercent(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
    {
		//сила хила скила
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

    		int heal = target.getMaxHp() / 100 * power;

    		//хилим таргет
    		target.skillHealHp(getDamageId(), heal, character);

    		// если цель в ПвП режиме а кастер нет
    		if(target.isPvPMode() && !character.isPvPMode())
    		{
    			// включаем пвп режим
    			character.setPvPMode(true);

    			// включаем боевую стойку
    			character.startBattleStance(target);
    		}
    	}
    }
}

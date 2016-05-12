package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель эффекта, ресторещего периодически мп.
 * 
 * @author Ronn
 */
public class HealOverTime extends AbstractEffect
{
	/**
	 * @param template темплейт эффекта.
	 * @param effector тот кто наложил эффект.
	 * @param effected тот на кого наложили эффект.
	 * @param skill скил, которым наложили.
	 */
	public HealOverTime(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	public boolean onActionTime()
	{
		Character effected = getEffected();
		
		if(effected == null)
			return false;

		if(effected.getCurrentHp() >= effected.getMaxHp())
			return true;
		
		effected.effectHealHp(template.getPower(), effector);
		
		return true;
	}
}

package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель эффекта, ресторещего периодически мп.
 * 
 * @author Ronn
 */
public class PercentHealOverTime extends AbstractEffect
{
	/**
	 * @param template темплейт эффекта.
	 * @param effector тот кто наложил эффект.
	 * @param effected тот на кого наложили эффект.
	 * @param skill скил, которым наложили.
	 */
	public PercentHealOverTime(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	public boolean onActionTime()
	{
		if(effected.getCurrentHp() >= effected.getMaxHp())
			return true;
		
		effected.effectHealHp(effected.getMaxHp() * template.getPower() / 100, effector);
		
		return true;
	}
}

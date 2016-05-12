package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модеэлт эффекта хила.
 * 
 * @author Ronn
 */
public class HealMod extends AbstractEffect
{
	public HealMod(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	public boolean onActionTime()
	{
		Character effected = getEffected();
		Character effector = getEffector();
		
		if(effected == null || effector == null)
			return false;
		
		if(effected.getCurrentHp() >= effected.getMaxHp())
			return true;
		
		//сила хила скила
		int power = template.getPower();	
		// увеличиваем на процентный бонус хилера
		power = (int) (power * effector.calcStat(StatType.HEAL_POWER_PERCENT, 1, null, null));
		// добавляем статичный бонус хилера
		power += (effector.calcStat(StatType.HEAL_POWER_STATIC, 0, null, null) * template.getValue());			
				
		if(power < 1)
			return true;
				
		effected.effectHealHp(power, effector);
		
		return true;
	}
}

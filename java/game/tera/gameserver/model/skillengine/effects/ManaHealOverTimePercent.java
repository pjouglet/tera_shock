package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель эффекта, процентно ресторещего периодически мп.
 *
 * @author Ronn
 */
public class ManaHealOverTimePercent extends ManaHealOverTime
{
	public ManaHealOverTimePercent(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	protected int getPower(Character effector, Character effected)
	{
		return effector.getMaxMp() / 100 * getTemplate().getPower();
	}
	@Override
	protected boolean isDone(Character effector, Character effected)
	{
		int limit = getTemplate().getLimit();

		if(limit == -1)
			return super.isDone(effector, effected);

		return effected.getCurrentMpPercent() >= limit;
	}
}

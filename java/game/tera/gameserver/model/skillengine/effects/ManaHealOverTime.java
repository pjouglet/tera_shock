package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель эффекта, ресторещего периодически мп.
 *
 * @author Ronn
 */
public class ManaHealOverTime extends AbstractEffect
{
	/**
	 * @param template темплейт эффекта.
	 * @param effector тот кто наложил эффект.
	 * @param effected тот на кого наложили эффект.
	 * @param skill скил, которым наложили.
	 */
	public ManaHealOverTime(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	protected int getPower(Character effector, Character effected)
	{
		return getTemplate().getPower();
	}

	protected boolean isDone(Character effector, Character effected)
	{
		return effected.getCurrentMp() >= effected.getMaxMp();
	}

	@Override
	public boolean onActionTime()
	{
		Character effected = getEffected();
		Character effector = getEffector();

		if(effected == null || effector == null)
		{
			LOGGER.warning(this, new Exception("not found effected or effector"));
			return false;
		}

		// если эффект выполненл свою цель, выходим
		if(isDone(effector, effected))
			return true;

		// хилим
		effected.effectHealMp(getPower(effector, effected), effector);

		return true;
	}
}

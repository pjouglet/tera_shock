package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель эффекта, который наносит промежуточно процентный урон.
 *
 * @author Ronn
 */
public class DamOverTimePercent extends DamOverTime
{
	public DamOverTimePercent(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	protected int getDamage(Character effector, Character effected)
	{
		return (int) (effected.getMaxHp() / 100F * getTemplate().getPower());
	}
}

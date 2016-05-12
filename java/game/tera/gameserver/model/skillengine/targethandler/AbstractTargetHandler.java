package tera.gameserver.model.skillengine.targethandler;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Базовая реализация хандлера рассчета целей.
 *
 * @author Ronn
 */
public abstract class AbstractTargetHandler implements TargetHandler
{
	/**
	 * Определяем, можно ли применять в принципе такой скил на эту цель.
	 *
	 * @param caster кастующий скил.
	 * @param target проверяемая цель.
	 * @return можно ли применять.
	 */
	protected boolean checkTarget(Character caster, Character target)
	{
		return caster.checkTarget(target);
	}

	/**
	 * Обновление точки приминения.
	 *
	 * @param skill сприменяемый скил.
	 * @param targetX координата.
	 * @param targetY координата.
	 * @param targetZ координата.
	 */
	protected void updateImpact(Skill skill, float targetX, float targetY, float targetZ)
	{
		skill.setImpactX(targetX);
		skill.setImpactY(targetY);
		skill.setImpactZ(targetZ);
	}
}

package tera.gameserver.model.skillengine.funcs.stat;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.lambdas.Lambda;

/**
 * Модель математической функция.
 *
 * @author Ronn
 */
public class MathFunc extends AbstractStatFunc
{
	public MathFunc(StatType stat, int order, Condition condition, Lambda lambda)
	{
		super(stat, order, condition, lambda);
	}

	@Override
	public float calc(Character attacker, Character attacked, Skill skill, float val)
	{
		if(condition == null || condition.test(attacker, attacked, skill, val))
			return lambda.calc(val);

		return val;
	}
}

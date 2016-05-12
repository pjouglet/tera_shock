package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.Skill;

public final class ConditionLogicNot extends AbstractCondition
{
	/** кондишен */
	private Condition condition;
	
	/**
	 * @param condition
	 */
	public ConditionLogicNot(Condition condition)
	{
		super();
		
		this.condition = condition;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		return !condition.test(attacker, attacked, skill, val);
	}
}

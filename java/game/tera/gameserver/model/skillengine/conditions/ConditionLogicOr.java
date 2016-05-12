package tera.gameserver.model.skillengine.conditions;

import java.util.Arrays;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.Skill;


public final class ConditionLogicOr extends AbstractCondition
{
	/** кондишены */
	private Condition[] conditions;
	
	public ConditionLogicOr()
	{
		super();
		
		conditions = Condition.EMPTY_CONDITIONS;
	}

	/** добавление нового кондишена */
	public void add(Condition condition)
	{
		if(condition == null)
			return;
		
		int index = conditions.length;
		
		conditions = Arrays.copyOf(conditions, index + 1);
		
		conditions[index] = condition;
	}
	
	/**
	 * @return true если пустой
	 */
	public boolean isEmpty()
	{
		return conditions.length == 0;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		for(Condition condition : conditions)
			if(condition.test(attacker, attacked, skill, val))
				return true;
		
		return true;
	}
}

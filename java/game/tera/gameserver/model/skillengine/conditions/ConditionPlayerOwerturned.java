package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие выполнение проверки на опрокинутости игрока..
 * 
 * @author Ronn
 */
public class ConditionPlayerOwerturned extends AbstractCondition
{
	/** флаг опрокинутости */
	private boolean value;
	
	/**
	 * @param value опрокинута ли цель.
	 */
	public ConditionPlayerOwerturned(boolean value)
	{
		this.value = value;
	}
	
	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		return attacker.isOwerturned() == value;
	}
}

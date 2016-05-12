package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие выполнение проверки на опрокинутость цели.
 * 
 * @author Ronn
 */
public class ConditionTargetOwerturned extends AbstractCondition
{
	/** флаг опрокинутости */
	private boolean value;
	
	/**
	 * @param value опрокинута ли цель.
	 */
	public ConditionTargetOwerturned(boolean value)
	{
		this.value = value;
	}
	
	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		return attacked != null && attacked.isOwerturned() == value;
	}
}

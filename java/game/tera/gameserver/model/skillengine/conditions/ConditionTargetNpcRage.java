package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие для выполнение проверки на тип атакуемго нпс.
 * 
 * @author Ronn
 */
public class ConditionTargetNpcRage extends AbstractCondition
{
	/**флаг ярости */
	private boolean value;
	
	/**
	 * @param value должен ли находится нпс в ярости.
	 */
	public ConditionTargetNpcRage(boolean value)
	{
		this.value = value;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		if(attacked == null)
			return false;
		
		if(value)
			return attacked.isNpc() && attacked.getCurrentHpPercent() < 35;
		else
			return attacked.isNpc() && attacked.getCurrentHpPercent() > 35;
	}
}

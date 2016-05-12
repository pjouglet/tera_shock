package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие для выполнение проверки на тип атакуемго нпс.
 * 
 * @author Ronn
 */
public class ConditionAttackerNpcRage extends AbstractCondition
{
	/**флаг ярости */
	private boolean value;
	
	/**
	 * @param value должен ли находится нпс в ярости.
	 */
	public ConditionAttackerNpcRage(boolean value)
	{
		this.value = value;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		if(value)
			return attacker.isNpc() && attacker.getCurrentHpPercent() < 35;
		else
			return attacker.isNpc() && attacker.getCurrentHpPercent() > 35;
	}
}

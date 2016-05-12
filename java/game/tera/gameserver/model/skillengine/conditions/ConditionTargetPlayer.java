package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие выполнение проверки цели на игрока.
 * 
 * @author Ronn
 */
public class ConditionTargetPlayer extends AbstractCondition
{
	/** значение флага */
	private boolean value;
	
	/**
	 * @param value флаг.
	 */
	public ConditionTargetPlayer(boolean value)
	{
		this.value = value;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		if(attacker == null || attacked == null)
			return false;
		
		return attacked.isPlayer() == value;
	}
}

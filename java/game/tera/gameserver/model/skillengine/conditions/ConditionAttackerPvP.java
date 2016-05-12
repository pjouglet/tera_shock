package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Кондишен, является ли атакующий playable
 * 
 * @author Ronn
 */
public class ConditionAttackerPvP extends AbstractCondition
{
	private boolean value;
	
	/**
	 * @param value
	 */
	public ConditionAttackerPvP(boolean value)
	{
		this.value = value;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		return attacker.isPlayer() == value;
	}
}

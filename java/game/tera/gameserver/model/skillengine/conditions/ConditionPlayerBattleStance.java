package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * @author Ronn
 */
public final class ConditionPlayerBattleStance extends AbstractCondition
{
	private boolean value;
	
	/**
	 * @param value
	 */
	public ConditionPlayerBattleStance(boolean value)
	{
		this.value = value;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		return attacker.isBattleStanced() == value;
	}
}

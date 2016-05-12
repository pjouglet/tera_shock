package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Кондишен на текущее состояние хп у игрока
 * 
 * @author Ronn
 */
public final class ConditionPlayerPercentHP extends AbstractCondition
{
	private int percent;
	
	/**
	 * @param percent
	 */
	public ConditionPlayerPercentHP(int percent)
	{
		this.percent = percent;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		return attacker.getCurrentHpPercent() <= percent;
	}
}

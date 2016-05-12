package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие для выполнение проверки на тип атакуемго нпс.
 * 
 * @author Ronn
 */
public class ConditionTargetAggroMe extends AbstractCondition
{
	/**флаг ярости */
	private boolean value;
	
	/**
	 * @param value должен ли находится нпс в ярости.
	 */
	public ConditionTargetAggroMe(boolean value)
	{
		this.value = value;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		if(attacked == null || !attacked.isNpc())
			return false;
		
		Npc npc = attacked.getNpc();
		
		Character top = npc.getMostHated();
		
		if(value)
			return top == attacker;
		else
			return top != attacker;
	}
}

package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.npc.NpcType;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие для выполнение проверки на тип атакуемго нпс.
 *
 * @author Ronn
 */
public class ConditionAttackerNpcType extends AbstractCondition
{
	/** необходимый тип нпс */
	private NpcType type;

	/**
	 * @param type тип нпс.
	 */
	public ConditionAttackerNpcType(NpcType type)
	{
		this.type = type;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		return attacker.isNpc() && attacker.getNpc().getNpcType() == type;
	}
}

package tera.gameserver.model.skillengine.conditions;

import rlib.util.array.Arrays;
import tera.gameserver.model.Character;
import tera.gameserver.model.npc.NpcType;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие для выполнения проверки на типы атакуемых нпс.
 *
 * @author Ronn
 */
public class ConditionAttackerNpcTypes extends AbstractCondition
{
	/** доступные типы нпс */
	private NpcType[] types;

	/**
	 * @param types типы нпс.
	 */
	public ConditionAttackerNpcTypes(NpcType[] types)
	{
		this.types = types;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		if(attacker.isNpc())
			return Arrays.contains(types, attacker.getNpc().getTemplateType());

		return false;
	}
}

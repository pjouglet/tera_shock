package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.NpcBarrier;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель условия, проверяющая наличие барьера около игрока.
 *
 * @author Ronn
 */
public final class ConditionPlayerNotBarrier extends AbstractCondition
{
	private boolean value;

	public ConditionPlayerNotBarrier(boolean value)
	{
		this.value = value;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		if(skill == null)
			return true;

		return World.getAroundCount(NpcBarrier.class, attacker, Math.abs(skill.getMoveDistance())) > 0? false == value : true == value;
	}
}

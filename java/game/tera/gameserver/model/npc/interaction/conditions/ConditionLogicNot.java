package tera.gameserver.model.npc.interaction.conditions;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;

/**
 * Обертка для реализации отрицания кондишена.
 *
 * @author Ronn
 */
public final class ConditionLogicNot implements Condition
{
	/** кондишен */
	private Condition condition;

	/**
	 * @param condition
	 */
	public ConditionLogicNot(Condition condition)
	{
		this.condition = condition;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		return !condition.test(npc, player);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + condition + "]";
	}
}

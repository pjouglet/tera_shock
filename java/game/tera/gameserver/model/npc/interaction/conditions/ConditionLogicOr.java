package tera.gameserver.model.npc.interaction.conditions;

import rlib.util.array.Arrays;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;

/**
 * Объеденяющее условие для реализации <or>
 *
 * @author Ronn
 */
public final class ConditionLogicOr implements Condition
{
	/** условия */
	private Condition[] conditions;

	public ConditionLogicOr()
	{
		conditions = Condition.EMPTY_CONDITIONS;
	}

	/**
	 * Добавление нового условия.
	 */
	public void add(Condition condition)
	{
		if(condition == null)
			return;

		// добавляем новый кондишен
		conditions = Arrays.addToArray(conditions, condition, Condition.class);
	}

	/**
	 * @return массив условий.
	 */
	private final Condition[] getConditions()
	{
		return conditions;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		// получаем условия
		Condition[] conditions = getConditions();

		// если их нет, возвращаем ок
		if(conditions.length < 1)
			return true;

		// перебираем
		for(int i = 0, length = conditions.length; i < length; i++)
			// если хоть один выполнен, возвращаем ок
			if(conditions[i].test(npc, player))
				return true;

		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + Arrays.toString(conditions);
	}
}

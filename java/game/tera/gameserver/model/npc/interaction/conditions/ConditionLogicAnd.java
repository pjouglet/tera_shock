package tera.gameserver.model.npc.interaction.conditions;

import rlib.util.array.Arrays;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;

/**
 * Объеденяющее условие для реализации <and>
 *
 * @author Ronn
 */
public final class ConditionLogicAnd implements Condition
{
	/** объедененные условия */
	private Condition[] conditions;

	public ConditionLogicAnd()
	{
		conditions = Condition.EMPTY_CONDITIONS;
	}

	/**
	 * Добавление нового условия.
	 */
	public void add(Condition condition)
	{
		// если его нет, выходим
		if(condition == null)
			return;

		conditions = Arrays.addToArray(conditions, condition, Condition.class);
	}

	/**
	 * @return массив кондишенов.
	 */
	private final Condition[] getConditions()
	{
		return conditions;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		Condition[] conditions = getConditions();

		// если условий нет, выходим
		if(conditions.length < 1)
			return true;

		// если хоть 1 условие не сработало, выходим
		for(int i = 0, length = conditions.length; i < length; i++)
			if(!conditions[i].test(npc, player))
				return false;

		// возвращаем все ок
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + Arrays.toString(conditions);
	}
}

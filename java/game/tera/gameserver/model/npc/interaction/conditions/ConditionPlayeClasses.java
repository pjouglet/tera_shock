package tera.gameserver.model.npc.interaction.conditions;

import rlib.util.array.Arrays;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;

/**
 * Условие проверки класса игрока.
 *
 * @author Ronn
 */
public class ConditionPlayeClasses extends AbstractCondition
{
	/** нужные классы игрока */
	private PlayerClass[] classes;

	public ConditionPlayeClasses(Quest quest, PlayerClass[] classes)
	{
		super(quest);

		this.classes = classes;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		if(player == null)
			return false;

		// проверяем, есть ли класс игрока в доступных
		return Arrays.contains(classes, player.getPlayerClass());
	}

	@Override
	public String toString()
	{
		return "ConditionPlayerClasses classes = " + Arrays.toString(classes);
	}
}

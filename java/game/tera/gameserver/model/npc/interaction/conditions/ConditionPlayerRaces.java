package tera.gameserver.model.npc.interaction.conditions;

import rlib.util.array.Arrays;

import tera.gameserver.model.base.Race;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;

/**
 * Условие проверки расы игрока.
 *
 * @author Ronn
 */
public class ConditionPlayerRaces extends AbstractCondition
{
	/** нужные классы игрока */
	private Race[] races;

	public ConditionPlayerRaces(Quest quest, Race[] races)
	{
		super(quest);

		this.races = races;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		if(player == null)
			return false;

		// проверяем, есть ли раса игрока в доступных
		return Arrays.contains(races, player.getRace());
	}

	@Override
	public String toString()
	{
		return "ConditionPlayerRaces races = " + Arrays.toString(races);
	}
}

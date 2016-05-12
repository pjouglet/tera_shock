package tera.gameserver.model.npc.interaction.conditions;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;

/**
 * Условие на проверку ид нпс.
 *
 * @author Ronn
 */
public class ConditionNpcId extends AbstractCondition
{
	/** ид нпс */
	private int id;
	/** тип */
	private int type;

	public ConditionNpcId(Quest quest, int id, int type)
	{
		super(quest);

		this.id = id;
		this.type = type;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		if(npc == null)
			return false;

		// проверяем, нужный ли нпс
		return npc.getTemplateId() == id && npc.getTemplateType() == type;
	}

	@Override
	public String toString()
	{
		return "ConditionNpcId id = " + id + ", type = " + type;
	}
}

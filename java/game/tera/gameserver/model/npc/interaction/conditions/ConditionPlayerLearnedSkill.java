package tera.gameserver.model.npc.interaction.conditions;

import tera.gameserver.manager.SkillLearnManager;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;

/**
 * @author Ronn
 */
public class ConditionPlayerLearnedSkill extends AbstractCondition
{
	/** ид скила */
	private int id;

	public ConditionPlayerLearnedSkill(Quest quest, int id)
	{
		super(quest);

		this.id = id;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		if(player == null)
			return false;

		// получаем менеджера изучаемых скилов
		SkillLearnManager learnManager = SkillLearnManager.getInstance();

		return learnManager.isLearned(id, player);
	}

	@Override
	public String toString()
	{
		return "ConditionPlayerLearnedSkill  id = " + id;
	}
}

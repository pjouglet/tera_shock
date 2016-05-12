package tera.gameserver.model.npc.interaction.conditions;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestList;

/**
 * Условие проверки на выполнение квеста.
 * 
 * @author Ronn
 */
public class ConditionQuestComplete extends AbstractCondition
{
	/** ид проверяемого квеста */
	private int questId;
	
	public ConditionQuestComplete(Quest quest, int questId)
	{
		super(quest);
		
		this.questId = questId;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		// если игрока нет, возвращаем плохо
		if(player == null)
		{
			log.warning(this, "not found player");
			return false;
		}
		
		// получаем список квестов игрока
		QuestList questList = player.getQuestList();
		
		// если его нет, возвращаем плохо
		if(questList == null)
		{
			log.warning(this, "not found quest list");
			return false;
		}
		
		return questList.isCompleted(questId);
	}

	@Override
	public String toString()
	{
		return "ConditionQuestComplete questId = " + questId;
	}
}

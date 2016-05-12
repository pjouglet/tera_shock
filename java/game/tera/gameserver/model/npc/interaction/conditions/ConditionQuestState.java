package tera.gameserver.model.npc.interaction.conditions;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestState;

/**
 * Условие проверки стадии квеста.
 * 
 * @author Ronn
 */
public class ConditionQuestState extends AbstractCondition
{
	/** необходимое состояние квеста */
	private int state;
	
	public ConditionQuestState(Quest quest, int state)
	{
		super(quest);
		
		this.state = state;
	}

	/**
	 * @return нужная стадия квеста.
	 */
	private final int getState()
	{
		return state;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return false;
		}
		
		QuestList questList = player.getQuestList();
		
		// если его нет, возвращаем плохо
		if(questList == null)
		{
			log.warning(this, "not found quest list");
			return false;
		}
				
		// получаем состояние квеста
		QuestState state = questList.getQuestState(quest);
		
		if(state == null)
			return getState() == 0;
		
		return state != null && state.getState() == getState();
	}

	@Override
	public String toString()
	{
		return "ConditionQuestState state = " + state;
	}
}

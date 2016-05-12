package tera.gameserver.model.npc.interaction.conditions;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestDate;
import tera.gameserver.model.quests.QuestList;

/**
 * Условие проверки на время, после выполения квеста.
 * 
 * @author Ronn
 */
public class ConditionQuestAgoComplete extends AbstractCondition
{
	/** сколько времени назад был выполнен квест */
	private long time;
	
	public ConditionQuestAgoComplete(Quest quest, long time)
	{
		super(quest);
		
		this.time = time * 60 * 1000;
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
		
		// получаем временной штамп квеста
		QuestDate date = questList.getQuestDate(quest.getId());
		
		if(date == null)
			return false;
		
		// проверяем, прошло ли нужное кол-во времени
		return System.currentTimeMillis() - date.getTime() > time;
	}

	@Override
	public String toString()
	{
		return "ConditionQuestAgoComplete time = " + time;
	}
}

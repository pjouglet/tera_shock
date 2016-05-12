package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.Reward;


/**
 * Акшен выдачи награды квеста.
 * 
 * @author Ronn
 */
public class ActionAddReward extends AbstractQuestAction
{
	public ActionAddReward(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);
	}

	@Override
	public void apply(QuestEvent event)
	{
		// если это нужный квест
		if(event.getQuest() == quest)
		{
			// получаем награду за квест
			Reward reward = quest.getReward();
			
			// если ее нет, выходим
			if(reward == null)
			{
				log.warning(this, new Exception("not found reward"));
				return;
			}
			
			// выдаем награду
			reward.giveReward(event);
		}
	}
}

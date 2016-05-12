package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;


/**
 * Акшен завершения квеста.
 * 
 * @author Ronn
 */
public class ActionQuestFinish extends AbstractQuestAction
{
	public ActionQuestFinish(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);
	}

	@Override
	public void apply(QuestEvent event)
	{
		if(event.getQuest() == quest)
			quest.finish(event);
	}
}

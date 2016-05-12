package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Слушатель принятия квестов.
 *
 * @author Ronn
 */
public class AcceptedQuestListener extends AbstractQuestEventListener
{
	public AcceptedQuestListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);
	}

	@Override
	public void notifyQuest(QuestEvent event)
	{
		if(event.getQuest().getId() == quest.getId())
			super.notifyQuest(event);
	}
}

package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Слушатель отмены квестов.
 *
 * @author Ronn
 */
public class CanceledQuestListener extends AbstractQuestEventListener
{
	public CanceledQuestListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);
	}

	@Override
	public void notifyQuest(QuestEvent event)
	{
		if(event.getQuest() == quest)
			super.notifyQuest(event);
	}
}

package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEventType;


/**
 * Пустой прослушиватель.
 * 
 * @author Ronn
 */
public class EmptyListener extends AbstractQuestEventListener
{
	public EmptyListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);
	}
}

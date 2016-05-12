package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Слушатель окончания квестового мувика.
 *
 * @author Ronn
 */
public class QuestMovieListener extends AbstractQuestEventListener
{
	/** искомый ид мувика */
	private int id;

	public QuestMovieListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);

		try
		{
			this.id = VarTable.newInstance(node).getInteger("id");
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void notifyQuest(QuestEvent event)
	{
		if(event.getQuest() == quest && event.getValue() == id)
			super.notifyQuest(event);
	}
}

package tera.gameserver.model.quests.events;

import java.util.Arrays;

import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventListener;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Базовая модель слушателя квестов.
 *
 * @author Ronn
 */
public abstract class AbstractQuestEventListener implements QuestEventListener
{
	protected static final Logger log = Loggers.getLogger(AbstractQuestEventListener.class);

	/** тип обрабатываемых ивентов */
	protected QuestEventType type;
	/** квест, к которому принадлежит этот слушатель */
	protected Quest quest;
	/** набор акшенов при выполнении события */
	protected QuestAction[] actions;

	public AbstractQuestEventListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		try
		{
			this.type = type;
			this.actions = actions;
			this.quest = quest;
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	/**
	 * @return список акшенов ивента.
	 */
	protected final QuestAction[] getActions()
	{
		return actions;
	}

	@Override
	public final QuestEventType getType()
	{
		return type;
	}

	@Override
	public void notifyQuest(QuestEvent event)
	{
		// получаем акшены ивента
		QuestAction[] actions = getActions();

		// перебираем их
		for(int i = 0, length = actions.length; i < length; i++)
		{
			// получаем акшен
			QuestAction action = actions[i];

			// если условие акшена выполняются
			if(action.test(event.getNpc(), event.getPlayer()))
				// применяем акшен
				action.apply(event);
		}
	}

	@Override
	public String toString()
	{
		return "AbstractQuestEventListener type = " + type + ", quest = " + quest.getId() + ", actions = " + Arrays.toString(actions);
	}
}

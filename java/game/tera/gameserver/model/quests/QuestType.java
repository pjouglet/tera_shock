package tera.gameserver.model.quests;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Node;

import rlib.logging.Loggers;
import tera.gameserver.model.quests.classes.DealyQuest;
import tera.gameserver.model.quests.classes.LevelUpQuest;
import tera.gameserver.model.quests.classes.StoryQuest;
import tera.gameserver.model.quests.classes.ZoneQuest;


/**
 * Перечисление типов квестов.
 *
 * @author Ronn
 */
public enum QuestType
{
	/** территориальные квесты */
	ZONE_QUEST(ZoneQuest.class, true),
	/** квесты гильдий */
	GUILD_QUEST(StoryQuest.class, true),
	/** переодические квесты */
	DEALY_QUEST(DealyQuest.class, true),
	/** авто кевесты при взятии уровня */
	LEVEL_UP_QUEST(LevelUpQuest.class, false),
	/** сюжетные квесты */
	STORY_QUEST(StoryQuest.class, true);

	/** конструктор квеста */
	private Constructor<? extends Quest> constructor;

	/** отменяемые ли квесты данного типа */
	private boolean cancelable;

	/**
	 * @param questClass класс квеста.
	 */
	private QuestType(Class<? extends Quest> questClass, boolean cancelable)
	{
		try
		{
			this.constructor = questClass.getConstructor(QuestType.class, Node.class);
			this.cancelable = cancelable;
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			Loggers.warning(this, e);
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @return отменяемы ли.
	 */
	public boolean isCancelable()
	{
		return cancelable;
	}

	/**
	 * Создание нового квеста.
	 *
	 * @param node хмл дерево.
	 * @return новый квест.
	 */
	public Quest newInstance(Node node)
	{
		try
		{
			return constructor.newInstance(this, node);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Loggers.warning(this, e);
		}

		return null;
	}
}

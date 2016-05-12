package tera.gameserver.model.quests;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Node;

import rlib.logging.Loggers;
import tera.gameserver.model.quests.events.AcceptedQuestListener;
import tera.gameserver.model.quests.events.AddNpcListener;
import tera.gameserver.model.quests.events.CanceledQuestListener;
import tera.gameserver.model.quests.events.CollectResourseListener;
import tera.gameserver.model.quests.events.EmptyListener;
import tera.gameserver.model.quests.events.FinishedQuestListener;
import tera.gameserver.model.quests.events.InventoryAddItemListener;
import tera.gameserver.model.quests.events.InventoryRemoveItemListener;
import tera.gameserver.model.quests.events.KillNpcListener;
import tera.gameserver.model.quests.events.LinkSelectListener;
import tera.gameserver.model.quests.events.PickUpItemListener;
import tera.gameserver.model.quests.events.QuestMovieListener;
import tera.gameserver.model.quests.events.SkillLearnListener;
import tera.gameserver.model.quests.events.UseItemListener;

/**
 * Перечисление типов ивентов.
 *
 * @author Ronn
 */
public enum QuestEventType
{
	/** завершение квеста */
	FINISHED_QUEST(FinishedQuestListener.class),
	/** взятие квеста */
	ACCEPTED_QUEST(AcceptedQuestListener.class),
	/** отмена квеста */
	CANCELED_QUEST(CanceledQuestListener.class),
	/** окончания просмотра мувика */
	QUEST_MOVIE_ENDED(QuestMovieListener.class),
	/** убийство моба */
	KILL_NPC(KillNpcListener.class),
	/** сбор ресурса */
	COLLECT_RESOURSE(CollectResourseListener.class),
	/** изменение стамины */
	CHANGED_HEART(EmptyListener.class),
	/** использование итема */
	USE_ITEM(UseItemListener.class),
	/** нажатие на кнопку */
	SELECT_BUTTON(LinkSelectListener.class),
	/** слушатель поднятий итемов */
	PICK_UP_ITEM(PickUpItemListener.class),
	/** слушатель добавлений в инвентарь */
	INVENTORY_ADD_ITEM(InventoryAddItemListener.class),
	/** слушатель удаление из инвенторя */
	INVENTORY_REMOVE_ITEM(InventoryRemoveItemListener.class),
	/** вход игрока в мир */
	PLAYER_SPAWN(EmptyListener.class),
	/** добовление отображения нпс игроку */
	ADD_NPC(AddNpcListener.class),
	/** событие изучение скила игроком */
	SKILL_LEARNED(SkillLearnListener.class),
	/** событие нажатия на ссылку */
	SELECT_LINK(LinkSelectListener.class);

	/** конструктор квеста */
	private Constructor<? extends QuestEventListener> constructor;

	/**
	 * @param eventClass класс обработчик ивента.
	 */
	private QuestEventType(Class<? extends QuestEventListener> eventClass)
	{
		try
		{
			constructor = eventClass.getConstructor(getClass(), QuestAction[].class, Quest.class, Node.class);
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			Loggers.warning(this, e);
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Создание нового слушателя под указанный квест.
	 *
	 * @param quest целевой квест.
	 * @param actions набор акшенов ивента.
	 * @param node набор параметров с хмл.
	 * @return новый слушатель.
	 */
	public QuestEventListener newInstance(Quest quest, QuestAction[] actions, Node node)
	{
		try
		{
			return constructor.newInstance(this, actions, quest, node);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			return null;
		}
	}
}

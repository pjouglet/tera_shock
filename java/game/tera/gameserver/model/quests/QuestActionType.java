package tera.gameserver.model.quests;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Node;

import rlib.logging.Loggers;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.quests.actions.ActionAddExp;
import tera.gameserver.model.quests.actions.ActionAddItem;
import tera.gameserver.model.quests.actions.ActionAddReward;
import tera.gameserver.model.quests.actions.ActionAddVar;
import tera.gameserver.model.quests.actions.ActionClearVar;
import tera.gameserver.model.quests.actions.ActionDropItem;
import tera.gameserver.model.quests.actions.ActionEventMessage;
import tera.gameserver.model.quests.actions.ActionFinishQuest;
import tera.gameserver.model.quests.actions.ActionMoveToPanel;
import tera.gameserver.model.quests.actions.ActionQuestCancel;
import tera.gameserver.model.quests.actions.ActionQuestFinish;
import tera.gameserver.model.quests.actions.ActionQuestMovie;
import tera.gameserver.model.quests.actions.ActionQuestStart;
import tera.gameserver.model.quests.actions.ActionRemoveItem;
import tera.gameserver.model.quests.actions.ActionSetNpcIcon;
import tera.gameserver.model.quests.actions.ActionSetQuestState;
import tera.gameserver.model.quests.actions.ActionShowQuestInfo;
import tera.gameserver.model.quests.actions.ActionStateQuest;
import tera.gameserver.model.quests.actions.ActionSubVar;
import tera.gameserver.model.quests.actions.ActionSystemMessage;
import tera.gameserver.model.quests.actions.ActionUpdateCounter;
import tera.gameserver.model.quests.actions.ActionUpdateIntresting;
import tera.gameserver.model.quests.actions.ActionUpdateItemCounter;

/**
 * Перечисление видов действий в квестах.
 *
 * @author Ronn
 */
public enum QuestActionType
{
	/** начало квеста */
	QUEST_START(ActionStateQuest.class),
	/** перемещение квеста на панель */
	QUEST_MOVE_TO_PANEL(ActionMoveToPanel.class),
	/** акшен отображения ролика */
	QUEST_MOVIE(ActionQuestMovie.class),
	/** перемещение квеста на панель */
	QUEST_FINISH(ActionFinishQuest.class),
	/** отправка событийного сообщения */
	EVENT_MESSAGE(ActionEventMessage.class),
	/** системное сообщение */
	SYSTEM_MESSAGE(ActionSystemMessage.class),
	/** запуск квеста */
	START_QUEST(ActionQuestStart.class),
	/** завершение квеста */
	FINISH_QUEST(ActionQuestFinish.class),
	/** отмена квеста */
	CANCEL_QUEST(ActionQuestCancel.class),
	/** приплюсовать к переменной */
	ADD_VAR(ActionAddVar.class),
	/** выдача опыта */
	ADD_EXP(ActionAddExp.class),
	/** выдача итемов */
	ADD_ITEM(ActionAddItem.class),
	/** акшен выдачи награды */
	ADD_REWARD(ActionAddReward.class),
	/** минусовать от квест. переменной */
	SUB_VAR(ActionSubVar.class),
	/** акшен для создание дропа */
	DROP_ITEM(ActionDropItem.class),
	/** удаление итема */
	REMOVE_ITEM(ActionRemoveItem.class),
	/** выдача квест стейта */
	QUEST_STATE(ActionSetQuestState.class),
	/** подсветка нпс */
	SET_NPC_ICON(ActionSetNpcIcon.class),
	/** обновить содежрание квестов у нпс */
	UPDATE_INTRESTING(ActionUpdateIntresting.class),
	/** обновление счетчика */
	UPDATE_COUNTER(ActionUpdateCounter.class),
	/** обновление счетчика итемов */
	UPDATE_ITEM_COUNTER(ActionUpdateItemCounter.class),
	/** действие по очистке переменной */
	CLEAR_VAR(ActionClearVar.class),
	/** отобразить инфу о квесте */
	SHOW_QUEST_INFO(ActionShowQuestInfo.class);

	/** конструктор акшена */
	private Constructor<? extends QuestAction> constructor;

	/**
	 * @param eventClass класс обработчик акшена.
	 */
	private QuestActionType(Class<? extends QuestAction> eventClass)
	{
		try
		{
			constructor = eventClass.getConstructor(getClass(), Quest.class, Condition.class, Node.class);
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			Loggers.warning(this, e);
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Создание нового акшена под указанный квест.
	 *
	 * @param quest целевой квест.
	 * @param node хмл параметры.
	 * @return новый акшен.
	 */
	public QuestAction newInstance(Quest quest, Condition condition, Node node)
	{
		try
		{
			return constructor.newInstance(this, quest, condition, node);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Loggers.warning(this, " quest id " + quest.getId());
			Loggers.warning(this, e);
			return null;
		}
	}
}

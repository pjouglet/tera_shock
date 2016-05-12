package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestState;

/**
 * Акшен для изминение стадии квеста.
 *
 * @author Ronn
 */
public class ActionSetQuestState extends AbstractQuestAction
{
	/** нужный стейт */
	private int state;

	public ActionSetQuestState(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		this.state = VarTable.newInstance(node).getInteger("state");
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();

		// если его неет, выходим
		if(player == null)
			return;

		// получаем список квестов
		QuestList questList = player.getQuestList();

		// если списка нет, выходим
		if(questList == null)
			return;

		// получаем состояние квеста
		QuestState state = questList.getQuestState(quest);

		// если состояния нет, выходим
		if(state == null)
			return;

		// применяем новую стадию
		state.setState(getState());

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// обновляем в БД
		dbManager.updateQuest(state);
	}

	/**
	 * @return стадия квеста.
	 */
	private final int getState()
	{
		return state;
	}

	@Override
	public String toString()
	{
		return "ActionSetQuestState state = " + state;
	}
}

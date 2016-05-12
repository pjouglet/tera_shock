package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;

import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestState;

/**
 * Действие по очистке переменной.
 *
 * @author Ronn
 */
public class ActionClearVar extends AbstractQuestAction
{
	/** название переменной */
	private String name;

	public ActionClearVar(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.name = vars.getString("var");
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();

		// если его нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		// получаем квестовый лист
		QuestList questList = player.getQuestList();

		// если его нет, выходим
		if(questList == null)
		{
			log.warning(this, "not found questList");
			return;
		}

		// получаем состояние квеста
		QuestState state = questList.getQuestState(quest);

		// если его нет, выходим
		if(state == null)
		{
			log.warning(this, "not found quest state");
			return;
		}

		// удаляем переменную
		state.removeWar(name);
	}

	@Override
	public String toString()
	{
		return "ActionClearVar  name = " + name;
	}
}

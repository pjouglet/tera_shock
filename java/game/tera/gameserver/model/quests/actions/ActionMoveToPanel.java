package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestPanelState;
import tera.gameserver.model.quests.QuestState;

/**
 * Модель акшена перемещения квеста на боковую панель.
 *
 * @author Ronn
 */
public class ActionMoveToPanel extends AbstractQuestAction
{
	public ActionMoveToPanel(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();
		// получаем квест
		Quest quest = event.getQuest();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		// если квеста нет, выходим
		if(quest == null)
		{
			log.warning(this, "not found quest");
			return;
		}

		// получаем квест лист игрока
		QuestList questList = player.getQuestList();

		if(questList == null)
		{
			log.warning(this, "not found quest list");
			return;
		}

		// получаем стейт квеста у этого игрока
		QuestState state = questList.getQuestState(quest);

		if(state == null)
		{
			log.warning(this, "not found quest state");
			return;
		}

		player.updateQuestInPanel(state, QuestPanelState.UPDATE);
	}
}

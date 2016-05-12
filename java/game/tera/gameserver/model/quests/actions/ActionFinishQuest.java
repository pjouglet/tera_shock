package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestState;
import tera.gameserver.network.serverpackets.QuestCompleted;


/**
 * Акшен удаления квеста с боковой панели.
 *
 * @author Ronn
 */
public class ActionFinishQuest extends AbstractQuestAction
{
	public ActionFinishQuest(QuestActionType type, Quest quest, Condition condition, Node node)
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

		// получаем список квеста
		QuestList questList = player.getQuestList();

		// если списка нету, выходим
		if(questList == null)
		{
			log.warning(this, "not found quest list");
			return;
		}

		// получаем состояние квеста
		QuestState state = questList.getQuestState(quest);

		// если состояния нет, выходим
		if(state == null)
		{
			log.warning(this, "not found quest state");
			return;
		}

		// отправляем пакет
		player.sendPacket(QuestCompleted.getInstance(state, false), true);
	}

	@Override
	public String toString()
	{
		return "ActionRemoveToPanel ";
	}
}

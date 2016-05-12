package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.manager.QuestManager;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;

/**
 * Акшен для запуска квеста.
 *
 * @author Ronn
 */
public class ActionQuestStart extends AbstractQuestAction
{
	/** ид запускаемого квеста */
	private int id;

	public ActionQuestStart(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		VarTable vars = VarTable.newInstance(node);

		this.id = vars.getInteger("id", 0);
	}

	@Override
	public void apply(QuestEvent event)
	{
		// если ид небыл указан, запускаем квест акшена
		if(id == 0)
			quest.start(event);
		else
		{
			// получаем менеджер квестов
			QuestManager questManager = QuestManager.getInstance();

			// получаем нужный квест
			Quest quest = questManager.getQuest(id);

			// если квеста нет, выходим
			if(quest == null)
			{
				log.warning(this, "not found quest");
				return;
			}

			// запускаем квест
			quest.start(event);
		}
	}

	@Override
	public String toString()
	{
		return "ActionQuestStart id = " + id;
	}
}

package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Просулшка добавление в отображение нпс игроку.
 *
 * @author Ronn
 */
public class AddNpcListener extends AbstractQuestEventListener
{
	/** ид нпс */
	private int id;
	/** тип нпс */
	private int type;

	public AddNpcListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.id = vars.getInteger("id");
			this.type = vars.getInteger("type");
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void notifyQuest(QuestEvent event)
	{
		// получаем нпс
		Npc npc = event.getNpc();

		// если нпс есть и ид подходит
		if(npc != null && npc.getTemplateId() == id && npc.getTemplateType() == type)
			super.notifyQuest(event);
	}
}

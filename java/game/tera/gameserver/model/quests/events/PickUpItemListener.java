package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Прослушка поднятий итемов.
 * 
 * @author Ronn
 */
public class PickUpItemListener extends AbstractQuestEventListener
{
	/** нужный ид итема */
	private int id;
	
	public PickUpItemListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);
		
		try
		{
			this.id = VarTable.newInstance(node).getInteger("id");
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void notifyQuest(QuestEvent event)
	{
		// получаем итем с ивента
		ItemInstance item = event.getItem();
		
		// если это нужный итем
		if(item != null && item.getItemId() == id)
			// пропускаем дальше ивент
			super.notifyQuest(event);
	}
}

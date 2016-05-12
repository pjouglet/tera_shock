package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Прослушка использования итемов.
 * 
 * @author Ronn
 */
public class UseItemListener extends AbstractQuestEventListener
{
	/** ид использованного итема */
	private int itemId;
	
	public UseItemListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);
		
		try
		{
			this.itemId = VarTable.newInstance(node).getInteger("id");
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void notifyQuest(QuestEvent event)
	{
		// получаем итем
		ItemInstance item = event.getItem();
		
		// если итем подходящий
		if(item != null && item.getItemId() == itemId)
			// пропускаем дальше ивент
			super.notifyQuest(event);
	}
}

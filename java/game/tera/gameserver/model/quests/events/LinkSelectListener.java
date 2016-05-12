package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Слушатель нажатий на линки.
 * 
 * @author Ronn
 */
public class LinkSelectListener extends AbstractQuestEventListener
{
	/** ид линка */
	private int id;
	
	public LinkSelectListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
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
		// получаем линк с ивента
		Link link = event.getLink();
		
		// если это нужный линк
		if(link != null && link.getId() == id)
			// пропускаем дальше ивент
			super.notifyQuest(event);
	}	
}

package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;
import tera.gameserver.model.resourse.ResourseInstance;

/**
 * Прослушка убийства нпс.
 * 
 * @author Ronn
 */
public class CollectResourseListener extends AbstractQuestEventListener
{
	/** ид нужного ресурса */
	private int templateId;
	
	public CollectResourseListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);
		
		try
		{
			VarTable vars = VarTable.newInstance(node);
			
			this.templateId = vars.getInteger("templateId");
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void notifyQuest(QuestEvent event)
	{		
		// получаем ресурс
		ResourseInstance resourse = event.getResourse();
		
		// получаем игрока
		Player player = event.getPlayer();
		
		// если кого-то из них нет ,выходим
		if(player == null || resourse == null)
		{
			log.warning(this, new Exception("not found resourse or player"));
			return;
		}

		// если нпс есть и ид и тип подходят
		if(resourse.getTemplateId() == templateId && resourse.isInRange(player, 500))
			// пропускаем дальше ивент
			super.notifyQuest(event);
	}
}

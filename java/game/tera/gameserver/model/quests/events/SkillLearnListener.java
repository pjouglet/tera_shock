package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Прослушка изучения скилов.
 *
 * @author Ronn
 */
public class SkillLearnListener extends AbstractQuestEventListener
{
	/** целевой скил */
	private int skillId;

	public SkillLearnListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.skillId = vars.getInteger("skillId", 0);
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void notifyQuest(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();

		// если его нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		if(skillId == 0 || skillId == event.getValue())
			super.notifyQuest(event);
	}
}

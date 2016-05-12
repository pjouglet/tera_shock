package tera.gameserver.model.quests.events;

import org.w3c.dom.Node;

import rlib.util.Rnd;
import rlib.util.VarTable;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;

/**
 * Прослушка убийства нпс.
 *
 * @author Ronn
 */
public class KillNpcListener extends AbstractQuestEventListener
{
	/** ид нужного нпс */
	private int npcId;
	/** тип нужного нпс */
	private int npcType;
	/** шанс срабатывания */
	private int chance;

	public KillNpcListener(QuestEventType type, QuestAction[] actions, Quest quest, Node node)
	{
		super(type, actions, quest, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.npcId = vars.getInteger("id");
			this.npcType = vars.getInteger("type");
			this.chance = vars.getInteger("chance", -1);
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

		// получаем игрока
		Player player = event.getPlayer();

		// если кого-то из них нет ,выходим
		if(player == null || npc == null)
		{
			log.warning(this, new Exception("not found npc or player"));
			return;
		}

		// если нпс есть и ид и тип подходят
		if(npc.getTemplateId() == npcId && npc.getTemplateType() == npcType && (chance < 0 || Rnd.chance(chance)) && npc.isInRange(player, 500))
			// пропускаем дальше ивент
			super.notifyQuest(event);
	}
}

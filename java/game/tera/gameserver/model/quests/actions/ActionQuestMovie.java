package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.network.serverpackets.QuestVideo;

/**
 * Акшен для отправки квест видео.
 *
 * @author Ronn
 */
public class ActionQuestMovie extends AbstractQuestAction
{
	/** ид мувика */
	private int id;

	public ActionQuestMovie(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.id = vars.getInteger("id");
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();

		//если его нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		// отправляем ему пакет
		player.sendPacket(QuestVideo.getInstance(id), true);
	}
}

package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.network.serverpackets.SystemMessage;

/**
 * Акшен для отображения квестового события.
 *
 * @author Ronn
 */
public class ActionSystemMessage extends AbstractQuestAction
{
	/** системное сообщение */
	private String message;

	public ActionSystemMessage(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			String id = "@" + vars.getString("id");

			message = vars.getString("message", "").replace('%', (char) 0x0B);

			message = message.isEmpty()? id : id + ((char) 0x0B) + message;
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
		player.sendPacket(SystemMessage.getInstance(message), true);
	}
}

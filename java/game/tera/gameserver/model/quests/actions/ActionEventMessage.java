package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.network.serverpackets.EventMessage;

/**
 * Акшен для отображения квестового события.
 * 
 * @author Ronn
 */
public class ActionEventMessage extends AbstractQuestAction
{
	/** заголовок */
	private String head;
	/** сообщение */
	private String message;
	/** инфа */
	private String info;
	
	public ActionEventMessage(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);
			
			this.head = vars.getString("head");
			this.message = vars.getString("message");
			this.info = vars.getString("info");
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
		player.sendPacket(EventMessage.getInstance(head, message, info), true);
	}

	@Override
	public String toString()
	{
		return "ActionEventMessage head = " + head + ", message = " + message + ", info = " + info;
	}
}

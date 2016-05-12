package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.network.serverpackets.QuestInfo;

/**
 * Акшен отображение инфы об квесте.
 *
 * @author Ronn
 */
public class ActionShowQuestInfo extends AbstractQuestAction
{
	/** название ссылки */
	private String button;

	/** ид диалога */
	private int dialogId;
	/** номер страницы */
	private int page;

	public ActionShowQuestInfo(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.button = vars.getString("button");
			this.dialogId = vars.getInteger("id");
			this.page = vars.getInteger("page", 2);
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем нпс
		Npc npc = event.getNpc();
		// получаем игрока
		Player player = event.getPlayer();
		// получаем квест
		Quest quest = event.getQuest();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		// если нпс нет, выходим
		if(npc == null)
		{
			log.warning(this, "not found npc");
			return;
		}

		// если квеста нет, выходим
		if(quest == null)
		{
			log.warning(this, "not found quest");
			return;
		}

		// отправляем пакет
		player.sendPacket(QuestInfo.getInstance(npc, player, quest, dialogId, page, button), true);
	}

	@Override
	public String toString()
	{
		return "ActionShowQuestInfo button = " + button + ", dialogId = " + dialogId;
	}
}

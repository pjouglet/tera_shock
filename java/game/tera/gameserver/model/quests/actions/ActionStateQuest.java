package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestState;
import tera.gameserver.network.serverpackets.QuestStarted;

/**
 * Акшен отображения стадии.
 *
 * @author Ronn
 */
public class ActionStateQuest extends AbstractQuestAction
{
	/** ид след. нпс */
	private int npcId;
	/** ид след. нпс */
	private int npcType;

	/** координаты точки назначения */
	private float x;
	private float y;
	private float z;

	public ActionStateQuest(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.npcId = vars.getInteger("npcId");
			this.npcType = vars.getInteger("npcType");
			this.x = vars.getFloat("x");
			this.y = vars.getFloat("y");
			this.z = vars.getFloat("z");
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
		// получаем квест
		Quest quest = event.getQuest();

		// если чего-то нет, выходим
		if(player == null || quest == null)
		{
			log.warning(this, "not found player or quest");
			return;
		}

		// получаем квестовый лист
		QuestList questList = player.getQuestList();

		// если квест листа нет, выходим
		if(questList == null)
		{
			log.warning(this, "not found quest list");
			return;
		}

		// получаем состояние квеста
		QuestState state = questList.getQuestState(quest);

		// если состояние есть
		if(state != null)
			// отправляем пакет
			player.sendPacket(QuestStarted.getInstance(state, npcType, npcId, x, y, z), true);
	}

	@Override
	public String toString()
	{
		return "ActionStateQuest npcId = " + npcId + ", npcType = " + npcType + ", x = " + x + ", y = " + y + ", z = " + z;
	}
}

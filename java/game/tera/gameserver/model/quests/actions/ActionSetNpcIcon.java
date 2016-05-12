package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Array;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.NpcIconType;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.network.serverpackets.QuestNpcNotice;
import tera.util.LocalObjects;

/**
 * Акшен установки иконки над НПС.
 *
 * @author Ronn
 */
public class ActionSetNpcIcon extends AbstractQuestAction
{
	/** тип иконки */
	private NpcIconType type;

	/** ид нпс */
	private int npcId;
	/** тип нпс */
	private int npcType;

	public ActionSetNpcIcon(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.type = vars.getEnum("type", NpcIconType.class);
			this.npcId = vars.getInteger("npcId", 0);
			this.npcType = vars.getInteger("npcType", 0);
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
		// получаем нпс
		Npc npc = event.getNpc();

		// если игрока нету, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		// если мы обновляем нпс, который с ивентом пришел
		if(npcId == 0)
		{
			// и если енго неету, выходим
			if(npc == null)
			{
				log.warning(this, "not found npc");
				return;
			}

			// отправляем пакет
			player.sendPacket(QuestNpcNotice.getInstance(npc, type), true);
		}
		// иначе мы обновляем все видимые
		else
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем список окружающих нпс
			Array<Npc> around = World.getAround(Npc.class, local.getNextNpcList(), player);

			// получаем массив нпс
			Npc[] array = around.array();

			// перебираем
			for(int i = 0, length = around.size(); i < length; i++)
			{
				// получаем нпс
				npc = array[i];

				// если нпс нету, либо он не подходит, пропускаем
				if(npc == null || npc.getTemplateId() != npcId || npc.getTemplateType() != npcType)
					continue;

				// обновляем иконку
				player.sendPacket(QuestNpcNotice.getInstance(npc, type), true);
			}
		}
	}

	@Override
	public String toString()
	{
		return "ActionSetNpcIcon type = " + type + ", npcId = " + npcId + ", npcType = " + npcType;
	}
}

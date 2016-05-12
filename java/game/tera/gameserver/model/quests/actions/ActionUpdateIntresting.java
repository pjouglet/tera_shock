package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Array;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.util.LocalObjects;

/**
 * Акшен обновления иконки над нпс для игрока.
 *
 * @author Ronn
 */
public class ActionUpdateIntresting extends AbstractQuestAction
{
	/** ид нужного НПС */
	private int npcId;

	/** тип нужного нпс */
	private int npcType;

	public ActionUpdateIntresting(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		VarTable vars = VarTable.newInstance(node);

		this.npcId = vars.getInteger("npcId", 0);
		this.npcType = vars.getInteger("npcType", 0);
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получам игрока
		Player player = event.getPlayer();

		// если игрка нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		if(npcId == 0 && npcType == 0)
		{
			// получаем нпс
			Npc npc = event.getNpc();

			// если нпс нет, выходим
			if(npc == null)
			{
				log.warning(this, "not found npc for quest " + quest.getId());
				return;
			}

			// обновляем икону
			npc.updateQuestInteresting(player, true);
		}
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
				Npc npc = array[i];

				// если нпс нету, либо он не подходит, пропускаем
				if(npc == null || npc.getTemplateId() != npcId || npc.getTemplateType() != npcType)
					continue;

				// обновляем значек
				npc.updateQuestInteresting(player, true);
			}
		}

	}

	@Override
	public String toString()
	{
		return "ActionUpdateIntresting ";
	}
}

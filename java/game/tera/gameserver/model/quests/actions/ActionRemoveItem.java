package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;

/**
 * Акшен удаления итема из инвенторя.
 *
 * @author Ronn
 */
public class ActionRemoveItem extends AbstractQuestAction
{
	/** ид итема */
	private int id;
	/** кол-во итемов */
	private long count;

	public ActionRemoveItem(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.id = vars.getInteger("id");
			this.count = vars.getInteger("count");
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

		// если его нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// если его нет, выходим
		if(inventory == null)
		{
			log.warning(this, "not found inventory");
			return;
		}

		if(count == -1)
		{
			if(inventory.removeItem(id))
			{
				// получаем менеджера событий
				ObjectEventManager eventManager = ObjectEventManager.getInstance();

				// если удалили успешно, обновляем инвентарь
				eventManager.notifyInventoryChanged(player);
			}
		}
		else if(inventory.removeItem(id, count))
		{
			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// если удалили успешно, обновляем инвентарь
			eventManager.notifyInventoryChanged(player);
		}
	}

	@Override
	public String toString()
	{
		return "ActionRemoveItem id = " + id + ", count = " + count;
	}
}

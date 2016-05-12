package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import tera.gameserver.events.EventConstant;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.MessageAddedItem;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель для просмотра очков славы.
 *
 * @author Ronn
 */
public class ReplyEventReward extends AbstractReply
{
	private static final int[] PRICE =
	{
		2,
		3,
		6,
		7,
	};

	private static final ItemTemplate[] ITEMS;

	static
	{
		// получаем таблицу итемов
		ItemTable itemTable = ItemTable.getInstance();

		ITEMS = new ItemTemplate[]
		{
			itemTable.getItem(408),
			itemTable.getItem(409),
			itemTable.getItem(410),
			itemTable.getItem(411),
		};
	}

	public ReplyEventReward(Node node)
	{
		super(node);
	}

	/**
	 * Конвектор уровня в индекс награды.
	 */
	private int levelToIndex(int level)
	{
		if(level > 57)
			return 3;
		else if(level > 47)
			return 2;
		else if(level > 34)
			return 1;
		else return 0;
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		// определяем индекс награды
		int index = levelToIndex(player.getLevel());

		// определяем цену награды
		int price = PRICE[index];

		// определяем выдаваемый итем
		ItemTemplate template = ITEMS[index];

		// если итема нет, выходим
		if(template == null)
		{
			log.warning("not found event reward.");
			return;
		}

		synchronized(player)
		{
			// получаем кол-во очков славы у игрока
			int val = player.getVar(EventConstant.VAR_NANE_HERO_POINT, 0);

			// если их не достаточно, выходим
			if(val < price)
				player.sendMessage("You don't have Fame points");
			else
			{
				// получаем инвентарь
				Inventory inventory = player.getInventory();

				// если выдать итем не удалось, сообщаем
				if(!inventory.addItem(template.getItemId(), 1, "EventReward"))
					player.sendMessage("Освободите инвентарь.");
				else
				{
					// тратим очки
					player.setVar(EventConstant.VAR_NANE_HERO_POINT, Math.max(val - price, 0));

					// уведомляем о затратах
					player.sendMessage("You spend " + price + " Fame point(s).");

					// отображаем выдачу награды
					player.sendPacket(MessageAddedItem.getInstance(player.getName(), template.getItemId(), 1), true);

					// получаем менеджера событий
					ObjectEventManager eventManager = ObjectEventManager.getInstance();

					// получаем менеджера БД
					DataBaseManager dbManager = DataBaseManager.getInstance();

					// на всякий случай сохраняем в БД
					dbManager.updatePlayerVar(player.getObjectId(), EventConstant.VAR_NANE_HERO_POINT, String.valueOf(Math.max(val - price, 0)));

					// обновляем инвентарь
					eventManager.notifyInventoryChanged(player);
				}
			}
		}
	}
}

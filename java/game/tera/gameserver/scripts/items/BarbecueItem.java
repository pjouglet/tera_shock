package tera.gameserver.scripts.items;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.MessageAddedItem;
import tera.gameserver.network.serverpackets.SystemMessage;

/**
 * Обработчик жарки мяса у костра по квесту.
 *
 * @author Ronn
 */
public class BarbecueItem extends AbstractItemExecutor
{
	public static final int RECEPT_ID = 5027;

	public static final int RESOURSE_ID = 5028;

	public static final int RESOURSE_2_ID = 5029;

	public static final int RESULT_ID = 5030;

	public BarbecueItem(int[] itemIds, int access)
	{
		super(itemIds, access);
	}

	@Override
	public void execution(ItemInstance item, Player player)
	{
		// если игроок не у огня, выходим
		if(!player.isInBonfireTerritory())
		{
			player.sendMessage("You can only use a fixed fire.");
			return;
		}

		// получаем инвентарь игроа
		Inventory inventory = player.getInventory();

		inventory.lock();
		try
		{
			// если в инвенторе нет нужного кол-во иитемов, выходим
			if(!inventory.containsItems(RECEPT_ID, 1) || !inventory.containsItems(RESOURSE_ID, 1) || !inventory.containsItems(RESOURSE_2_ID, 1))
			{
				player.sendMessage("You are not components.");
				return;
			}

			// удаляем 1 рецепт
			inventory.removeItem(RECEPT_ID, 1L);
			// удаляем 1 ресурс
			inventory.removeItem(RESOURSE_ID, 1L);
			// удаляем 1 ресурс
			inventory.removeItem(RESOURSE_2_ID, 1L);
			// выдаем 1 итоговый
			inventory.forceAddItem(RESULT_ID, 1, "Bonfire");
		}
		finally
		{
			inventory.unlock();
		}

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем о использовании придмета
		eventManager.notifyUseItem(item, player);

		// уведомляем всех об этом
		eventManager.notifyInventoryChanged(player);

		// отправляем пакет о использовании рецепта
		player.sendPacket(SystemMessage.getInstance(MessageType.ITEM_USE).addItem(RECEPT_ID, 1), true);
		// отпправляем пакет о выдачи
		player.sendPacket(MessageAddedItem.getInstance(player.getName(), RESULT_ID, 1), true);
	}
}

package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.GameLogManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.SystemMessage;

/**
 * Клиентский пакет с информацией о удалении итема
 *
 * @author Ronn
 */
public class RequestDeleteItem extends ClientPacket
{
	/** игрок */
	private Player player;

	/** кол-во */
	private long count;

	/** номер ячейки */
	private int index;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();

		readInt();
		readInt();

		index = readInt();
		count = readInt();
	}

	@Override
    public void runImpl()
    {
		if(player == null)
			return;

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// если его нет, выходим
		if(inventory == null)
		{
			log.warning(this, new Exception("not found inventory."));
			return;
		}

		inventory.lock();
		try
		{
			// получаем ячейку, где лежит итем
			Cell cell = inventory.getCell(index);

			// если ячейки нет либо она пуста, выходим
			if(cell == null || cell.isEmpty())
				return;

			// получаем итем в ячейке
			ItemInstance item = cell.getItem();

			// если его нельзя удалять
			if(!item.isDeletable())
			{
				// создаем пакет с сообщением
				SystemMessage message = SystemMessage.getInstance(MessageType.YOU_CANT_DISCARD_ITEM_NAME);

				// добавляем ид и кол-во итемов
				message.addItem(item.getItemId(), (int) item.getItemCount());

				// отправляем игроку
				player.sendPacket(message, true);
				return;
			}

			// удаляем итем
			if(inventory.removeItemFromIndex(count, index))
			{
				// получаем менеджера событий
				ObjectEventManager eventManager = ObjectEventManager.getInstance();

				// обновляем инвентарь
				eventManager.notifyInventoryChanged(player);

				// отправляем сообщение о удалении итема
				PacketManager.showDeleteItem(player, item);

				// получаем логера игровых событий
				GameLogManager gameLogger = GameLogManager.getInstance();

				// записываем событие удаление итема
				gameLogger.writeItemLog(player.getName() + " delete item [id = " + item.getItemId() + ", count = " + count + ", name = " + item.getName() + "]");
			}
		}
		finally
		{
			inventory.unlock();
		}
    }
}
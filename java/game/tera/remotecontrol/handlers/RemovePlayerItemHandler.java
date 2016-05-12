package tera.remotecontrol.handlers;

import rlib.concurrent.Locks;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.World;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.Slot;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Обновление итема игрока.
 *
 * @author Ronn
 * @created 09.04.2012
 */
public class RemovePlayerItemHandler implements PacketHandler
{
	public static final RemovePlayerItemHandler instance = new RemovePlayerItemHandler();

	@Override
	public Packet processing(Packet packet)
	{
		Player player = World.getPlayer(packet.nextString());

		if(player == null)
			return null;

		int objectId = packet.nextInt();

		Inventory inventory = player.getInventory();
		Equipment equipment = player.getEquipment();

		if(inventory == null || equipment == null)
			return null;

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();


		Locks.lock(inventory, equipment);
		try
		{
			Cell cell = inventory.getCellForObjectId(objectId);

			if(cell != null)
			{
				ItemInstance item = cell.getItem();

				item.setOwnerId(0);

				cell.setItem(null);

				dbManager.updateLocationItem(item);
				eventManager.notifyInventoryChanged(player);

				return new Packet(PacketType.RESPONSE);
			}

			Slot slot = equipment.getSlotForObjectId(objectId);

			if(slot != null)
			{
				ItemInstance item = slot.getItem();

				item.setOwnerId(0);

				slot.setItem(null);

				dbManager.updateLocationItem(item);
				eventManager.notifyEquipmentChanged(player);

				return new Packet(PacketType.RESPONSE);
			}
		}
		finally
		{
			Locks.unlock(inventory, equipment);
		}

		return null;
	}
}

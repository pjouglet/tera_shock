package tera.remotecontrol.handlers;

import rlib.concurrent.Locks;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.World;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.Slot;
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
public class UpdatePlayerItemHandler implements PacketHandler
{
	public static final UpdatePlayerItemHandler instance = new UpdatePlayerItemHandler();

	@Override
	public Packet processing(Packet packet)
	{
		Player player = World.getPlayer(packet.nextString());

		if(player == null)
			return null;

		int objectId = packet.nextInt();
		long newCount = packet.nextLong();

		Inventory inventory = player.getInventory();
		Equipment equipment = player.getEquipment();

		if(inventory == null || equipment == null)
			return null;

		Locks.lock(inventory, equipment);
		try
		{
			ItemInstance item = inventory.getItemForObjectId(objectId);

			if(item == null)
			{
				Slot slot = equipment.getSlotForObjectId(objectId);

				if(slot != null)
					item = slot.getItem();
			}

			if(item == null || !item.isStackable())
				return null;

			item.setItemCount(newCount);

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			eventManager.notifyInventoryChanged(player);

			return new Packet(PacketType.RESPONSE);
		}
		finally
		{
			Locks.unlock(inventory, equipment);
		}
	}
}

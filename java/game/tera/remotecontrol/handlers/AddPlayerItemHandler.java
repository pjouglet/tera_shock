package tera.remotecontrol.handlers;

import rlib.logging.Loggers;
import rlib.util.array.Arrays;
import tera.Config;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.World;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Обновление итема игрока.
 *
 * @author Ronn
 * @created 09.04.2012
 */
public class AddPlayerItemHandler implements PacketHandler
{
	public static final AddPlayerItemHandler instance = new AddPlayerItemHandler();

	@Override
	public Packet processing(Packet packet)
	{
		Player player = World.getPlayer(packet.nextString());

		if(player == null)
			return null;

		int itemId = packet.nextInt();
		long count = packet.nextLong();

		ItemTable itemTable = ItemTable.getInstance();

		ItemTemplate template = itemTable.getItem(itemId);

		if(template == null)
			return null;

		// проверяем отношение итема к донату
		if(Arrays.contains(Config.WORLD_DONATE_ITEMS, template.getItemId()))
		{
			Loggers.warning(this, new Exception("not create donate item for id " + template.getItemId()));
			return null;
		}

		ItemInstance item = template.newInstance();

		if(item == null)
			return null;

		item.setAutor("RemoteAdmin");

		if(item.isStackable())
			item.setItemCount(count);

		if(player.getInventory().putItem(item))
		{
			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			eventManager.notifyInventoryChanged(player);

			return new Packet(PacketType.RESPONSE);
		}

		return null;
	}
}

package tera.gameserver.scripts.commands;

import rlib.geom.Coords;
import rlib.logging.Loggers;
import rlib.util.array.Arrays;
import tera.Config;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ItemTemplateInfo;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;
import tera.util.Location;

/**
 * Список команд для работы с итемами.
 *
 * @author Ronn
 * @created 27.03.2012
 */
public class ItemCommand extends AbstractCommand
{
	public ItemCommand(int access, String[] commands)
	{
		super(access, commands);
	}

	@Override
	public void execution(String command, Player player, String values)
	{
		// получаем таблицу итемов
		ItemTable itemTable = ItemTable.getInstance();

		switch(command)
		{
			case "reload_items":
			{
				itemTable.reload();

				player.sayMessage("items reloaded.");

				break;
			}
			case "spawn_item":
			{
				String[] vals = values.split(" ");

				if(vals.length != 2)
					return;

				int id = Integer.parseInt(vals[0]);
				int count = Integer.parseInt(vals[1]);

				// проверяем отношение итема к донату
				if(Arrays.contains(Config.WORLD_DONATE_ITEMS, id))
				{
					Loggers.warning(this, new Exception("not create donate item for id " + id));
					return;
				}

				if(count < 1)
					return;

				ItemTemplate template = itemTable.getItem(id);

				if(template == null)
					return;

				if(template.isStackable() || count < 2)
				{
					ItemInstance item = template.newInstance();

					if(item == null)
						return;

					item.setItemCount(count);
					item.setTempOwner(player);
					item.setContinentId(player.getContinentId());

					item.spawnMe(player.getX(), player.getY(), player.getZ(), 0);
				}
				else
				{
					Location[] locs = Coords.circularCoords(Location.class, player.getX(), player.getY(), player.getZ(), 45, count);

					for(int i = 0; i < count; i++)
					{
						ItemInstance item = template.newInstance();

						item.setItemCount(1);
						item.setTempOwner(player);
						item.setContinentId(player.getContinentId());

						item.spawnMe(locs[i]);
					}
				}

				break;
			}
			case "item_info":
			{
				player.sendPacket(ItemTemplateInfo.getInstance(Integer.parseInt(values)), true);

				break;
			}
			case "create_item":
			{
				String[] vals = values.split(" ");

				if(vals.length < 1)
					return;

				int id = Integer.parseInt(vals[0]);

				// проверяем отношение итема к донату
				if(Arrays.contains(Config.WORLD_DONATE_ITEMS, id))
				{
					Loggers.warning(this, new Exception("not create donate item for id " + id));
					return;
				}

				ItemTemplate template = itemTable.getItem(id);

				if(template == null)
					return;

				ItemInstance newItem = template.newInstance();

				if(newItem == null)
					return;

				newItem.setAutor("GM_Create_Item");

				Inventory inventory = player.getInventory();

				if(vals.length > 1 && template.isStackable())
					newItem.setItemCount(Integer.parseInt(vals[1]));

				if(inventory.putItem(newItem))
				{
					// получаем менеджера событий
					ObjectEventManager eventManager = ObjectEventManager.getInstance();

					// обновляемся
					eventManager.notifyInventoryChanged(player);
				}

				break;
			}
		}
	}
}

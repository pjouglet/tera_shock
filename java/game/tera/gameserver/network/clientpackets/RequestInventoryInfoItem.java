package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.World;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.Slot;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.InventoryItemInfo;

/**
 * Клиенсткий пакет для запроса информации о итеме.
 *
 * @author Ronn
 */
public class RequestInventoryInfoItem extends ClientPacket
{
	/** ид итема */
    private int objectId;

    /** имя игрока */
    private String name;
    /** игрок */
    private Player player;

    @Override
	public void finalyze()
	{
		name = null;
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
    	readShort();

        objectId = readInt(); //обжект ид итема для которого узнаём инфу

        readLong();
        readLong();
        readInt();
        readShort();
        readShort();

        name = readString();
    }

	@Override
    public void runImpl()
    {
		if(player == null || name == null)
			return;

    	// целевой игрок
    	Player target = null;

    	// если нужный игрок и есть игрок, который запрашивает
    	if(name.equals(player.getName()))
    		target = player;
    	else
    	{
    		// иначе ищим по окружающим
    		target = World.getAroundByName(Player.class, player, name);
    		// если не находим
    		if(target == null)
    			// пытаемся потянуть из мира
    			target = World.getPlayer(name);
    	}

    	// если не находим, выходим
    	if(target == null)
    		return;

    	// извлекаем экиперовку и инвентарь
    	Equipment equipment = target.getEquipment();

		// если экиперовки нет, выходим
		if(equipment == null)
			return;

		equipment.lock();
		try
		{
			// пытаемся получить слот из экиперовки с этим итемом
			Slot slot = equipment.getSlotForObjectId(objectId);

			// если такой слот нашелся
			if(slot != null)
			{
				// отправляем пакет и выходимм
				player.sendPacket(InventoryItemInfo.getInstance(slot.getIndex(), slot.getItem()), true);
				return;
			}
		}
		finally
		{
			equipment.unlock();
		}

		Inventory inventory = target.getInventory();

		// если инвенторя нет, выходим
		if(inventory == null)
			return;

		inventory.lock();
		try
		{
			// пытаемся получить ячейку из изнвенторя
			Cell cell = inventory.getCellForObjectId(objectId);

			// если такой нашли
			if(cell != null)
			{
				// отправляем пакет и выходим
				player.sendPacket(InventoryItemInfo.getInstance(cell.getIndex(), cell.getItem()), true);
				return;
			}
		}
		finally
		{
			inventory.unlock();
		}

		Bank bank = target.getBank();

		if(bank == null)
			return;

		bank.lock();
		try
		{
			ItemInstance item = bank.getItemForObjectId(objectId);

			if(item != null)
			{
				// отправляем пакет и выходим
				player.sendPacket(InventoryItemInfo.getInstance(0, item), true);
				return;
			}
		}
		finally
		{
			bank.unlock();
		}

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если гильдии нет, выходим
		if(guild == null)
			return;

		// получаем банк гильдии
		bank = guild.getBank();

		bank.lock();
		try
		{
			ItemInstance item = bank.getItemForObjectId(objectId);

			if(item != null)
			{
				// отправляем пакет и выходим
				player.sendPacket(InventoryItemInfo.getInstance(0, item), true);
				return;
			}
		}
		finally
		{
			bank.unlock();
		}
    }
}
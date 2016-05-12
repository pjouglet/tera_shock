package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.ItemExecutorManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;

/**
 * Класс, присылающий инфу об итеме, который хотим юзнуть.
 *
 * @author Ronn
 */
public class RequestUseItem extends ClientPacket
{
	/** игрок */
	private Player player;

	/** ид итема, который юзаем */
	private int itemId;
	/** обджект ид итема */
	private int objectId;
	/** направление разворота */
	private int heading;

	@Override
	public void finalyze()
	{
		player = null;
		objectId = 0;
		heading = -1;
	}

	@Override
	public void readImpl()
    {
		player = owner.getOwner();

        readInt();
        readInt();

        itemId = readInt(); //ид итема который юзаем
        objectId =  readInt(); // обджект ид итема

        readLong();
		readLong();
		readInt();//00 00 00 00
		readInt();
		readFloat();//08 51 aa 47     x где юзаем
		readFloat();//18 3d a5 c7     y где юзаем
		readFloat();//b1 eb 8f c5     z где юзаем

		heading = readShort();//2a c5      heading где юзаем
	}

	@Override
	public void runImpl()
    {
		if(player == null || player.isAllBlocking() || player.isOnMount())
			return;

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// если инвенторя нет, выходим
		if(inventory == null)
			return;

		// создаем ссылку на итем
		ItemInstance item = null;

		inventory.lock();
		try
		{
			// если указан уникальный и итема, ищем по нему
			if(objectId != 0)
				item = inventory.getItemForObjectId(objectId);
			else
				// иначе ищем по итем иду
				item = inventory.getItemForItemId(itemId);
		}
		finally
		{
			inventory.unlock();
		}

		// если итем не нашли либо он забольшой для игрока, выходим
		if(item == null || item.getItemLevel() > player.getLevel())
			return;

		// если неверных разворот, берем разворот игрока
		if(heading == -1)
			heading = player.getHeading();

		// получаем исполнителя
		ItemExecutorManager executor = ItemExecutorManager.getInstance();

		// если нету отдельного обработчика
		if(!executor.execute(item, player))
			// передамем юз на АИ
			player.getAI().startUseItem(item, heading, false);
    }
}
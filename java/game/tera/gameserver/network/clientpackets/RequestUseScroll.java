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
public class RequestUseScroll extends ClientPacket
{
	/** игрок */
	private Player player;

	/** ид итема, который юзаем */
	private int itemId;

	@Override
	public void readImpl()
    {
		player = owner.getOwner();

		itemId = readInt();
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

		// получаем исполнителя
		ItemExecutorManager executor = ItemExecutorManager.getInstance();

		// если нету отдельного обработчика
		if(!executor.execute(item, player))
			// передамем юз на АИ
			player.getAI().startUseItem(item, player.getHeading(), false);
    }
}
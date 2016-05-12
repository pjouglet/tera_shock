package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.interaction.dialogs.BankDialog;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.playable.Player;

/**
 * Пакет запроса на сортировку итемов в инвентаре.
 *
 * @author Ronn
 * @created 11.04.2012
 */
public class RequestSortInventory extends ClientPacket
{
	private static enum SortLocation
	{
		INVENTORY,
		BANK,
	}

	/** игрок, который хочет отсортировать инвентарь */
	private Player player;

	/** что именно сортировать */
	private SortLocation location;

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
	protected void readImpl()
	{
		player = owner.getOwner();

		location = SortLocation.values()[readInt()];
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		switch(location)
		{
			case INVENTORY:
			{
				// получаем инвентарь
				Inventory inventory = player.getInventory();

				// если его нет, выходим
				if(inventory == null)
				{
					log.warning(this, new Exception("not found inventory"));
					return;
				}

				// сортируем
				inventory.sort();

				// получаем менеджера событий
				ObjectEventManager eventManager = ObjectEventManager.getInstance();

				// обновляем
				eventManager.notifyInventoryChanged(player);

				break;
			}
			case BANK:
			{
				Dialog dialog = player.getLastDialog();

				if(dialog == null || !(dialog instanceof BankDialog))
					return;

				BankDialog bank = (BankDialog) dialog;

				bank.sort();
			}
		}

	}
}

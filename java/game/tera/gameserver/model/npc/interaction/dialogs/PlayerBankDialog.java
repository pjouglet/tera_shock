package tera.gameserver.model.npc.interaction.dialogs;

import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.PlayerBankPanel;

/**
 * Модель окна банка.
 *
 * @author Ronn
 */
public final class PlayerBankDialog extends AbstractDialog implements BankDialog
{
	/**
	 * @param npc нпс.
	 * @param player игрок.
	 * @return новый диалог.
	 */
	public static final PlayerBankDialog newInstance(Npc npc, Player player)
	{
		PlayerBankDialog dialog = (PlayerBankDialog) DialogType.PLAYER_BANK.newInstance();

		dialog.npc = npc;
		dialog.player = player;
		dialog.startCell = 0;

		return dialog;
	}

	/** стартовй индекс в банке */
	private int startCell;

	@Override
	public synchronized void addItem(int index, int itemId, int count)
	{
		// если что-то не то ,выходим
		if(index < 0 || count < 1)
			return;

		// получаем игрока
		Player player = getPlayer();

		// если игрока нет ,выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// получаем банк
		Bank bank = player.getBank();

		// если чего-то из этого нет, выходим
		if(bank == null || inventory == null)
		{
			log.warning(this, new Exception("not found bank or inventory"));
			return;
		}

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		inventory.lock();
		try
		{
			// получаем ячейку, в котором лежит нужный итем
			Cell cell = inventory.getCell(index);

			// если ячейка пуста, выходим
			if(cell == null || cell.isEmpty())
				return;

			// если кол-ва не хватает, выходим
			if(cell.getItemCount() < count)
				return;

			// получаем итем с ячейки
			ItemInstance item = cell.getItem();

			// если его нет, что-то здесь не то о_О
			if(item == null)
			{
				log.warning(this , new Exception("not found item"));
				return;
			}

			// если итем нельзя ложить в банк, выходим
			if(!item.isBank())
			{
				player.sendMessage(MessageType.THAT_ITEM_CANTT_BE_STORED_IN_THE_BANK);
				return;
			}

			bank.lock();
			try
			{
				// если не стакуемый
				if(!item.isStackable())
				{
					// если в банк положился но с инвенторя не удалился
					if(bank.putItem(item) && !inventory.removeItem(item))
						bank.removeItem(item);

					// обновляем банк игроку
					eventManager.notifyPlayerBankChanged(player, startCell);

					// обновляем инвентарь
					eventManager.notifyInventoryChanged(player);
				}
				// добавляем в банк итеимы
				else if(bank.addItem(itemId, count))
				{
					// если итемы не удалились из инвенторя
					if(!inventory.removeItem(itemId, (long) count))
						// удаляем из банка их
						bank.removeItem(itemId, count);

					// обновляем банк игроку
					eventManager.notifyPlayerBankChanged(player, startCell);

					// обновляем инвентарь
					eventManager.notifyInventoryChanged(player);
				}
			}
			finally
			{
				bank.unlock();
			}
		}
		finally
		{
			inventory.unlock();
		}
	}

	@Override
	public synchronized void addMoney(int money)
	{
		// если денег нет, выходим
		if(money < 1)
			return;

		// получаем игрока
		Player player = getPlayer();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// получаем банк игрока
		Bank bank = player.getBank();

		// если инвенторя или банка нет ,выходим
		if(inventory == null || bank == null)
		{
			log.warning(this, new Exception("not found bank or inventory"));
			return;
		}

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		inventory.lock();
		try
		{
			// если в инвенторе столько нет, выходим
			if(inventory.getMoney() < money)
				return;

			// забираем деньги
			inventory.subMoney(money);
			// ложим деньги
			bank.addMoney(money);

			// обновляем банк игроку
			eventManager.notifyPlayerBankChanged(player, startCell);

			// обновляем инвентарь
			eventManager.notifyInventoryChanged(player);
		}
		finally
		{
			inventory.unlock();
		}
	}

	@Override
	public synchronized boolean apply()
	{
		return false;
	}

	@Override
	public synchronized void getItem(int index, int itemId, int count)
	{
		index += startCell;

		// если что-то не то ,выходим
		if(index < 0 || count < 1)
			return;

		// получаем игрока
		Player player = getPlayer();

		// если игрока нет ,выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// получаем банк
		Bank bank = player.getBank();

		// если чего-то из этого нет, выходим
		if(bank == null || inventory == null)
		{
			log.warning(this, new Exception("not found bank or inventory"));
			return;
		}

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		bank.lock();
		try
		{
			// получаем ячейку, в котором лежит нужный итем
			Cell cell = bank.getCell(index);

			// если ячейка пуста, выходим
			if(cell == null || cell.isEmpty())
				return;

			// еслиданные не сходятся
			if(cell.getItemId() != itemId || cell.getItemCount() < count)
				return;

			// получаем итем с ячейки
			ItemInstance item = cell.getItem();

			// если его нет, что-то здесь не то о_О
			if(item == null)
			{
				log.warning(this , new Exception("not found item"));
				return;
			}

			inventory.lock();
			try
			{
				// если не стакуемый
				if(!item.isStackable())
				{
					// пробуем перенести
					if(!inventory.putItem(item))
						player.sendMessage(MessageType.INVENTORY_IS_FULL);
					else
					{
						// если удаление прошло не успешно
						if(!bank.removeItem(item))
							// удалям из инвенторя
							inventory.removeItem(item);

						// обновляем банк игроку
						eventManager.notifyPlayerBankChanged(player, startCell);

						// обновляем инвентарь
						eventManager.notifyInventoryChanged(player);
					}

					return;
				}

				try
				{
					// добавляем в инвентарь
					if(!inventory.addItem(itemId, count, "Bank"))
						player.sendMessage(MessageType.INVENTORY_IS_FULL);
					else
					{
						// если удаление не прошло успешно
						if(!bank.removeItem(itemId, count))
							// удаляем из инвенторя
							inventory.removeItem(itemId, (long) count);

						// обновляем банк игроку
						eventManager.notifyPlayerBankChanged(player, startCell);

						// обновляем инвентарь
						eventManager.notifyInventoryChanged(player);
					}
				}
				catch(Exception e)
				{
					log.warning(this, e);
				}
			}
			finally
			{
				inventory.unlock();
			}
		}
		finally
		{
			bank.unlock();
		}
	}

	@Override
	public synchronized void getMoney(int money)
	{
		// если денег нет, выходим
		if(money < 1)
			return;

		// получаем игрока
		Player player = getPlayer();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// получаем банк игрока
		Bank bank = player.getBank();

		// если инвенторя или банка нет ,выходим
		if(inventory == null || bank == null)
		{
			log.warning(this, new Exception("not found bank or inventory"));
			return;
		}

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		inventory.lock();
		try
		{
			// если в банке столько нет, выходим
			if(bank.getMoney() < money)
				return;

			// забираем деньги
			bank.subMoney(money);
			// ложим деньги
			inventory.addMoney(money);

			// обновляем банк игроку
			eventManager.notifyPlayerBankChanged(player, startCell);

			// обновляем инвентарь
			eventManager.notifyInventoryChanged(player);
		}
		finally
		{
			inventory.unlock();
		}
	}

	@Override
	public DialogType getType()
	{
		return DialogType.PLAYER_BANK;
	}

	@Override
	public synchronized boolean init()
	{
		if(!super.init())
			return false;

		// получаем игрока
		Player player = getPlayer();

		// если его нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		// отправляем
		player.sendPacket(PlayerBankPanel.getInstance(player), true);
		// отправляем пакет банка
		PacketManager.updatePlayerBank(player, startCell);

		return true;
	}

	@Override
	public void movingItem(int oldCell, int newCell)
	{
		oldCell += startCell;
		newCell += startCell;

		// получаем игрока
		Player player = getPlayer();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		// получаем банк игрока
		Bank bank = player.getBank();

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		bank.lock();
		try
		{
			Cell start = bank.getCell(oldCell);
			Cell end = bank.getCell(newCell);

			if(start == null || end == null || start == end)
				return;

			ItemInstance oldItem = start.getItem();

			start.setItem(end.getItem());
			end.setItem(oldItem);

			dbManager.updateLocationItem(end.getItem());
			dbManager.updateLocationItem(start.getItem());

			// обновляем банк игроку
			eventManager.notifyPlayerBankChanged(player, startCell);
		}
		finally
		{
			bank.unlock();
		}
	}

	@Override
	public void setStartCell(int startCell)
	{
		this.startCell = startCell;

		PacketManager.updatePlayerBank(player, startCell);
	}

	@Override
	public void sort()
	{
		// получаем игрока
		Player player = getPlayer();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		// получаем банк игрока
		Bank bank = player.getBank();

		// сортируем
		bank.sort();

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем
		eventManager.notifyPlayerBankChanged(player, startCell);
	}
}

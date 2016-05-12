package tera.gameserver.model.npc.interaction.dialogs;

import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.PlayerBankPanel;

/**
 * Модель окна банка гильдии.
 *
 * @author Ronn
 */
public final class GuildBankDialog extends AbstractDialog implements BankDialog
{
	/**
	 * @param npc нпс.
	 * @param player игрок.
	 * @return новый диалог.
	 */
	public static final GuildBankDialog newInstance(Npc npc, Player player)
	{
		GuildBankDialog dialog = (GuildBankDialog) DialogType.GUILD_BANK.newInstance();

		dialog.npc = npc;
		dialog.player = player;
		dialog.startCell = 0;

		return dialog;
	}

	/** стартовая ячейка */
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

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если ее нет, выходим
		if(guild == null)
		{
			player.sendMessage(MessageType.YOU_NOT_IN_GUILD);
			return;
		}

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// получаем банк
		Bank bank = guild.getBank();

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

			// если не стакуемый
			if(!item.isStackable())
			{
				// пробуем перенести
				if(bank.putItem(item))
					// если перенесли, удаляем из инвенторя
					inventory.removeItem(item);

				// обновляем банк игроку
				eventManager.notifyGuildBankChanged(player, startCell);

				// обновляем инвентарь
				eventManager.notifyInventoryChanged(player);

				return;
			}

			// добавляем в банк
			if(bank.addItem(itemId, count))
			{
				// удаляем итем из инвенторя
				inventory.removeItem(itemId, (long) count);

				// обновляем банк игроку
				eventManager.notifyGuildBankChanged(player, startCell);

				// обновляем инвентарь
				eventManager.notifyInventoryChanged(player);
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

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если ее нет, выходим
		if(guild == null)
		{
			player.sendMessage(MessageType.YOU_NOT_IN_GUILD);
			return;
		}

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// получаем банк игрока
		Bank bank = guild.getBank();

		// если инвенторя или банка нет ,выходим
		if(inventory == null || bank == null)
		{
			log.warning(this, new Exception("not found bank or inventory"));
			return;
		}

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
			eventManager.notifyGuildBankChanged(player, startCell);

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

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если ее нет, выходим
		if(guild == null)
		{
			player.sendMessage(MessageType.YOU_NOT_IN_GUILD);
			return;
		}

		// получаем ранг в гильдии
		GuildRank rank = player.getGuildRank();

		// если нет прав, выходим
		if(!rank.isAccessBank())
		{
			player.sendMessage("У вас нет прав.");
			return;
		}

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// получаем банк
		Bank bank = guild.getBank();

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

			// если не стакуемый
			if(!item.isStackable())
			{
				// пробуем перенести
				if(!inventory.putItem(item))
					player.sendMessage(MessageType.INVENTORY_IS_FULL);
				else
				{
					// если перенесли, удаляем из инвенторя
					bank.removeItem(item);

					// обновляем банк игроку
					eventManager.notifyGuildBankChanged(player, startCell);

					// обновляем инвентарь
					eventManager.notifyInventoryChanged(player);
				}

				return;
			}

			// добавляем в инвентарь
			if(!inventory.addItem(itemId, count, "Bank"))
				player.sendMessage(MessageType.INVENTORY_IS_FULL);
			else
			{
				// удаляем итем из банка
				bank.removeItem(itemId, count);

				// обновляем банк игроку
				eventManager.notifyGuildBankChanged(player, startCell);

				// обновляем инвентарь
				eventManager.notifyInventoryChanged(player);
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

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если ее нет, выходим
		if(guild == null)
		{
			player.sendMessage(MessageType.YOU_NOT_IN_GUILD);
			return;
		}

		// получаем ранг в гильдии
		GuildRank rank = player.getGuildRank();

		// если нет прав, выходим
		if(!rank.isAccessBank())
		{
			player.sendMessage("У вас нет прав.");
			return;
		}

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// получаем банк игрока
		Bank bank = guild.getBank();

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
			eventManager.notifyGuildBankChanged(player, startCell);

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
		return DialogType.GUILD_BANK;
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
		PacketManager.updateGuildBank(player, startCell);

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

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если ее нет, выходим
		if(guild == null)
		{
			player.sendMessage(MessageType.YOU_NOT_IN_GUILD);
			return;
		}

		// получаем ранг в гильдии
		GuildRank rank = player.getGuildRank();

		// если нет прав, выходим
		if(!rank.isAccessBank())
		{
			player.sendMessage("У вас нет прав.");
			return;
		}

		// получаем банк игрока
		Bank bank = guild.getBank();

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

			// обновляем положение итемов в БД
			dbManager.updateLocationItem(end.getItem());
			dbManager.updateLocationItem(start.getItem());

			// обновляем банк игроку
			eventManager.notifyGuildBankChanged(player, startCell);
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

		PacketManager.updateGuildBank(player, startCell);
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

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если ее нет, выходим
		if(guild == null)
		{
			player.sendMessage(MessageType.YOU_NOT_IN_GUILD);
			return;
		}

		// получаем ранг в гильдии
		GuildRank rank = player.getGuildRank();

		// если нет прав, выходим
		if(!rank.isAccessBank())
		{
			player.sendMessage("У вас нет прав.");
			return;
		}

		// получаем банк игрока
		Bank bank = guild.getBank();

		// сортируем
		bank.sort();

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем
		eventManager.notifyGuildBankChanged(player, startCell);
	}
}

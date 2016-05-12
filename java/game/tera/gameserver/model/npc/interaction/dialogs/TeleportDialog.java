package tera.gameserver.model.npc.interaction.dialogs;

import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.TeleportRegion;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.territory.LocalTerritory;
import tera.gameserver.network.serverpackets.TeleportPoints;

/**
 * Модель окна телепорта.
 *
 * @author Ronn
 */
public class TeleportDialog extends AbstractDialog
{
	/**
	 * Создание нового диалога маршрутов локальных телепортов.
	 *
	 * @param npc нпс, у которого будем телепортироваться.
	 * @param player игрок, который будет телепортироваться.
	 * @param regions набор доступных регионов.
	 * @param table таблица доступных регионов.
	 * @return новый диалог.
	 */
	public static final TeleportDialog newInstance(Npc npc, Player player, TeleportRegion[] regions, Table<IntKey, TeleportRegion> table)
	{
		TeleportDialog dialog = (TeleportDialog) DialogType.TELEPORT.newInstance();

		dialog.npc = npc;
		dialog.player = player;
		dialog.regions = regions;
		dialog.table = table;

		return dialog;
	}

	/** таблица доступных телепортов */
	private Table<IntKey, TeleportRegion> table;

	/** список доступных телепортов */
	private TeleportRegion[] regions;

	public TeleportRegion[] getRegions()
	{
		return regions;
	}

	@Override
	public DialogType getType()
	{
		return DialogType.TELEPORT;
	}

	@Override
	public synchronized boolean init()
	{
		// если не инициализировалось, выходим
		if(!super.init())
			return false;

		// получаем игрока
		Player player = getPlayer();

		// если игрока нету, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		// отправляем пакет с доступными точками телепорта
		player.sendPacket(TeleportPoints.getInstance(npc, player, regions), true);

		return true;
	}

	/**
	 * Телепорт игрока в указанную точку.
	 *
	 * @param index номер точки.
	 */
	public synchronized void teleport(int index)
	{
		// получаем игрока
		Player player = getPlayer();

		// если игрока нету, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		// получаем целевой регион
		TeleportRegion region = table.get(index);

		// если региона нет, выходим
		if(region == null)
		{
			log.warning(this, new Exception("not found region for index " + index));
			return;
		}

		// получаем територию
		LocalTerritory territory = region.getRegion();

		// если игрок небыл в этой зоне
		if(!player.isWhetherIn(territory))
		{
			// сообщаем
			player.sendMessage(MessageType.NO_TERRAIN_FOUND_PLEASE_TELEPORT_TO_ANOTHER_AREA);
			//выходим
			return;
		}

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// если его нет, выходим
		if(inventory == null)
		{
			log.warning(this, new Exception("not found inventory"));
			return;
		}

		// получаем стоимость телепорта
		int price = region.getPrice();

		// если недостаточно денег
		if(inventory.getMoney() < price)
		{
			// сообщаем
			player.sendMessage(MessageType.YOU_DONT_HAVE_ENOUGH_GOLD);
			// выходим
			return;
		}

		// отнимаем деньги
		inventory.subMoney(price);

		// получаем логера игровых событий
		GameLogManager gameLogger = GameLogManager.getInstance();

		// записываем событие о покупке скила
		gameLogger.writeItemLog(player.getName() + " buy local teleport for " + price + " gold");

		// телепортируем игрока
		player.teleToLocation(territory.getTeleportLoc());

		// закрываем окно
		close();
	}
}

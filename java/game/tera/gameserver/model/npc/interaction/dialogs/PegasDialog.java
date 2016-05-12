package tera.gameserver.model.npc.interaction.dialogs;

import java.util.concurrent.ScheduledFuture;

import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Route;
import tera.gameserver.model.TownInfo;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.DeparturePortal;
import tera.gameserver.network.serverpackets.GetOffPegas;
import tera.gameserver.network.serverpackets.PegasFly;
import tera.gameserver.network.serverpackets.PegasReplyPacket;
import tera.gameserver.network.serverpackets.PegasRouts;
import tera.gameserver.network.serverpackets.PutAnPegas;
import tera.gameserver.network.serverpackets.StateAllowed;
import tera.gameserver.network.serverpackets.WorldZone;

/**
 * Модель окна полетов на пегасе.
 *
 * @author Ronn
 * @created 25.02.2012
 */
public class PegasDialog extends AbstractDialog implements Runnable
{
	/**
	 * Перечисление состояний полета на пегасе.
	 *
	 * @author Ronn
	 */
	public static enum State
	{
		STARTING,
		FLY,
		LANDING,
	}

	public static final int DEPARTURE_CONTINUE_STATE = 999800;

	/**
	 * Создание новогоэкземпляра диалога.
	 *
	 * @param npc нпс, у которого начали диалог.
	 * @param player игрок, который начал диалог.
	 * @param routes набор маршрутов.
	 * @param town город вылета.
	 * @return новый диалог.
	 */
	public static final PegasDialog newInstance(Npc npc, Player player, Table<IntKey, Route> routes, TownInfo town)
	{
		PegasDialog dialog = (PegasDialog) DialogType.PEGAS.newInstance();

		dialog.npc = npc;
		dialog.player = player;
		dialog.routes = routes;
		dialog.town = town;

		return dialog;
	}

	/** таблица маршрутов */
	private Table<IntKey, Route> routes;

	/** текущее состояние */
	private State state;

	/** ид города */
	private TownInfo town;

	private ScheduledFuture<PegasDialog> schedule;

	public PegasDialog()
	{
		setState(State.STARTING);
	}

	@Override
	public synchronized boolean apply()
	{
		// получаем игрока
		Player player = getPlayer();

		// если его нет, закрываем и выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		switch(getState())
		{
			// стадия старта полета
			case STARTING:
			{
				// получаем выбранный маршрут
				Route route = player.getRoute();

				// если маршрута нет, выходим
				if(route == null)
				{
					log.warning(this, new Exception("not found route"));
					return false;
				}

				// если игрок на маунте
				if(player.isOnMount())
					// слазим с маунта
					player.getOffMount();

				// применяем точку взлета текущего города
				player.setLoc(town.getLanding());
				// ставим флаг, что игрок в полете
				player.setFlyingPegas(true);
				// ставим флаг неуязвимости
				player.setInvul(true);

				// посылаем пакет посадки на пегаса
				player.broadcastPacket(PutAnPegas.getInstance(player));
				// посылаем пакет полета на пегасе
				player.broadcastPacket(PegasFly.getInstance(player, route, 0));
				player.broadcastPacket(PegasFly.getInstance(player, route, 3000));

				// если перелет локальный
				if(route.isLocal())
				{
					// ставим стадию посадки
					setState(State.LANDING);

					// запускаем таск
					schedule = executor.scheduleGeneral(this, route.getTarget().getLocal());
				}
				// иначе
				else
				{
					// ставим стадию палета
					setState(State.FLY);

					// запускаем таск
					schedule = executor.scheduleGeneral(this, town.getToPortal());
				}

				break;
			}
			// стадия влета в портал
			case FLY:
			{
				// получаем текущий маршрут игрока
				Route route = player.getRoute();

				// если его нет, выходим
				if(route == null)
				{
					log.warning(this, new Exception("not found route"));
					return false;
				}

				// получаем целевой город
				TownInfo target = route.getTarget();

				// ставим координаты портала этого города
				player.setLoc(target.getPortal());

				// получам менеджера БД
				DataBaseManager dbManager = DataBaseManager.getInstance();

				// обновляем в БД
				dbManager.updatePlayerContinentId(player);

				// посылаем пакет вылета из портала
				player.broadcastPacket(DeparturePortal.getInstance(player));
				player.broadcastPacket(StateAllowed.getInstance(player, DEPARTURE_CONTINUE_STATE));

				// применяем новый ид зоны
				player.setZoneId(target.getZone());
				// отправляем пакет новой зоны
				player.sendPacket(WorldZone.getInstance(player, target.getZone()), true);

				// ставим стадию посадки
				setState(State.LANDING);

				// запускаем таск
				schedule = executor.scheduleGeneral(this, target.getToLanding());

				break;
			}
			// стадия посадки
			case LANDING:
			{
				// получаем текущий путь
				Route route = player.getRoute();

				// если путя нету, выходим
				if(route == null)
				{
					log.warning(this, new Exception("not found route"));
					return false;
				}

				// получаем целевой город
				TownInfo target = route.getTarget();

				// ставим позицию посадки
				player.teleToLocation(target.getLanding());

				// отправляем пакет завершения полета
				player.broadcastPacket(GetOffPegas.getInstance(player));

				// убираем флаг полета
				player.setFlyingPegas(false);

				// убираем флаг неуязвимости
				player.setInvul(false);

				// зануляем путь
				player.setRoute(null);

				// завершение
				close();
			}
		}

		return true;
	}

	@Override
	public synchronized boolean close()
	{
		if(schedule != null)
		{
			schedule.cancel(false);
			schedule = null;
		}

		return super.close();
	}

	/**
	 * Запускает полет по указанному маршруту.
	 *
	 * @param index номер выбранного маршрута.
	 * @return успешно ли запущен полет.
	 */
	public synchronized boolean fly(int index)
	{
		// если стадия диалога не стартовая, выходим
		if(state != State.STARTING)
			return false;

		// получаем игрока
		Player player = getPlayer();

		// если игрока нету, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		// целевой путь
		Route route = routes.get(index);

		// если его нету, выходим
		if(route == null)
		{
			log.warning(this, new Exception("not found route"));
			return false;
		}

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// если денег не хватает, выходим
		if(inventory.getMoney() < route.getPrice())
			return false;

		// забираем деньги
		inventory.subMoney(route.getPrice());

		// получаем логера игровых событий
		GameLogManager gameLogger = GameLogManager.getInstance();

		// записываем событие о покупке скила
		gameLogger.writeItemLog(player.getName() + " buy fly pegas for " + route.getPrice() + " gold");

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем инвентарь
		eventManager.notifyInventoryChanged(player);

		// запоминаем маршрут
		player.setRoute(route);

		return true;
	}

	/**
	 * @return текущее состояние.
	 */
	public State getState()
	{
		return state;
	}

	@Override
	public DialogType getType()
	{
		return DialogType.PEGAS;
	}

	@Override
	public synchronized boolean init()
	{
		// если маршрутов нет, выходим
		if(routes.isEmpty())
			return false;

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

		// отправляем пакеты окна выбора маршрута
		player.sendPacket(PegasRouts.getInstance(routes, town.getId()), true);
		player.sendPacket(PegasReplyPacket.getInstance(player), true);

		return true;
	}

	@Override
	public void reinit()
	{
		state = State.STARTING;
	}

	@Override
	public void run()
	{
		if(!apply())
			close();
	}

	/**
	 * @param state текущее состояние.
	 */
	public void setState(State state)
	{
		this.state = state;
	}
}

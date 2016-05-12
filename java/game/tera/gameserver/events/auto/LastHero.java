package tera.gameserver.events.auto;

import rlib.geom.Coords;
import rlib.util.Rnd;
import rlib.util.array.Array;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.Config;
import tera.gameserver.events.EventConstant;
import tera.gameserver.events.EventPlayer;
import tera.gameserver.events.EventState;
import tera.gameserver.events.EventType;
import tera.gameserver.events.EventUtils;
import tera.gameserver.manager.EventManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.LinkType;
import tera.gameserver.model.npc.interaction.links.NpcLink;
import tera.gameserver.model.npc.interaction.replyes.Reply;
import tera.gameserver.model.npc.spawn.Spawn;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.AppledEffect;
import tera.gameserver.network.serverpackets.CancelEffect;
import tera.gameserver.tables.WorldZoneTable;
import tera.util.Location;

/**
 * Ивент Последний Герой.
 * 
 * @author Ronn
 * @created 11.04.2012
 */
public final class LastHero extends AbstractAutoEvent
{
	/** название ивента */
	public static final String EVENT_NAME = "LH";

	/** ид ивент территории */
	public static final int TERRITORY_ID = 54;

	/** центр ивент зоны */
	private static final Location CENTER = new Location(11995, 7736, 974, 0, 0);

	/**
	 * Обработка нажатия регистрации.
	 */
	private final Reply REPLY_REGISTER = new Reply()
	{
		@Override
		public void reply(Npc npc, Player player, Link link)
		{
			registerPlayer(player);
		}
	};

	/**
	 * обработка нажатия отрегистрации.
	 */
	private final Reply REPLY_UNREGISTER = new Reply()
	{
		@Override
		public void reply(Npc npc, Player player, Link link)
		{
			unregisterPlayer(player);
		}
	};

	/** ссылка на регистрацию */
	private final Link LINK_REGISTER = new NpcLink("Рег. LastHero", LinkType.DIALOG, IconType.GREEN, REPLY_REGISTER);
	/** ссылка отрегистрации */
	private final Link LINK_UNREGISTER = new NpcLink("Отрег. LastHero", LinkType.DIALOG, IconType.GREEN, REPLY_UNREGISTER);

	@Override
	public void addLinks(Array<Link> links, Npc npc, Player player)
	{
		// если не запущен ивент
		if(!isStarted())
			return;

		// еси не тот НПС
		if(npc.getTemplate() != EventConstant.MYSTEL)
			return;

		// если не подходит стадия ивента
		if(getState() != EventState.REGISTER)
			return;

		// если игрок не подходит по уровню
		if(player.getLevel() > getMaxLevel() || player.getLevel() < getMinLevel())
			return;

		// если игрок мертв
		if(player.isDead())
			return;

		// если игрок в дуэли
		if(player.hasDuel())
			return;

		// получаем список зарегестрированных
		Array<Player> prepare = getPrepare();

		// если игрок уже зарегестрирован
		if(prepare.contains(player))
			// добавляем ссылку на отрегистрацию
			links.add(LINK_UNREGISTER);
		else
			// добавляем ссылку на регистрацию
			links.add(LINK_REGISTER);
	}

	@Override
	protected void finishingState()
	{
		// получаем таблицу игроков
		Table<IntKey, EventPlayer> players = getPlayers();

		// перебираем участников
		for(EventPlayer eventPlayer : players)
		{
			// получаем игрока
			Player player = eventPlayer.getPlayer();

			// убераем ему фракцию
			player.setFractionId(0);
			// убераем блокировку воскрешения
			player.setResurrected(true);
			// убераем флаг нахождения на ивенте
			player.setEvent(false);

			// восстанавливаем статы
			eventPlayer.restoreState();

			// обновляем статы
			player.updateInfo();

			// возвращаем на старую позицию
			eventPlayer.restoreLoc();

			// складываем в пул
			eventPlayer.fold();
		}

		// останавливаем ивент
		stop();

		// получаем ивент менеджера
		EventManager eventManager = EventManager.getInstance();

		// уведомляем о финише его
		eventManager.finish(this);
	}

	@Override
	protected int getMaxLevel()
	{
		return Config.EVENT_LH_MAX_LEVEL;
	}

	@Override
	protected int getMinLevel()
	{
		return Config.EVENT_LH_MIN_LEVEL;
	}

	@Override
	public String getName()
	{
		return EVENT_NAME;
	}

	@Override
	protected int getRegisterTime()
	{
		return Config.EVENT_LH_REGISTER_TIME;
	}

	@Override
	protected int getTerritoryId()
	{
		return TERRITORY_ID;
	}

	@Override
	public EventType getType()
	{
		return EventType.LAST_HERO;
	}

	@Override
	protected boolean isCheckDieState()
	{
		return state == EventState.RUNNING || state == EventState.PREPARE_END;
	}

	@Override
	protected boolean isCheckTerritoryState()
	{
		return state == EventState.RUNNING || state == EventState.PREPARE_END;
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	protected void onDelete(Player player)
	{
		lock();
		try
		{
			switch(state)
			{
				case REGISTER:
				case PREPARE_START:
					getPrepare().fastRemove(player);
					break;
				case PREPARE_END:
				case RUNNING:
				{
					// получаем ивент игрока
					EventPlayer eventPlayer = removeEventPlayer(player.getObjectId());

					// если он есть, ложим в пул
					if(eventPlayer != null)
						eventPlayer.fold();

					// удаляем его из активных участников
					removeActivePlayer(player);

					// обновляем результат ивента
					updateResult();
				}
			}
		}
		finally
		{
			unlock();
		}
	}

	@Override
	protected void onDie(Player killed, Character killer)
	{
		lock();
		try
		{
			// удаляем из списка активных игроков
			removeActivePlayer(killed);

			// обновляем ивент
			updateResult();
		}
		finally
		{
			unlock();
		}
	}

	@Override
	protected void onEnter(Player player)
	{
		// если он не мертв
		if(!player.isDead())
		{
			// получаем таблицу регионов
			WorldZoneTable zoneTable = WorldZoneTable.getInstance();

			// отправляем его в ближайшуюю точку респа
			player.teleToLocation(zoneTable.getRespawn(player));
		}
	}

	@Override
	protected void onExit(Player player)
	{
		// если он не мертвый
		if(!player.isDead())
		{
			// зануляем хп
			player.setCurrentHp(0);
			// убиваем
			player.doDie(player);
			// сообщаем
			player.sendMessage("You are out of the event-zone.");
		}
	}

	@Override
	protected void prepareEndState()
	{
		World.sendAnnounce("Бой окончен.");

		Spawn[] guards = EventUtils.guards;

		// убераем гвардов
		for(int i = 0, length = guards.length; i < length; i++)
			guards[i].stop();

		// получаем список активных игроков
		Array<Player> activePlayers = getActivePlayers();

		// получаем победителя
		Player player = activePlayers.first();

		// получаем таблицу игроков
		Table<IntKey, EventPlayer> players = getPlayers();

		// если нет победителя
		if(activePlayers.size() > 1 || player == null)
			World.sendAnnounce("Победивших нет...");
		else
		{
			World.sendAnnounce("Победил " + player.getName());

			// подсчитываем уровень
			int level = 0;

			// перебираем всех участников
			for(EventPlayer eventPlayer : players)
			{
				Player target = eventPlayer.getPlayer();

				if(target == null)
					continue;

				level += target.getLevel();
			}

			// получаем средний лвл
			level /= players.size();

			// рассчитываем награду
			int reward = (int) Math.max(Math.sqrt(players.size()) * level / 2, 1);

			synchronized(player)
			{
				// выдаем награду
				player.setVar(EventConstant.VAR_NANE_HERO_POINT, player.getVar(EventConstant.VAR_NANE_HERO_POINT, 0) + reward);
			}

			// сообщаем об награде
			player.sendMessage("Вы получили " + reward + " очка(ов) славы.");
		}

		// ставим стадию финиша
		setState(EventState.FINISHING);

		// получаем исполнительный менеджер
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем завершение
		executor.scheduleGeneral(this, 5000);
	}

	@Override
	protected void prepareStartState()
	{
		// получаем список зарегестрированных
		Array<Player> prepare = getPrepare();
		// получаем список активных игроков
		Array<Player> activePlayers = getActivePlayers();

		// получаем таблицу участников
		Table<IntKey, EventPlayer> players = getPlayers();

		// получаем массив зарегестрированных игроков
		Player[] array = prepare.array();

		// перебираем их
		for(int i = 0, length = prepare.size(); i < length; i++)
		{
			// если уже набрано нужное кол-во игроков, выходим
			if(i >= Config.EVENT_LH_MAX_PLAYERS)
				break;

			// получаем игрока
			Player player = array[i];

			// если игрок мертв
			if(player.isDead())
			{
				// сообщаем ему и пропускаем его
				player.sendMessage("You is dead.");
				continue;
			}

			// если игрок в дуэли
			if(player.hasDuel())
			{
				// сообщаем ему и пропускаем его
				player.sendMessage(MessageType.YOU_ARE_IN_A_DUEL_NOW);
				continue;
			}

			// если игрок уже добавлен в таблицу участников, пропускаем
			if(players.containsKey(player.getObjectId()))
				continue;

			// ставим флаг не воскрешаемости
			player.setResurrected(false);

			// создаем обертку ивент плеера
			EventPlayer eventPlayer = EventPlayer.newInstance(player);

			// вносим его в таблицу
			players.put(player.getObjectId(), eventPlayer);

			// указываем ид фракции
			player.setFractionId(i + 1);

			// добавляем к активным игрокам
			activePlayers.add(player);

			// запоминаем позицию
			eventPlayer.saveLoc();
			// запоминаем статы
			eventPlayer.saveState();
		}

		// очищаем промежуточный список
		prepare.clear();

		// если нужного кол-во не набралось
		if(players.size() < Config.EVENT_LH_MIN_PLAYERS)
		{
			World.sendAnnounce("Недостаточное кол-во участников.");

			// ставим сталию финиша
			state = EventState.FINISHING;

			// вырубаем таск ивента
			if(schedule != null)
				schedule.cancel(true);

			// получаем исполнительный менеджер
			ExecutorManager executor = ExecutorManager.getInstance();

			// выполняем завершени е
			executor.execute(this);

			// выходим
			return;
		}

		World.sendAnnounce("В ивенте будут участвовать " + players.size() + " игрока(ов)");
		World.sendAnnounce("Бой начнется через 1 минуту.");

		// получаем массив участников
		array = activePlayers.array();

		// получаем локацию буферную
		Location loc = EventUtils.takeLocation();

		// перебираем участников
		for(int i = 0, length = activePlayers.size(); i < length; i++)
		{
			// получаем участника
			Player player = array[i];

			// телепортируем его на арену
			player.teleToLocation(Coords.randomCoords(loc, CENTER.getX(), CENTER.getY(), CENTER.getZ(), Rnd.nextInt(65000), 40, 300));

			// добавляем локер движения
			lockMove(player);

			// ставим флаг блокировки
			player.setStuned(true);
			// отображаем анимацию блока
			player.broadcastPacket(AppledEffect.getInstance(player, player, 701100, 60000));
			// обновляем инфу игроку
			player.updateInfo();
		}

		EventUtils.putLocation(loc);

		// очищаем территорию
		clearTerritory();

		// ставим стадию запуска
		setState(EventState.RUNNING);
	}

	@Override
	protected void runningState()
	{
		Spawn[] guards = EventUtils.guards;

		// спавним гвардов
		for(int i = 0, length = guards.length; i < length; i++)
			guards[i].start();

		// получаем список активных игроков
		Array<Player> activePlayers = getActivePlayers();

		// получаем массив участников
		Player[] array = activePlayers.array();

		// перебираем участников
		for(int i = 0, length = activePlayers.size(); i < length; i++)
		{
			// получаем участника
			Player player = array[i];

			// удаляем локер движения
			unlockMove(player);

			// восстанавливаем полностью статы
			player.setStamina(player.getMaxStamina());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());

			// убераем блокировку
			player.setStuned(false);
			// убераем анимацию блока
			player.broadcastPacket(CancelEffect.getInstance(player, 701100));
			// обновляем инфу игроку
			player.updateInfo();
			// обновляем инфу
			player.updateInfo();
		}

		World.sendAnnounce("В БОЙ!!!");

		// ставим стадию подготовки к финишу
		setState(EventState.PREPARE_END);

		// отменяем таск
		if(schedule != null)
			schedule.cancel(false);

		// получаем исполнительный менеджер
		ExecutorManager executor = ExecutorManager.getInstance();

		// создаем новый
		schedule = executor.scheduleGeneral(this, Config.EVENT_LH_BATTLE_TIME * 60 * 1000);

		// обновляем состяоние ивента
		updateResult();
	}

	/**
	 * @param player исключенный игрок.
	 */
	private void updateResult()
	{
		lock();
		try
		{
			// получаем объекты в территории
			Array<TObject> objects = getEventTerritory().getObjects();

			// получаем список активных игроков
			Array<Player> activePlayers = getActivePlayers();

			// получаем массив участников
			Player[] array = activePlayers.array();

			// перебираем их
			for(int i = 0, length = activePlayers.size(); i < length; i++)
			{
				// получаем участника
				Player player = array[i];

				// если его нет, пропускаем
				if(player == null)
					continue;

				// если его нет в территории
				if(!objects.contains(player))
				{
					// получаем ивент игрока
					EventPlayer eventPlayer = removeEventPlayer(player.getObjectId());

					// если он есть, ложим в пул
					if(eventPlayer != null)
						eventPlayer.fold();

					// удаляем его из активных участников
					activePlayers.fastRemove(player);
				}
			}

			// если осталось меньше 1ого игрока
			if(activePlayers.size() < 2)
			{
				// если стадия боев
				if(getState() == EventState.PREPARE_END)
				{
					// отменяем таск
					if(schedule != null)
						schedule.cancel(false);

					// получаем исполнительный менеджер
					ExecutorManager executor = ExecutorManager.getInstance();

					schedule = executor.scheduleGeneral(this, 5000);
				}
			}
		}
		finally
		{
			unlock();
		}
	}
}

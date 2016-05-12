package tera.gameserver.events.auto;

import rlib.geom.Coords;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.random.Random;
import rlib.util.random.Randoms;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;

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
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.LinkType;
import tera.gameserver.model.npc.interaction.links.NpcLink;
import tera.gameserver.model.npc.interaction.replyes.Reply;
import tera.gameserver.model.npc.playable.EventEpicBattleNpc;
import tera.gameserver.model.npc.spawn.NpcSpawn;
import tera.gameserver.model.npc.spawn.Spawn;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.AppledEffect;
import tera.gameserver.network.serverpackets.CancelEffect;
import tera.gameserver.tables.ConfigAITable;
import tera.gameserver.tables.NpcTable;
import tera.gameserver.tables.WorldZoneTable;
import tera.gameserver.templates.NpcTemplate;
import tera.util.Location;

/**
 * Ивент Эпичной Битвы
 *
 * @author Ronn
 * @created 11.04.2012
 */
public final class EpicBattle extends AbstractAutoEvent
{
	/** название ивента */
	public static final String EVENT_NAME = "EpicBattle";

	/** ид территории, на которой ивент проводится */
	public static final int TERRITORY_ID = 54;

	/** точки для размещение первой тимы */
	private static final Location[] playerPoints =
	{
		new Location(10970, 7848, 974, 0, 0),
		new Location(11148, 7978, 975, 0, 0),
		new Location(11282, 8079, 979, 0, 0),
		new Location(11464, 8206, 983, 0, 0),
		new Location(11647, 8337, 977, 0, 0),
		new Location(11838, 8466, 977, 0, 0),
		new Location(12106, 8658, 974, 0, 0),
	};

	/** точки для размещение второй тимы */
	private static final Location[] bootsPoints =
	{
		new Location(11818, 6736, 978, 0, 0),
		new Location(12000, 6858, 976, 0, 0),
		new Location(12179, 6996, 976, 0, 0),
		new Location(12390, 7156, 976, 0, 0),
		new Location(12614, 7336, 978, 0, 0),
		new Location(12925, 7576, 972, 0, 0),
	};

	/** шаблоны НПС участников */
	private final NpcTemplate[] NPC_TYPES;

	/** настройки АИ НПС */
	private final ConfigAI[] NPC_CONFIG;

	/** таблица пулов спавно. */
	private final Table<NpcTemplate, Array<NpcSpawn>> spawnPool;

	/** обработка нажатия регистрации */
	private final Reply REPLY_REGISTER= new Reply()
	{
		@Override
		public void reply(Npc npc, Player player, Link link)
		{
			registerPlayer(player);
		}
	};

	/** обработка нажатия отрегистрации */
	private final Reply REPLY_UNREGISTER= new Reply()
	{
		@Override
		public void reply(Npc npc, Player player, Link link)
		{
			unregisterPlayer(player);
		}
	};

	/** ссылка на регистрацию */
	private final Link LINK_REGISTER = new NpcLink("Рег. EpicBattle", LinkType.DIALOG, IconType.GREEN, REPLY_REGISTER);
	/** ссылка отрегистрации */
	private final Link LINK_UNREGISTER = new NpcLink("Отрег. EpicBattle", LinkType.DIALOG, IconType.GREEN, REPLY_UNREGISTER);

	/** состав команды игроков*/
	private final Array<Player> playerTeam;
	/** состав команды ботов */
	private final Array<EventEpicBattleNpc> bootsTeam;
	/** спавны участников ботов */
	private final Array<NpcSpawn> currentSpawns;

	/** рандоминайзер */
	private final Random random;

	public EpicBattle()
	{
		this.playerTeam = Arrays.toArray(Player.class);
		this.bootsTeam = Arrays.toArray(EventEpicBattleNpc.class);
		this.currentSpawns = Arrays.toArray(NpcSpawn.class);
		this.random = Randoms.newRealRandom();
		this.spawnPool = Tables.newObjectTable();

		// получаем таблицу НПС
		NpcTable npcTable = NpcTable.getInstance();

		NPC_TYPES = new NpcTemplate[]
	    {
			npcTable.getTemplate(20001, 300),
			npcTable.getTemplate(20002, 300),
			npcTable.getTemplate(20003, 300),
			npcTable.getTemplate(20004, 300),
			npcTable.getTemplate(20005, 300),
			npcTable.getTemplate(20006, 300),
	    };

		// получаем таблицу настроек АИ
		ConfigAITable configTable = ConfigAITable.getInstance();

		NPC_CONFIG = new ConfigAI[]
		{
			configTable.getConfig("PlayableWarrior"),
			configTable.getConfig("PlayableSorcerer"),
			configTable.getConfig("PlayablePriest"),
			configTable.getConfig("PlayableSlayer"),
			configTable.getConfig("PlayableLancer"),
			configTable.getConfig("PlayableArcher"),
		};
	}

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

		// получаем список использованных спавнов
		NpcSpawn[] array = currentSpawns.array();

		// перебираем использованные спавны
		for(int i = 0, length = currentSpawns.size(); i < length; i++)
		{
			// получаем спавн
			NpcSpawn spawn = array[i];

			// останавливаем его
			spawn.stop();

			// получаем темплейт НПС
			NpcTemplate template = spawn.getTemplate();

			// получаем пул этого спавна
			Array<NpcSpawn> pool = spawnPool.get(template);

			// складываем его туда
			pool.add(spawn);
		}

		// очмщаем список спавнов
		currentSpawns.clear();

		// останавливаем ивент
		stop();

		// получаем менеджер ивентов
		EventManager eventManager = EventManager.getInstance();

		// уведомляем о финише его
		eventManager.finish(this);
	}

	@Override
	protected int getMaxLevel()
	{
		return Config.EVENT_EB_MAX_LEVEL;
	}

	@Override
	protected int getMinLevel()
	{
		return Config.EVENT_EB_MIN_LEVEL;
	}

	/**
	 * @return команда монстров.
	 */
	public Array<EventEpicBattleNpc> getMonsterTeam()
	{
		return bootsTeam;
	}

	@Override
	public String getName()
	{
		return EVENT_NAME;
	}

	/**
	 * @return команда игроков.
	 */
	public Array<Player> getPlayerTeam()
	{
		return playerTeam;
	}

	@Override
	protected int getRegisterTime()
	{
		return Config.EVENT_EB_REGISTER_TIME;
	}

	@Override
	protected int getTerritoryId()
	{
		return TERRITORY_ID;
	}

	@Override
	public EventType getType()
	{
		return EventType.TEAM_VS_MONSTERS;
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
			switch(getState())
			{
				case REGISTER:
				case PREPARE_START: getPrepare().fastRemove(player); break;
				case PREPARE_END:
				case RUNNING:
				{
					// поучаем ивент обертку игрока
					EventPlayer eventPlayer = removeEventPlayer(player.getObjectId());

					// если такая имеется
					if(eventPlayer != null)
						// складываем ее в пул
						eventPlayer.fold();

					// удаляем из команды игроков игрока
					playerTeam.fastRemove(player);

					// обновляем результат
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
	public void onDie(Character killer, Character killed)
	{
		// проверяем стадию ивента
		if(!isCheckDieState() || !(killed.isPlayer() || killed.getClass() == EventEpicBattleNpc.class))
			return;

		lock();
		try
		{
			// если убитый игрок и состоит в команде игроков
			if(killed.isPlayer() && playerTeam.contains(killed))
			{
				// удаляем оттуда
				playerTeam.fastRemove(killed);

				// обновляем статус ивента
				updateResult();
			}
			// если убитый нпс и состоит в команде НПС
			else if(bootsTeam.contains(killed))
			{
				// удаляем оттуда
				bootsTeam.fastRemove(killed);

				// обновляем статус ивента
				updateResult();
			}
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
			player.teleToLocation(zoneTable.getDefaultRespawn(player));
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
    		player.sendMessage("You left the event area");
    	}
	}

	@Override
	protected void prepareEndState()
	{
		World.sendAnnounce("The battle is over");

		Spawn[] guards = EventUtils.guards;

		// убераем гвардов
		for(int i = 0, length = guards.length; i < length; i++)
			guards[i].stop();

		// определяем победителя
		int winner = -1;

		// если победила вторая тима
		if(playerTeam.isEmpty() && !bootsTeam.isEmpty())
			World.sendAnnounce("The winner team of the elite!");
		// если победила первая
		else if(!playerTeam.isEmpty() && bootsTeam.isEmpty())
		{
			World.sendAnnounce("Team won the of players!");
			winner = 1;
		}
		// если никто не победил
		else
		{
			World.sendAnnounce("The event has no winners...");
		}

		// если есть победитель
		if(winner == 1)
		{
			// получаем таблицу игроков
			Table<IntKey, EventPlayer> players = getPlayers();

			// перебираем всех участников
			for(EventPlayer eventPlayer : players)
			{
				// получаем игрока
				Player player = eventPlayer.getPlayer();

				// если он не из победившей тимы, пропускаем
				if(player.getFractionId() != winner)
					continue;

				// рассчитываем награду
				int reward = (int) Math.max(Math.sqrt(players.size()) * Math.sqrt(player.getLevel()), 1) * 2;

				synchronized(player)
				{
					// выдаем награду
					player.setVar(EventConstant.VAR_NANE_HERO_POINT, player.getVar(EventConstant.VAR_NANE_HERO_POINT, 0) + reward);
				}

				// сообщаем об награде
				player.sendMessage("You received " + reward + " point(s) of fame");
			}
		}

		// ставим статус финиша
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

		// получаем таблицу участников
		Table<IntKey, EventPlayer> players = getPlayers();

		// получаем массив зарегестрированных игроков
		Player[] array = prepare.array();

		// перебираем их
		for(int i = 0, length = prepare.size(); i < length; i++)
		{
			// если уже набрано нужное кол-во игроков, выходим
			if(i >= Config.EVENT_EB_MAX_PLAYERS)
				break;

			// получаем игрока
			Player player = array[i];

			// если игрок мертв
			if(player.isDead())
			{
				// сообщаем ему и пропускаем его
				player.sendMessage("You are dead.");
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

			// устанавливаем общую фракцию
			player.setFractionId(1);

			// добавляем в команду игроков
			playerTeam.add(player);

			// запоминаем позицию
			eventPlayer.saveLoc();
			// запоминаем статы
			eventPlayer.saveState();
		}

		// очищаем промежуточный список
		prepare.clear();

		// если нужного кол-во не набралось
		if(players.size() < Config.EVENT_EB_MIN_PLAYERS)
		{
			World.sendAnnounce("Insufficient number of participants.");

			// ставим сталию финиша
			setState(EventState.FINISHING);

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
		World.sendAnnounce("Fight will stat in 1 minute");

		array = playerTeam.array();

		Location loc = EventUtils.takeLocation();

		// перебираем участников
		for(int i = 0, length = playerTeam.size(); i < length; i++)
		{
			// получаем участника
			Player player = array[i];

			// получаем позицию игрока
			Location point = playerPoints[random.nextInt(0, playerPoints.length - 1)];

			// расчитываем случайную точку от центра
			loc = Coords.randomCoords(loc, point.getX(), point.getY(), point.getZ(), 10, 100);

			// УСТАНАВЛИВАЕМ ИД КОНТИНЕНТА
			loc.setContinentId(0);

			// телепортируем его на арену
			player.teleToLocation(loc);

			// добавляем локер движения
			lockMove(player);

			// ставим флаг блокировки
			player.setStuned(true);
			// отображаем анимацию блока
			player.broadcastPacket(AppledEffect.getInstance(player, player, 701100, 60000));
			// обновляем инфу игроку
			player.updateInfo();
		}

		clearTerritory();

		// меняем стадию
		setState(EventState.RUNNING);
	}

	@Override
	protected void runningState()
	{
		Spawn[] guards = EventUtils.guards;

		// спавним гвардов
		for(int i = 0, length = guards.length; i < length; i++)
			guards[i].start();

		Player[] array = playerTeam.array();

		// перебираем участников
		for(int i = 0, length = playerTeam.size(); i < length; i++)
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
		}

		// спавним их
		for(int i = 0, length = NPC_TYPES.length; i < length; i++)
		{
			// получаем тип монстра
			NpcTemplate template = NPC_TYPES[i];
			// получаем конфиг АИ для этого типа монстра
			ConfigAI config = NPC_CONFIG[i];

			// получаем пул спавнов этого типа монстра
			Array<NpcSpawn> pool = spawnPool.get(template);

			// если пула еще нету
			if(pool == null)
			{
				// создаем его
				pool = Arrays.toArray(NpcSpawn.class);
				// вставляем пул
				spawnPool.put(template, pool);
			}

			// пробуем получить спавн из пула
			NpcSpawn spawn = pool.pop();

			// если спавна нет
			if(spawn == null)
				// создаем новый
				spawn = new NpcSpawn(template, new Location(), config, NpcAIClass.EPIC_BATTLE_EVENT);

			// получаем точку для разсмещения
			Location point = bootsPoints[random.nextInt(0, bootsPoints.length - 1)];

			// получаем точку спавна
			Location loc = spawn.getLocation();

			// обновляем его
			loc.setXYZHC(point.getX(), point.getY(), point.getZ(), -1, 0);

			// ставим рандомную область
			spawn.setMaxRadius(100);
			spawn.setMinRadius(60);

			// ставим большой респ
			spawn.setRespawnTime(1800);

			// добавляем в список спавнов
			currentSpawns.add(spawn);

			// спавним
			spawn.start();

			// добавляем в команду
			bootsTeam.add((EventEpicBattleNpc) spawn.getSpawned());
		}

		World.sendAnnounce("TO BATTLE!!!");

		// ставим стадию подготовки к финишу
		setState(EventState.PREPARE_END);

		// отменяем таск
		if(schedule != null)
			schedule.cancel(false);

		// получаем исполнительный менеджер
		ExecutorManager executor = ExecutorManager.getInstance();

		// создаем новый
		schedule = executor.scheduleGeneral(this, Config.EVENT_EB_BATTLE_TIME * 60 * 1000);

		// обновляем состяоние ивента
		updateResult();
	}

	@Override
	public synchronized boolean stop()
	{
		if(super.stop())
		{
			// очищаем список игроков
			playerTeam.clear();
			bootsTeam.clear();

			return true;
		}

		return false;
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

			// получаем массив команды игроков
			Player[] array = playerTeam.array();

			// перебираем их
			for(int i = 0, length = playerTeam.size(); i < length; i++)
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
					playerTeam.fastRemove(player);
				}
			}

			// если хоть одна тима опустила
			if(playerTeam.isEmpty() || bootsTeam.isEmpty())
			{
				// если стадия боя
				if(getState() == EventState.PREPARE_END)
				{
					// отменяем таск
					if(schedule != null)
						schedule.cancel(false);

					// получаем исполнительный менеджер
					ExecutorManager executor = ExecutorManager.getInstance();

					// запускаем таск обработки завершения
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

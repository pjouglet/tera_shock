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
import tera.gameserver.events.EventTeam;
import tera.gameserver.events.EventType;
import tera.gameserver.events.EventUtils;
import tera.gameserver.manager.EventManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
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
 * Реализация ивента "Турнир"
 *
 * @author Ronn
 */
public class Tournament extends AbstractAutoEvent
{
	/** центр ивент зоны */
	private static final Location CENTER = new Location(11995, 7736, 974, 0 , 0);

	/** название ивента */
	public static final String EVENT_NAME = "Tournament";

	/** ид территории ивента */
	public static final int TERRITORY_ID = 54;

	/** точки для размещение первой тимы */
	private static final Location[] firstPoints =
	{
		new Location(11670, 8222, 977, 0, 0),
		new Location(11600, 8179, 977, 0, 0),
		new Location(11748, 8274, 977, 0, 0),
		new Location(11522, 8128, 977, 0, 0),
		new Location(11829, 8327, 977, 0, 0),
	};

	/** точки для размещение второй тимы */
	private static final Location[] secondPoints =
	{
		new Location(12356, 7232, 975, 0, 0),
		new Location(12423, 7275, 975, 0, 0),
		new Location(12285, 7184, 975, 0, 0),
		new Location(12501, 7327, 975, 0, 0),
		new Location(12201, 7130, 975, 0, 0),
	};

	/**
	 * Обработка нажатия регистрации.
	 */
	private final Reply REPLY_REGISTER= new Reply()
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
	private final Reply REPLY_UNREGISTER= new Reply()
	{
		@Override
		public void reply(Npc npc, Player player, Link link)
		{
			unregisterPlayer(player);
		}
	};

	/** ссылка на регистрацию */
	private final Link LINK_REGISTER = new NpcLink("Рег. Tournament", LinkType.DIALOG, IconType.GREEN, REPLY_REGISTER);
	/** ссылка отрегистрации */
	private final Link LINK_UNREGISTER = new NpcLink("Отрег. Tournament", LinkType.DIALOG, IconType.GREEN, REPLY_UNREGISTER);

	/** таблица команд для игроков */
	private final Table<IntKey, EventTeam> playerTeams;

	/** список учавствующих команд */
	private final Array<EventTeam> teams;
	/** список команд */
	private final Array<EventTeam> teamSet;

	/** список позиций для просмотра игроков */
	private final Array<Location> points;

	/** рандоминайзер */
	private final Random random;

	private EventTeam first;
	private EventTeam second;

	/** размер команды */
	private int teamSize;
	/** этап ивента */
	private int level;

	public Tournament()
	{
		this.teams = Arrays.toArray(EventTeam.class);
		this.teamSet = Arrays.toArray(EventTeam.class);
		this.points = Arrays.toArray(Location.class);
		this.playerTeams = Tables.newIntegerTable();
		this.random = Randoms.newRealRandom();
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
		Spawn[] guards = EventUtils.guards;

		// спавним гвардов
		for(int i = 0, length = guards.length; i < length; i++)
			guards[i].stop();

		if(teams.size() != 1)
			World.sendAnnounce("Победителя нет...");
		else if(teams.size() == 1)
		{
			// пполучаем таблицу всех игроков
			Table<IntKey, EventPlayer> players = getPlayers();

			// получаем команду победителей
			EventTeam team = teams.first();

			// сообщаем об этом
			World.sendAnnounce("Победила команда \"" + team.getName() + "\".");

			// получаем участников побежившей команды
			EventPlayer[] eventPlayers = team.getPlayers();

			// перебираем их
			for(int i = 0, length = team.size(); i < length; i++)
			{
				EventPlayer eventPlayer = eventPlayers[i];

				if(eventPlayer == null)
					continue;

				Player player = eventPlayer.getPlayer();

				if(player == null)
					continue;

				// рассчитываем награду
				int reward = (int) Math.max(Math.sqrt(players.size()) * Math.sqrt(player.getLevel()), 1) * 2;

				synchronized(player)
				{
					// выдаем награду
					player.setVar(EventConstant.VAR_NANE_HERO_POINT, player.getVar(EventConstant.VAR_NANE_HERO_POINT, 0) + reward);
				}

				// сообщаем об награде
				player.sendMessage("Вы получили " + reward + " очка(ов) славы.");
			}
		}

		// убераем блокировку
		stopLock();

		// воскрешаем мертвых
		ressurectPlayers();

		// перебираем участников
		for(EventPlayer eventPlayer : getPlayers())
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
		}

		// получаем список команд
		for(EventTeam team : playerTeams)
			if(!teamSet.contains(team))
				teamSet.add(team);

		// получаем массив тим
		EventTeam[] arrayTeams = teamSet.array();

		// складываем команды в пул
		for(int i = 0, length = teamSet.size(); i < length; i++)
			arrayTeams[i].fold();

		// получаем массв точек
		Location[] arrayLoc = points.array();

		// складвываем точки в пул
		for(int i = 0, length = points.size(); i < length; i++)
			EventUtils.putLocation(arrayLoc[i]);

		// очищаем списки и таблицы
		points.clear();
		playerTeams.clear();
		teams.clear();
		teamSet.clear();

		setFirst(null);
		setSecond(null);

		// останавливаем ивент
		stop();

		// получаем менеджер ивентов
		EventManager eventManager = EventManager.getInstance();

		// уведомляем о финише его
		eventManager.finish(this);
	}

	/**
	 * @return первая команда.
	 */
	protected final EventTeam getFirst()
	{
		return first;
	}

	@Override
	protected int getMaxLevel()
	{
		return Config.EVENT_TMT_MAX_LEVEL;
	}

	@Override
	protected int getMinLevel()
	{
		return Config.EVENT_TMT_MIN_LEVEL;
	}

	@Override
	public String getName()
	{
		return EVENT_NAME;
	}

	/**
	 * @return таблица ид игрока - его команда.
	 */
	public Table<IntKey, EventTeam> getPlayerTeams()
	{
		return playerTeams;
	}

	@Override
	protected int getRegisterTime()
	{
		return Config.EVENT_TMT_REGISTER_TIME;
	}

	/**
	 * @return вторая команда.
	 */
	protected final EventTeam getSecond()
	{
		return second;
	}

	/**
	 * @return список всех команд.
	 */
	public Array<EventTeam> getTeams()
	{
		return teams;
	}

	@Override
	protected int getTerritoryId()
	{
		return TERRITORY_ID;
	}

	@Override
	public EventType getType()
	{
		return null;//EventType.TOURNAMENT;
	}

	@Override
	protected boolean isCheckDieState()
	{
		return state == EventState.PREPARE_END;
	}

	@Override
	protected boolean isCheckTerritoryState()
	{
		return state == EventState.PREPARE_END || state == EventState.RUNNING;
	}

	/**
	 * перенос игроков на боевую позицию.
	 *
	 * @param players список команды.
	 * @param count кол-во игроков.
	 * @param fractionId ид фракции.
	 * @param points список точек.
	 */
	protected void movePlayersToPoints(EventPlayer[] players, int count, int fractionId, Location[] points)
	{
		for(int i = 0; i < count; i++)
		{
			Player player = players[i].getPlayer();

			// ставим сторону
			player.setFractionId(fractionId);
			// перемещаем игрока на боевую позицию
			player.teleToLocation(points[i]);

			// лочим движение
			lockMove(player);

			// лочим игрока
			player.setStuned(true);

			// отображаем анимацию блока
			player.broadcastPacket(AppledEffect.getInstance(player, player, EventUtils.SLEEP_ID, 30000));

			// обновляем инфо
			player.updateInfo();
		}
	}

	@Override
	protected void onDelete(Player player)
	{
		lock();
		try
		{
			// получаем таблицу всех игроков ивента
			Table<IntKey, EventPlayer> players = getPlayers();

			// получам таблицу команд
			Table<IntKey, EventTeam> playerTeams = getPlayerTeams();

			// получаем команду игрока
			EventTeam team = playerTeams.get(player.getObjectId());

			// если команда не найдена, выходим
			if(team == null)
				return;

			// удаляем игрока с команды
			team.removePlayer(players.get(player.getObjectId()));
		}
		finally
		{
			unlock();
		}

		updateState();
	}

	@Override
	protected void onDie(Player killed, Character killer)
	{
		updateState();
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
    		player.sendMessage("You are out of the event-zone.");
    	}

    	updateState();
	}

	@Override
	protected void prepareBattleState()
	{
		// получаем список всех команд.
		Array<EventTeam> teams = getTeams();

		// если осталась 1 команда
		if(teams.size() < 2)
		{
			// ставим стадию финиша
			setState(EventState.FINISHING);

			// вырубаем таск ивента
			if(schedule != null)
				schedule.cancel(false);

			// получаем исполнительный менеджер
			ExecutorManager executor = ExecutorManager.getInstance();

			// выполняем завершение
			executor.execute(this);

			// выходим
			return;
		}

		// ссылка на первую команду
		EventTeam first = null;
		// ссылка на вторую
		EventTeam second = null;

		// получаем список команд
		EventTeam[] array = teams.array();

		// перебираем все команды
		for(int i = 0, length = teams.size(); i < length; i++)
		{
			// получаем команду
			EventTeam team = array[i];

			// если команда подходит под этап
			if(team.getLevel() == level)
			{
				// запоминаем и выходим
				first = team;
				break;
			}
		}

		// если нет не одной подходящей команды
		if(first == null)
		{
			// ставим сталию финиша
			setState(EventState.FINISHING);

			// вырубаем таск ивента
			if(schedule != null)
				schedule.cancel(false);

			// получаем исполнительный менеджер
			ExecutorManager executor = ExecutorManager.getInstance();

			// выполняем завершение
			executor.execute(this);

			// выходим
			return;
		}

		// перебираем все команды с конца
		for(int i = teams.size() - 1; i > 0; i--)
		{
			// получаем команду
			EventTeam team = array[i];

			// если команда подходит под этап
			if(team != first && team.getLevel() == level)
			{
				// запоминаем и выходим
				second = team;
				break;
			}
		}

		// если нет соперников для первой команды
		if(second == null)
		{
			// увеличиваем этап первой команды
			first.increaseLevel();

			// увеличиваем этап ивента
			level++;

			// вырубаем таск ивента
			if(schedule != null)
				schedule.cancel(false);

			// получаем исполнительный менеджер
			ExecutorManager executor = ExecutorManager.getInstance();

			// выполняем заново подготовку к бою
			executor.execute(this);

			// выходим
			return;
		}

		// воскрешаем всех
		ressurectPlayers();

		// возвращаем игроков из битвы на свои места
		returnPlayers(getFirst());
		returnPlayers(getSecond());

		// разблокируем всех
		stopLock();

		// запоминаем новые команды
		setFirst(first);
		setSecond(second);

		// размещаем их на боевую позиции
		movePlayersToPoints(first.getPlayers(), first.size(), 1, firstPoints);
		// размещаем их на боевую позиции
		movePlayersToPoints(second.getPlayers(), second.size(), 2, secondPoints);

		// вешаем новый слип на всех, кроме тех, у кого щас бой будет
		startLock();

		// сообщаем о бое
		World.sendAnnounce("Этап " + (level + 1) + " : бой через 1 минуту \"" + first.getName() + "\" Vs \"" + second.getName() + "\"!");

		// ставим стадию запуска боя
		setState(EventState.RUNNING);

		// вырубаем таск ивента
		if(schedule != null)
			schedule.cancel(false);

		// получаем исполнительный менеджер
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускам таск боя
		schedule = executor.scheduleGeneral(this, 30000);
	}

	@Override
	protected void prepareEndState()
	{
		// получаем первую команду
		EventTeam first = getFirst();
		// получаем вторую команду
		EventTeam second = getSecond();

		// если почему-то хоть одной из них нет
		if(first == null || second == null)
		{
			// ставим сталию финиша
			setState(EventState.FINISHING);

			// вырубаем таск ивента
			if(schedule != null)
				schedule.cancel(false);

			// получаем исполнительный менеджер
			ExecutorManager executor = ExecutorManager.getInstance();

			// выполняем завершение
			executor.execute(this);

			// выходим
			return;
		}

		// если ничья
		if(first.isDead() && second.isDead() || !first.isDead() && !second.isDead())
		{
			// сообщаем об этом
			World.sendAnnounce("Победила ничья...");

			// удаляем обе команды
			teams.fastRemove(second);
			teams.fastRemove(first);
		}
		else
		{
			// ссылка на победителя
			EventTeam winner = null;
			// ссылка на проигравшего
			EventTeam loser = null;

			// если победила вторая команда
			if(first.isDead() && !second.isDead())
			{
				winner = second;
				loser = first;
			}
			// иначе если победила первая
			else if(!first.isDead() && second.isDead())
			{
				winner = first;
				loser = second;
			}

			// сообщаем об этом
			World.sendAnnounce("Победила команда \"" + winner.getName() + "\"!!!");

			// удаляем проиграввшую команду
			teams.fastRemove(loser);

			// увеличиваем уровень победившей
			winner.increaseLevel();
		}

		// ставим стадию подготовки к след. бою
		setState(EventState.PREPARE_BATLE);

		// вырубаем таск ивента
		if(schedule != null)
			schedule.cancel(false);

		// получаем исполнительный менеджер
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем новый таск
		schedule = executor.scheduleGeneral(this, 5000);
	}

	@Override
	protected void prepareStartState()
	{
		Spawn[] guards = EventUtils.guards;

		// спавним гвардов
		for(int i = 0, length = guards.length; i < length; i++)
			guards[i].start();

		// получаем список зарегестрированных
		Array<Player> prepare = getPrepare();

		// получаем таблицу участников
		Table<IntKey, EventPlayer> players = getPlayers();

		// получаем список аткивнхы игроков
		Array<Player> activePlayers = getActivePlayers();

		// если игроков не хватает на минимальное кол-во команд
		if(prepare.size() < (teamSize * Config.EVENT_TMT_MIN_TEAMS - Math.max(0, teamSize - 1)))
		{
			World.sendAnnounce("Недостаточное кол-во участников.");

			// ставим сталию финиша
			setState(EventState.FINISHING);

			// вырубаем таск ивента
			if(schedule != null)
				schedule.cancel(false);

			// получаем исполнительный менеджер
			ExecutorManager executor = ExecutorManager.getInstance();

			// выполняем завершение
			executor.execute(this);

			// выходим
			return;
		}

		// ссылка на команду
		EventTeam team = null;

		// перебираем игроков
		while(!prepare.isEmpty())
		{
			// получаем след. участника
			Player player = random.chance(50)? prepare.poll() : prepare.pop();

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

			// если тимы нет
			if(team == null)
				// создаем новую
				team = EventTeam.newInstance();

			// добавляем в команду игрока
			team.addPlayer(eventPlayer);

			// вносим тиму, за которй закреплен игрок
			playerTeams.put(player.getObjectId(), team);

			// добавляем в активным участникам
			activePlayers.add(player);

			// сохраняем его текущую позицию
			eventPlayer.saveLoc();
			// сохраняем его текущие статы
			eventPlayer.saveState();

			// если тима уже заполнена
			if(team.size() == teamSize)
			{
				// добавляем ее в список тим
				teams.add(team);
				// зануляем
				team = null;
			}
		}

		// очищаем промежуточный список
		prepare.clear();

		// если нужного кол-во не набралось
		if(teams.size() < Config.EVENT_TMT_MIN_TEAMS)
		{
			// ставим сталию финиша
			setState(EventState.FINISHING);

			// вырубаем таск ивента
			if(schedule != null)
				schedule.cancel(false);

			// получаем исполнительный менеджер
			ExecutorManager executor = ExecutorManager.getInstance();

			// выполняем завершени е
			executor.execute(this);

			// выходим
			return;
		}

		// заполняем список локациями
		for(int i = 0, length = players.size(); i < length; i++)
		{
			// получаем локацию с пула
			Location loc = EventUtils.takeLocation();

			// устанавливаем контирнент
			loc.setContinentId(0);

			// добавляем
			points.add(loc);
		}

		// получаем уже точки по кругу от центра
		Location[] locs = Coords.getCircularPoints(points.array(), CENTER.getX(), CENTER.getY(), CENTER.getZ(), players.size(), 440);

		// получаем список участников
		Player[] array = activePlayers.array();

		// перебираем игроков
		for(int i = 0, length = activePlayers.size(); i < length; i++)
		{
			// получаем игрока
			Player player = array[i];

			// телепортируем игрока на арену
			player.teleToLocation(locs[i]);

			// добавляем локер движения
			lockMove(player);

			// ставим флаг блокировки
			player.setStuned(true);
			// ставим флаг неуязвимости
			player.setInvul(true);
			// отображаем анимацию блока
			player.broadcastPacket(AppledEffect.getInstance(player, player, EventUtils.SLEEP_ID, 30000));
			// обновляем инфу игроку
			player.updateInfo();

			// получаем команду игрока
			team = playerTeams.get(player.getObjectId());
		}

		World.sendAnnounce("В ивенте будут участвовать " + players.size() + " игрока(ов) и " + teams.size() + " команд(а).");
		World.sendAnnounce("Через 30 секунд определится первый бой.");

		// очищаем зону от левых
		clearTerritory();

		// ставим стадию подготовки боя
		setState(EventState.PREPARE_BATLE);

		// вырубаем таск ивента
		if(schedule != null)
			schedule.cancel(false);

		// получаем исполнительный менеджер
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем таск подготовки первого боя
		schedule = executor.scheduleGeneral(this, 30000);
	}

	/**
	 * Воскрешение всех игроков.
	 */
	protected void ressurectPlayers()
	{
		// получаем спиоск игроков
		Array<Player> players = getActivePlayers();

		// получаем массив игроков
		Player[] array = players.array();

		// перебираем их
		for(int i = 0, length = players.size(); i < length; i++)
		{
			// получаем игрока
			Player player = array[i];

			// если игрок мертв
			if(player.isDead())
			{
				// восстанавливаем стамину
				player.setStamina(player.getMaxStamina());
				// восстанавливаем ХП
				player.setCurrentHp(player.getMaxHp());
				// восстаналвиваем МП
				player.setCurrentMp(player.getMaxMp());
			}
		}
	}

	/**
	 * Возвращение игроков из команды назад в круг.
	 *
	 * @param players игроки команды.
	 * @param count кол-во их.
	 */
	protected void returnPlayers(EventPlayer[] players, int count)
	{
		// получаем спиоск активных игроков
		Array<Player> activePlayers = getActivePlayers();

		// перебираем игроков команды
		for(int i = 0; i < count; i++)
		{
			// получаем игрока
			Player player = players[i].getPlayer();

			// ищем его номер в списке среди всех игроков
			int index = activePlayers.indexOf(player);

			// если его там нет оО, пропускаем
			if(index < 0)
				continue;

			// убераем фракцию
			player.setFractionId(0);

			// телепортируем его на свое место
			player.teleToLocation(points.get(index));
		}
	}

	/**
	 * Возвращение игроков команды на исходную позицию.
	 *
	 * @param team команда.
	 */
	protected void returnPlayers(EventTeam team)
	{
		if(team == null)
			return;

		returnPlayers(team.getPlayers(), team.size());
	}

	@Override
	protected void runningState()
	{
		// размещаем их на боевую позиции
		unlockPlayers(first.getPlayers(), second.size());
		// размещаем их на боевую позиции
		unlockPlayers(second.getPlayers(), second.size());

		// указываем анонс начала боя
		World.sendAnnounce("В БОЙ!!!");

		// получаем исполнительный менеджер
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускам таск боя
		schedule = executor.scheduleGeneral(this, Config.EVENT_TVT_BATTLE_TIME * 60000);

		// меняем стадию
		setState(EventState.PREPARE_END);
	}

	/**
	 * @param first задаваемое first
	 */
	protected final void setFirst(EventTeam first)
	{
		this.first = first;
	}

	/**
	 * @param second задаваемое second
	 */
	protected final void setSecond(EventTeam second)
	{
		this.second = second;
	}

	@Override
	public boolean start()
	{
		if(super.start())
		{
			// определяем размер тимы
			teamSize = random.nextInt(Config.EVENT_TMT_MIN_TEAM_SIZE, Config.EVENT_TMT_MAX_TEAM_SIZE);

			// обнулем этап
			level = 0;
		}

		return false;
	}

	protected void startLock()
	{
		// получаем список аткивных игроков
		Array<Player> activePlayers = getActivePlayers();

		// получаем массив аткивных игроков
		Player[] array = activePlayers.array();

		// получаем первую команду
		EventTeam first = getFirst();
		// получаем вторую команду
		EventTeam second = getSecond();

		// перебираем
		for(int i = 0, length = activePlayers.size(); i < length; i++)
		{
			// получаем игрока
			Player player = array[i];

			// получаем тиму игрока
			EventTeam team = playerTeams.get(player.getObjectId());

			// если игрок из активной команды, пропускаем
			if(team == first || team == second)
				continue;

			// ставим локер движения
			lockMove(player);

			// ставим флаг блокировки
			player.setStuned(true);
			// ставим флаг неуязвимости
			player.setInvul(true);
			// отображаем анимацию блока
			player.broadcastPacket(AppledEffect.getInstance(player, player, EventUtils.SLEEP_ID, (Config.EVENT_TMT_BATTLE_TIME + 1) * 60000));
			// обновляем инфу игроку
			player.updateInfo();
		}
	}

	protected void stopLock()
	{
		// получаем список аткивных игроков
		Array<Player> activePlayers = getActivePlayers();

		// получаем массив аткивных игроков
		Player[] array = activePlayers.array();

		// перебираем
		for(int i = 0, length = activePlayers.size(); i < length; i++)
		{
			// получаем игрока
			Player player = array[i];

			// убираем локер движения
			unlockMove(player);

			// убираем флаг блокировки
			player.setStuned(false);
			// убираем флаг неуязвимости
			player.setInvul(false);
			// снимаем иконку слипа
			player.broadcastPacket(CancelEffect.getInstance(player, EventUtils.SLEEP_ID));
			// обновляем инфу игроку
			player.updateInfo();
		}
	}

	/**
	 * Разблокирование игроков.
	 *
	 * @param players список команды.
	 * @param count кол-во игроков.
	 */
	protected void unlockPlayers(EventPlayer[] players, int count)
	{
		for(int i = 0; i < count; i++)
		{
			Player player = players[i].getPlayer();

			// удаляем локер движения
			unlockMove(player);

			// восстанавливаем стамину
			player.setStamina(player.getMaxStamina());
			// восстанавливаем ХП
			player.setCurrentHp(player.getMaxHp());
			// восстаналвиваем МП
			player.setCurrentMp(player.getMaxMp());

			// убераем блокировку
			player.setStuned(false);
			// убераем неузвимость
			player.setInvul(false);
			// убераем анимацию блока
			player.broadcastPacket(CancelEffect.getInstance(player, EventUtils.SLEEP_ID));
			// обновляем инфу игроку
			player.updateInfo();
		}
	}

	/**
	 * Обновление рсостояние боя.
	 */
	private void updateState()
	{
		lock();
		try
		{
			// получаем активные тимы
			EventTeam first = getFirst();
			EventTeam second = getSecond();

			// если хоть одна тима мертва или отсутствует
			if(first == null || first.isDead() || second == null || second.isDead())
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

package tera.gameserver.events.global.regionwars;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import rlib.geom.Coords;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Rnd;
import rlib.util.SafeTask;
import rlib.util.Strings;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.FuncValue;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import rlib.util.wraps.Wrap;
import rlib.util.wraps.Wraps;

import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.SayType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.listeners.DieListener;
import tera.gameserver.model.listeners.PlayerSelectListener;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.NpcType;
import tera.gameserver.model.npc.RegionWarBarrier;
import tera.gameserver.model.npc.RegionWarControl;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.LinkType;
import tera.gameserver.model.npc.interaction.links.ControlLink;
import tera.gameserver.model.npc.interaction.links.NpcLink;
import tera.gameserver.model.npc.interaction.replyes.Reply;
import tera.gameserver.model.npc.spawn.RegionWarSpawn;
import tera.gameserver.model.npc.spawn.Spawn;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.model.territory.RegionTerritory;
import tera.gameserver.network.serverpackets.CharSay;
import tera.gameserver.network.serverpackets.ServerPacket;
import tera.gameserver.tables.WorldZoneTable;
import tera.util.LocalObjects;
import tera.util.Location;

/**
 * Модель захватываемого региона.
 *
 * @author Ronn
 */
public class Region extends SafeTask implements DieListener, PlayerSelectListener
{
	private static final Logger log = Loggers.getLogger(Region.class);

	/**
	 * Обработка нажатия регистрации.
	 */
	private final Reply REPLY_REGISTER = new Reply()
	{
		@Override
		public void reply(Npc npc, Player player, Link link)
		{
			register(npc, player);
		}
	};

	/**
	 * Обработка нажатия отрегистрации.
	 */
	private final Reply REPLY_UNREGISTER = new Reply()
	{
		@Override
		public void reply(Npc npc, Player player, Link link)
		{
			unregister(npc, player);
		}
	};

	/**
	 * Обработка нажатия статуса.
	 */
	private final Reply REPLY_STATUS = new Reply()
	{
		@Override
		public void reply(Npc npc, Player player, Link link)
		{
			status(npc, player);
		}
	};

	/**
	 * Обработка нажатия статуса.
	 */
	private final Reply REPLY_CONTROL = new Reply()
	{
		@Override
		public void reply(Npc npc, Player player, Link link)
		{
			if(link.test(npc, player))
				teleportTo(player, link);
		}
	};

	/**
	 * Функция складирование оберток в таблице.
	 */
	private final FuncValue<Wrap> WRAP_FOLD_FUNC = new FuncValue<Wrap>()
	{
		@Override
		public void apply(Wrap value)
		{
			value.fold();
		}
	};

	/**
	 * Задача по выдачу очков битвы.
	 */
	private final Runnable REWARD_TASK = new SafeTask()
	{
		@Override
		protected void runImpl()
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем локальный список игроков
			Array<Player> players = local.getNextPlayerList();

			Spawn[] control = getControl();

			for(int i = 0, length = control.length; i < length; i++)
			{
				Spawn spawn = control[i];

				// полуаем отмпавненное НПС
				RegionWarControl npc = (RegionWarControl) ((RegionWarSpawn) spawn).getSpawned();

				// получаем владеющую гильдию
				Guild guild = npc.getGuildOwner();

				// если ее нет, пропускаем
				if(guild == null)
					continue;

				// получаем игроков около точки
				World.getAround(Player.class, players, npc, 400);

				// получаем их массив
				Player[] array = players.array();

				// перебираем игроков
				for(int g = 0, size = players.size(); g < size; g++)
				{
					Player player = array[g];

					// если игрок принадлежит к владеющей гильдии
					if(player.getGuild() == guild)
					{
						synchronized(player)
						{
							// выдаем очко битвы
							player.setVar(RegionWars.BATTLE_POINT, player.getVar(RegionWars.BATTLE_POINT, 0) + 1);
						}

						// сообщаем об этом
						player.sendMessage("You've got a battle point.");
					}
				}

				// очищаем список
				players.clear();
			}
		}
	};

	/** ссылка на регистрацию */
	private final Link LINK_REGISTER = new NpcLink("Registration", LinkType.DIALOG, IconType.GREEN, REPLY_REGISTER);
	/** ссылка на отрегистрацию */
	private final Link LINK_UNREGISTER = new NpcLink("Unregister", LinkType.DIALOG, IconType.GREEN, REPLY_UNREGISTER);
	/** ссылка на статус */
	private final Link LINK_STATUS = new NpcLink("Status", LinkType.DIALOG, IconType.GREEN, REPLY_STATUS);

	/** формат отображении времени */
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

	/** контейнер времени */
	private final Date date = new Date();

	/** зарегестрированные гильдии */
	private final Array<Guild> registerGuilds;

	/** контейнер ключей гильдий */
	private final Array<Guild> guildKeys;

	/** спавны убраных монстров на время осады */
	private final Array<Spawn> monsterSpawns;

	/** ссылка на владеющий ивент */
	private final RegionWars event;

	/** подчиняемая территория */
	private final RegionTerritory territory;

	/** таблица удерживаемых контролов */
	private final Table<Guild, Wrap> controlTable;

	/** владеющая гильдия */
	private Guild owner;

	/** положительные функции */
	private Func[] positive;
	/** негативные функции */
	private Func[] negative;

	/** список спавнов защиты */
	private Spawn[] defense;
	/** список спавнов барьеров */
	private Spawn[] barriers;
	/** список спавнов контроля */
	private Spawn[] control;
	/** список спавнов менеджерей */
	private Spawn[] manager;
	/** спавны магазинов */
	private Spawn[] shops;

	/** список ссылок на ТП к контрольным точкам */
	private ControlLink[] controlLinks;

	/** дата следующей битвы */
	private String nextBattleDate;

	/** текущее состояние региона */
	private volatile RegionState state;

	/** ссылка на исполняемую задачу */
	private volatile ScheduledFuture<Region> schedule;

	/** ссызка на задачу по выдачи очков битвы */
	private volatile ScheduledFuture<Runnable> rewardSchedule;

	/** точка отсчета по времени */
	private long startTime;
	/** интервал осад */
	private long interval;
	/** время следующей битвы */
	private long nextBattle;
	/** время битвы */
	private long battleTime;

	/** процент налога на магазины и изучение скилов */
	private int tax;

	public Region(RegionWars event, RegionTerritory territory)
	{
		this.state = RegionState.WAIT_WAR;
		this.event = event;
		this.territory = territory;
		this.territory.setRegion(this);
		this.registerGuilds = Arrays.toConcurrentArray(Guild.class);
		this.guildKeys = Arrays.toArray(Guild.class);
		this.controlTable = Tables.newObjectTable();
		this.monsterSpawns = Arrays.toArray(Spawn.class);
	}

	/**
	 * Выдача функций игроку.
	 *
	 * @param funcs выдаваемый набор функций.
	 * @param player выдоваемый игрок.
	 */
	private void addFuncsTo(Func[] funcs, Player player)
	{
		if(funcs.length < 1)
			return;

		for(int i = 0, length = funcs.length; i < length; i++)
			funcs[i].addFuncTo(player);
	}

	/**
	 * Применить функции на игрока.
	 *
	 * @param player вошедший игрок.
	 */
	public void addFuncsTo(Player player)
	{
		// получаем владеющую гильди.
		Guild owner = getOwner();

		// если ее нет, выходим
		if(owner == null)
			return;

		// если игрок не принадлежит владеющей
		if(player.getGuild() != owner)
			// добавляем отрицательные бонусы
			addFuncsTo(getNegative(), player);
		else
			// иначе добавляем положительные бонусы
			addFuncsTo(getPositive(), player);
	}

	/**
	 * Выдача нужных ссылок.
	 */
	public void addLinks(Array<Link> links, Npc npc, Player player)
	{
		// если НПС не является менеджером региона, выходим
		if(!Arrays.contains(getManager(), npc.getSpawn()))
			return;

		// получаем ранг игрока
		GuildRank rank = player.getGuildRank();

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если гильдия и ранг в наличии
		if(guild != null && rank != null)
		{
			// если стадия ожидания битвы и это не владеющая гильдия
			if(getState() == RegionState.PREPARE_START_WAR && guild != getOwner())
			{
				// если игрок ГМ
				if(rank.isGuildMaster())
					// даем соответствующую ссылку
					links.add(registerGuilds.contains(guild) ? LINK_UNREGISTER : LINK_REGISTER);
			}
			else if(getState() == RegionState.PREPARE_END_WAR)
			{
				// получаем ссылки на ТП к контролам
				ControlLink[] controlLinks = getControlLinks();

				// перебираем их
				for(int i = 0, length = controlLinks.length; i < length; i++)
				{
					ControlLink controlLink = controlLinks[i];

					// если ссылка подходит
					if(controlLink.test(npc, player))
						// добавляем в список
						links.add(controlLink);
				}
			}
		}

		// добавляем ссылку на получение информации о состоянии
		links.add(LINK_STATUS);
	}

	/**
	 * Добавляем зарегестрированную гильдию из БД.
	 *
	 * @param guild добавляемая гильдия.
	 */
	public void addRegisterGuild(Guild guild)
	{
		// если это владеющая гильдия, выходим
		if(guild == getOwner())
			return;

		// вносим в списое зарегестрированных на этот регион
		getRegisterGuilds().add(guild);

		// вносим в список всех зарегестрированных
		getEvent().addRegisterGuild(guild);
	}

	/**
	 * Очистка зарегестрированных гильдий.
	 */
	private void clearRegisterGuilds()
	{
		// получаем список зарегестрированных гильдий
		Array<Guild> registerGuilds = getRegisterGuilds();

		// получаем владеющий ивент
		RegionWars event = getEvent();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// удаляем зарегестрированные гильдии
		for(Guild guild : registerGuilds)
		{
			// уаляем из общих зарегестрированных
			event.removeRegisterGuild(guild);

			// удаляем запись о регистрации гильдии из БД
			dbManager.removeRegionRegisterGuild(this, guild);
		}

		// очищаем список
		registerGuilds.clear();
	}

	/**
	 * @return спавны баррьеров.
	 */
	public Spawn[] getBarriers()
	{
		return barriers;
	}

	/**
	 * @return время битвы за регион.
	 */
	public long getBattleTime()
	{
		return battleTime;
	}

	/**
	 * @return контрольные НПС.
	 */
	public Spawn[] getControl()
	{
		return control;
	}

	/**
	 * @return ссылки на ТП к захваченным контрольным точкам.
	 */
	public ControlLink[] getControlLinks()
	{
		return controlLinks;
	}

	/**
	 * @return таблица контроля точек.
	 */
	public Table<Guild, Wrap> getControlTable()
	{
		return controlTable;
	}

	/**
	 * @return спавны оборонительных НПС.
	 */
	public Spawn[] getDefense()
	{
		return defense;
	}

	/**
	 * @return ивент, владеющий регионом.
	 */
	public RegionWars getEvent()
	{
		return event;
	}

	/**
	 * @return контейнер ключей гильдий.
	 */
	public Array<Guild> getGuildKeys()
	{
		return guildKeys;
	}

	/**
	 * @return ид региона.
	 */
	public int getId()
	{
		return territory.getId();
	}

	/**
	 * @return интервал битв.
	 */
	public long getInterval()
	{
		return interval;
	}

	/**
	 * @return список НПС отвечающих за работу с регионом.
	 */
	public Spawn[] getManager()
	{
		return manager;
	}

	public Array<Spawn> getMonsterSpawns()
	{
		return monsterSpawns;
	}

	/**
	 * @return название региона.
	 */
	public String getName()
	{
		return territory.getName();
	}

	/**
	 * @return список негативных функций.
	 */
	public Func[] getNegative()
	{
		return negative;
	}

	/**
	 * @return владеющая гильдия.
	 */
	public Guild getOwner()
	{
		return owner;
	}

	/**
	 * @return ид владельца.
	 */
	public int getOwnerId()
	{
		return owner == null? 0 : owner.getId();
	}

	/**
	 * @return список позитивных функций.
	 */
	public Func[] getPositive()
	{
		return positive;
	}

	/**
	 * @return список зарегестрированных гильдий.
	 */
	public Array<Guild> getRegisterGuilds()
	{
		return registerGuilds;
	}

	/**
	 * @return спавны магазинных НПС.
	 */
	public Spawn[] getShops()
	{
		return shops;
	}

	/**
	 * @return время первых осад.
	 */
	public long getStartTime()
	{
		return startTime;
	}

	/**
	 * @return состояние региона.
	 */
	public RegionState getState()
	{
		return state;
	}

	/**
	 * @return ставка налога.
	 */
	public int getTax()
	{
		return tax;
	}

	/**
	 * @return трритория региона.
	 */
	public RegionTerritory getTerritory()
	{
		return territory;
	}

	/**
	 * @return есть ли владелец у региона.
	 */
	public boolean hasOwner()
	{
		return owner != null;
	}

	/**
	 * @return является ли гильдия участником битвы.
	 */
	public boolean isRegister(Guild guild)
	{
		return guild != null && (registerGuilds.contains(guild) || guild == owner);
	}

	@Override
	public void onDie(Character killer, Character killed)
	{
		if(getState() != RegionState.PREPARE_END_WAR)
			return;

		Class<? extends Character> cs = killed.getClass();

		// если нужный нас НПС
		if(cs == RegionWarControl.class)
		{
			// получаем НПС
			Npc npc = killed.getNpc();

			// получаем спавн контрола
			RegionWarSpawn spawn = (RegionWarSpawn) npc.getSpawn();

			// если этот контрол не относится к этому региону, выходим
			if(!Arrays.contains(getControl(), spawn))
				return;

			// получаем гильдию убийцы
			Guild guild = killer.getGuild();

			// запоминаем владеющую гильдию
			spawn.setOwner(guild);

			// формируем сообщение
			String announce = spawn.getChatLoc() + "<FONT FACE=\"$ChatFont\"> захвачена " + (guild == null? " NPC" : " гильдией \"" + guild + "\"") + "!</FONT>";

			// сообщаем об захвате
			sendAnnounce(announce);
		}
		else if(cs == RegionWarBarrier.class)
		{
			// получаем НПС
			Npc npc = killed.getNpc();

			// получаем спавн забора
			RegionWarSpawn spawn = (RegionWarSpawn) npc.getSpawn();

			// если этот забор не относится к этому региону, выходим
			if(!Arrays.contains(getBarriers(), spawn))
				return;

			// получаем гильдию убийцы
			Guild guild = killer.getGuild();

			// формируем сообщение
			String announce = spawn.getChatLoc() + "<FONT FACE=\"$ChatFont\"> разрушена " + (guild == null? " NPC" : " гильдией \"" + guild + "\"") + "!</FONT>";

			// сообщаем об разбитии забора
			sendAnnounce(announce);
		}
	}

	@Override
	public void onSelect(Player player)
	{
		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если ее нет, выходим
		if(guild == null)
			return;

		// получаем список зарегестрированных гиьдий
		Array<Guild> registerGuilds = getRegisterGuilds();

		// елси эта гильдия не учавствует в битве за регион, выходим
		if(guild != getOwner() && !registerGuilds.contains(guild))
			return;

		// получаем текущее состояние битвы
		switch(getState())
		{
			case PREPARE_START_WAR:
			{
				player.sendPacket(CharSay.getInstance(Strings.EMPTY, "Battle for \"" + getName() + "\" will start the " + nextBattleDate, SayType.NOTICE_CHAT, 0, 0), true);

				break;
			}
			case PREPARE_END_WAR:
			{
				if(schedule == null)
					return;
				// получаем оставшееся время до окончании битвы
				long endTime = schedule.getDelay(TimeUnit.MINUTES);

				player.sendPacket(CharSay.getInstance(Strings.EMPTY, "До окончания битвы за \"" + getName() + "\" осталось " + endTime + " мин.", SayType.NOTICE_CHAT, 0, 0), true);
				player.sendPacket(CharSay.getInstance(Strings.EMPTY, "Контрольные точки:", SayType.NOTICE_CHAT, 0, 0), true);

				// получаем спавны контролов
				Spawn[] control = getControl();

				for(int i = 0, length = control.length; i < length; i++)
				{
					// получаем спавн
					RegionWarSpawn spawn = (RegionWarSpawn) control[i];

					// получаем владельца
					Guild owner = spawn.getOwner();

					// отправляем позицию точки
					player.sendPacket(CharSay.getInstance(Strings.EMPTY, spawn.getChatLoc() + "<FONT FACE=\"$ChatFont\" > [" + (owner == null? "NPC" : owner.getName()) + "]</FONT>", SayType.NOTICE_CHAT, 0, 0), true);
				}

				break;
			}
			default:
				return;
		}
	}

	/**
	 * Подготовка региона к работе.
	 */
	public void prepare()
	{
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// загружаем данные региона
		dbManager.loadRegion(this);

		// загружаем зарегестрированные гильдии
		dbManager.loadRegionRegister(this);

		// спавним менеджеров региона
		Spawn[] manager = getManager();

		// спавним менеджеров
		for(Spawn spawn : manager)
			spawn.start();

		// получаем спавны контролов
		Spawn[] control = getControl();

		// перебираем спавны контролов
		for(int i = 0, length = control.length; i < length; i++)
		{
			RegionWarSpawn spawn = (RegionWarSpawn) control[i];

			// создаем ссылку
			ControlLink controlLink = new ControlLink(spawn.getName(), spawn, REPLY_CONTROL);

			// добавляем в список
			controlLinks = Arrays.addToArray(controlLinks, controlLink, ControlLink.class);
		}

		// получаем спавны магазинов
		Spawn[] shops = getShops();

		// перебираем спавны контролов
		for(int i = 0, length = shops.length; i < length; i++)
		{
			RegionWarSpawn spawn = (RegionWarSpawn) shops[i];

			// вносим регион
			spawn.setRegion(this);

			// запускаем спавн
			spawn.start();
		}

		// рассчитываем время респа охраны
		int respawn = (int) (getBattleTime() / 1000 * 2);

		// перебираем спавных охраны
		for(Spawn spawn : getDefense())
		{
			// получаем спавн НПС
			RegionWarSpawn npcSpawn = (RegionWarSpawn) spawn;

			// обновляем время респавна
			npcSpawn.setRespawnTime(respawn);

			// запоминаем регион
			npcSpawn.setRegion(this);
		}

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// устанавливаем начальное время
		nextBattle = startTime;

		// если текущая стадия ожидания битвы
		if(getState() == RegionState.WAIT_WAR)
		{
			// получаем текущее
			long currentTime = System.currentTimeMillis();

			// рассчитывааем новое
			for(int i = 0; nextBattle < currentTime; i++)
				nextBattle = startTime + interval * i;

			// конвектируем время в дату
			nextBattleDate = timeFormat(nextBattle);

			// отображаем в консоль
			log.info("next battle \"" + getName() + "\" in " + nextBattleDate + ".");

			// получаем время, через которое надо начать битву
			long diff = nextBattle - currentTime;

			// обновляем состояние
			setState(RegionState.PREPARE_START_WAR);

			// запускаем подготовку к битве через оставшееся время
			schedule = executor.scheduleGeneral(this, diff);
		}
		else if(getState() == RegionState.WAR)
		{
			// получаем текущее
			long currentTime = System.currentTimeMillis();

			// рассчитывааем новое
			for(int i = 0; nextBattle < currentTime; i++)
				nextBattle = startTime + interval * i;

			// получаем время, когда битвы началась
			nextBattle -= interval;

			// конвектируем время в дату
			nextBattleDate = timeFormat(nextBattle);

			// указываем стадию подготовки к битве
			setState(RegionState.PREPARE_START_WAR);

			// запускаем подготовку к битве
			schedule = executor.scheduleGeneral(this, 100);
		}

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// добавляемся на прослушку спавна игроков
		eventManager.addPlayerSelectListener(this);
	}

	/**
	 * Подготовка к завершению битвы.
	 */
	private void prepareEndWar()
	{
		if(rewardSchedule != null)
		{
			rewardSchedule.cancel(false);
			rewardSchedule = null;
		}

		// получаем менеджер событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// удаляемся из прослушки
		eventManager.removeDieListener(this);

		// удаляем баррьеры
		for(Spawn spawn : getBarriers())
			spawn.stop();

		// удаляем защиту
		for(Spawn spawn : getDefense())
			spawn.stop();

		// получаем спавны удаленных монстров
		Array<Spawn> monsterSpawns = getMonsterSpawns();

		// запускаем обрабтно их
		for(Spawn spawn : monsterSpawns)
			spawn.start();

		// очищаем список
		monsterSpawns.clear();

		// получаем таблицу контролируемых точек
		Table<Guild, Wrap> controlTable = getControlTable();

		// очищаем таблицу
		controlTable.clear();

		// получаем спавны контрольных точек
		Spawn[] control = getControl();

		int npc = 0;

		// запускаем спавн
		for(Spawn spawn : control)
		{
			// получаем спавн контрола
			RegionWarSpawn regionSpawn = (RegionWarSpawn) spawn;

			Guild guild = regionSpawn.getOwner();

			if(guild == null)
				npc++;
			else
			{
				// получаем обретку значения
				Wrap wrap = controlTable.get(guild);

				// если ее нет
				if(wrap == null)
				{
					// создаем новую
					wrap = Wraps.newIntegerWrap(0, true);

					// вносим в таблицу
					controlTable.put(guild, wrap);
				}

				// увеличиваем счетчик
				wrap.setInt(wrap.getInt() + 1);
			}

			spawn.stop();
		}

		// получаем контейнер ключей гильдий
		Array<Guild> guildKeys = getGuildKeys();

		// очищаем его
		guildKeys.clear();

		// вносим гильдии
		controlTable.keyArray(guildKeys);

		// ссылка на победившую гильдию
		Guild top = null;

		// кол-во захваченных точек топ гильдией
		int guild = 0;

		// перебираем гильдии
		for(Guild key : guildKeys)
		{
			// получаем кол-во контрольных точек
			Wrap wrap = controlTable.get(key);

			// получаем  кол-во
			int count = wrap.getInt();

			// если у этой гильдии захваченых контролов больше чем у других и НПС
			if(count > npc && count > guild)
			{
				// запоминаем гильдию
				top = key;

				// запоминаем их кол-во
				guild = count;
			}
		}

		// если есть гильдия претендующая на победу
		if(top != null)
		{
			// делаем проверяемый счетчик
			int check = 0;

			// перебираем кол-во захваченых точек
			for(Wrap wrap : controlTable)
				if(wrap.getInt() == guild)
					check++;

			// если есть поровну, значит убираем победителя
			if(check > 1)
			{
				// зануляем счетчик НПС
				npc = 0;

				// зануляем победившую гильдию
				top = null;
			}
		}

		// если победителя нет
		if(top == null && npc == 0)
			sendAnnounce("В битве за \"" + getName() + "\" победила ничья, владелец остается прежний.");
		// если победитель НПС
		else if(top == null && npc > 0)
		{
			// удаляем прошлего владельца
			removeOwner();

			// сообщаем о победившем
			sendAnnounce("В битве за \"" + getName() + "\" победил \"NPC\".");
		}
		// если победитель гильдия
		else if(top != null)
		{
			// получаем текущего владельца
			Guild currentOwner = getOwner();

			// удаляем прежнего владельца
			removeOwner();

			if(currentOwner == top)
				sendAnnounce("Гильдия \"" + top.getName() + "\" отстаяла регион \"" + getName() + "\".");
			else
				sendAnnounce("В битве за \"" + getName() + "\" победила гильдия \"" + top.getName() + "\".");

			// запоминаем нового владельца
			setOwner(top);
		}

		// меням состояние регионпа
		setState(RegionState.WAIT_WAR);

		// обновляем его в БД
		updateState();

		// обновляем владельца
		updateOwner();

		// возвращения игроков на места
		returnPlayers();

		// складываем обертки в таблице
		controlTable.apply(WRAP_FOLD_FUNC);

		// очищаем таблицу
		controlTable.clear();

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// ставим на исполнение
		schedule = executor.scheduleGeneral(this, 5000);

		// очищаем зарегестрированные гильдии
		clearRegisterGuilds();
	}

	/**
	 * Подготовка региона к началу битвы.
	 */
	private void prepareStartWar()
	{
		// очищаем таблицу контроля точек
		controlTable.clear();

		// получаем менеджер событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// добавляемся на прослушку
		eventManager.addDieListener(this);

		// получаем спавны контрольных точек
		Spawn[] control = getControl();

		// сообщаем о начале битвы за регион
		sendAnnounce("Началась битва за \"" + getName() + "\"!");
		sendAnnounce("Контрольные точеки:");

		// получаем текущего владельца
		Guild owner = getOwner();

		// получаем сам ивент
		RegionWars event = getEvent();

		if(owner != null && event.isRegisterGuild(owner))
			removeOwner();

		// получаем список всех регионов
		Region[] regions = event.getRegions();

		// получаем список зарегестрированных гильдий на этот регион
		Array<Guild> registerGuilds = getRegisterGuilds();

		synchronized(regions)
		{
			// перебираем регионы
			for(Region region : regions)
			{
				// пропускаем неинтересуемые
				if(region == this || !registerGuilds.contains(region.getOwner()))
					continue;

				// удаляем владельца у региона, который зарегистрировался на осаду этого
				region.removeOwner();
			}
		}

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем список НПС
		Array<Npc> npcs = local.getNextNpcList();

		// получаем спавны монстров
		Array<Spawn> monsterSpawns = getMonsterSpawns();

		// запускаем спавн
		for(Spawn spawn : control)
		{
			// получаем спавн контрола
			RegionWarSpawn regionSpawn = (RegionWarSpawn) spawn;

			// запоминаем в нем регион
			regionSpawn.setRegion(this);

			// вносим владельца
			regionSpawn.setOwner(owner);

			// получаем позицию спавна
			Location loc = spawn.getLocation();

			World.getAround(Npc.class, npcs, loc.getContinentId(), loc.getX(), loc.getY(), loc.getZ(), 0, 0, 3000);

			// перебираем НПС вокруг коньтрола
			for(Npc npc : npcs)
			{
				// РБ пропускаем
				if(npc.getNpcType() == NpcType.RAID_BOSS || npc.isMinion())
					continue;

				// получаем спавн
				Spawn target = npc.getSpawn();

				// останавливаем
				target.stop();

				// вносим в список
				monsterSpawns.add(target);
			}

			// запускаем спавн
			spawn.start();

			// отправляем сообщение
			sendAnnounce(regionSpawn.getChatLoc() + "<FONT FACE=\"$ChatFont\" > [" + (owner == null? "NPC" : owner.getName()) + "]</FONT>");
		}

		// ставим баррьеры
		for(Spawn spawn : getBarriers())
			spawn.start();

		// если у региона нет владельца
		if(getOwner() == null)
			// спавним защиту
			for(Spawn spawn : getDefense())
				spawn.start();

		// меняем состояние региона
		setState(RegionState.WAR);

		// получаем территорию
		RegionTerritory territory = getTerritory();

		synchronized(territory)
		{
			// получаем объекты территории
			Array<TObject> objects = territory.getObjects();

			// перебираем объекты
			for(TObject object : objects)
			{
				// пропускаем не игроков
				if(!object.isPlayer())
					continue;

				// удаляем его функции
				removeFuncsTo(object.getPlayer());
			}
		}

		// обновляем состояние в БД
		updateState();

		// возвращаем игроков на места респа
		returnPlayers();

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// ставим на исполнение
		schedule = executor.scheduleGeneral(this, 100);

		// запускаем задачу по выдачи очков битвы
		rewardSchedule = executor.scheduleAiAtFixedRate(REWARD_TASK, RegionWars.REWARD_INTERVAL, RegionWars.REWARD_INTERVAL);
	}

	/**
	 * Регистрация гильдии на захват региона.
	 */
	private synchronized void register(Npc npc, Player player)
	{
		// проверяем корректность НПС
		if(!Arrays.contains(getManager(), npc.getSpawn()))
			return;

		// получаем ранг игрока в гильдии
		GuildRank guildRank = player.getGuildRank();

		// если его нет, выходим
		if(guildRank == null)
		{
			player.sendMessage("You are not in a Guild.");
			return;
		}

		// если игрок не мастер гильдии, выходим
		if(!guildRank.isGuildMaster())
		{
			player.sendMessage("You are not the Guild Master.");
			return;
		}

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если ее нет, выходим
		if(guild == null)
			return;

		// если гильдия является владельцом
		if(guild == getOwner())
		{
			player.sendMessage("You're the region's owner.");
			return;
		}

		// получаем ивент
		RegionWars event = getEvent();

		// если гильдия уже на другой битве зарегестрирована
		if(event.isRegisterGuild(guild))
		{
			player.sendMessage("You are already registered to another battle.");
			return;
		}

		// получаем список зарегестрированных гильдий
		Array<Guild> registerGuilds = getRegisterGuilds();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// если не удалось внести гильдию в БД
		if(!dbManager.insertRegionGuildRegister(this, guild))
			player.sendMessage("Failed to register Guild. Please contact an administrator");
		else
		{
			// вносим в список зарегестрированных гильдий на этот регион
			registerGuilds.add(guild);

			// вносим в общий список всех зарегестрированных гильдий
			event.addRegisterGuild(guild);

			// если гильдия уже имеет во владениях регион
			if(event.isOwnerGuild(guild))
				player.sendMessage("You have successfully signed up for battle, ownership of your region will be canceled at the beginning of this battle.");
			else
				// сообщаем о успешной регистрации
				player.sendMessage("You have successfully signed up for battle.");
		}
	}

	/**
	 * Удаление функций игроку.
	 *
	 * @param funcs выдаваемый набор функций.
	 * @param player выдоваемый игрок.
	 */
	private void removeFuncsTo(Func[] funcs, Player player)
	{
		if(funcs.length < 1)
			return;

		for(int i = 0, length = funcs.length; i < length; i++)
			funcs[i].removeFuncTo(player);
	}

	/**
	 * Удалить функции у игрока.
	 *
	 * @param player вышедший игрок.
	 */
	public void removeFuncsTo(Player player)
	{
		removeFuncsTo(getNegative(), player);
		removeFuncsTo(getPositive(), player);
	}

	/**
	 * Удаление владельца региона.
	 */
	public void removeOwner()
	{
		// получаем владельца региона
		Guild owner = getOwner();

		// если его нет, выходим
		if(owner == null)
			return;

		// получаем сам ивент
		RegionWars event = getEvent();

		// удаляем владельца из владеющих гильдий
		event.removeOwnerGuild(owner);

		// на всякий случай, удаляем из зареестрированных сюда
		getRegisterGuilds().fastRemove(owner);

		// зануляем владельца
		setOwner(null);
	}

	/**
	 * Возвращения всех игроков в ближайший город.
	 */
	private void returnPlayers()
	{
		// получаем таблицу зон
		WorldZoneTable zoneTable = WorldZoneTable.getInstance();

		// получаем территорию
		RegionTerritory territory = getTerritory();

		synchronized(territory)
		{
			// получаем объекты территории
			Array<TObject> objects = territory.getObjects();

			// перебираем объекты
			for(TObject object : objects)
			{
				// пропускаем не игроков
				if(!object.isPlayer())
					continue;

				// получаем игрока
				Player player = object.getPlayer();

				// получаемт очку респа
				Location loc = zoneTable.getRespawn(player);

				// переносим
				player.teleToLocation(loc);
			}
		}
	}

	@Override
	protected synchronized void runImpl()
	{
		switch(getState())
		{
			case WAIT_WAR: startWaitWar(); break;
			case PREPARE_START_WAR: prepareStartWar(); break;
			case WAR: startWar();  break;
			case PREPARE_END_WAR: prepareEndWar();
		}
	}

	/**
	 * Отправка анонса участникам битвы.
	 *
	 * @param message отправляемое сообщение.
	 */
	public void sendAnnounce(String message)
	{
		sendPacket(CharSay.getInstance(Strings.EMPTY, message, SayType.NOTICE_CHAT, 0, 0));
	}

	/**
	 * отправка пакета участникам битвы.
	 *
	 * @param packet отправляемый пакет.
	 */
	public void sendPacket(ServerPacket packet)
	{
		// получаем владеющую гильдию
		Guild owner = getOwner();

		// увеличиваем отправку
		packet.increaseSends();

		// если владелец есть
		if(owner != null)
			// отправляем им пакет
			owner.sendPacket(null, packet);

		// получаем зарегестрированные гильдии
		Array<Guild> registerGuilds = getRegisterGuilds();

		registerGuilds.readLock();
		try
		{
			// получаем массив гильдий
			Guild[] array = registerGuilds.array();

			// отправляем им пакет
			for(int i = 0, length = registerGuilds.size(); i < length; i++)
				array[i].sendPacket(null, packet);
		}
		finally
		{
			registerGuilds.readUnlock();
		}

		// завершаем пакет
		packet.complete();
	}

	public void setBarriers(Spawn[] barriers)
	{
		this.barriers = barriers;
	}

	/**
	 * @param battleTime время битвы за регион.
	 */
	public void setBattleTime(long battleTime)
	{
		this.battleTime = battleTime;
	}

	/**
	 * @param control спавны контрольных башень.
	 */
	public void setControl(Spawn[] control)
	{
		this.control = control;
	}

	/**
	 * @param defense спавны защитных НПС.
	 */
	public void setDefense(Spawn[] defense)
	{
		this.defense = defense;
	}

	/**
	 * @param interval интервал битв.
	 */
	public void setInterval(long interval)
	{
		this.interval = interval;
	}

	/**
	 * @param manager спавны менеджеров региона.
	 */
	public void setManager(Spawn[] manager)
	{
		this.manager = manager;
	}

	/**
	 * @param negative отрицательные бонусы.
	 */
	public void setNegative(Func[] negative)
	{
		this.negative = negative;
	}

	/**
	 * @param owner владелец региона.
	 */
	public void setOwner(Guild owner)
	{
		this.owner = owner;

		// если владеющая гильдия есть
		if(owner != null)
			// вносим в общим список владеющих
			event.addOwnerGuild(owner);
	}

	/**
	 * @param positive положительные бонусы.
	 */
	public void setPositive(Func[] positive)
	{
		this.positive = positive;
	}

	/**
	 * @param shops спавны магазинов.
	 */
	public void setShops(Spawn[] shops)
	{
		this.shops = shops;
	}

	/**
	 * @param startTime точка отсчета.
	 */
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @param state статус региона.
	 */
	public void setState(RegionState state)
	{
		this.state = state;
	}

	/**
	 * @param tax налог региона.
	 */
	public void setTax(int tax)
	{
		this.tax = tax;
	}

	/**
	 * Запуск ожидания битвы.
	 */
	private void startWaitWar()
	{
		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// получаем текущее
		long currentTime = System.currentTimeMillis();

		// рассчитывааем новое
		for(int i = 0; nextBattle < currentTime; i++)
			nextBattle = startTime + interval * i;

		// конвектируем время в дату
		nextBattleDate = timeFormat(nextBattle);

		// отображаем в консоль
		log.info("next battle \"" + getName() + "\" in " + nextBattleDate + ".");

		// получаем время, через которое надо начать битву
		long diff = nextBattle - currentTime;

		// обновляем состояние
		setState(RegionState.PREPARE_START_WAR);

		// запускаем подготовку к битве через оставшееся время
		schedule = executor.scheduleGeneral(this, diff);

		// возвращаем игроков назад
		returnPlayers();
	}

	/**
	 * Запуск битвы.
	 */
	private void startWar()
	{
		// меняем состоние
		setState(RegionState.PREPARE_END_WAR);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// ставим на исполнение
		schedule = executor.scheduleGeneral(this, battleTime);
	}

	/**
	 * Получение статуса региона.
	 */
	@SuppressWarnings("incomplete-switch")
	private synchronized void status(Npc npc, Player player)
	{
		switch(getState())
		{
			case PREPARE_START_WAR:
			{
				StringBuilder builder = new StringBuilder("\nСтатус \"");

				builder.append(getName());
				builder.append("\":\n");

				builder.append("След. битва - ");
				builder.append(nextBattleDate);
				builder.append("\n");

				Guild owner = getOwner();

				builder.append("Владелец - \"").append(owner == null? "NPC" : owner.getName()).append("\"\n");

				if(registerGuilds.isEmpty())
					builder.append("Атакующие гильдии отсутствуют.");
				else
				{
					builder.append("Атакующие гильдии:\n");

					Guild[] array = registerGuilds.array();

					for(int i = 0, length = registerGuilds.size() - 1; i <= length; i++)
					{
						Guild guild = array[i];

						builder.append("\"");
						builder.append(guild);
						builder.append("\"");

						if(i < length)
							builder.append(";\n");
						else
							builder.append(".");
					}
				}

				player.sendMessage(builder.toString());

				break;
			}
			case PREPARE_END_WAR:
			{
				if(schedule == null)
					return;

				StringBuilder builder = new StringBuilder("\nСтатус \"");

				builder.append(getName());
				builder.append("\":\n");

				builder.append("До окончании битвы - ");
				builder.append(schedule.getDelay(TimeUnit.MINUTES));
				builder.append(" мин.\n");

				// получаем владеющую гильдию
				Guild owner = getOwner();

				// получаем зарегестрированные гильдии
				Array<Guild> registerGuilds = getRegisterGuilds();

				builder.append("Защитник - \"").append(owner == null? "NPC" : owner.getName()).append("\"\n");

				if(registerGuilds.isEmpty())
					builder.append("Атакующие гильдии отсутствуют.");
				else
				{
					builder.append("Атакующие гильдии:\n");

					Guild[] array = registerGuilds.array();

					for(int i = 0, length = registerGuilds.size() - 1; i <= length; i++)
					{
						Guild guild = array[i];

						builder.append("\"");
						builder.append(guild);
						builder.append("\"");

						if(i < length)
							builder.append(";\n");
						else
							builder.append(".");
					}
				}

				player.sendMessage(builder.toString());

				break;
			}
		}
	}

	/**
	 * Телепорт к нужному контролу игрока.
	 *
	 * @param player телепортируемый игрок.
	 * @param link кнопка телепортации.
	 */
	public void teleportTo(Player player, Link link)
	{
		// получаем линк на контрол
		ControlLink controlLink = (ControlLink) link;

		// получаем его спавн
		RegionWarSpawn spawn = controlLink.getSpawn();

		// получаем локацию спавна
		Location location = spawn.getLocation();

		// получаем направление
		int heading = Rnd.nextInt(0, 65000);

		// получаем новые координаты
		float newX = Coords.calcX(location.getX(), 100, heading);
		float newY = Coords.calcY(location.getY(), 100, heading);

		// получаем менеджера геодаты
		GeoManager geoManager = GeoManager.getInstance();

		// получаем высоту
		float newZ = geoManager.getHeight(location.getContinentId(), newX, newY, location.getZ());

		// телепортируем
		player.teleToLocation(location.getContinentId(), newX, newY, newZ);
	}

	/**
	 * Перевод времени в стороковую дату.
	 *
	 * @param time время.
	 * @return строковая дата.
	 */
	private String timeFormat(long time)
	{
		// применяем время
		date.setTime(time);

		// конвектируем в дату
		return timeFormat.format(date);
	}

	@Override
	public String toString()
	{
		return "Region  nextBattleDate = " + nextBattleDate + ", state = " + state + ", nextBattle = " + nextBattle;
	}

	/**
	 * Отрегистрация гильдии от захвата региона.
	 */
	private synchronized void unregister(Npc npc, Player player)
	{
		// проверяем корректность НПС
		if(!Arrays.contains(getManager(), npc.getSpawn()))
			return;

		// получаем ранг игрока в гильдии
		GuildRank guildRank = player.getGuildRank();

		// если его нет, выходим
		if(guildRank == null)
		{
			player.sendMessage("You are not in a Guild.");
			return;
		}


		// если игрок не мастер гильдии, выходим
		if(!guildRank.isGuildMaster())
		{
			player.sendMessage("You're not the Guild Master");
			return;
		}

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		// если ее нет, выходим
		if(guild == null || guild == getOwner())
			return;

		// получаем ивент
		RegionWars event = getEvent();

		// проверем вообще наличие регистрации
		if(!event.isRegisterGuild(guild))
		{
			player.sendMessage("You are already registered to another battle.");
			return;
		}

		// получаем список зарегестрированных гильдий
		Array<Guild> registerGuilds = getRegisterGuilds();

		// проверям регистрацию именно в этом регионе
		if(!registerGuilds.contains(guild))
		{
			player.sendMessage("Вы не были зарегестрированы на этой битве.");
			return;
		}

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// удаляем запись о регистрации гильдии из БД
		dbManager.removeRegionRegisterGuild(this, guild);

		// удаляем из списка всех зарегестрированных
		event.removeRegisterGuild(guild);

		// удаляем из списка зарегестрированных на этот регион
		registerGuilds.fastRemove(guild);

		// сообщаем об успехе
		player.sendMessage("Your Guild has been successfully registered with the battle.");
	}

	/**
	 * Обновление владельца в БД.
	 */
	private void updateOwner()
	{
		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// обновляемся в БД
		dbManager.updateRegionOwner(this);
	}

	/**
	 * Обновление состояния в БД.
	 */
	private void updateState()
	{
		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// обновляемся в БД
		dbManager.updateRegionState(this);
	}
}

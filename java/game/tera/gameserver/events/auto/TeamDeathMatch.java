package tera.gameserver.events.auto;

import rlib.util.Rnd;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.Config;
import tera.gameserver.config.MissingConfig;
import tera.gameserver.events.EventConstant;
import tera.gameserver.events.EventPlayer;
import tera.gameserver.events.EventState;
import tera.gameserver.events.EventType;
import tera.gameserver.events.EventUtils;
import tera.gameserver.manager.EventManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.MoveType;
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
import tera.gameserver.network.serverpackets.CharDead;
import tera.gameserver.tables.WorldZoneTable;
import tera.util.Location;

/**
 * Ивент TDM
 * 
 * @author Ronn
 * @created 11.04.2012
 */
public final class TeamDeathMatch extends AbstractAutoEvent {

	/** название ивента */
	public static final String EVENT_NAME = "TDM";

	/** ид территории, на которой ивент проводится */
	public static final int TERRITORY_ID = 54;

	/** точки для размещение первой тимы */
	private static final Location[] firstPoints = {
		new Location(10970, 7848, 974, 0, 0),
		new Location(11148, 7978, 975, 0, 0),
		new Location(11282, 8079, 979, 0, 0),
		new Location(11464, 8206, 983, 0, 0),
		new Location(11647, 8337, 977, 0, 0),
		new Location(11838, 8466, 977, 0, 0),
		new Location(12106, 8658, 974, 0, 0),
	};

	/** точки для размещение второй тимы */
	private static final Location[] secondPoints = {
		new Location(11818, 6736, 978, 0, 0),
		new Location(12000, 6858, 976, 0, 0),
		new Location(12179, 6996, 976, 0, 0),
		new Location(12390, 7156, 976, 0, 0),
		new Location(12614, 7336, 978, 0, 0),
		new Location(12925, 7576, 972, 0, 0),
	};

	/**
	 * Обработка нажатия регистрации.
	 */
	private final Reply REPLY_REGISTER = new Reply() {

		@Override
		public void reply(Npc npc, Player player, Link link) {
			registerPlayer(player);
		}
	};

	/**
	 * обработка нажатия отрегистрации.
	 */
	private final Reply REPLY_UNREGISTER = new Reply() {

		@Override
		public void reply(Npc npc, Player player, Link link) {
			unregisterPlayer(player);
		}
	};

	/** ссылка на регистрацию */
	private final Link LINK_REGISTER = new NpcLink("Рег. TeamDeathMatch", LinkType.DIALOG, IconType.GREEN, REPLY_REGISTER);
	/** ссылка отрегистрации */
	private final Link LINK_UNREGISTER = new NpcLink("Отрег. TeamDeathMatch", LinkType.DIALOG, IconType.GREEN, REPLY_UNREGISTER);

	/** состав первой команды */
	private final Array<Player> fisrtTeam;
	/** состав второй команды */
	private final Array<Player> secondTeam;

	public TeamDeathMatch() {
		this.fisrtTeam = Arrays.toArray(Player.class);
		this.secondTeam = Arrays.toArray(Player.class);
	}

	@Override
	public void addLinks(Array<Link> links, Npc npc, Player player) {

		if(!isStarted()) {
			return;
		}

		if(npc.getTemplate() != EventConstant.MYSTEL) {
			return;
		}

		if(getState() != EventState.REGISTER) {
			return;
		}

		if(player.getLevel() > getMaxLevel() || player.getLevel() < getMinLevel()) {
			return;
		}

		if(player.isDead()) {
			return;
		}

		if(player.hasDuel()) {
			return;
		}

		Array<Player> prepare = getPrepare();

		if(prepare.contains(player)) {
			links.add(LINK_UNREGISTER);
		} else {
			links.add(LINK_REGISTER);
		}
	}

	@Override
	protected void finishingState() {

		Table<IntKey, EventPlayer> players = getPlayers();

		for(EventPlayer eventPlayer : players) {

			Player player = eventPlayer.getPlayer();

			player.setFractionId(0);
			player.setResurrected(true);
			player.setEvent(false);

			eventPlayer.restoreState();

			player.updateInfo();

			eventPlayer.restoreLoc();
			eventPlayer.fold();
		}

		stop();

		EventManager eventManager = EventManager.getInstance();
		eventManager.finish(this);
	}

	@Override
	protected int getMaxLevel() {
		return MissingConfig.EVENT_TDM_MAX_LEVEL;
		//return Config.EVENT_TDM_MAX_LEVEL;
	}

	@Override
	protected int getMinLevel() {
		return MissingConfig.EVENT_TDM_MIN_LEVEL;
		//return Config.EVENT_TDM_MIN_LEVEL;
	}

	@Override
	public String getName() {
		return EVENT_NAME;
	}

	@Override
	protected int getRegisterTime() {
		return MissingConfig.EVENT_TDM_REGISTER_TIME;
		//return Config.EVENT_TDM_REGISTER_TIME;
	}

	@Override
	protected int getTerritoryId() {
		return TERRITORY_ID;
	}

	@Override
	public EventType getType() {
		return EventType.TEAM_DEATH_MATCH;
	}

	@Override
	protected boolean isCheckDieState() {
		return state == EventState.RUNNING || state == EventState.PREPARE_END;
	}

	@Override
	protected boolean isCheckTerritoryState() {
		return state == EventState.RUNNING || state == EventState.PREPARE_END;
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	protected void onDelete(Player player) {
		lock();
		try {

			switch(state) {
				case REGISTER:
				case PREPARE_START: {
					getPrepare().fastRemove(player);
					break;
				}
				case PREPARE_END:
				case RUNNING: {

					EventPlayer eventPlayer = removeEventPlayer(player.getObjectId());

					if(eventPlayer != null) {
						eventPlayer.fold();
					}

					if(player.getFractionId() == 1) {
						getFirstTeam().fastRemove(player);
					} else if(player.getFractionId() == 2) {
						getSecondTeam().fastRemove(player);
					}

					updateResult();
				}
			}

		} finally {
			unlock();
		}
	}

	@Override
	protected void onDie(final Player killed, Character killer) {
		lock();
		try {

			EventPlayer eventPlayer = getPlayers().get(killer.getObjectId());

			if(eventPlayer != null) {
				eventPlayer.incrementCounter();
			}

		} finally {
			unlock();
		}

		ExecutorManager executorManager = ExecutorManager.getInstance();
		executorManager.scheduleGeneral(new Runnable() {

			@Override
			public void run() {

				lock();
				try {

					if(getState() != EventState.PREPARE_END) {
						return;
					}

					EventPlayer eventPlayer = getPlayers().get(killed.getObjectId());

					if(eventPlayer == null) {
						return;
					}

					Location[] points = null;

					if(killed.getFractionId() == 1) {
						points = firstPoints;
					} else if(killed.getFractionId() == 2) {
						points = secondPoints;
					}

					Location location = points[Rnd.nextInt(0, points.length - 1)];

					killed.sendMessage("Вы воскресните через 5 секунд.");
					killed.setStamina(killed.getMaxStamina());
					killed.setCurrentHp(killed.getMaxHp());
					killed.setCurrentMp(killed.getMaxMp());
					killed.updateHp();
					killed.updateMp();
					killed.updateStamina();
					killed.broadcastPacket(CharDead.getInstance(killed, false));
					killed.setXYZ(location.getX(), location.getY(), location.getZ());
					killed.broadcastMove(killed.getX(), killed.getY(), killed.getZ(), killed.getHeading(), MoveType.STOP, killed.getX(), killed.getY(), killed.getZ(), true);

				} finally {
					unlock();
				}
			}

		}, 5000);
	}

	@Override
	protected void onEnter(Player player) {

		if(!player.isDead()) {
			WorldZoneTable zoneTable = WorldZoneTable.getInstance();
			player.teleToLocation(zoneTable.getDefaultRespawn(player));
		}
	}

	@Override
	protected void onExit(Player player) {

		if(!player.isDead()) {
			player.setCurrentHp(0);
			player.doDie(player);
			player.sendMessage("You are out of the event-zone.");
		}
	}

	@Override
	protected void prepareEndState() {

		World.sendAnnounce("Бой окончен.");

		Spawn[] guards = EventUtils.guards;

		for(int i = 0, length = guards.length; i < length; i++) {
			guards[i].stop();
		}

		Table<IntKey, EventPlayer> players = getPlayers();
		Array<Player> firstTeam = getFirstTeam();
		Array<Player> secondTeam = getSecondTeam();

		int firstCount = 0;
		int secondCount = 0;

		for(Player player : firstTeam.array()) {

			if(player == null) {
				break;
			}

			EventPlayer eventPlayer = players.get(player.getObjectId());

			if(eventPlayer == null) {
				continue;
			}

			firstCount += eventPlayer.getCounter();
		}

		for(Player player : secondTeam.array()) {

			if(player == null) {
				break;
			}

			EventPlayer eventPlayer = players.get(player.getObjectId());

			if(eventPlayer == null) {
				continue;
			}

			secondCount += eventPlayer.getCounter();
		}

		int winner = -1;

		if(secondCount > firstCount) {
			World.sendAnnounce("Победила вторая команда!");
			winner = 2;
		} else if(firstCount > secondCount) {
			World.sendAnnounce("Победила первая команда!");
			winner = 1;
		} else {
			World.sendAnnounce("Победивших нет...");
		}

		if(winner > 0) {

			for(EventPlayer eventPlayer : players) {

				Player player = eventPlayer.getPlayer();

				if(player.getFractionId() != winner) {
					continue;
				}

				int reward = (int) Math.max(Math.sqrt(players.size()) * Math.sqrt(player.getLevel()), 1);

				synchronized(player) {
					player.setVar(EventConstant.VAR_NANE_HERO_POINT, player.getVar(EventConstant.VAR_NANE_HERO_POINT, 0) + reward);
				}

				player.sendMessage("Вы получили " + reward + " очка(ов) славы.");
			}
		}

		setState(EventState.FINISHING);

		ExecutorManager executor = ExecutorManager.getInstance();
		executor.scheduleGeneral(this, 5000);
	}

	@Override
	protected void prepareStartState() {

		Table<IntKey, EventPlayer> players = getPlayers();

		Array<Player> prepare = getPrepare();
		Array<Player> firstTeam = getFirstTeam();
		Array<Player> secondTeam = getSecondTeam();

		Player[] array = prepare.array();

		for(int i = 0, length = prepare.size(); i < length; i++) {

			/*if(i >= Config.EVENT_TDM_MAX_PLAYERS) {
				break;
			}*/
			if(i >= MissingConfig.EVENT_TDM_MAX_PLAYERS){
				break;
			}

			Player player = array[i];

			if(player.isDead()) {
				player.sendMessage("You is dead.");
				continue;
			}

			if(player.hasDuel()) {
				player.sendMessage(MessageType.YOU_ARE_IN_A_DUEL_NOW);
				continue;
			}

			if(players.containsKey(player.getObjectId())) {
				continue;
			}

			player.setResurrected(false);

			EventPlayer eventPlayer = EventPlayer.newInstance(player);

			players.put(player.getObjectId(), eventPlayer);

			if(i % 2 == 0) {
				player.setFractionId(1);
				firstTeam.add(player);
			} else {
				player.setFractionId(2);
				secondTeam.add(player);
			}

			eventPlayer.saveLoc();
			eventPlayer.saveState();
		}

		prepare.clear();

		if(players.size() < MissingConfig.EVENT_TDM_MIN_PLAYERS) {

			World.sendAnnounce("Недостаточное кол-во участников.");

			setState(EventState.FINISHING);

			if(schedule != null) {
				schedule.cancel(true);
			}

			ExecutorManager executor = ExecutorManager.getInstance();
			executor.execute(this);
			return;
		}

		World.sendAnnounce("Бой начнется через 1 минуту.");

		for(Player player : firstTeam.array()) {

			if(player == null) {
				break;
			}

			player.teleToLocation(firstPoints[Rnd.nextInt(0, firstPoints.length - 1)]);

			lockMove(player);

			player.setStuned(true);
			player.broadcastPacket(AppledEffect.getInstance(player, player, 701100, 60000));
			player.updateInfo();
		}

		for(Player player : secondTeam.array()) {

			if(player == null) {
				break;
			}

			player.teleToLocation(secondPoints[Rnd.nextInt(0, secondPoints.length - 1)]);

			lockMove(player);

			player.setStuned(true);
			player.broadcastPacket(AppledEffect.getInstance(player, player, 701100, 60000));
			player.updateInfo();
		}

		clearTerritory();
		setState(EventState.RUNNING);
	}

	@Override
	protected void runningState() {

		Spawn[] guards = EventUtils.guards;

		for(int i = 0, length = guards.length; i < length; i++) {
			guards[i].start();
		}

		Array<Player> firstTeam = getFirstTeam();
		Array<Player> secondTeam = getSecondTeam();

		for(Player player : firstTeam.array()) {

			if(player == null) {
				continue;
			}

			unlockMove(player);

			player.setStamina(player.getMaxStamina());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.setStuned(false);
			player.broadcastPacket(CancelEffect.getInstance(player, 701100));
			player.updateInfo();
		}

		for(Player player : secondTeam.array()) {

			if(player == null) {
				continue;
			}

			unlockMove(player);

			player.setStamina(player.getMaxStamina());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());

			player.setStuned(false);
			player.broadcastPacket(CancelEffect.getInstance(player, 701100));
			player.updateInfo();
		}

		World.sendAnnounce("В БОЙ!!!");

		setState(EventState.PREPARE_END);

		if(schedule != null) {
			schedule.cancel(false);
		}

		ExecutorManager executor = ExecutorManager.getInstance();
		//schedule = executor.scheduleGeneral(this, Config.EVENT_TDM_BATTLE_TIME * 60 * 1000);
		schedule = executor.scheduleGeneral(this, MissingConfig.EVENT_TDM_BATTLE_TIME * 60 * 1000);

		updateResult();
	}

	@Override
	public synchronized boolean stop() {

		if(super.stop()) {
			fisrtTeam.clear();
			secondTeam.clear();
			return true;
		}

		return false;
	}

	/**
	 * @param Player исключенный игрок.
	 */
	private void updateResult() {
		lock();
		try {

			Array<TObject> objects = getEventTerritory().getObjects();
			Array<Player> firstTeam = getFirstTeam();
			Array<Player> secondTeam = getSecondTeam();

			for(Player player : firstTeam.array()) {

				if(player == null) {
					continue;
				}

				if(!objects.contains(player)) {

					EventPlayer eventPlayer = removeEventPlayer(player.getObjectId());

					if(eventPlayer != null) {
						eventPlayer.fold();
					}

					firstTeam.fastRemove(player);
				}
			}

			for(Player player : secondTeam.array()) {

				if(player == null) {
					continue;
				}

				if(!objects.contains(player)) {

					EventPlayer eventPlayer = removeEventPlayer(player.getObjectId());

					if(eventPlayer != null) {
						eventPlayer.fold();
					}

					secondTeam.fastRemove(player);
				}
			}

		} finally {
			unlock();
		}
	}

	/**
	 * @return состав первой команды.
	 */
	public Array<Player> getFirstTeam() {
		return fisrtTeam;
	}

	/**
	 * @return состав второй команды.
	 */
	public Array<Player> getSecondTeam() {
		return secondTeam;
	}

	@Override
	protected boolean isNeedShowPlayerCount() {
		return false;
	};
}

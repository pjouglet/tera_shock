package tera.gameserver.events.auto;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.util.SafeTask;
import rlib.util.Synchronized;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.gameserver.Messages.CustomMessage;
import tera.gameserver.events.Event;
import tera.gameserver.events.EventPlayer;
import tera.gameserver.events.EventState;
import tera.gameserver.events.NpcInteractEvent;
import tera.gameserver.events.Registered;
import tera.gameserver.manager.EventManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.listeners.DeleteListener;
import tera.gameserver.model.listeners.DieListener;
import tera.gameserver.model.listeners.TerritoryListener;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.model.skillengine.funcs.stat.MathFunc;
import tera.gameserver.model.territory.Territory;
import tera.gameserver.tables.TerritoryTable;
import tera.gameserver.tables.WorldZoneTable;

/**
 * Базовая модель авто ивента.
 * 
 * @author Ronn
 */
public abstract class AbstractAutoEvent extends SafeTask implements Event, Synchronized, NpcInteractEvent, Registered, TerritoryListener, DieListener, DeleteListener {

	/** блокировщик движения игроков */
	private static final Func RUN_LOCKER = new MathFunc(StatType.RUN_SPEED, 0x50, null, null) {

		@Override
		public float calc(Character attacker, Character attacked, Skill skill, float val) {
			return 0;
		}
	};

	/** синхронизатор */
	private final Lock lock;

	/** таблица участников */
	private final Table<IntKey, EventPlayer> players;

	/** ожидающие участия игроки */
	private final Array<Player> prepare;
	/** активные участники */
	private final Array<Player> activePlayers;

	/** территория ивента */
	private final Territory eventTerritory;

	/** ссылка на таск ивента */
	protected ScheduledFuture<? extends AbstractAutoEvent> schedule;

	/** статус ивента */
	protected EventState state;

	/** счетчик времени */
	protected int time;

	/** запущен ли ивент */
	protected boolean started;

	protected AbstractAutoEvent() {
		this.lock = Locks.newLock();
		this.prepare = Arrays.toArray(Player.class);
		this.activePlayers = Arrays.toArray(Player.class);

		// получаем таблицу территорий
		TerritoryTable territoryTable = TerritoryTable.getInstance();

		this.eventTerritory = territoryTable.getTerritory(getTerritoryId());
		this.players = Tables.newIntegerTable();
	}

	/**
	 * добавление игрока в активные.
	 */
	public final void addActivePlayer(Player player) {
		activePlayers.add(player);
	}

	@Override
	public void addLinks(Array<Link> links, Npc npc, Player player) {
	}

	/**
	 * Очистить территорию от левых игроков.
	 */
	protected final void clearTerritory() {

		if(eventTerritory == null) {
			return;
		}

		WorldZoneTable worldZoneTable = WorldZoneTable.getInstance();
		Array<TObject> objects = eventTerritory.getObjects();

		TObject[] objs = objects.array();

		objects.writeLock();
		try {

			for(int i = 0, length = objects.size(); i < length; i++) {

				TObject object = objs[i];

				if(!object.isPlayer()) {
					continue;
				}

				Player player = object.getPlayer();

				if(!players.containsKey(player.getObjectId())) {
					player.teleToLocation(worldZoneTable.getRespawn(player));
					i--;
					length--;
				}
			}

		} finally {
			objects.writeUnlock();
		}
	}

	protected void finishedState() {
	}

	protected void finishingState() {
	}

	/**
	 * @return список аткивных игроков.
	 */
	public Array<Player> getActivePlayers() {
		return activePlayers;
	}

	/**
	 * @return территория ивента.
	 */
	public final Territory getEventTerritory() {
		return eventTerritory;
	}

	/**
	 * @return максимальный уровень для участия.
	 */
	protected int getMaxLevel() {
		return 0;
	}

	/**
	 * @return минимальный уровень для участия.
	 */
	protected int getMinLevel() {
		return 0;
	}

	/**
	 * @return таблица участников.
	 */
	public final Table<IntKey, EventPlayer> getPlayers() {
		return players;
	}

	/**
	 * @return список зарегестрированных.
	 */
	public final Array<Player> getPrepare() {
		return prepare;
	}

	/**
	 * @return время на регистрацию игроков.
	 */
	protected int getRegisterTime() {
		return 0;
	}

	/**
	 * @return стадия ивента.
	 */
	protected final EventState getState() {
		return state;
	}

	/**
	 * @return ид ивент территории.
	 */
	protected int getTerritoryId() {
		return 0;
	}

	@Override
	public boolean isAuto() {
		return true;
	}

	/**
	 * @return нужно ли при текущей стадии смотреть на убийства.
	 */
	protected boolean isCheckDieState() {
		return false;
	}

	/**
	 * @return нужно ли при текущей стадии ивента проверять зону.
	 */
	protected boolean isCheckTerritoryState() {
		return false;
	}

	/**
	 * @return запущен ли ивент.
	 */
	public final boolean isStarted() {
		return started;
	}

	@Override
	public final void lock() {
		lock.lock();
	}

	/**
	 * Блокер движения.
	 */
	protected void lockMove(Player player) {
		RUN_LOCKER.addFuncTo(player);
	}

	/**
	 * обработка удаление из мира указанного игрока.
	 * 
	 * @param player удаляемый игрок.
	 */
	protected void onDelete(Player player) {
	}

	@Override
	public void onDelete(TObject object) {

		if(!object.isPlayer()) {
			return;
		}

		Player player = object.getPlayer();

		if(!player.isEvent()) {
			return;
		}

		onDelete(player);
	}

	@Override
	public void onDie(Character killer, Character killed) {

		if(!isCheckDieState() || !killed.isPlayer()) {
			return;
		}

		Player player = killed.getPlayer();

		if(!player.isEvent() || !players.containsKey(killed.getObjectId())) {
			return;
		}

		onDie(player, killer);
	}

	/**
	 * Обработка убийства игрока.
	 * 
	 * @param killed убитый игрок.
	 * @param killer убийка игрока.
	 */
	protected void onDie(Player killed, Character killer) {
	}

	/**
	 * Обработка входа левого игрока в ивент зону.
	 * 
	 * @param player вошедший игрок.
	 */
	protected void onEnter(Player player) {
	}

	@Override
	public void onEnter(Territory territory, TObject object) {

		if(territory != eventTerritory || !isCheckTerritoryState() || !object.isPlayer() || players.containsKey(object.getObjectId())) {
			return;
		}

		onEnter(object.getPlayer());
	}

	/**
	 * Обработка выхода левого игрока из ивент зоны.
	 * 
	 * @param player вышедший игрок.
	 */
	protected void onExit(Player player) {
	}

	@Override
	public void onExit(Territory territory, TObject object) {

		if(territory != eventTerritory || !isCheckTerritoryState() || !object.isPlayer() || !players.containsKey(object.getObjectId())) {
			return;
		}

		onExit(object.getPlayer());
	}

	@Override
	public boolean onLoad() {
		return true;
	}

	@Override
	public boolean onReload() {
		return true;
	}

	@Override
	public boolean onSave() {
		return true;
	}

	protected void prepareBattleState() {
	}

	protected void prepareEndState() {
	}

	protected void prepareStartState() {
	}

	@Override
	public boolean registerPlayer(Player player) {
		lock();
		try {

			if(!isStarted()) {
				player.sendMessage(CustomMessage.EVENT_NO_RUNNING);
				return false;
			}

			if(getState() != EventState.REGISTER) {
				player.sendMessage(CustomMessage.EVENT_REGISTRATION_LEFT);
				return false;
			}

			if(player.getLevel() > getMaxLevel() || player.getLevel() < getMinLevel()) {
				player.sendMessage(CustomMessage.EVENT_NO_LVL_REQUIRED);
				return false;
			}

			Array<Player> prepare = getPrepare();

			if(prepare.contains(player)) {
				player.sendMessage(CustomMessage.EVENT_ALREADY_REGISTERED);
				return false;
			}

			if(player.isDead()) {
				player.sendMessage(CustomMessage.EVENT_PLAYER_DEAD);
				return false;
			}

			if(player.hasDuel()) {
				player.sendMessage(CustomMessage.EVENT_PLAYER_IN_DUEL);
				return false;
			}

			prepare.add(player);

			player.setEvent(true);
			player.sendMessage(CustomMessage.EVENT_REGISTRATION_OK);
			return true;

		} finally {
			unlock();
		}
	}

	protected boolean isNeedShowPlayerCount() {
		return true;
	}

	/**
	 * Отправка анонса о регистрации.
	 */
	protected void registerState() {

		World.sendAnnounce(CustomMessage.EVENT_TIME_BEFORE_EVENT + time + CustomMessage.MINUTES);
		World.sendAnnounce("Prior to the start of Event left " + time + " Minute(s)");

		if(isNeedShowPlayerCount()) {
			World.sendAnnounce("They are " + prepare.size() + CustomMessage.REGISTERED_PARTICIPANTS);
		}

		World.sendAnnounce(CustomMessage.EVENT_HOW_TO_REGISTER + getName());

		time--;

		if(time == 0) {
			setState(EventState.PREPARE_START);
		}
	}

	/**
	 * Удаление игрока из активных.
	 */
	public final void removeActivePlayer(Object player) {
		activePlayers.fastRemove(player);
	}

	/**
	 * удаление из таблицы участников игрока.
	 * 
	 * @param objectId ид игрока.
	 * @return удаляемый игрок.
	 */
	public final EventPlayer removeEventPlayer(int objectId) {
		return players.remove(objectId);
	}

	@Override
	protected void runImpl() {
		lock();
		try {
			switch(getState()) {
				case REGISTER: {
					registerState();
					break;
				}
				case PREPARE_START: {
					prepareStartState();
					break;
				}
				case PREPARE_BATLE: {
					prepareBattleState();
					break;
				}
				case RUNNING: {
					runningState();
					break;
				}
				case PREPARE_END: {
					prepareEndState();
					break;
				}
				case FINISHING: {
					finishingState();
					break;
				}
				case FINISHED: {
					finishedState();
				}
			}
		} finally {
			unlock();
		}
	}

	protected void runningState() {
	}

	/**
	 * @param запущен ли ивент.
	 */
	public final void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * @param state стадия ивента.
	 */
	protected final void setState(EventState state) {
		this.state = state;
	}

	@Override
	public boolean start() {
		lock();
		try {

			if(isStarted()) {
				return false;
			}

			if(eventTerritory != null) {
				eventTerritory.addListener(this);
			}

			ObjectEventManager objectEventManager = ObjectEventManager.getInstance();
			objectEventManager.addDeleteListener(this);
			objectEventManager.addDieListener(this);

			time = getRegisterTime();

			EventManager eventManager = EventManager.getInstance();
			eventManager.start(this);

			World.sendAnnounce(CustomMessage.EVENT_START_MESSAGE + "\"" + getName() + "\"");

			setStarted(true);
			setState(EventState.REGISTER);

			ExecutorManager executor = ExecutorManager.getInstance();
			schedule = executor.scheduleGeneralAtFixedRate(this, 60000, 60000);
			return true;

		} finally {
			unlock();
		}
	}

	@Override
	public boolean stop() {
		lock();
		try {

			if(!isStarted()) {
				return false;
			}

			players.clear();
			activePlayers.clear();

			if(eventTerritory != null) {
				eventTerritory.removeListener(this);
			}

			ObjectEventManager objectEventManager = ObjectEventManager.getInstance();
			objectEventManager.removeDeleteListener(this);
			objectEventManager.removeDieListener(this);

			World.sendAnnounce(CustomMessage.EVENT_END_MESSAGE +  "\"" + getName() + "\"");

			setStarted(false);
			setState(EventState.FINISHED);
			return true;

		} finally {
			unlock();
		}
	}

	@Override
	public final void unlock() {
		lock.unlock();
	}

	/**
	 * Разблокер движения.
	 */
	protected void unlockMove(Player player) {
		RUN_LOCKER.removeFuncTo(player);
	}

	@Override
	public boolean unregisterPlayer(Player player) {
		lock();
		try {

			if(!isStarted()) {
				player.sendMessage(CustomMessage.EVENT_NO_RUNNING);
				return false;
			}

			if(getState() != EventState.REGISTER) {
				player.sendMessage(CustomMessage.EVENT_CAN_REGISTER);
				return false;
			}

			Array<Player> prepare = getPrepare();

			if(!prepare.contains(player)) {
				player.sendMessage(CustomMessage.EVENT_NO_REGISTER);
				return false;
			}

			prepare.fastRemove(player);

			player.setEvent(false);
			player.sendMessage(CustomMessage.EVENT_USER_REGISTERED);
			return false;

		} finally {
			unlock();
		}
	}
}

package tera.gameserver.model.playable;

import rlib.geom.Angles;
import rlib.geom.Coords;
import rlib.idfactory.IdGenerator;
import rlib.idfactory.IdGenerators;
import rlib.util.Nameable;
import rlib.util.Rnd;
import rlib.util.SafeTask;
import rlib.util.Strings;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.FuncKeyValue;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import rlib.util.wraps.Wrap;
import rlib.util.wraps.WrapType;
import rlib.util.wraps.Wraps;
import tera.Config;
import tera.gameserver.config.MissingConfig;
import tera.gameserver.events.global.regionwars.Region;
import tera.gameserver.events.global.regionwars.RegionState;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.manager.GuildManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.*;
import tera.gameserver.model.Character;
import tera.gameserver.model.actions.Action;
import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.ai.PlayerAI;
import tera.gameserver.model.base.Experience;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.base.PlayerGeomTable;
import tera.gameserver.model.base.Race;
import tera.gameserver.model.base.Sex;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.Slot;
import tera.gameserver.model.geom.Geom;
import tera.gameserver.model.geom.PlayerGeom;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.CrystalList;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.ItemLocation;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestPanelState;
import tera.gameserver.model.quests.QuestState;
import tera.gameserver.model.regenerations.PlayerNegativeRegenMp;
import tera.gameserver.model.regenerations.PlayerPositiveRegenMp;
import tera.gameserver.model.regenerations.PlayerRegenHp;
import tera.gameserver.model.regenerations.Regen;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.EffectType;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.territory.RegionTerritory;
import tera.gameserver.model.territory.Territory;
import tera.gameserver.network.model.UserClient;
import tera.gameserver.network.serverpackets.AddExp;
import tera.gameserver.network.serverpackets.AppledCharmEffect;
import tera.gameserver.network.serverpackets.AppledEffect;
import tera.gameserver.network.serverpackets.CancelTargetHp;
import tera.gameserver.network.serverpackets.CharDead;
import tera.gameserver.network.serverpackets.CharSay;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.gameserver.network.serverpackets.GuildInfo;
import tera.gameserver.network.serverpackets.GuildLogs;
import tera.gameserver.network.serverpackets.GuildMembers;
import tera.gameserver.network.serverpackets.IncreaseLevel;
import tera.gameserver.network.serverpackets.ItemReuse;
import tera.gameserver.network.serverpackets.MountOff;
import tera.gameserver.network.serverpackets.NameColor;
import tera.gameserver.network.serverpackets.PlayerCurrentHp;
import tera.gameserver.network.serverpackets.PlayerCurrentMp;
import tera.gameserver.network.serverpackets.PlayerDeadWindow;
import tera.gameserver.network.serverpackets.PlayerInfo;
import tera.gameserver.network.serverpackets.PlayerMove;
import tera.gameserver.network.serverpackets.PlayerPvPOff;
import tera.gameserver.network.serverpackets.PlayerPvPOn;
import tera.gameserver.network.serverpackets.QuestMoveToPanel;
import tera.gameserver.network.serverpackets.ServerPacket;
import tera.gameserver.network.serverpackets.SkillListInfo;
import tera.gameserver.network.serverpackets.SkillReuse;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.gameserver.network.serverpackets.TargetHp;
import tera.gameserver.network.serverpackets.Tp1;
import tera.gameserver.network.serverpackets.UserInfo;
import tera.gameserver.network.serverpackets.WorldZone;
import tera.gameserver.tables.TerritoryTable;
import tera.gameserver.taskmanager.RegenTaskManager;
import tera.gameserver.tasks.BattleStanceTask;
import tera.gameserver.tasks.EmotionTask;
import tera.gameserver.tasks.ResourseCollectTask;
import tera.gameserver.templates.PlayerTemplate;
import tera.gameserver.templates.SkillTemplate;
import tera.util.ExtUtils;
import tera.util.Identified;
import tera.util.LocalObjects;

/**
 * Модель игрока в Tera-Online.
 * 
 * @author Ronn
 */
public final class Player extends Playable implements Nameable, Identified {

	/** фабрика ид кастов для скилов */
	private static final IdGenerator ID_FACTORY = IdGenerators.newSimpleIdGenerator(0, 300000);

	/** максимальное число квестов на квест трекере */
	private static final int MAXIMUM_QUEST_IN_PANEL = 7;

	/** набор лок он таргетов */
	private final Array<Character> lockOnTargets;
	/** список квестов на панели */
	private final Array<QuestState> questInPanel;

	/** таблица территорий, в которых был игрок */
	private final Table<IntKey, Territory> storedTerrs;
	/** таблица переменных игрока */
	private final Table<String, Wrap> variables;

	/** обработчик сбора ресурсов */
	private final ResourseCollectTask collectTask;
	/** обработчик боевой стойки игрока */
	private final BattleStanceTask battleStanceTask;

	/** функция по сохранению переменных игрока */
	private final FuncKeyValue<String, Wrap> saveVarFunc;

	/** клиент */
	private volatile UserClient client;
	/** аккаунт игрока */
	private volatile Account account;
	/** пати, в которой состоит игрок */
	private volatile Party party;
	/** клан, в котором состоит игрок */
	private volatile Guild guild;
	/** ранг в клане */
	private volatile GuildRank guildRank;
	/** маршрут полета */
	private volatile Route route;
	/** последний нпс с которым говорили */
	private volatile Npc lastNpc;
	/** последний диалог с нпс */
	private volatile Dialog lastDialog;
	/** последний экшен */
	private volatile Action lastAction;
	/** последний диалог акшена */
	private volatile ActionDialog lastActionDialog;
	/** скил, которым сели на маунта */
	private volatile Skill mountSkill;
	/** дуэль игрока */
	private volatile Duel duel;
	/** активный костер */
	private volatile Bonfire bonfire;
	/** последний нажатый линк */
	private volatile Link lastLink;
	/** список друзей */
	private volatile FriendList friendList;
	/** список квестов */
	private volatile QuestList questList;

	/** заметка для гильдии */
	private String guildNote;
	/** внешность игрока */
	private PlayerAppearance appearance;
	/** последний список линков */
	private final Array<Link> lastLinks;

	/** настройки клиента игрока */
	private byte[] settings;
	/** настройки горячих клавиш игрока */
	private byte[] hotkey;

	/** время создания игрока */
	private long createTime;
	/** время онлаина игрока */
	private long onlineTime;
	/** время входа в игру */
	private long onlineBeginTime;
	/** конец бана игрока */
	private long endBan;
	/** конец бана чата */
	private long endChatBan;
	/** время последней блокировки удара */
	private long lastBlock;

	/** ид фракции */
	private int fraction;
	/** уровень доступа */
	private int accessLevel;
	/** уровень усталости */
	private int stamina;
	/** счетчики убийтсв игроков */
	private int pvpCount;
	/** счетчик убийств нпс */
	private int pveCount;
	/** счетчик нанесеных ударов */
	private int attackCounter;
	/** карма игрока */
	private int karma;
	/** ид активного маунта */
	private int mountId;
	/** уровень сбора кристалов */
	private int energyLevel;
	/** уровень сбора камней */
	private int miningLevel;
	/** уровень сбора растений */
	private int plantLevel;

	/** приконекчен ли */
	private boolean connected;
	/** находится ли игрок на ивенте */
	private boolean event;
	/** в течении каста скила был ли уже нанесен урон кому-то */
	private boolean attacking;
	/** активирован ли у игрока пвп режим */
	private boolean pvpMode;
	/** были ли изменения в настройках */
	private boolean changedSettings;
	/** были ли изменения в раскладке */
	private boolean changeHotkey;
	/** была ли изменена внешность */
	private boolean changedFace;
	/** можно ли воскресится самому */
	private boolean resurrected;

	/**
	 * @param objectId уникальный ид игрока.
	 * @param template темплейт игрока.
	 * @param accountName имя аккаунта игрока.
	 */
	public Player(int objectId, PlayerTemplate template) {
		super(objectId, template);

		this.saveVarFunc = new FuncKeyValue<String, Wrap>() {

			@Override
			public void apply(String key, Wrap value) {
				DataBaseManager dbManager = DataBaseManager.getInstance();
				dbManager.updatePlayerVar(getObjectId(), key, value.toString());
			}
		};

		this.battleStanceTask = new BattleStanceTask(this);

		this.lastLinks = Arrays.toConcurrentArray(Link.class);
		this.lockOnTargets = Arrays.toConcurrentArray(Character.class);
		this.questInPanel = Arrays.toConcurrentArray(QuestState.class);

		this.collectTask = new ResourseCollectTask(this);

		this.storedTerrs = Tables.newConcurrentIntegerTable();
		this.variables = Tables.newConcurrentObjectTable();

		Formulas formulas = Formulas.getInstance();
		formulas.addFuncsToNewPlayer(this);

		this.resurrected = true;
	}

	@Override
	public void abortCollect() {
		collectTask.cancel(true);
	}

	@Override
	public void addAttackCounter() {
		attackCounter++;

		if(attackCounter >= Config.WORLD_PLAYER_THRESHOLD_ATTACKS) {
			subHeart();
			attackCounter = 0;
		}
	}

	/**
	 * @return раса игрока.
	 */
	public Race getRace() {
		return getTemplate().getRace();
	}

	/**
	 * Можно ли добавляться на обработку к указанному костру.
	 * 
	 * @param newBonfire новый костер.
	 * @return можно ли добавлятся.
	 */
	public boolean addBonfire(Bonfire newBonfire) {

		if(bonfire == null) {
			synchronized(this) {
				if(bonfire == null) {
					bonfire = newBonfire;
					sendMessage(MessageType.YOU_ARE_RECHARGING_STAMINE);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void addDefenseCounter() {

		attackCounter += 3;

		lastBlock = System.currentTimeMillis();

		if(attackCounter >= Config.WORLD_PLAYER_THRESHOLD_BLOOKS) {
			subHeart();
			attackCounter = 0;
		}
	}

	@Override
	public void addExp(int added, TObject object, String creator) {

		if(added < 1)
			return;

		GameLogManager gameLogger = GameLogManager.getInstance();
		gameLogger.writeExpLog(getName() + " added " + added + " exp " + (object == null ? " by " + creator : " by object [" + creator + "]"));

		exp += added;

		int next = Experience.getNextExperience(level);

		if(exp > next) {
			synchronized(this) {
				while(exp > next) {
					exp -= next;
					increaseLevel();
					next = Experience.LEVEL[level + 1];
				}
			}
		}

		sendPacket(AddExp.getInstance(exp, added, next, object != null ? object.getObjectId() : 0, object != null ? object.getSubId() : 0), true);
	}

	/**
	 * Добавить линк в список последнего списка линков.
	 * 
	 * @param link добавляемый линк.
	 */
	public void addLink(Link link) {
		lastLinks.add(link);
	}

	@Override
	public boolean addLockOnTarget(Character target, Skill skill) {

		if(skill.getMaxTargets() <= lockOnTargets.size() || !skill.getTargetType().check(this, target) || lockOnTargets.contains(target) || !target.isInRange(this, skill.getRange())) {
			return false;
		}

		lockOnTargets.add(target);

		PacketManager.showLockTarget(this, target, skill);
		return true;
	}

	@Override
	public void addMe(Player player) {

		try {
			player.sendPacket(PlayerInfo.getInstance(this, player), true);

			super.addMe(player);

			if(isPvPMode() && !isDead())
				player.sendPacket(TargetHp.getInstance(this, TargetHp.RED), true);

		} catch(NullPointerException e) {
			log.warning(this, e);
		}
	}

	@Override
	public void addPvECount() {
		pveCount += 1;
	}

	@Override
	public void addPvPCount() {
		pvpCount += 1;
	}

	@Override
	public boolean addSkill(Skill skill, boolean sendPacket) {

		if(super.addSkill(skill, sendPacket)) {

			if(sendPacket) {
				sendPacket(SkillListInfo.getInstance(this), true);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean addSkill(SkillTemplate template, boolean sendPacket) {

		if(super.addSkill(template, sendPacket)) {

			if(sendPacket) {
				sendPacket(SkillListInfo.getInstance(this), true);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean addSkills(Skill[] skills, boolean sendPacket) {

		if(super.addSkills(skills, sendPacket)) {

			if(sendPacket) {
				sendPacket(SkillListInfo.getInstance(this), true);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean addSkills(SkillTemplate[] templates, boolean sendPacket) {

		if(super.addSkills(templates, sendPacket)) {

			if(sendPacket) {
				sendPacket(SkillListInfo.getInstance(this), true);
			}

			return true;
		}

		return false;
	}

	/**
	 * Обработка добавления уровня усталости.
	 */
	public void addStamina() {
		setStamina(stamina + 1);
	}

	@Override
	public void addVisibleObject(TObject object) {

		if(object == null || object.getObjectId() == objectId || !object.isVisible()) {
			return;
		}

		object.addMe(this);
	}

	@Override
	public void broadcastMove(float x, float y, float z, int heading, MoveType type, float targetX, float targetY, float targetZ, boolean selfPacket) {

		ServerPacket packet = getMovePacket(x, y, z, heading, type, targetX, targetY, targetZ);

		if(selfPacket) {
			broadcastPacket(packet);
		} else {
			broadcastPacketToOthers(packet);
		}
	}

	@Override
	public final void broadcastPacket(ServerPacket packet) {

		UserClient client = getClient();

		if(client == null) {
			return;
		}

		packet.increaseSends();
		broadcastPacketToOthers(packet);
		client.sendPacket(packet);
	}

	@Override
	public void causingDamage(Skill skill, AttackInfo info, Character attacker) {

		Duel duel = getDuel();

		if(duel != null) {
			synchronized(this) {

				duel = getDuel();

				if(duel != null && duel.update(skill, info, attacker, this)) {
					return;
				}
			}
		}

		if(attacker.isPlayer() && isPvPMode() && !attacker.isPvPMode()) {
			attacker.setPvPMode(true);
		}

		super.causingDamage(skill, info, attacker);
	}

	@Override
	public boolean checkTarget(Character target) {

		if(target == null || target == this) {
			return false;
		}

		if(target.isSummon()) {
			return checkTarget(target.getOwner());
		}

		Duel duel = getDuel();

		if(duel != null) {

			if(!target.isPlayer() || target.getDuel() != duel) {
				return false;
			}

			return true;
		}

		Player player = target.getPlayer();

		if(player != null) {

			if(!isGM() && (isInPeaceTerritory() || player.isInPeaceTerritory())) {
				return false;
			}

			if(isInBattleTerritory() != player.isInBattleTerritory()) {
				return false;
			}

			if(fractionId != 0 && player.getFractionId() == fractionId) {
				return false;
			}

			if(isInBattleTerritory()) {
				return true;
			}

			if(party != null && party == player.getParty()) {
				return false;
			}

			if(guild != null && target.getGuild() == guild) {
				return false;
			}

			//return (isPvPMode() && target.getLevel() > Config.WORLD_MIN_TARGET_LEVEL_FOR_PK && !player.hasPremium()) || player.isPvPMode();
			return (isPvPMode() && target.getLevel() > MissingConfig.WORLD_MIN_TARGET_LEVEl_FOR_PK && !player.hasPremium()) || player.isPvPMode();
		}

		Npc npc = target.getNpc();

		if(npc != null && npc.isFriendNpc()) {
			return false;
		}

		return true;
	}

	/**
	 * Очитска от прошлых ссылок.
	 */
	public void clearLinks() {
		lastLinks.clear();
	}

	/**
	 * Закрытие коннекта с клиентом.
	 */
	public void closeConnection() {
		if(client != null) {
			connected = false;
			client.close();
		}
	}

	@Override
	public void decayMe(int type) {

		TerritoryTable territoryTable = TerritoryTable.getInstance();
		territoryTable.onExitWorld(this);

		super.decayMe(type);
	}

	/**
	 * @param questList список квестов игрока.
	 */
	public void setQuestList(QuestList questList) {
		this.questList = questList;
	}

	/**
	 * @param friendList список друзей.
	 */
	public void setFriendList(FriendList friendList) {
		this.friendList = friendList;
	}

	@Override
	public void deleteMe() {

		if(isDeleted()) {
			return;
		}

		abortCast(true);
		abortCollect();
		setLastNpc(null);

		Dialog lastDialog = getLastDialog();

		if(lastDialog != null) {
			lastDialog.close();
			setLastDialog(null);
		}

		Action lastAction = getLastAction();

		if(lastAction != null) {
			lastAction.cancel(this);
			setLastAction(null);
		}

		ActionDialog lastActionDialog = getLastActionDialog();

		if(lastActionDialog != null) {
			lastActionDialog.cancel(this);
			setLastActionDialog(null);
		}

		Summon summon = getSummon();

		if(summon != null) {
			summon.remove();
			setSummon(null);
		}

		Party party = getParty();

		if(party != null)
			party.removePlayer(this);

		World.removeOldPlayer(this);

		RegenTaskManager regenManager = RegenTaskManager.getInstance();
		regenManager.removeCharacter(this);

		synchronized(this) {

			QuestList questList = getQuestList();

			if(questList != null) {
				questList.save();
				questList.fold();
				setQuestList(null);
			}

			store(true);

			Guild guild = getGuild();

			if(guild != null) {
				guild.exitOutGame(this);
				setGuild(null);
			}

			Table<IntKey, Skill> skills = getSkills();
			skills.apply(ExtUtils.FOLD_SKILL_TABLE_FUNC);
			skills.clear();

			Table<String, Wrap> variables = getVariables();
			variables.apply(ExtUtils.FOLD_WRAP_TABLE_FUNC);
			variables.clear();

			Inventory inventory = getInventory();

			if(inventory != null) {
				inventory.fold();
				setInventory(null);
			}

			Equipment equipment = getEquipment();

			if(equipment != null) {
				equipment.fold();
				setEquipment(null);
			}

			Bank bank = getBank();

			if(bank != null) {
				bank.fold();
				setBank(null);
			}

			PlayerAppearance appearance = getAppearance();

			if(appearance != null) {
				appearance.fold();
				setAppearance(null, false);
			}

			FriendList friendList = getFriendList();

			if(friendList != null) {
				friendList.fold();
				setFriendList(null);
			}

			setClient(null);
		}

		super.deleteMe();
	}

	/**
	 * @param account аккаунт игрока.
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	@Override
	public boolean disableItem(Skill skill, ItemInstance item) {

		if(super.disableItem(skill, item)) {
			sendPacket(ItemReuse.getInstance(item.getItemId(), skill.getReuseDelay(this) / 1000), true);
			return true;
		}

		return false;
	}

	@Override
	public void doCast(float startX, float startY, float startZ, Skill skill, int state, int heading, float targetX, float targetY, float targetZ) {

		attacking = false;

		super.doCast(startX, startY, startZ, skill, state, heading, targetX, targetY, targetZ);
	}

	@Override
	public void doCollect(ResourseInstance resourse) {
		collectTask.nextTask(resourse);
	}

	@Override
	public void doDie(Character attacker) {

		if(isOnMount()) {
			getOffMount();
		}

		Party party = getParty();

		if(party != null) {
			SystemMessage message = SystemMessage.getInstance(MessageType.PARTY_PLAYER_NAME_IS_DEAD);
			message.add("PartyPlayerName", getName());
			party.sendPacket(this, message);
		}

		if(attacker != this && attacker.isPlayer()) {

			Player killer = attacker.getPlayer();

			attacker.sendPacket(SystemMessage.getInstance(MessageType.YOU_KILLED_PLAYER).addPlayer(getName()), true);
			sendPacket(SystemMessage.getInstance(MessageType.PLAYER_KILLED_YOU).addPlayer(killer.getName()), true);

			checkPK(killer);

			if(!killer.isEvent()) {
				killer.addPvPCount();
			}

			World.addKilledPlayers();
		}

		destroyCrystals(attacker);

		if(isPK() && attacker != this) {
			dropItems();
		}

		broadcastPacket(CharDead.getInstance(this, true));
		sendPacket(PlayerDeadWindow.getInstance(), true);

		super.doDie(attacker);
	}

	@Override
	public int doFall(float startZ, float endZ) {

		int damage = super.doFall(startZ, endZ);

		if(damage > 0 && MissingConfig.SERVER_FALLING_DAMAGE)
			sendMessage("Get " + damage + " damage from falling.");

		return damage;
	}

	@Override
	public void doOwerturn(Character attacker) {

		if(isOwerturned())
			return;

		super.doOwerturn(attacker);

		float radians = Angles.headingToRadians(heading + 32500);

		float newX = Coords.calcX(x, 90, radians);
		float newY = Coords.calcY(y, 90, radians);

		GeoManager geoManager = GeoManager.getInstance();

		setXYZ(newX, newY, geoManager.getHeight(continentId, newX, newY, z));

		SafeTask task = new SafeTask() {

			@Override
			protected void runImpl() {
				cancelOwerturn();
			}
		};

		ExecutorManager executor = ExecutorManager.getInstance();
		executor.scheduleGeneral(task, 3000);

		broadcastMove(x, y, z, heading, MoveType.STOP, x, y, z, true);
	}

	@Override
	public void effectHealHp(int heal, Character healer) {

		int add = getCurrentHp();

		super.effectHealHp(heal, healer);

		add = getCurrentHp() - add;

		sendPacket(PlayerCurrentHp.getInstance(this, healer, add, PlayerCurrentHp.INCREASE_PLUS), true);
	}

	@Override
	public void effectHealMp(int heal, Character healer) {

		int add = getCurrentMp();

		super.effectHealMp(heal, healer);

		add = getCurrentMp() - add;

		sendPacket(PlayerCurrentMp.getInstance(this, healer, add, PlayerCurrentMp.INCREASE_PLUS), true);
	}

	/**
	 * @return уровень доступа.
	 */
	public int getAccessLevel() {
		return accessLevel;
	}

	/**
	 * @return аккаунт игрока.
	 */
	public Account getAccount() {
		return account;
	}

	@Override
	public PlayerAI getAI() {

		if(ai == null)
			ai = new PlayerAI(this);

		return (PlayerAI) ai;
	}

	/**
	 * @return кол-во сделанных атак/блоков между изминением усталости.
	 */
	public int getAttackCounter() {
		return attackCounter;
	}

	@Override
	protected EmotionType[] getAutoEmotions() {
		return EmotionTask.PLAYER_TYPES;
	}

	/**
	 * @return базовая атака.
	 */
	public int getBaseAttack() {
		return (int) calcStat(StatType.ATTACK, 0, 0x20, null, null);
	}

	/**
	 * @return базовый баланс.
	 */
	public int getBaseBalance() {
		return (int) calcStat(StatType.BALANCE, 0, 0x20, null, null);
	}

	/**
	 * @return базовая защита.
	 */
	public int getBaseDefense() {
		return (int) calcStat(StatType.DEFENSE, 0, 0x20, null, null);
	}

	/**
	 * @return базовая сила.
	 */
	public int getBaseImpact() {
		return (int) calcStat(StatType.IMPACT, 0, 0x20, null, null);
	}

	/**
	 * @return ид класса.
	 */
	@Override
	public int getClassId() {
		return getTemplate().getClassId();
	}

	/**
	 * @return клиент игрока.
	 */
	public UserClient getClient() {
		return client;
	}

	/**
	 * Определние цвета ника цели игрока.
	 * 
	 * @param target цель.
	 * @return нужный цвет ника.
	 */
	public int getColor(Player target) {
		if(fractionId != 0)
			return fractionId != target.getFractionId() ? NameColor.COLOR_RED_PVP : NameColor.COLOR_NORMAL;
		else if(duel != null && duel == target.duel)
			return NameColor.COLOR_RED_PVP;
		else if(party != null && party == target.party)
			return NameColor.COLOR_BLUE;
		else if(guild != null && guild == target.getGuild())
			return NameColor.COLOR_GREEN;

		return isPvPMode() || isPK() || target.isPK() || target.isPvPMode() ? NameColor.COLOR_RED_PVP : NameColor.COLOR_NORMAL;
	}

	/**
	 * @return текущий цвет своего ника.
	 */
	public int getColor() {

		if(isPvPMode() || isPK()) {
			return NameColor.COLOR_RED;
		} else {
			return NameColor.COLOR_NORMAL;
		}
	}

	/**
	 * @return дата создания игрока.
	 */
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public Duel getDuel() {
		return duel;
	}

	/**
	 * @return дата окончания бана.
	 */
	public final long getEndBan() {
		return endBan;
	}

	/**
	 * @return дата окончания бана чата.
	 */
	public final long getEndChatBan() {
		return endChatBan;
	}

	/**
	 * @return уровень сбора кристалов.
	 */
	@Override
	public final int getEnergyLevel() {
		return energyLevel;
	}

	/**
	 * @return кол-во опыта у игрока.
	 */
	public long getExp() {
		return exp;
	}

	/**
	 * @return внешность игрока.
	 */
	public PlayerAppearance getAppearance() {
		return appearance;
	}

	/**
	 * @return фракция игрока.
	 */
	public final int getFraction() {
		return fraction;
	}

	/**
	 * @return список друзей.
	 */
	public FriendList getFriendList() {
		if(friendList == null)
			synchronized(this) {
				if(friendList == null)
					friendList = FriendList.getInstance(this);
			}

		return friendList;
	}

	@Override
	public Guild getGuild() {
		return guild;
	}

	/**
	 * @return ид клана.
	 */
	public int getGuildId() {
		return guild == null ? 0 : guild.getId();
	}

	/**
	 * @return название клана.
	 */
	public String getGuildName() {
		return guild == null ? null : guild.getName();
	}

	/**
	 * @return титул клана.
	 */
	public String getGuildTitle() {
		return guild == null ? null : guild.getTitle();
	}

	/**
	 * @return заметка для гильдии о игроке.
	 */
	public String getGuildNote() {
		return guildNote;
	}

	/**
	 * @return ранг в клане.
	 */
	public final GuildRank getGuildRank() {
		return guildRank;
	}

	/**
	 * @return ид ранга в клане.
	 */
	public final int getGuildRankId() {
		return guildRank == null ? 0 : guildRank.getIndex();
	}

	/**
	 * @return название иконки гильдии.
	 */
	public String getGuildIconName() {

		if(guild == null)
			return null;

		GuildIcon icon = guild.getIcon();

		return icon == null ? null : icon.getName();
	}

	/**
	 * @return настроки горячих клавишь.
	 */
	public byte[] getHotkey() {
		return hotkey;
	}

	@Override
	public int getKarma() {
		return karma;
	}

	/**
	 * @return последни акшен.
	 */
	public Action getLastAction() {
		return lastAction;
	}

	/**
	 * @return последний акшен диалог.
	 */
	public ActionDialog getLastActionDialog() {
		return lastActionDialog;
	}

	/**
	 * @return время последней блокировки.
	 */
	public final long getLastBlock() {
		return lastBlock;
	}

	/**
	 * @return последний диалог.
	 */
	public Dialog getLastDialog() {
		return lastDialog;
	}

	/**
	 * @return последняя нажатая ссылка.
	 */
	public Link getLastLink() {
		return lastLink;
	}

	/**
	 * @return последний нпс с которым взаимодействовал игрок.
	 */
	public Npc getLastNpc() {
		return lastNpc;
	}

	@Override
	public int getLevel() {
		return level;
	}

	/**
	 * Получение линка по индексу из списка последних линков.
	 * 
	 * @param index индекс линка.
	 * @return линк.
	 */
	public Link getLink(int index) {
		lastLinks.writeLock();
		try {
			if(index >= lastLinks.size() || index < 0)
				return null;

			return lastLinks.get(index);
		} finally {
			lastLinks.writeUnlock();
		}
	}

	@Override
	public Array<Character> getLockOnTargets() {
		return lockOnTargets;
	}

	/**
	 * @return максимальный уровень усталости.
	 */
	public int getMaxStamina() {
		return (int) calcStat(StatType.BASE_HEART, 120, this, null);
	}

	/**
	 * @return уровень сбора камней.
	 */
	@Override
	public final int getMiningLevel() {
		return miningLevel;
	}

	/**
	 * @return минимальный уровень усталости.
	 */
	public int getMinStamina() {
		return (int) (calcStat(StatType.BASE_HEART, 120, this, null) / 100 * calcStat(StatType.MIN_HEART_PERCENT, 1, this, null));
	}

	/**
	 * @return ид активного маунта.
	 */
	public final int getMountId() {
		return mountId;
	}

	/**
	 * @return скил активного маунта.
	 */
	public final Skill getMountSkill() {
		return mountSkill;
	}

	@Override
	public ServerPacket getMovePacket(float x, float y, float z, int heading, MoveType type, float targetX, float targetY, float targetZ) {
		return PlayerMove.getInstance(this, type, x, y, z, heading, targetX, targetY, targetZ);
	}

	@Override
	public void getOffMount() {

		Skill skill = getMountSkill();

		if(skill == null) {
			return;
		}

		SkillTemplate template = skill.getTemplate();
		template.removePassiveFuncs(this);

		setMountId(0);
		broadcastPacket(MountOff.getInstance(this, skill.getIconId()));
		setMountSkill(null);
		updateInfo();
	}

	/**
	 * @return время последнего входа в игру.
	 */
	public long getOnlineBeginTime() {
		return onlineBeginTime;
	}

	/**
	 * @return сколько уже времени онлаин.
	 */
	public long getOnlineTime() {
		return onlineTime + (System.currentTimeMillis() - onlineBeginTime);
	}

	@Override
	public int getOwerturnId() {
		return 0x080F6C72; // 0x080F6C72; //0x080F6C72 1010802 80F6C72
	}

	@Override
	public Party getParty() {
		return party;
	}

	/**
	 * @return уровень сбора растений.
	 */
	@Override
	public final int getPlantLevel() {
		return plantLevel;
	}

	@Override
	public Player getPlayer() {
		return this;
	}

	/**
	 * @return класс игрока.
	 */
	public PlayerClass getPlayerClass() {
		return getTemplate().getPlayerClass();
	}

	/**
	 * @return кол-во убитых нпс.
	 */
	public final int getPveCount() {
		return pveCount;
	}

	/**
	 * @return кол-во убитых игроков.
	 */
	public final int getPvpCount() {
		return pvpCount;
	}

	/**
	 * @return список квестов.
	 */
	@Override
	public QuestList getQuestList() {

		if(questList == null)
			synchronized(this) {
				if(questList == null)
					questList = QuestList.newInstance(this);
			}

		return questList;
	}

	/**
	 * @return ид расы игрока.
	 */
	public int getRaceId() {
		return getTemplate().getRaceId();
	}

	/**
	 * @return маршрут полета.
	 */
	public Route getRoute() {
		return route;
	}

	/**
	 * @return настройки клиента.
	 */
	public byte[] getSettings() {
		return settings;
	}

	/**
	 * @return пол игрока.
	 */
	public Sex getSex() {
		return getTemplate().getSex();
	}

	/**
	 * @return пол игрока.
	 */
	public int getSexId() {
		return getTemplate().getSex().ordinal();
	}

	/**
	 * @return уровень усталости игрока.
	 */
	public int getStamina() {
		return stamina;
	}

	@Override
	public int getSubId() {
		return Config.SERVER_PLAYER_SUB_ID;
	}

	@Override
	public PlayerTemplate getTemplate() {
		return (PlayerTemplate) template;
	}

	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * Получение значения переменной.
	 * 
	 * @param name название переменной.
	 * @return значение переменной.
	 */
	public final Wrap getVar(String name) {
		return variables.get(name);
	}

	/**
	 * @param name название переменной.
	 * @param def значение при отсутствии.
	 * @return значение переменной.
	 */
	public final int getVar(String name, int def) {

		Table<String, Wrap> variables = getVariables();

		synchronized(variables) {

			Wrap wrap = variables.get(name);

			if(wrap == null) {
				return def;
			}

			if(wrap.getWrapType() == WrapType.INTEGER) {
				return wrap.getInt();
			}
		}

		return def;
	}

	/**
	 * @return таблица переменных.
	 */
	public final Table<String, Wrap> getVariables() {
		return variables;
	}

	/**
	 * @return находится ли игрок в дуэли.
	 */
	public boolean hasDuel() {
		return duel != null;
	}

	@Override
	public boolean hasGuild() {
		return guild != null;
	}

	/**
	 * @return если сохраненные настройки клавишь.
	 */
	public boolean hasHotKey() {
		return hotkey != null;
	}

	/**
	 * @return есть ли последний акшен.
	 */
	public boolean hasLastAction() {
		return lastAction != null;
	}

	/**
	 * @return есть ли диалог с последнего акшена.
	 */
	public boolean hasLastActionDialog() {
		return lastActionDialog != null;
	}

	/**
	 * @return есть ли коннект.
	 */
	public boolean hasNetConnection() {
		return client != null;
	}

	@Override
	public boolean hasParty() {
		return party != null;
	}

	@Override
	public boolean hasPremium() {

		Account account = getAccount();

		if(account == null) {
			return false;
		}

		return System.currentTimeMillis() < account.getEndPay();
	}

	/**
	 * @return есть ли сохраненные настройки клиента.
	 */
	public boolean hasSettings() {
		return settings != null;
	}

	/**
	 * Увеличение уровня игрока.
	 */
	public void increaseLevel() {

		if(level + 1 > Config.WORLD_PLAYER_MAX_LEVEL) {
			return;
		}

		level += 1;

		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		broadcastPacket(IncreaseLevel.getInstance(this));

		ObjectEventManager eventManager = ObjectEventManager.getInstance();
		eventManager.notifyChangedLevel(this);
	}

	@Override
	public boolean isAttacking() {
		return attacking;
	}

	/**
	 * @return the changedFace
	 */
	public final boolean isChangedFace() {
		return changedFace;
	}

	/**
	 * @return the changedSettings
	 */
	public final boolean isChangedSettings() {
		return changedSettings;
	}

	/**
	 * @return the changeHotkey
	 */
	public final boolean isChangeHotkey() {
		return changeHotkey;
	}

	@Override
	public boolean isCollecting() {
		return collectTask.isRunning();
	}

	/**
	 * @return подключен ли к клиенту.
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @return находится ли игрок на ивенте.
	 */
	public final boolean isEvent() {
		return event;
	}

	@Override
	public boolean isGM() {
		return accessLevel > 100;
	}

	@Override
	public boolean isOnMount() {
		return mountId != 0;
	}

	@Override
	public final boolean isPlayer() {
		return true;
	}

	@Override
	public boolean isPvPMode() {
		return pvpMode;
	}

	/**
	 * @return можно ли самому вскресится.
	 */
	public final boolean isResurrected() {
		return resurrected;
	}

	/**
	 * Был ли игрок в этой территории.
	 * 
	 * @param territory проверяемая территория.
	 * @return был ли в ней уже игрок.
	 */
	public boolean isWhetherIn(Territory territory) {
		return storedTerrs.containsKey(territory.getId());
	}

	public void loadVariables() {
		// TODO
	}

	@Override
	protected Geom newGeomCharacter() {
		return new PlayerGeom(this, PlayerGeomTable.getHeight(getRaceId(), getSexId()), PlayerGeomTable.getRadius(getRaceId(), getSexId()));
	}

	@Override
	protected Regen newRegenHp() {
		return new PlayerRegenHp(this);
	}

	@Override
	protected Regen newRegenMp() {

		if(getClassId() == 2 || getClassId() == 3) {
			return new PlayerNegativeRegenMp(this);
		}

		return new PlayerPositiveRegenMp(this);
	}

	@Override
	public int nextCastId() {
		return ID_FACTORY.getNextId();
	}

	/**
	 * Удаление из обработки костра.
	 * 
	 * @param oldBonfire удаляющий костер.
	 */
	public void removeBonfire(Bonfire oldBonfire) {

		if(bonfire == oldBonfire) {
			synchronized(this) {
				if(bonfire == oldBonfire) {
					bonfire = null;
					sendMessage(MessageType.YOU_ARE_NO_LONGER_RECHARGING_STAMINA);
				}
			}
		}
	}

	@Override
	public void removeMe(Player player, int type) {

		Duel duel = getDuel();

		if(duel != null) {
			synchronized(this) {

				duel = getDuel();

				if(duel != null && player.getDuel() == duel) {
					duel.cancel(false, true);
				}
			}
		}

		player.sendPacket(DeleteCharacter.getInstance(this, type), true);
	}

	@Override
	public void removeSkill(int skillId, boolean sendPacket) {

		super.removeSkill(skillId, sendPacket);

		if(sendPacket) {
			sendPacket(SkillListInfo.getInstance(this), true);
		}
	}

	@Override
	public void removeSkill(Skill skill, boolean sendPacket) {

		super.removeSkill(skill, sendPacket);

		if(sendPacket) {
			sendPacket(SkillListInfo.getInstance(this), true);
		}
	}

	@Override
	public void removeSkill(SkillTemplate template, boolean sendPacket) {

		super.removeSkill(template, sendPacket);

		if(sendPacket) {
			sendPacket(SkillListInfo.getInstance(this), true);
		}
	}

	@Override
	public void removeSkills(SkillTemplate[] templates, boolean sendPacket) {

		super.removeSkills(templates, sendPacket);

		if(sendPacket) {
			sendPacket(SkillListInfo.getInstance(this), true);
		}
	}

	@Override
	public void removeVisibleObject(TObject object, int type) {

		if(object == null || object.getObjectId() == objectId) {
			return;
		}

		object.removeMe(this, type);
	}

	/**
	 * Сохранение переменных в БД.
	 */
	public void saveVars() {

		Table<String, Wrap> variables = getVariables();

		synchronized(variables) {
			variables.apply(saveVarFunc);
		}
	}

	/**
	 * Отправка пакетов эффектов.
	 */
	public void sendEffects() {

		if(effectList == null || effectList.size() < 1) {
			return;
		}

		effectList.lock();
		try {

			Array<Effect> effects = effectList.getEffects();

			Effect[] array = effects.array();

			for(int i = 0, length = effects.size(); i < length; i++) {

				Effect effect = array[i];

				if(effect == null || effect.isEnded()) {
					continue;
				}

				if(effect.getEffectType() == EffectType.CHARM_BUFF) {
					sendPacket(AppledCharmEffect.getInstance(this, effect.getEffectId(), effect.getTimeEnd() * 1000), true);
					continue;
				}

				sendPacket(AppledEffect.getInstance(effect.getEffector(), effect.getEffected(), effect.getEffectId(), effect.getTimeEnd() * 1000), true);
			}
		} finally {
			effectList.unlock();
		}
	}

	@Override
	public void sendMessage(MessageType type) {
		sendPacket(SystemMessage.getInstance(type), true);
	}

	@Override
	public void sendMessage(String message) {
		sendPacket(CharSay.getInstance(Strings.EMPTY, message, SayType.SYSTEM_CHAT, 0, 0), true);
	}

	@Override
	public void sendPacket(ServerPacket packet, boolean increaseSends) {

		if(packet == null) {
			return;
		}

		UserClient client = getClient();

		if(client == null) {
			return;
		}

		client.sendPacket(packet, increaseSends);
	}

	/**
	 * Отсылка итемов находяхчихся в откате.
	 */
	public void sendReuseItems() {

		Table<IntKey, ReuseSkill> reuses = getReuseSkills();

		for(ReuseSkill reuse : reuses) {
			if(reuse.isItemReuse()) {
				sendPacket(ItemReuse.getInstance(reuse.getItemId(), (int) Math.max(0, (reuse.getEndTime() - System.currentTimeMillis()) / 1000)), true);
			}
		}
	}

	/**
	 * Отсылка скилов находяхчихся в откате.
	 */
	public void sendReuseSkills() {

		Table<IntKey, ReuseSkill> reuses = getReuseSkills();

		for(ReuseSkill reuse : reuses) {
			if(!reuse.isItemReuse()) {
				sendPacket(SkillReuse.getInstance(reuse.getSkillId(), (int) Math.max(0, reuse.getEndTime() - System.currentTimeMillis())), true);
			}
		}
	}

	/**
	 * Установка уровня прав игрока.
	 * 
	 * @param level новый уровень прав.
	 */
	public void setAccessLevel(int level) {
		accessLevel = level;
	}

	/**
	 * @param attackCounter кол-во сделанных атак между изминениями усталости.
	 */
	public void setAttackCounter(int attackCounter) {
		this.attackCounter = attackCounter;
	}

	@Override
	public void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	/**
	 * @param changedFace the changedFace to set
	 */
	public final void setChangedFace(boolean changedFace) {
		this.changedFace = changedFace;
	}

	/**
	 * @param changedSettings the changedSettings to set
	 */
	public final void setChangedSettings(boolean changedSettings) {
		this.changedSettings = changedSettings;
	}

	/**
	 * @param changeHotkey the changeHotkey to set
	 */
	public final void setChangeHotkey(boolean changeHotkey) {
		this.changeHotkey = changeHotkey;
	}

	/**
	 * @param client клиент, который управляет игроком.
	 */
	public void setClient(UserClient client) {
		this.client = client;
		this.account = client != null ? client.getAccount() : null;
		this.connected = client != null && client.isConnected();
	}

	/**
	 * @param connected подключен ли клиент.
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	/**
	 * @param createTime дата создания игрока.
	 */
	public void setCreateTime(final long createTime) {
		this.createTime = createTime;
	}

	/**
	 * @param duel текущий дуэль.
	 */
	public void setDuel(Duel duel) {
		this.duel = duel;
	}

	/**
	 * @param endBan дата окончания бана.
	 */
	public final void setEndBan(long endBan) {
		this.endBan = endBan;
	}

	/**
	 * @param endChatBan дата окончания бана чата.
	 */
	public final void setEndChatBan(long endChatBan) {
		this.endChatBan = endChatBan;
	}

	/**
	 * @param energyLevel уровень сбора кристалов.
	 */
	public final void setEnergyLevel(int energyLevel) {
		this.energyLevel = Math.min(energyLevel, 300);
	}

	/**
	 * @param event находится ли игрок на ивенте.
	 */
	public final void setEvent(boolean event) {
		this.event = event;
	}

	/**
	 * @param val кол-во опыта.
	 */
	public void setExp(int exp) {
		this.exp = Math.max(exp, 0);
	}

	/**
	 * @param appearance внешность игрока.
	 * @param isNew является ли это новой внешностью.
	 */
	public void setAppearance(PlayerAppearance appearance, boolean isNew) {
		this.appearance = appearance;

		if(isNew) {
			setChangedFace(true);
		}
	}

	/**
	 * @param fraction фракция игрока.
	 */
	public final void setFraction(int fraction) {
		this.fraction = fraction;
	}

	/**
	 * @param guild гильдия.
	 */
	public void setGuild(Guild guild) {
		this.guild = guild;
	}

	/**
	 * @param id ид гильдии.
	 */
	public void setGuildId(int id) {
		GuildManager guildManager = GuildManager.getInstance();
		guild = guildManager.getGuild(id);
	}

	/**
	 * @param guildNote заметка для гильдии о игроке.
	 */
	public void setGuildNote(String guildNote) {

		if(guildNote.isEmpty()) {
			guildNote = Strings.EMPTY;
		}

		this.guildNote = guildNote;
	}

	/**
	 * @param clanRank ранг клана.
	 */
	public final void setGuildRank(GuildRank guildRank) {
		this.guildRank = guildRank;
	}

	/**
	 * @param hotkey настроки горячих клавишь.
	 * @param isNew является ли это новым хначением.
	 */
	public void setHotkey(byte[] hotkey, boolean isNew) {
		this.hotkey = hotkey;

		if(isNew) {
			changeHotkey = true;
		}
	}

	@Override
	public void setKarma(int karma) {
		this.karma = Math.max(karma, 0);
	}

	/**
	 * @param lastAction последний акшен.
	 */
	public void setLastAction(Action lastAction) {
		this.lastAction = lastAction;
	}

	/**
	 * @param lastActionDialog последний акшен диалог.
	 */
	public void setLastActionDialog(ActionDialog lastActionDialog) {
		this.lastActionDialog = lastActionDialog;
	}

	/**
	 * @param lastDialog последний диалог.
	 */
	public void setLastDialog(Dialog lastDialog) {
		this.lastDialog = lastDialog;
	}

	/**
	 * @param lastLink последний линк.
	 */
	public void setLastLink(Link lastLink) {
		this.lastLink = lastLink;
	}

	/**
	 * @param lastNpc последний нпс, с которым было взаимодействие.
	 */
	public void setLastNpc(Npc lastNpc) {
		this.lastNpc = lastNpc;
	}

	/**
	 * Установка уровня игрока.
	 * 
	 * @param newLevel новый уровень игрока.
	 * @return приминилось ли изминение.
	 */
	public boolean setLevel(int newLevel) {

		if(newLevel > Config.WORLD_PLAYER_MAX_LEVEL) {
			level = Config.WORLD_PLAYER_MAX_LEVEL;
		} else if(newLevel < 1) {
			level = 1;
		} else {
			level = newLevel;
		}

		return level == newLevel;
	}

	/**
	 * @param miningLevel уровень сбора кристалов.
	 */
	public final void setMiningLevel(int miningLevel) {
		this.miningLevel = Math.min(miningLevel, 300);
	}

	/**
	 * @param mountId ид активного маунта.
	 */
	public final void setMountId(int mountId) {
		this.mountId = mountId;
	}

	/**
	 * @param mountSkill скил активного маунта.
	 */
	public final void setMountSkill(Skill mountSkill) {
		this.mountSkill = mountSkill;
	}

	/**
	 * @param onlineBeginTime время начала текущей сессии.
	 */
	public void setOnlineBeginTime(long onlineBeginTime) {
		this.onlineBeginTime = onlineBeginTime;
	}

	/**
	 * @param time общее время онлайна.
	 */
	public void setOnlineTime(final long time) {
		onlineTime = time;
		setOnlineBeginTime(System.currentTimeMillis());
	}

	/**
	 * @param party группа игрока.
	 */
	public void setParty(Party party) {
		this.party = party;
	}

	/**
	 * @param plantLevel уровень сбора растений.
	 */
	public final void setPlantLevel(int plantLevel) {
		this.plantLevel = Math.min(plantLevel, 300);
	}

	/**
	 * @param pveCount кол-во убитых нпс.
	 */
	public final void setPvECount(int pveCount) {
		this.pveCount = pveCount;
	}

	/**
	 * @param pvpCount кол-во убитых игроков.
	 */
	public final void setPvPCount(int pvpCount) {
		this.pvpCount = pvpCount;
	}

	@Override
	public void setPvPMode(boolean pvpMode) {

		this.pvpMode = pvpMode;

		if(isSpawned()) {

			LocalObjects local = LocalObjects.get();

			Array<Player> players = World.getAround(Player.class, local.getNextPlayerList(), this);

			Player[] array = players.array();

			ServerPacket hp = null;
			ServerPacket pvp = null;

			if(pvpMode) {
				hp = TargetHp.getInstance(this, TargetHp.RED);
				pvp = PlayerPvPOn.getInstance(this);
			} else {
				hp = CancelTargetHp.getInstance(this);
				pvp = PlayerPvPOff.getInstance(this);
			}

			hp.increaseSends();
			pvp.increaseSends();

			for(int i = 0, length = players.size(); i < length; i++) {

				Player player = array[i];

				if(player == null)
					continue;

				player.sendPacket(pvp, true);
				player.sendPacket(hp, true);
			}

			sendPacket(pvp, true);
			sendPacket(NameColor.getInstance(getColor(), this), true);

			updateColor(players);

			hp.complete();
			pvp.complete();
		}
	}

	/**
	 * Обновление цветов ников.
	 */
	public void updateColor(Array<Player> players) {

		Player[] array = players.array();

		for(int i = 0, length = players.size(); i < length; i++) {

			Player player = array[i];

			player.updateColor(this);

			updateColor(player);
		}
	}

	/**
	 * Обновление цветов ников.
	 */
	public void updateColor() {

		LocalObjects local = LocalObjects.get();

		Array<Player> players = World.getAround(Player.class, local.getNextPlayerList(), this);

		updateColor(players);
	}

	/**
	 * @param resurrected можно ли самому вскресится.
	 */
	public final void setResurrected(boolean resurrected) {
		this.resurrected = resurrected;
	}

	/**
	 * @param route маршрут палета.
	 */
	public void setRoute(Route route) {
		this.route = route;
	}

	/**
	 * @param settings настройки клиента.
	 * @param isNew является ли это новым хначением.
	 */
	public void setSettings(byte[] settings, boolean isNew) {
		this.settings = settings;

		if(isNew)
			changedSettings = true;
	}

	/**
	 * @param stamina уровень усталости игрока.
	 */
	public void setStamina(int stamina) {

		int maxStamina = getMaxStamina();
		int minStamina = getMinStamina();

		if(stamina > maxStamina) {
			stamina = maxStamina;
		}

		if(stamina < minStamina) {
			stamina = minStamina;
		}

		this.stamina = stamina;

		int current = getCurrentHp();
		int max = getMaxHp();

		boolean updateUserInfo = false;

		if(current > max) {
			setCurrentHp(max);
			updateUserInfo = true;
		}

		current = getCurrentMp();
		max = getMaxMp();

		if(current > max) {
			setCurrentMp(max);
			updateUserInfo = true;
		}

		if(updateUserInfo) {
			updateInfo();
		}

		ObjectEventManager eventManager = ObjectEventManager.getInstance();
		eventManager.notifyStaminaChanged(this);
	}

	/**
	 * Установка нового значения переменной.
	 * 
	 * @param name название переменной.
	 * @param val значеие переменной.
	 */
	public void setVar(String name, int val) {

		Table<String, Wrap> variables = getVariables();

		synchronized(variables) {

			Wrap wrap = variables.get(name);

			if(wrap == null) {

				variables.put(name, Wraps.newIntegerWrap(val, true));

				DataBaseManager dbManager = DataBaseManager.getInstance();
				dbManager.insertPlayerVar(objectId, name, String.valueOf(val));

			} else if(wrap.getWrapType() == WrapType.INTEGER) {
				wrap.setInt(val);
			} else {
				variables.put(name, Wraps.newIntegerWrap(val, true));
			}
		}
	}

	/**
	 * Установка кастомного параметра игроку.
	 * 
	 * @param name название параметра.
	 * @param value значение параметра.
	 */
	public void setVar(String name, String value) {
		// TODO userVariables.put(name, value);
	}

	@Override
	public void setXYZ(float x, float y, float z) {
		super.setXYZ(x, y, z);
		updateTerritories();
	}

	@Override
	public void spawnMe() {
		super.spawnMe();

		TerritoryTable territoryTable = TerritoryTable.getInstance();
		territoryTable.onEnterWorld(this);

		//Show guild when player start
		//updateGuild();

		emotionTask.start();
	}

	@Override
	public boolean startBattleStance(Character enemy) {

		if(isBattleStanced()) {
			battleStanceTask.update();
			return false;
		}

		if(Config.DEVELOPER_MAIN_DEBUG)
			sendMessage("start battle stance.");

		battleStanceTask.now();
		battleStanced = true;

		sendMessage(MessageType.BATTLE_STANCE_ON);

		updateInfo();

		return true;
	}

	@Override
	public void stopBattleStance() {

		if(Config.DEVELOPER_MAIN_DEBUG)
			sendMessage("stop battle stance.");

		battleStanceTask.stop();
		battleStanced = false;

		sendMessage(MessageType.BATTLE_STANCE_OFF);
		updateInfo();
	}

	/**
	 * Сохранение игрока в БД.
	 */
	public synchronized void store(boolean deleted) {

		if(isDeleted())
			return;

		DataBaseManager dbManager = DataBaseManager.getInstance();
		dbManager.fullStore(this);
	}

	/**
	 * Добавление территорий в число побывавшихся.
	 * 
	 * @param territory добавляемая территория.
	 * @param inDB вносить ли в БД.
	 */
	public void storeTerritory(Territory territory, boolean inDB) {

		storedTerrs.put(territory.getId(), territory);

		if(inDB) {
			DataBaseManager dbManager = DataBaseManager.getInstance();
			dbManager.storeTerritory(this, territory);
		}
	}

	/**
	 * Обработка уменьшения уровня усталости.
	 */
	public void subHeart() {
		setStamina(stamina - 1);
	}

	@Override
	public void teleToLocation(int continentId, float x, float y, float z, int heading) {

		decayMe(DeleteCharacter.DISAPPEARS);

		int current = getContinentId();

		super.teleToLocation(continentId, x, y, z, heading);

		if(current != continentId) {
			DataBaseManager dbManager = DataBaseManager.getInstance();
			dbManager.updatePlayerContinentId(this);
		}

		broadcastPacket(Tp1.getInstance(this));

		int zoneId = World.getRegion(this).getZoneId(this);

		if(zoneId < 1) {
			zoneId = getContinentId() + 1;
		}

		setZoneId(zoneId);

		ObjectEventManager eventManager = ObjectEventManager.getInstance();
		eventManager.notifyChangedZoneId(this);

		sendPacket(WorldZone.getInstance(this), true);
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Удаление кастомного параметра.
	 * 
	 * @param name название параметра.
	 */
	public void unsetVar(String name) {
		if(name == null)
			return;
	}

	/**
	 * Обновление цветов ника.
	 * 
	 * @param target целевой игрок.
	 */
	public void updateColor(Player target) {
		sendPacket(NameColor.getInstance(getColor(target), target), true);
		target.sendPacket(NameColor.getInstance(target.getColor(this), this), true);
	}

	@Override
	public void updateCoords() {
		if(party != null) {
			party.updateCoords(this);
		}
	}

	@Override
	public void updateEffects() {

		if(effectList == null || effectList.size() < 1) {
			return;
		}

		Array<Effect> effects = effectList.getEffects();

		Effect[] array = effects.array();

		for(int i = 0, length = effects.size(); i < length; i++) {

			Effect effect = array[i];

			if(effect == null || effect.isEnded()) {
				continue;
			}

			broadcastPacket(AppledEffect.getInstance(effect.getEffector(), effect.getEffected(), effect));
		}
	}

	/**
	 * Обновление гильдии.
	 */
	public void updateGuild() {

		sendPacket(GuildInfo.getInstance(this), true);

		if(guild == null) {
			return;
		}

		sendPacket(GuildMembers.getInstance(this), true);
		sendPacket(GuildLogs.getInstance(this), true);
	}

	@Override
	public void updateHp() {

		sendPacket(PlayerCurrentHp.getInstance(this, null, 0, PlayerCurrentHp.INCREASE), true);

		Party party = getParty();

		if(party != null) {
			party.updateStat(this);
		}

		Duel duel = getDuel();

		if(duel != null) {

			Player enemy = duel.getEnemy(this);

			if(enemy != null) {
				enemy.sendPacket(TargetHp.getInstance(this, TargetHp.RED), true);
			}
		}

		if(pvpMode) {
			broadcastPacketToOthers(TargetHp.getInstance(this, TargetHp.RED));
		}
	}

	@Override
	public void updateInfo() {
		sendPacket(UserInfo.getInstance(this), true);
	}

	@Override
	public void updateMp() {

		sendPacket(PlayerCurrentMp.getInstance(this, null, 0, PlayerCurrentMp.INCREASE), true);

		if(party != null) {
			party.updateStat(this);
		}
	}

	/**
	 * Обновление отображения для окружающих.
	 */
	public void updateOtherInfo() {

		LocalObjects local = LocalObjects.get();

		Array<Player> players = World.getAround(Player.class, local.getNextPlayerList(), this);

		Player[] array = players.array();

		for(int i = 0, length = players.size(); i < length; i++) {

			Player target = array[i];
			target.removeMe(this, DeleteCharacter.DISAPPEARS);

			removeMe(target, DeleteCharacter.DISAPPEARS);

			target.addMe(this);

			addMe(target);
		}
	}

	/**
	 * обновление квеста на панели.
	 * 
	 * @param quest обновляемый квест.
	 * @param panel новое запраиваемое состояние.
	 */
	public void updateQuestInPanel(QuestState quest, QuestPanelState panelState) {

		DataBaseManager dbManager = DataBaseManager.getInstance();

		questInPanel.writeLock();
		try {
			switch(panelState) {
				case REMOVED: {

					int index = questInPanel.indexOf(quest);

					if(index < 0) {
						return;
					}

					questInPanel.fastRemove(index);

					quest.setPanelState(QuestPanelState.REMOVED);
					dbManager.updateQuest(quest);
					break;
				}
				case ADDED: {

					if(questInPanel.contains(quest)) {
						return;
					}

					if(questInPanel.size() >= MAXIMUM_QUEST_IN_PANEL) {
						sendMessage(MessageType.QUEST_TRACKER_DISPLAYS_UP_TO_7_QUESTS);
						return;
					}

					questInPanel.add(quest);

					quest.setPanelState(QuestPanelState.ADDED);

					sendPacket(QuestMoveToPanel.getInstance(quest), true);

					dbManager.updateQuest(quest);
					break;
				}
				case ACCEPTED: {

					if(questInPanel.size() >= MAXIMUM_QUEST_IN_PANEL)
						quest.setPanelState(QuestPanelState.REMOVED);
					else {

						quest.setPanelState(QuestPanelState.ADDED);

						if(!questInPanel.contains(quest)) {
							questInPanel.add(quest);
						}

						sendPacket(QuestMoveToPanel.getInstance(quest), true);
					}

					dbManager.updateQuest(quest);
					break;
				}
				case UPDATE: {

					if(questInPanel.contains(quest)) {
						return;
					}

					if(questInPanel.size() >= MAXIMUM_QUEST_IN_PANEL) {
						return;
					}

					questInPanel.add(quest);

					quest.setPanelState(QuestPanelState.ADDED);

					sendPacket(QuestMoveToPanel.getInstance(quest), true);

					dbManager.updateQuest(quest);
					break;
				}
				case NONE:
					break;
				default:
					log.warning(this, new Exception("incorrect panel state."));
			}
		} finally {
			questInPanel.writeUnlock();
		}
	}

	/**
	 * Обновление панели квестов.
	 */
	public void updateQuestPanel() {
		/*
		 * Array<QuestState> quests = questList.getActiveQuests();
		 * 
		 * quests.readLock(); try { QuestState[] array = quests.array();
		 * 
		 * for(int i = 0, length = quests.size(); i < length; i++) { QuestState
		 * quest = array[i];
		 * 
		 * if(quest.getPanelState() == QuestPanelState.ADDED &&
		 * !questInPanel.contains(quest)) questInPanel.add(quest); } } finally {
		 * quests.readUnlock(); }
		 */
	}

	@Override
	public void updateReuse(Skill skill, int reuseDelay) {
		sendPacket(SkillReuse.getInstance(skill.getReuseId(), reuseDelay), true);
	}

	@Override
	public void updateStamina() {
		updateInfo();

		if(party != null) {
			party.updateStat(this);
		}
	}

	/**
	 * Обработка уничтожения кристалов.
	 */
	public void destroyCrystals(Character killer) {

		if(killer == this || isInBattleTerritory()) {
			return;
		}

		Equipment equipment = getEquipment();

		if(equipment != null) {
			equipment.lock();
			try {

				Slot[] slots = equipment.getSlots();

				boolean changed = false;

				for(int i = 0, length = slots.length; i < length; i++) {

					ItemInstance item = slots[i].getItem();

					if(item == null)
						continue;

					CrystalList crystals = item.getCrystals();

					if(crystals == null || crystals.isEmpty()) {
						continue;
					}

					if(changed) {
						crystals.destruction(this);
					} else {
						changed = crystals.destruction(this);
					}
				}

				if(changed) {
					ObjectEventManager eventManager = ObjectEventManager.getInstance();
					eventManager.notifyInventoryChanged(this);
				}
			} finally {
				equipment.unlock();
			}
		}
	}

	/**
	 * Обработка ПК.
	 * 
	 * @param player убийца игрока.
	 */
	public void checkPK(Player player) {

		if(!player.isPvPMode() || player.isEvent() || isPvPMode() || player.getLevel() - getLevel() < 5) {
			return;
		}

		boolean updateColor = !player.isPK();

		Array<Territory> territories = player.getTerritories();

		territories.readLock();
		try {

			Territory[] array = territories.array();

			for(int i = 0, length = territories.size(); i < length; i++) {

				Territory territory = array[i];

				switch(territory.getType()) {
					case BATTLE_TERRITORY:
						return;
					case REGION_TERRITORY: {

						RegionTerritory regionTerritory = (RegionTerritory) territory;
						Region region = regionTerritory.getRegion();

						if(region.getState() == RegionState.PREPARE_END_WAR) {
							return;
						}

						break;
					}
					default:
						continue;
				}
			}
		} finally {
			territories.readUnlock();
		}

		int karma = 100 * (player.getLevel() - getLevel());

		synchronized(player) {
			player.setKarma(player.getKarma() + karma);
		}

		player.sendMessage("Вы получили " + karma + " кармы, итоговая составляет: " + player.getKarma());

		if(updateColor) {
			player.updateColor();
			player.sendPacket(NameColor.getInstance(player.getColor(), player), true);
		}
	}

	/**
	 * Обработка дропа итемов.
	 */
	public void dropItems() {

		LocalObjects local = LocalObjects.get();

		// получаем список итемов
		Array<ItemInstance> itemList = local.getNextItemList();

		// опредкляем шанс выпадения
		int chance = 5;

		// список донат итемов
		int[] donat = Config.WORLD_DONATE_ITEMS;

		// получаем экиперовку игрока
		Equipment equipment = getEquipment();

		// выпало ли что-то из эквипа
		boolean equipDrop = false;

		// если экиперовка есть
		if(equipment != null) {
			equipment.lock();
			try {
				// получаем все слоты
				Slot[] slots = equipment.getSlots();

				// перебираем слоты
				for(int i = 0, length = slots.length; i < length; i++) {
					// получаем слот
					Slot slot = slots[i];

					// получаем итем в слоте
					ItemInstance item = slot.getItem();

					// если итема нет, либо шанс не выпал, пропускаем
					if(item == null || Arrays.contains(donat, item.getItemId()) || !Rnd.chance(chance))
						continue;

					// удаляем итем из слота
					slot.setItem(null);

					// ложим в список
					itemList.add(item);

					// помечаем что эквип дропнулся
					equipDrop = true;
				}
			} finally {
				equipment.unlock();
			}
		}

		// получаем инвентарь игрока
		Inventory inventory = getInventory();

		// флаг дропнутости итема из инвенторя
		boolean inventoryDrop = false;

		// если инвентарь есть
		if(inventory != null) {
			inventory.lock();
			try {
				// получаем все ячейки
				Cell[] cells = inventory.getCells();

				// перебираем ячейки
				for(int i = 0, length = cells.length; i < length; i++) {
					Cell cell = cells[i];

					// получаем итем в ячейке
					ItemInstance item = cell.getItem();

					// если итема нет, либо шанс не выпал, пропускаем
					if(item == null || item.isStackable() || Arrays.contains(donat, item.getItemId()) || !Rnd.chance(chance))
						continue;

					// удаляем итем из ячейки
					cell.setItem(null);

					// ложим в список
					itemList.add(item);

					inventoryDrop = true;
				}
			} finally {
				inventory.unlock();
			}
		}

		// если есть дропнутые итемы
		if(!itemList.isEmpty()) {
			// получаем менеджера БД
			DataBaseManager dbManager = DataBaseManager.getInstance();

			// получаем массив итемов
			ItemInstance[] array = itemList.array();

			// перебираем итемы
			for(int i = 0, length = itemList.size(); i < length; i++) {
				ItemInstance item = array[i];

				// зануляем владельца
				item.setOwnerId(0);

				// обновляем положение
				item.setLocation(ItemLocation.NONE);

				// указываем кто дропнул
				item.setDropper(this);

				// обновляем в БД
				dbManager.updateLocationItem(item);
			}

			Npc.spawnDropItems(this, array, itemList.size());

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			if(equipDrop)
				eventManager.notifyEquipmentChanged(this);
			else if(inventoryDrop)
				eventManager.notifyInventoryChanged(this);
		}
	}

	/**
	 * Очитска кармы при убийстве НПС.
	 * 
	 * @param npc убитый НПС.
	 */
	public void clearKarma(Npc npc) {

		if(npc.getExp() < 20)
			return;

		int clear = Math.max(Math.min(getLevel() - npc.getLevel(), 5), 0);

		clear = Math.min((5 - clear) * 50 * npc.getKarmaMod(), getKarma());

		if(clear > 0) {
			setKarma(getKarma() - clear);
			sendMessage("Вы очистили " + clear + " кармы, у вас осталось " + getKarma() + ".");
		}

		if(!isPK()) {
			updateColor();
			sendPacket(NameColor.getInstance(getColor(), this), true);
		}
	}

	@Override
	public boolean isRangeClass() {
		return getPlayerClass().isRange();
	}

	@Override
	public boolean isPK() {
		return getKarma() > 0;
	}
}
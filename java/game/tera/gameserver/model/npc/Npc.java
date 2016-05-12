package tera.gameserver.model.npc;

import java.util.Comparator;

import rlib.geom.Angles;
import rlib.geom.Coords;
import rlib.util.Rnd;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import rlib.util.random.Random;
import tera.Config;
import tera.gameserver.IdFactory;
import tera.gameserver.manager.EventManager;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.manager.RandomManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.EmotionType;
import tera.gameserver.model.Party;
import tera.gameserver.model.World;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.ai.CharacterAI;
import tera.gameserver.model.geom.Geom;
import tera.gameserver.model.geom.NpcGeom;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.interaction.DialogData;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.playable.NpcAppearance;
import tera.gameserver.model.npc.spawn.Spawn;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.NpcIconType;
import tera.gameserver.model.quests.QuestData;
import tera.gameserver.model.quests.QuestType;
import tera.gameserver.model.regenerations.NpcRegenHp;
import tera.gameserver.model.regenerations.NpcRegenMp;
import tera.gameserver.model.regenerations.Regen;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.gameserver.network.serverpackets.NameColor;
import tera.gameserver.network.serverpackets.NpcInfo;
import tera.gameserver.network.serverpackets.NpcNotice;
import tera.gameserver.network.serverpackets.QuestNpcNotice;
import tera.gameserver.network.serverpackets.TargetHp;
import tera.gameserver.tables.SkillTable;
import tera.gameserver.taskmanager.RegenTaskManager;
import tera.gameserver.tasks.EmotionTask;
import tera.gameserver.tasks.TurnTask;
import tera.gameserver.templates.NpcTemplate;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;
import tera.util.Location;

/**
 * Базовая модель нпс.
 * 
 * @author Ronn
 */
public abstract class Npc extends Character implements Foldable {

	/** таблица штрафа на экспу в зависимости от разницы в уровнях */
	public static final float[] PENALTY_EXP = {
		1F, // 0
		1F, // 1
		1F, // 2
		1F, // 3
		1F, // 4
		1F, // 5
		0.5F, // 6
		0.4F, // 7
		0.3F, // 8
		0.2F, // 9
		0.1F, // 10
		0F, // 12
	};

	public static final int INTERACT_RANGE = 200;

	/**
	 * public static final float[] PENALTY_EXP = { 1F, // 0 1F, // 1 1F, // 2
	 * 1F, // 3 1F, // 4 1F, // 5 0.6F, // 6 0.5F, // 7 0.4F, // 8 0.35F, // 9
	 * 0.3F, // 10 0.25F, // 11 0.2F, // 12 0.15F, // 13 0.1F, // 14 0.08F, //
	 * 15 0.06F, // 16 0.04F, // 17 0.02F, // 18 0.01F, // 19 0F, // 20 };
	 */

	/** сортировщик аггресоров по уровню агрессии */
	private static final Comparator<AggroInfo> AGGRO_COMPORATOR = new Comparator<AggroInfo>() {

		@Override
		public int compare(AggroInfo info, AggroInfo next) {

			if(info == null) {
				return 1;
			}

			if(next == null) {
				return -1;
			}

			return next.compareTo(info);
		}
	};

	/**
	 * Спавн объектов вокруг объекта.
	 * 
	 * @param character центральный объект.
	 * @param items список объектов, которые нужно отспавнить.
	 * @param length кол-во объектов.
	 * @param radius радиус, в котором нужно отспавнить объекты.
	 */
	public static void spawnDropItems(Character character, ItemInstance[] items, int length) {

		if(length < 1) {
			return;
		}

		RandomManager randManager = RandomManager.getInstance();
		Random random = randManager.getDropItemPointRandom();

		float x = character.getX();
		float y = character.getY();
		float z = character.getZ();

		int continentId = character.getContinentId();

		GeoManager geoManager = GeoManager.getInstance();

		for(int i = 1; i <= length; i++) {

			float radians = Angles.headingToRadians(random.nextInt(0, Short.MAX_VALUE * 2));

			int distance = random.nextInt(40, 80);

			float newX = Coords.calcX(x, distance, radians);
			float newY = Coords.calcY(y, distance, radians);
			float newZ = geoManager.getHeight(continentId, newX, newY, z);

			ItemInstance item = items[i - 1];

			item.setContinentId(continentId);
			item.spawnMe(newX, newY, newZ, 0);
		}
	}

	/** пул контейнеров информации об агре */
	protected final FoldablePool<AggroInfo> aggroInfoPool;

	/** аггро лист */
	protected final Array<AggroInfo> aggroList;

	/** обработчик разворота нпс */
	protected final TurnTask turnTask;

	/** спавнер */
	protected Spawn spawn;

	/** точка спавна */
	protected Location spawnLoc;

	/** таблица скилов НПС */
	protected Skill[][] skills;

	/** отсортирован ли агро лист */
	protected volatile boolean aggroSorted;

	/**
	 * @param objectId уникальный ид.
	 * @param template темплейт нпс.
	 */
	public Npc(int objectId, NpcTemplate template) {
		super(objectId, template);

		aggroInfoPool = Pools.newConcurrentFoldablePool(AggroInfo.class);
		aggroList = Arrays.toConcurrentArray(AggroInfo.class);

		turnTask = new TurnTask(this);

		SkillTemplate[][] temps = template.getSkills();

		skills = new Skill[temps.length][];

		for(int i = 0, length = temps.length; i < length; i++) {

			SkillTemplate[] list = temps[i];

			if(list == null) {
				continue;
			}

			skills[i] = SkillTable.create(list);

			addSkills(skills[i], false);
		}

		Formulas formulas = Formulas.getInstance();
		formulas.addFuncsToNewNpc(this);

		RegenTaskManager regenManager = RegenTaskManager.getInstance();
		regenManager.addCharacter(this);
	}

	/**
	 * Добавление агрессии на персонажа.
	 * 
	 * @param aggressor агрессор.
	 * @param aggro агр поинты.
	 * @param damage урон ли это.
	 */
	public void addAggro(Character aggressor, long aggro, boolean damage) {

		if(aggro < 1)
			return;

		aggressor.addHated(this);

		aggro *= aggressor.calcStat(StatType.AGGRO_MOD, 1, this, null);

		Array<AggroInfo> aggroList = getAggroList();

		aggroList.writeLock();
		try {

			int index = aggroList.indexOf(aggressor);

			if(index < 0) {
				aggroList.add(newAggroInfo(aggressor, aggro, damage ? aggro : 0));
			} else {

				AggroInfo info = aggroList.get(index);

				info.addAggro(aggro);

				if(damage) {
					info.addDamage(Math.min(aggro, getCurrentHp()));
				}
			}

			setAggroSorted(index == 0);
		} finally {
			aggroList.writeUnlock();
		}

		ObjectEventManager eventManager = ObjectEventManager.getInstance();
		eventManager.notifyAgression(this, aggressor, aggro);
	}

	@Override
	public void addMe(Player player) {

		player.sendPacket(NpcInfo.getInstance(this, player), true);

		if(isBattleStanced()) {
			PacketManager.showBattleStance(player, this, getEnemy());
		}

		ObjectEventManager eventManager = ObjectEventManager.getInstance();
		eventManager.notifyAddNpc(player, this);

		super.addMe(player);
	}

	/**
	 * Рассчет выдачи экспы.
	 * 
	 * @param killer убийца нпс.
	 */
	protected void calculateRewards(Character killer) {

		Character top = getMostDamager();

		if(top == null) {
			top = killer;
		}

		if(top.isPK())
			return;

		if(top.isSummon()) {
			top = top.getOwner();
		}

		if(top == null || !top.isPlayer()) {
			return;
		}

		NpcTemplate template = getTemplate();

		int exp = (int) (template.getExp() * Config.SERVER_RATE_EXP);

		Player player = top.getPlayer();

		if(exp > 0) {

			Party party = player.getParty();

			if(party != null)
				party.addExp(exp, player, this);
			else {

				float reward = exp;

				int diff = Math.abs(player.getLevel() - getLevel());

				if(diff >= PENALTY_EXP.length) {
					reward *= 0F;
				} else if(diff > 5) {
					reward *= PENALTY_EXP[diff];
				}

				if(Config.ACCOUNT_PREMIUM_EXP && player.hasPremium()) {
					reward *= Config.ACCOUNT_PREMIUM_EXP_RATE;
				}

				player.addExp((int) reward, this, getName());
			}
		}

		if(template.isCanDrop()) {

			LocalObjects local = LocalObjects.get();

			Array<ItemInstance> items = template.getDrop(local.getNextItemList(), this, player);

			if(items != null) {

				Party party = player.getParty();

				ItemInstance[] array = items.array();

				for(int i = 0, length = items.size(); i < length; i++) {

					ItemInstance item = array[i];

					item.setDropper(this);
					item.setTempOwner(player);
					item.setTempOwnerParty(party);
				}

				spawnDropItems(this, array, items.size());
			}
		}
	}

	/**
	 * Проверка на возможность разговора игрока с нпс.
	 * 
	 * @param player игрок, который хочет взаимодействовать с нпс.
	 * @return может ли игрок взаимодействовать.
	 */
	public boolean checkInteraction(Player player) {
		return isInRange(player, INTERACT_RANGE);
	}

	@Override
	public boolean checkTarget(Character target) {
		return true;
	}

	/**
	 * Полная очистка аггр листа.
	 */
	public void clearAggroList() {

		Array<AggroInfo> aggroList = getAggroList();

		FoldablePool<AggroInfo> pool = getAggroInfoPool();

		aggroList.writeLock();
		try {

			AggroInfo[] array = aggroList.array();

			for(int i = 0, length = aggroList.size(); i < length; i++) {

				AggroInfo info = array[i];

				Character aggressor = info.getAggressor();
				aggressor.removeHate(this);

				pool.put(info);
			}

			aggroList.clear();
		} finally {
			aggroList.writeUnlock();
		}

		setAggroSorted(true);
	}

	@Override
	public void decayMe(int type) {
		super.decayMe(type);
		clearAggroList();
	}

	@Override
	public void deleteMe() {

		CharacterAI ai = getAI();

		if(ai != null) {
			ai.stopAITask();
		}

		super.deleteMe();
	}

	/**
	 * Увеличение счетчика убийств.
	 * 
	 * @param attacker убийца.
	 */
	protected void addCounter(Character attacker) {

		World.addKilledNpc();

		if(attacker != null) {
			attacker.addPvECount();
			if(attacker.isPK() && attacker.isPlayer()) {
				Player player = attacker.getPlayer();
				player.clearKarma(this);
			}
		}
	}

	@Override
	public void doDie(Character attacker) {

		addCounter(attacker);

		synchronized(this) {
			if(isSpawned())
				calculateRewards(attacker);

			super.doDie(attacker);

			deleteMe(DeleteCharacter.DEAD);
		}

		Spawn spawn = getSpawn();

		if(spawn != null) {
			spawn.doDie(this);
		}
	}

	@Override
	public void doOwerturn(Character attacker) {

		if(isOwerturned()) {
			return;
		}

		super.doOwerturn(attacker);

		float radians = Angles.degreeToRadians(Angles.headingToDegree(heading) + 180);

		NpcTemplate template = getTemplate();

		int distance = template.getOwerturnDist();

		float newX = Coords.calcX(x, distance, radians);
		float newY = Coords.calcY(y, distance, radians);

		GeoManager geoManager = GeoManager.getInstance();

		float newZ = geoManager.getHeight(getContinentId(), newX, newY, getZ());

		setXYZ(newX, newY, newZ);

		owerturnTask.nextOwerturn(template.getOwerturnTime());
	}

	@Override
	public void finalyze() {
	}

	/**
	 * Получить кол-во агра на персонажа.
	 * 
	 * @param aggressor агрессор.
	 * @return кол-во агра.
	 */
	public long getAggro(Character aggressor) {

		Array<AggroInfo> aggroList = getAggroList();

		aggroList.writeLock();
		try {

			int index = aggroList.indexOf(aggressor);

			if(index < 0) {
				return -1;
			}

			AggroInfo info = aggroList.get(index);
			return info.getAggro();
		} finally {
			aggroList.writeUnlock();
		}
	}

	/**
	 * @return пул контейнеров информации об агре.
	 */
	protected FoldablePool<AggroInfo> getAggroInfoPool() {
		return aggroInfoPool;
	}

	/**
	 * @return список агрессоров.
	 */
	public final Array<AggroInfo> getAggroList() {
		return aggroList;
	}

	/**
	 * @return радиус агра нпс.
	 */
	public final int getAggroRange() {
		return getTemplate().getAggro();
	}

	@Override
	public final CharacterAI getAI() {
		return (CharacterAI) ai;
	}

	@Override
	public final int getAttack(Character attacked, Skill skill) {
		return (int) calcStat(StatType.ATTACK, getTemplate().getAttack(), attacked, skill);
	}

	@Override
	protected EmotionType[] getAutoEmotions() {
		return EmotionTask.MONSTER_TYPES;
	}

	@Override
	public final int getBalance(Character attacker, Skill skill) {
		return (int) calcStat(StatType.BALANCE, getTemplate().getBalance(), attacker, skill);
	}

	@Override
	public final int getDefense(Character attacker, Skill skill) {
		return (int) calcStat(StatType.DEFENSE, getTemplate().getDefense(), attacker, skill);
	}

	/**
	 * @return конечное направление.
	 */
	public final int getEndHeading() {
		return turnTask.getEndHeading();
	}

	/**
	 * @return базовый получаемый опыт с нпс.
	 */
	public final int getExp() {
		return getTemplate().getExp();
	}

	/**
	 * @return название фракции нпс.
	 */
	public final String getFraction() {
		return getTemplate().getFactionId();
	}

	/**
	 * @return радиус фракции нпс.
	 */
	public final int getFractionRange() {
		return getTemplate().getFactionRange();
	}

	@Override
	public final int getImpact(Character attacked, Skill skill) {
		return (int) calcStat(StatType.IMPACT, getTemplate().getImpact(), attacked, skill);
	}

	@Override
	public int getLevel() {
		return getTemplate().getLevel();
	}

	/**
	 * @param player игрок, запрашивающий диалог.
	 * @return набор ссылок.
	 */
	public final Array<Link> getLinks(Player player) {

		NpcTemplate template = getTemplate();

		LocalObjects local = LocalObjects.get();

		Array<Link> links = local.getNextLinkList();

		EventManager eventManager = EventManager.getInstance();
		eventManager.addLinks(links, this, player);

		DialogData dialog = template.getDialog();

		if(dialog != null)
			dialog.addLinks(links, this, player);

		QuestData quests = template.getQuests();
		quests.addLinks(links, this, player);

		return links;
	}

	/**
	 * @return лидер этого миниона.
	 */
	public MinionLeader getMinionLeader() {
		return null;
	}

	/**
	 * @return персонаж, который само много надэмажил.
	 */
	public Character getMostDamager() {

		Array<AggroInfo> aggroList = getAggroList();

		if(aggroList.isEmpty()) {
			return null;
		}

		Character top = null;

		aggroList.readLock();
		try {

			AggroInfo[] array = aggroList.array();

			long damage = -1;

			for(int i = 0, length = aggroList.size(); i < length; i++) {

				AggroInfo info = array[i];

				if(info == null) {
					continue;
				}

				if(info.getDamage() > damage) {
					top = info.getAggressor();
					damage = info.getDamage();
				}
			}
		} finally {
			aggroList.readUnlock();
		}

		return top;
	}

	/**
	 * @return приоритетная цель нпс.
	 */
	public Character getMostHated() {

		Array<AggroInfo> aggroList = getAggroList();

		if(aggroList.isEmpty()) {
			return null;
		}

		if(!isAggroSorted()) {
			aggroList.sort(AGGRO_COMPORATOR);
			setAggroSorted(true);
		}

		AggroInfo top = aggroList.first();

		return top != null ? top.getAggressor() : null;
	}

	@Override
	public final String getName() {
		return getTemplate().getName();
	}

	@Override
	public Npc getNpc() {
		return this;
	}

	/**
	 * @return тип НПС.
	 */
	public final NpcType getNpcType() {
		return getTemplate().getNpcType();
	}

	@Override
	public int getOwerturnId() {
		return 0x482DEB16;
	}

	/**
	 * @return случайный скил из указанной группы.
	 */
	public Skill getRandomSkill(SkillGroup group) {
		Skill[] list = skills[group.ordinal()];
		return list == null || list.length < 1 ? null : list[Rnd.nextInt(0, list.length - 1)];
	}

	/**
	 * Получить первый доступный скил указанной группы.
	 * 
	 * @param group группа скилов.
	 * @return первый доступный в не откате скил.
	 */
	public Skill getFirstEnabledSkill(SkillGroup group) {

		Skill[] array = skills[group.ordinal()];

		if(array.length > 0) {
			for(Skill skill : array) {
				if(!isSkillDisabled(skill)) {
					return skill;
				}
			}
		}

		return null;
	}

	/**
	 * @return спанер нпс.
	 */
	public final Spawn getSpawn() {
		return spawn;
	}

	/**
	 * @return точка спавна.
	 */
	public final Location getSpawnLoc() {
		return spawnLoc;
	}

	@Override
	public final int getSubId() {
		return Config.SERVER_NPC_SUB_ID;
	}

	@Override
	public final NpcTemplate getTemplate() {
		return (NpcTemplate) template;
	}

	/**
	 * @return есть ли у нпс диалог.
	 */
	public final boolean hasDialog() {
		return getTemplate().getDialog() != null;
	}

	/**
	 * @return аггресивный ли нпс.
	 */
	public final boolean isAggressive() {
		return getTemplate().getAggro() > 0;
	}

	/**
	 * @return отсортирован ли аггро лист.
	 */
	public final boolean isAggroSorted() {
		return aggroSorted;
	}

	/**
	 * @return является ли НПС дружелюбным.
	 */
	public boolean isFriendNpc() {
		return false;
	}

	/**
	 * @return является ли НПС гвардом.
	 */
	public boolean isGuard() {
		return false;
	}

	/**
	 * @return является ли НПС минионом.
	 */
	public boolean isMinion() {
		return false;
	}

	/**
	 * @return является ли НПС лидером минионов.
	 */
	public boolean isMinionLeader() {
		return false;
	}

	/**
	 * @return является ли НПС монстром.
	 */
	public boolean isMonster() {
		return false;
	}

	@Override
	public final boolean isNpc() {
		return true;
	}

	/**
	 * @return является ли НПС РБ.
	 */
	public boolean isRaidBoss() {
		return false;
	}

	/**
	 * @return находится ли нпс в процессе разворота.
	 */
	public boolean isTurner() {
		return turnTask.isTurner();
	}

	/**
	 * @param aggressor агрессор.
	 * @param aggro уровень агрессии.
	 * @param damage нанесенный урон.
	 * @return новый контейнер информации об агре.
	 */
	protected AggroInfo newAggroInfo(Character aggressor, long aggro, long damage) {

		AggroInfo info = aggroInfoPool.take();

		if(info == null)
			info = new AggroInfo();

		info.setAggressor(aggressor);
		info.setAggro(aggro);
		info.setDamage(damage);

		return info;
	}

	@Override
	protected Geom newGeomCharacter() {
		NpcTemplate template = getTemplate();
		return new NpcGeom(this, template.getGeomHeight(), template.getGeomRadius());
	}

	@Override
	protected Regen newRegenHp() {
		return new NpcRegenHp(this);
	}

	@Override
	protected Regen newRegenMp() {
		return new NpcRegenMp(this);
	}

	/**
	 * Развернуть нпс до указанного направления.
	 * 
	 * @param newHeading новое направление.
	 */
	public void nextTurn(int newHeading) {
		turnTask.nextTurn(newHeading);
	}

	@Override
	public void reinit() {
		IdFactory idFactory = IdFactory.getInstance();
		objectId = idFactory.getNextNpcId();
	}

	/**
	 * Удалить персонажа с аггр листа.
	 * 
	 * @param agressor удаляемый персонаж.
	 */
	public void removeAggro(Character agressor) {

		Array<AggroInfo> aggroList = getAggroList();

		aggroList.writeLock();
		try {

			int index = aggroList.indexOf(agressor);

			if(index >= 0) {

				AggroInfo aggroInfo = aggroList.get(index);

				long aggro = aggroInfo.getAggro();

				agressor.removeHate(this);

				aggroInfoPool.put(aggroInfo);

				aggroList.fastRemove(index);

				setAggroSorted(index != 0);

				ObjectEventManager eventManager = ObjectEventManager.getInstance();
				eventManager.notifyAgression(this, agressor, -aggro);
			}
		} finally {
			aggroList.writeUnlock();
		}
	}

	@Override
	public void removeMe(Player player, int type) {
		player.sendPacket(DeleteCharacter.getInstance(this, type), true);
	}

	/**
	 * @param aggroSorted отсортирован ли список агрессоров.
	 */
	public final void setAggroSorted(boolean aggroSorted) {
		this.aggroSorted = aggroSorted;
	}

	/**
	 * @param spawn спавнер нпс.
	 */
	public final void setSpawn(Spawn spawn) {
		this.spawn = spawn;
	}

	/**
	 * @param spawnLoc точка спавна.
	 */
	public final void setSpawnLoc(Location spawnLoc) {
		this.spawnLoc = spawnLoc;
	}

	@Override
	public void spawnMe() {
		super.spawnMe();

		World.addSpawnedNpc();
	}

	@Override
	public void spawnMe(Location loc) {
		setSpawnLoc(loc);

		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());

		super.spawnMe(loc);

		WorldRegion region = getCurrentRegion();

		if(region != null && region.isActive()) {
			getAI().startAITask();
			emotionTask.start();
		}
	}

	@Override
	public boolean startBattleStance(Character enemy) {

		if(enemy != null && enemy != getEnemy() || enemy == null && isBattleStanced()) {
			PacketManager.showBattleStance(this, enemy);
		}

		setBattleStanced(enemy != null);
		setEnemy(enemy);

		return true;
	}

	@Override
	public void stopBattleStance() {
		setBattleStanced(false);
		broadcastPacketToOthers(NpcNotice.getInstance(this, 0, 0));
	}

	/**
	 * Уменьшени агрессии.
	 * 
	 * @param aggressor агрессор.
	 * @param aggro агр поинты.
	 */
	public void subAggro(Character aggressor, long aggro) {

		Array<AggroInfo> aggroList = getAggroList();

		aggroList.writeLock();
		try {

			int index = aggroList.indexOf(aggressor);

			if(index > -1) {

				AggroInfo info = aggroList.get(index);
				info.subAggro(aggro);

				if(info.getAggro() < 1) {
					aggroList.fastRemove(index);
					aggressor.removeHate(this);
					aggroInfoPool.put(info);
				}

				setAggroSorted(index != 0);

				ObjectEventManager eventManager = ObjectEventManager.getInstance();
				eventManager.notifyAgression(this, aggressor, aggro * -1);
			}
		} finally {
			aggroList.writeUnlock();
		}
	}

	@Override
	public void teleToLocation(int continentId, float x, float y, float z, int heading) {
		decayMe(DeleteCharacter.DISAPPEARS);
		super.teleToLocation(continentId, x, y, z, heading);
		spawnMe(getSpawnLoc());
	}

	@Override
	public String toString() {
		return "NpcInstance  id = " + getTemplateId() + ", type = " + getTemplateType();
	}

	@Override
	public void updateHp() {

		TargetHp packet = TargetHp.getInstance(this, TargetHp.RED);

		Array<AggroInfo> aggroList = getAggroList();

		aggroList.readLock();
		try {

			AggroInfo[] array = aggroList.array();

			for(int i = 0, length = aggroList.size(); i < length; i++) {

				Character aggressor = array[i].getAggressor();

				if(aggressor != null && (aggressor.isPlayer() || aggressor.isSummon())) {
					packet.increaseSends();
				}
			}

			for(int i = 0, length = aggroList.size(); i < length; i++) {

				Character aggressor = array[i].getAggressor();

				if(aggressor != null) {
					if(aggressor.isPlayer()) {
						aggressor.sendPacket(packet, false);
					} else if(aggressor.isSummon() && aggressor.getOwner() != null) {
						aggressor.getOwner().sendPacket(packet, false);
					}
				}
			}
		} finally {
			aggroList.readUnlock();
		}
	}

	/**
	 * @param player игрок.
	 */
	public void updateQuestInteresting(Player player, boolean delete) {

		if(player == null) {
			log.warning(this, new Exception("not found player"));
			return;
		}

		QuestData quests = getTemplate().getQuests();

		QuestType type = quests.hasQuests(this, player);

		if(type == null && delete)
			player.sendPacket(QuestNpcNotice.getInstance(this, NpcIconType.NONE), true);
		else if(type != null) {
			switch(type) {
				case STORY_QUEST:
					player.sendPacket(QuestNpcNotice.getInstance(this, NpcIconType.RED_NOTICE), true);
					break;
				case LEVEL_UP_QUEST:
				case ZONE_QUEST:
					player.sendPacket(QuestNpcNotice.getInstance(this, NpcIconType.YELLOW_NOTICE), true);
					break;
				case GUILD_QUEST:
					player.sendPacket(QuestNpcNotice.getInstance(this, NpcIconType.BLUE_NOTICE), true);
					break;
				case DEALY_QUEST:
					player.sendPacket(QuestNpcNotice.getInstance(this, NpcIconType.GREEN_NOTICE), true);
					break;
			}
		}
	}

	/**
	 * Является ли дружественным для указанного игрока.
	 * 
	 * @param player игрок.
	 * @return является ли дружественным.
	 */
	public boolean isFriend(Player player) {
		return isFriendNpc();
	}

	/**
	 * @return модификатор отмытия кармы.
	 */
	public int getKarmaMod() {
		return 1;
	}

	@Override
	public boolean isOwerturnImmunity() {
		return getTemplate().isOwerturnImmunity();
	}

	/**
	 * Будет ли цель спереди после разворота НПС.
	 * 
	 * @param target проверяемая цель.
	 * @return будет ли она спереди.
	 */
	public boolean isInTurnFront(Character target) {

		if(target == null)
			return false;

		float dx = target.getX() - getX();
		float dy = target.getY() - getY();

		int head = (int) (Math.atan2(-dy, -dx) * HEADINGS_IN_PI + 32768);

		head = turnTask.getEndHeading() - head;

		if(head < 0)
			head = head + 1 + Integer.MAX_VALUE & 0xFFFF;
		else if(head > 0xFFFF)
			head &= 0xFFFF;

		return head != -1 && head <= 8192 || head >= 57344;
	}

	/**
	 * @return внешность НПС.
	 */
	public NpcAppearance getAppearance() {
		return null;
	}

	/**
	 * @return цвет имени НПС.
	 */
	public int getNameColor() {
		return NameColor.COLOR_NORMAL;
	}

	/**
	 * Завершение отображения смерти.
	 */
	public void finishDead() {
	}

	@Override
	public boolean isBroadcastEndSkillForCollision() {
		return true;
	}

	/**
	 * @return маршрут патрулирования.
	 */
	public Location[] getRoute() {
		return getSpawn().getRoute();
	}
}
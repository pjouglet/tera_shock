package tera.gameserver.model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import rlib.concurrent.Locks;
import rlib.geom.Angles;
import rlib.geom.Coords;
import rlib.geom.Geometry;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Strings;
import rlib.util.Synchronized;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.random.Random;
import rlib.util.table.FuncValue;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import rlib.util.wraps.Wrap;
import tera.Config;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.manager.RandomManager;
import tera.gameserver.model.ai.AI;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.geom.Geom;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.listeners.DamageListener;
import tera.gameserver.model.listeners.DieListener;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.regenerations.Regen;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.skillengine.Calculator;
import tera.gameserver.model.skillengine.ChanceType;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.skillengine.OperateType;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillName;
import tera.gameserver.model.skillengine.SkillType;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.model.skillengine.funcs.StatFunc;
import tera.gameserver.model.skillengine.funcs.chance.ChanceFunc;
import tera.gameserver.network.serverpackets.CharMove;
import tera.gameserver.network.serverpackets.CharSay;
import tera.gameserver.network.serverpackets.Damage;
import tera.gameserver.network.serverpackets.ServerPacket;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.tasks.EmotionTask;
import tera.gameserver.tasks.MoveNextTask;
import tera.gameserver.tasks.OwerturnTask;
import tera.gameserver.tasks.SkillCastTask;
import tera.gameserver.tasks.SkillMoveTask;
import tera.gameserver.tasks.SkillUseTask;
import tera.gameserver.templates.CharTemplate;
import tera.gameserver.templates.ItemTemplate;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;
import tera.util.Location;

/**
 * Модель персонажа.
 * 
 * @author Ronn
 */
public abstract class Character extends TObject implements Synchronized {

	/** логер */
	protected static final Logger log = Loggers.getLogger(Character.class);

	protected static final FuncValue<ReuseSkill> FUNC_REUSE_SKILL_FOLD = new FuncValue<ReuseSkill>() {

		@Override
		public void apply(ReuseSkill value) {
			value.fold();
		}
	};

	protected static final FuncValue<Wrap> FUNC_SKILL_VAR_FOLD = new FuncValue<Wrap>() {

		@Override
		public void apply(Wrap value) {
			value.fold();
		}
	};

	/** блокировщики */
	protected final Lock charLock;
	protected final Lock readStatLock;
	protected final Lock writeStatLock;

	/** темплейт перса */
	protected final CharTemplate template;

	/** имя */
	protected String name;
	/** титул */
	protected String title;

	/** текущий ид каста */
	protected int castId;
	/** состояние кастуемого скила */
	protected int chargeLevel;

	/** тикущее хп */
	protected volatile int currentHp;
	/** текущее мп */
	protected volatile int currentMp;

	/** время последнего каста скила */
	protected long lastCast;

	/** находится ли в защитном режиме */
	protected volatile boolean defenseStance;
	/** находится верхом на каком-то транспорте */
	protected volatile boolean mounted;
	/** уязвим ли персонаж */
	protected volatile boolean invul;
	/** находится ли в движении */
	protected volatile boolean moving;
	/** находится ли персонаж в боевой стойке */
	protected volatile boolean battleStanced;
	/** опрокидан ли */
	protected volatile boolean owerturned;
	/** оглушен ли персонаж */
	protected volatile boolean stuned;
	/** заблокировано ли двиежние */
	protected volatile boolean rooted;
	/** находится ли в движении от скила */
	protected volatile boolean skillMoved;
	/** заблокировано использование скилов */
	protected volatile boolean skillBlocking;
	/** летит ли на пегасе */
	protected volatile boolean flyingPegas;
	/** отспавнен ли уже самон */
	protected volatile boolean spawned;

	/** ИИ персонажа */
	protected AI ai;

	/** цель */
	protected volatile TObject enemy;
	/** цели персанажа */
	protected volatile Character target;
	/** вызванный суммон */
	protected volatile Summon summon;

	/** геометрия персонажа */
	protected final Geom geom;

	/** обработчики регена */
	protected final Regen regenHp;
	protected final Regen regenMp;

	/** обработчик авто эмоций */
	protected final EmotionTask emotionTask;
	/** обработчик перемещения персонажа */
	protected final MoveNextTask moveNextTask;
	/** обрабочик использования скила */
	protected final SkillUseTask skillUseTask;
	/** обработчик каста скила */
	protected final SkillCastTask skillCastTask;
	/** обработчик перемещение во время каста скила */
	protected final SkillMoveTask skillMoveTask;
	/** оброботчик времени опрокидывания */
	protected final OwerturnTask owerturnTask;

	/** эффект лист персонажа */
	protected final EffectList effectList;

	/** калькуляторы статов */
	protected final Calculator[] calcs;

	/** скилы */
	protected final Table<IntKey, Skill> skills;
	/** откаты скилов */
	protected final Table<IntKey, ReuseSkill> reuseSkills;

	/** список нпс, которых агрли персонаж */
	protected final Array<Npc> hateList;
	/** набор шансовых функций */
	protected final Array<ChanceFunc> chanceFuncs;
	/** слушатели получения урона */
	protected final Array<DamageListener> damageListeners;
	/** слушатели смерти */
	protected final Array<DieListener> dieListeners;

	/** переменные скилов */
	protected volatile Table<IntKey, Wrap> skillVariables;

	/** кастуемый скил */
	protected volatile Skill castingSkill;
	protected volatile Skill activateSkill;
	protected volatile Skill lockOnSkill;
	protected volatile Skill chargeSkill;

	/** имя последнего скастанувшегося скила */
	protected SkillName lastSkillName;

	/**
	 * @param objectId уникальный ид персонажа.
	 * @param template темплейт персонажа.
	 */
	public Character(int objectId, CharTemplate template) {
		super(objectId);

		// темлпейт персонажа
		this.template = template;
		// хп/мп персонажа
		this.currentHp = 1;
		this.currentMp = 1;

		// Направление персонажа
		this.heading = 0;
		// имя персонажа по умолчанию
		this.name = Strings.EMPTY;
		// титул персонажа по умолчанию
		this.title = Strings.EMPTY;

		// создаем геометрию персонажа
		this.geom = newGeomCharacter();

		// создаем обработчики регена
		this.regenHp = newRegenHp();
		this.regenMp = newRegenMp();

		// создаем массив калкуляторов
		this.calcs = new Calculator[StatType.SIZE];

		// создаем обработчика перемещения персонажа
		this.moveNextTask = new MoveNextTask(this);
		// создаем обработчика приминения скилов
		this.skillUseTask = new SkillUseTask(this);
		// создаем обработчика каста скилов
		this.skillCastTask = new SkillCastTask(this);
		// создаем обработчика движения во время каста скилов
		this.skillMoveTask = new SkillMoveTask(this);
		// создаем обработчик запуска авто эмоций
		this.emotionTask = new EmotionTask(this, getAutoEmotions());
		// создаем обработчик времени опрокидывания
		this.owerturnTask = new OwerturnTask(this);

		ReadWriteLock lock = Locks.newRWLock();

		// создаем набор блокировщиков
		this.readStatLock = lock.readLock();
		this.writeStatLock = lock.writeLock();
		this.charLock = Locks.newLock();

		// создаем список эффектов
		this.effectList = EffectList.newInstance(this);
		// список сагренных нпс
		this.hateList = Arrays.toConcurrentArray(Npc.class);
		// список шансовых функций
		this.chanceFuncs = Arrays.toConcurrentArray(ChanceFunc.class);
		// список слушателей урона
		this.damageListeners = Arrays.toConcurrentArray(DamageListener.class);
		// список слушателей смерти персонажа
		this.dieListeners = Arrays.toConcurrentArray(DieListener.class);
		// таблица отктаов скилов
		this.reuseSkills = Tables.newConcurrentIntegerTable();
		// таблица скилов персонажа
		this.skills = Tables.newConcurrentIntegerTable();

		Formulas formulas = Formulas.getInstance();

		formulas.addFuncsToNewCharacter(this);

		Func[] funcs = template.getFuncs();

		for(int i = 0, length = funcs.length; i < length; i++)
			funcs[i].addFuncTo(this);
	}

	/**
	 * Обработка отмены использования скила.
	 */
	public void abortCast(boolean force) {
		if(Config.DEVELOPER_MAIN_DEBUG)
			sendMessage("abort cast skill");

		// прерываем приминение скила
		skillUseTask.cancel(force);
		// прерываем каст скила
		skillCastTask.cancel(force);
		// прерываем перемещение скилом
		skillMoveTask.cancel(force);

		// забываем кастующийся скил
		castingSkill = null;

		// отменяем активированный скил
		if(activateSkill != null) {
			activateSkill.endSkill(this, x, y, z, force);
			activateSkill = null;
		}
	}

	/**
	 * Отмена сбора.
	 */
	public void abortCollect() {
		log.warning(this, new Exception("unsupperted method."));
	}

	/**
	 * Увеличение счетчика атак скилом.
	 */
	public void addAttackCounter() {
	}

	/**
	 * @param func добавляемая шансовая функция.
	 */
	public void addChanceFunc(ChanceFunc func) {
		chanceFuncs.add(func);
	}

	/**
	 * @param listener слушатель урона.
	 */
	public void addDamageListener(DamageListener listener) {
		damageListeners.add(listener);
	}

	/**
	 * Увеличение счетчика отбитых атак.
	 */
	public void addDefenseCounter() {
	}

	/**
	 * @param listener слушатель смерти.
	 */
	public void addDieListener(DieListener listener) {
		dieListeners.add(listener);
	}

	/**
	 * Добавление нового эффекта персонажу.
	 * 
	 * @param effect новый эффект.
	 */
	public final void addEffect(Effect effect) {
		// если эффект не добавлен
		if(!effectList.addEffect(effect))
			// складываем в пул
			effect.fold();
		else {
			// получаем менеджер событий
			ObjectEventManager manager = ObjectEventManager.getInstance();

			// собщаем об наложении эффекта
			manager.notifyAppliedEffect(this, effect);
		}
	}

	/**
	 * Метод, добавляющий опыт игроку.
	 * 
	 * @param added кол-во добавленного опыта.
	 * @param object объект, с которого был получен опыт.
	 * @param creator кто выдал экспу.
	 */
	public void addExp(int added, TObject object, String creator) {
	}

	/**
	 * Добавление сагринного нпс.
	 * 
	 * @param hated сагренный нпс.
	 */
	public void addHated(Npc hated) {
		if(!hateList.contains(hated))
			hateList.add(hated);
	}

	/**
	 * Добавление таргета для скила.
	 * 
	 * @param target цель.
	 * @param skill скил.
	 */
	public boolean addLockOnTarget(Character target, Skill skill) {
		return false;
	}

	@Override
	public void addMe(Player player) {
		// обновляем перемещние для игрока
		moveNextTask.update(player);

		// TODO в этом месте дедлок, из-за того, что scheduleEffect вызывается
		// из синхронизированного эффект листа
		// отображение висящих эффектов, если они есть
		/*
		 * if(effectList.size() > 0) { effectList.lock(); try { // список всех
		 * эффектов Array<Effect> effects = effectList.getEffects();
		 * 
		 * // массив для быстрого перебора Effect[] array = effects.array();
		 * 
		 * for(int i = 0, length = effects.size(); i < length; i++) { // висящий
		 * эффект Effect effect = array[i]; // отправляем игроку анимацию
		 * эффекта, который висит на этом игроке
		 * PacketManager.showEffect(player, effect); } } finally {
		 * effectList.unlock(); } }
		 */
	}

	/**
	 * Увеличение счетчика убитых нпс.
	 */
	public void addPvECount() {
	}

	/**
	 * Увеличение счетчика убитых игроков.
	 */
	public void addPvPCount() {
	}

	/**
	 * Обработка добавления нового скила.
	 * 
	 * @param skill новый скил.
	 * @param sendPacket отправлять ли пакет с новым списком скилов.
	 */
	public boolean addSkill(Skill skill, boolean sendPacket) {
		// если скила нет, выходим
		if(skill == null)
			return false;

		// получаем таблицу скилов
		Table<IntKey, Skill> current = getSkills();

		// если схожий скил уже есть, выходим
		if(current.containsKey(skill.getId()))
			return false;

		// если это не тогл, то добавляем пассивные функции
		if(!skill.isToggle())
			skill.getTemplate().addPassiveFuncs(this);

		// вставляем в таблицу
		current.put(skill.getId(), skill);

		return true;
	}

	/**
	 * Обработка добавления нового скила.
	 * 
	 * @param template новый скил.
	 * @param sendPacket отправлять ли пакет с новым списком скилов.
	 */
	public boolean addSkill(SkillTemplate template, boolean sendPacket) {
		// если темплейта нет, выходим
		if(template == null)
			return false;

		// получаем таблицу скилов игрока
		Table<IntKey, Skill> current = getSkills();

		// если схожий скил уже есть, выходим
		if(current.containsKey(template.getId()))
			return false;

		// создаем новый инстанс скила
		Skill skill = template.newInstance();

		// если скил не тогл, выдаем пассивные функции
		if(!skill.isToggle())
			template.addPassiveFuncs(this);

		// вставляем новый скил
		current.put(skill.getId(), skill);

		return true;
	}

	/**
	 * Обработка добавления нового скила.
	 * 
	 * @param skills новые скилы.
	 * @param sendPacket отправлять ли пакет с новым списком скилов.
	 */
	public boolean addSkills(Skill[] skills, boolean sendPacket) {
		// если массив пуст, выходим
		if(skills == null || skills.length == 0)
			return false;

		// добавляем все скилы
		for(int i = 0, length = skills.length; i < length; i++)
			addSkill(skills[i], false);

		return true;
	}

	/**
	 * Обработка добавления нового скила.
	 * 
	 * @param templates новые скилы.
	 * @param sendPacket отправлять ли пакет с новым списком скилов.
	 */
	public boolean addSkills(SkillTemplate[] templates, boolean sendPacket) {
		// если массив пуст, выходим
		if(templates == null || templates.length == 0)
			return false;

		// получаем текущую таблицу скилов
		Table<IntKey, Skill> current = getSkills();

		// перебираем темплейты
		for(int i = 0, length = templates.length; i < length; i++) {
			// получаем темплейт скила
			SkillTemplate template = templates[i];

			// если такой скил есть, пропускаем
			if(current.containsKey(template.getId()))
				continue;

			// получаем новый инстанс
			Skill skill = template.newInstance();

			// если он не тогл, добавляем пассивные функции
			if(!skill.isToggle())
				template.addPassiveFuncs(this);

			// добавляем сам скил
			current.put(skill.getId(), skill);

			// получаем менеджера БД
			DataBaseManager dbManager = DataBaseManager.getInstance();

			// добавляем скил в БД
			dbManager.createSkill(this, skill);
		}

		return true;
	}

	/**
	 * Добавлении функции персонажу.
	 * 
	 * @param func новая функция.
	 */
	public final void addStatFunc(StatFunc func) {
		// если функции нет, то выходим
		if(func == null)
			return;

		// получаем индекс в массиве калькулятора
		int ordinal = func.getStat().ordinal();

		writeStatLock.lock();
		try {
			// получаем калькулятор для этой функции
			Calculator calc = calcs[ordinal];

			// если калькулятора для этого стата нету, создаем новый
			if(calc == null) {
				calc = new Calculator();
				calcs[ordinal] = calc;
			}

			// добавляем функцию в калькулятор
			calc.addFunc(func);
		} finally {
			writeStatLock.unlock();
		}
	}

	/**
	 * Добавление массива функций.
	 * 
	 * @param funcs новые функции.
	 */
	public final void addStatFuncs(StatFunc[] funcs) {
		writeStatLock.lock();
		try {
			// перебираем стат функции
			for(int i = 0, length = funcs.length; i < length; i++) {
				// получаем функцию
				StatFunc func = funcs[i];

				// если функции нету, пропускаем
				if(func == null)
					continue;

				// получаем индекс в массиве калькулятора
				int ordinal = func.getStat().ordinal();

				// получаем калькулятор для этой функции
				Calculator calc = calcs[ordinal];

				// если калькулятора для этого стата нету, создаем новый
				if(calc == null) {
					calc = new Calculator();
					calcs[ordinal] = calc;
				}

				// добавляем функцию в калькулятор
				calc.addFunc(func);
			}
		} finally {
			writeStatLock.unlock();
		}
	}

	/**
	 * Добавление в видимость нового объекта.
	 * 
	 * @param object новый объект.
	 */
	public void addVisibleObject(TObject object) {
	}

	/**
	 * Рассчет приминения шансовых функций.
	 * 
	 * @param type тип события.
	 * @param target цель.
	 * @param skill используемый скил.
	 */
	public void applyChanceFunc(ChanceType type, Character target, Skill skill) {
		// если шансовых функций нету, выходим
		if(chanceFuncs.isEmpty())
			return;

		chanceFuncs.readLock();
		try {
			// получаем массив функций
			ChanceFunc[] array = chanceFuncs.array();

			for(int i = 0, length = chanceFuncs.size(); i < length; i++) {
				// получаем функцию
				ChanceFunc func = array[i];

				// проверяем на совместимость с событием
				if(type == ChanceType.ON_ATTACK && !func.isOnAttack())
					continue;
				else if(type == ChanceType.ON_ATTACKED && !func.isOnAttacked())
					continue;
				else if(type == ChanceType.ON_OWERTURNED && !func.isOnOwerturned())
					continue;
				else if(type == ChanceType.ON_SHIELD_BLOCK && !func.isOnShieldBlocked())
					continue;
				else if(type == ChanceType.ON_OWERTURN && !func.isOnOwerturn())
					continue;
				else if(type == ChanceType.ON_CRIT_ATTACK && !func.isOnCritAttack())
					continue;
				else if(type == ChanceType.ON_CRIT_ATTACKED && !func.isOnCritAttacked())
					continue;

				// получаем менеджер рандома
				RandomManager randManager = RandomManager.getInstance();

				// получаем рандоминайзер для функций
				Random rand = randManager.getFuncRandom();

				// если шанс не выпадает, пропускаем
				if(!rand.chance(func.getChance()))
					continue;

				// применяем функцию
				func.apply(this, target, skill);
			}
		} finally {
			chanceFuncs.readUnlock();
		}
	}

	/**
	 * Отправка пакета о перемезении персонажа.
	 * 
	 * @param x стартовая координата.
	 * @param y стартовая координата.
	 * @param z стартовая координата.
	 * @param heading направление.
	 * @param type тип перемезения.
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 * @param sendSelfPacket отправлять ли и себе пакет.
	 */
	public void broadcastMove(float x, float y, float z, int heading, MoveType type, float targetX, float targetY, float targetZ, boolean sendSelfPacket) {
		broadcastPacket(getMovePacket(x, y, z, heading, type, targetX, targetY, targetZ));
	}

	/**
	 * Отправляем пакет персонажу и окружающим.
	 * 
	 * @param packet отправляемый пакет.
	 */
	public void broadcastPacket(ServerPacket packet) {
		broadcastPacketToOthers(packet);
	}

	/**
	 * Отправить пакет только окружающим.
	 * 
	 * @param packets отправляемый пакет.
	 */
	public final void broadcastPacketToOthers(ServerPacket packet) {
		// получаем текущий регион
		WorldRegion region = getCurrentRegion();

		// если региона нет, выходим
		if(region == null)
			return;

		// получаем соседние регионы
		WorldRegion[] regions = region.getNeighbors();

		// увеличиваем счетчик отправок
		packet.increaseSends();

		// TODO
		// рассчитываем кол-во отправок
		// for(int i = 0, length = regions.length; i < length; i++)
		// regions[i].calcSendCount(this, packet);

		// отправялем пакет игрокам
		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].sendPacket(this, packet);

		// завершаем работу пакета
		packet.complete();
	}

	/**
	 * Расчет разворота в указанные координаты.
	 * 
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @return нужный разворот.
	 */
	public final int calcHeading(float targetX, float targetY) {
		return (int) (Math.atan2(y - targetY, x - targetX) * HEADINGS_IN_PI) + 32768;
	}

	/**
	 * Расчет разворота в указанные координаты.
	 * 
	 * @param loc целевая точка.
	 * @return нужный разворот.
	 */
	public final int calcHeading(Location loc) {
		return loc == null ? 0 : calcHeading(loc.getX(), loc.getY());
	}

	/**
	 * Рассчет итогового значения указаного стата для этого персонажа.
	 * 
	 * @param stat стат, который нужно рассчитать.
	 * @param init базовое значение.
	 * @param target цель.
	 * @param skill скилл, учавствующий в процессе расчета.
	 * @return итоговое значение стата.
	 */
	public final float calcStat(StatType stat, int init, Character target, Skill skill) {
		if(stat == null)
			return init;

		readStatLock.lock();
		try {
			// получаем калькулятор для этого стата
			Calculator calc = calcs[stat.ordinal()];

			// если его нету или он пустой, возвращаем init
			if(calc == null || calc.size() == 0)
				return init;

			// вычисляем
			return calc.calc(this, target, skill, init);
		} finally {
			readStatLock.unlock();
		}
	}

	/**
	 * Рассчет итогового значения указаного стата для этого персонажа.
	 * 
	 * @param stat стат, который нужно рассчитать.
	 * @param init базовое значение.
	 * @param order - максимальный учитываемый ордер
	 * @param target цель.
	 * @param skill скилл, учавствующий в процессе расчета.
	 * @return итоговое значение стата.
	 */
	public final float calcStat(StatType stat, int init, int order, Character target, Skill skill) {
		// вычисляем
		readStatLock.lock();
		try {
			// получаем калькулятор для этого стата
			Calculator calc = calcs[stat.ordinal()];

			// если его нету или он пустой, возвращаем init
			if(calc == null || calc.size() == 0)
				return init;

			// вычисляем
			return calc.calcToOrder(this, target, skill, init, order);
		} finally {
			readStatLock.unlock();
		}
	}

	/**
	 * Отмена опрокинутого состояния.
	 */
	public void cancelOwerturn() {
		// флаг, нужно ли отправлять пакет
		boolean send = false;

		// если игрок опрокинут
		if(isOwerturned()) {
			// синхронизируемся
			synchronized(this) {
				// если он точно опрокинут
				if(isOwerturned()) {
					// убераем флаг
					setOwerturned(false);
					// ставим пометку для отправки пакета
					send = true;
				}
			}
		}

		// если нужно отправлять
		if(send)
			// отправляем пакет
			PacketManager.showCharacterOwerturn(this);
	}

	/**
	 * Обработка нанесения урона.
	 * 
	 * @param skill атакующий скилл.
	 * @param info информация об атаке.
	 * @param attacker атакующий персонаж.
	 */
	public void causingDamage(Skill skill, AttackInfo info, Character attacker) {
		if(Config.DEVELOPER_MAIN_DEBUG)
			sayMessage("id = " + getTemplateId() + ", class = " + getClass().getSimpleName());

		// если мертвый или без урона, то ничего не делаем
		if(isDead())
			return;

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// добавляем атакеру счетчик атаки для сердец
		if(!attacker.isAttacking()) {
			attacker.setAttacking(true);
			attacker.addAttackCounter();
		}

		// если атакуемый в защитной стойке и удар был блокирован
		if(isDefenseStance() && info.isBlocked()) {
			// отображаем факт блокировки
			PacketManager.showShieldBlocked(this);

			// уведомляем о факте блокирования
			eventManager.notifyShieldBlocked(this, attacker, skill);
		}

		// сколько мп восстановит атакующий
		int abs = (int) attacker.calcStat(StatType.ATTACK_ABSORPTION_MP, 0, this, skill);

		// если есть востанновленное мп
		if(abs > 0) {
			// добавляем мп
			attacker.setCurrentMp(attacker.getCurrentMp() + abs);

			// обновляем полоску мп
			eventManager.notifyMpChanged(attacker);
		}

		// тип урона
		int type = Damage.DAMAGE;

		// кол-во урона
		int damage = info.getDamage();

		boolean hpChanged = false;
		boolean mpChanged = false;

		charLock.lock();
		try {
			// если не заблокирован
			if(!info.isBlocked()) {
				// если есть урон
				if(damage > 0) {
					// применяем урон
					setCurrentHp(getCurrentHp() - damage);

					// ставим флаг изменения хп
					hpChanged = true;
				}
			}

			// получаем кол-во восстанавливаемого мп
			abs = (int) calcStat(StatType.DEFENSE_ABSORPTION_MP, 0, attacker, skill);

			// если такое есть
			if(abs > 0) {
				// восстанавливаем
				setCurrentMp(getCurrentMp() + abs);

				// обновляем полоску мп
				mpChanged = true;
			}

			if(info.isBlocked() || damage < 1)
				type = Damage.BLOCK;
		} finally {
			charLock.unlock();
		}

		// если атака идет с опрокидыванием
		if(info.isOwerturn())
			// опрокидываем
			doOwerturn(attacker);

		// отображаем нанесение урона
		PacketManager.showDamage(attacker, this, info, skill, type);

		// уведомляем об атаке
		eventManager.notifyAttacked(this, attacker, skill, damage, info.isCrit());
		eventManager.notifyAttack(attacker, this, skill, damage, info.isCrit());

		if(hpChanged)
			// обновляем полоску хп
			eventManager.notifyHpChanged(this);

		if(mpChanged)
			// обновляем полоску мп
			eventManager.notifyMpChanged(this);

		updateDefense();

		// если персонаж умер
		if(isDead())
			// обрабатываем смерть
			doDie(attacker);
	}

	/**
	 * Обработка нанесения урона.
	 * 
	 * @param skill атакующий скилл.
	 * @param info информация об атаке.
	 * @param attacker атакующий персонаж.
	 */
	public void causingManaDamage(Skill skill, AttackInfo info, Character attacker) {
		/*
		 * if(Config.DEVELOPER_MAIN_DEBUG) sayMessage("id = " + getNpcId() +
		 * ", class = " + getClass().getSimpleName());
		 * 
		 * //если мертвый или без урона, то ничего не делаем if(isDead() ||
		 * info.isNoDamage()) return;
		 * 
		 * //запускаем боевую стойку attacker.startBattleStance(this);
		 * startBattleStance(attacker);
		 * 
		 * //добавляем атакеру счетчик атаки для сердец
		 * if(!attacker.isAttacking()) { attacker.setAttacking(true);
		 * attacker.addAttackCounter(); }
		 * 
		 * if(isDefenseStance() && info.isBlocked()) { addDefenseCounter();
		 * //broadcastPacket(new CharShieldBlock(this)); }
		 * 
		 * charLock.lock(); try { int type = 0; int damage =
		 * Math.min(info.getDamage(), currentMp);
		 * 
		 * if(info.isBlocked()) type = Damage.BLOCK; else if(damage > 0) { type
		 * = Damage.DAMAGE;
		 * 
		 * setCurrentMp(getCurrentMp() - damage);
		 * attacker.setCurrentMp(attacker.getCurrentMp() + damage);
		 * 
		 * ObjectEventManager.notifyMpChanged(this);
		 * ObjectEventManager.notifyMpChanged(attacker); }
		 * 
		 * //broadcastPacket(new Damage(attacker, this, info, skill, type));
		 * 
		 * ObjectEventManager.notifyAttacked(this, attacker, skill, damage,
		 * info.isCrit()); ObjectEventManager.notifyAttack(attacker, this,
		 * skill, damage, info.isCrit());
		 * 
		 * updateDefense(); } finally { charLock.unlock(); }
		 * 
		 * if(isDead()) doDie(attacker);
		 */
	}

	/**
	 * Определяет, есть ли впереди персонажи, мешающие проходу вперед.
	 * 
	 * @param barriers список персонажей.
	 * @param distance проверяемая дистанция.
	 * @param radians проверяемое направление.
	 * @return есть ли барьеры.
	 */
	public final boolean checkBarriers(Array<Character> barriers, int distance, float radians) {
		// если преград нету, выходим
		if(barriers.isEmpty())
			return true;

		// получаем массив преград
		Character[] array = barriers.array();

		// получаем точку следующей позиции
		float newX = Coords.calcX(x, distance, radians);
		float newY = Coords.calcY(y, distance, radians);

		boolean isNpc = isNpc();

		// перебераем преграды
		for(int i = 0, length = barriers.size(); i < length; i++) {
			// получаем персонажа
			Character target = array[i];

			// если он не находитмся на пути, пропускаем
			if(target == null || target.isDead() || (isNpc && !checkTarget(target))
					|| Geometry.getDistanceToLine(x, y, newX, newY, target.getX(), target.getY()) - (target.getGeomRadius() + getGeomRadius()) > 10)
				continue;

			if(Config.DEVELOPER_DEBUG_TARGET_TYPE) {
				Location[] locs = Coords.circularCoords(Location.class, target.getX(), target.getY(), target.getZ(), (int) target.getGeomRadius(), 10);

				// получаем таблицу итемов
				ItemTable itemTable = ItemTable.getInstance();

				ItemTemplate template = itemTable.getItem(125);

				for(int g = 0; g < 10; g++) {
					ItemInstance item = template.newInstance();

					item.setItemCount(1);
					item.setTempOwner(this);

					Location loc = locs[g];

					loc.setContinentId(continentId);

					item.spawnMe(loc);
				}

				locs = Coords.circularCoords(Location.class, x, y, z, (int) getGeomRadius(), 10);

				template = itemTable.getItem(127);

				for(int g = 0; g < 10; g++) {
					ItemInstance item = template.newInstance();

					item.setItemCount(1);
					item.setTempOwner(this);

					Location loc = locs[g];

					loc.setContinentId(continentId);

					item.spawnMe(loc);
				}
			}

			return false;
		}

		return true;
	}

	/**
	 * Проверяет на возможность атаки цель.
	 * 
	 * @param target проверяемая цель.
	 * @return можно ли атаковать.
	 */
	public boolean checkTarget(Character target) {
		return true;
	}

	@Override
	public void decayMe(int type) {
		super.decayMe(type);

		spawned = false;
	}

	@Override
	public void deleteMe() {
		if(isDeleted())
			return;

		// получаем список схейтенных НПС
		Array<Npc> hateList = getHateList();

		// если в хэйти листе есть нпс
		if(!hateList.isEmpty()) {
			// получаем массив нпс
			Npc[] array = hateList.array();

			// перебираем их
			for(int i = 0, length = hateList.size(); i < length; i++) {
				// получаем нпс
				Npc npc = array[i];

				// если его нету, пропускаем
				if(npc == null)
					continue;

				// удаляем из агр листа у него себя
				npc.removeAggro(this);

				// уменшьаем счетчик и длинну
				i--;
				length--;
			}

			// очищаем список
			hateList.clear();
		}

		// очищаем эффект лист
		effectList.clear();

		// получаем таблицу откатов скилов
		Table<IntKey, ReuseSkill> reuseSkills = getReuseSkills();

		// если есть откаты скилов
		if(!reuseSkills.isEmpty()) {
			// складируем все откаты в пул
			reuseSkills.apply(FUNC_REUSE_SKILL_FOLD);

			// очищаем таблицу
			reuseSkills.clear();
		}

		// получаем переменные скилов
		Table<IntKey, Wrap> skillVariables = getSkillVariables();

		// если есть переменные для скилов
		if(skillVariables != null) {
			// складируем переменные
			skillVariables.apply(FUNC_SKILL_VAR_FOLD);

			// очищаем таблицу
			skillVariables.clear();
		}

		// останавливаем авто эмоции
		emotionTask.stop();

		// останавливаем обработку движения
		moveNextTask.stopTask();

		super.deleteMe();
	}

	/**
	 * Запуск отката для итема.
	 * 
	 * @param skill скил итема.
	 * @param item использованный итем.
	 */
	public boolean disableItem(Skill skill, ItemInstance item) {
		// получаем время отката скила
		int reuseDelay = skill.getReuseDelay(this);

		// если его нет, выходим
		if(reuseDelay < 1)
			return false;

		// получаем конйтенер отката скила
		ReuseSkill reuse = reuseSkills.get(skill.getReuseId());

		// если его нет, создаем новый и вставляем в таблицу
		if(reuse == null)
			reuseSkills.put(skill.getReuseId(), ReuseSkill.newInstance(skill.getReuseId(), reuseDelay).setItemId(item.getItemId()));
		else
			// иначе обновляем его
			reuse.setEndTime(System.currentTimeMillis() + reuseDelay);

		return true;
	}

	/**
	 * Запуск отката скила.
	 * 
	 * @param skill использованный скилл.
	 */
	public boolean disableSkill(Skill skill) {
		// получаем время отката скила
		int reuseDelay = skill.getReuseDelay(this);

		// если он безоткатный, пропускаем
		if(reuseDelay < 1)
			return false;

		// получаем таблицу откатов
		Table<IntKey, ReuseSkill> reuseSkills = getReuseSkills();

		// получаем контейнер отката
		ReuseSkill reuse = reuseSkills.get(skill.getReuseId());

		// если его нет, создаем новый и вставляем в таблицу
		if(reuse == null)
			reuseSkills.put(skill.getReuseId(), ReuseSkill.newInstance(skill.getReuseId(), reuseDelay));
		else
			// иначе обновляем старый
			reuse.setEndTime(System.currentTimeMillis() + reuseDelay);

		// обновляем отображение отката скила
		updateReuse(skill, reuseDelay);

		// получаем массив ид скилов, которые нужно откатывать вместе с этим
		int[] reuseIds = skill.getReuseIds();

		// если такие есть
		if(reuseIds != null) {
			// перебираем
			for(int i = 0, length = reuseIds.length; i < length; i++) {
				// получаем ид отката
				int id = reuseIds[i];

				// получаем скил
				skill = skills.get(id);

				// если такого скила нету либо он уже в откате ,пропускаем
				if(skill == null || isSkillDisabled(skill))
					continue;

				// получаем время отката скила
				reuseDelay = skill.getReuseDelay(this);

				// если время отката нету, пропускаем
				if(reuseDelay < 1)
					continue;

				// получаем контейнер отката скила
				reuse = reuseSkills.get(id);

				// если контейнера нет, создаем новый и вставляем
				if(reuse == null)
					reuseSkills.put(id, ReuseSkill.newInstance(id, reuseDelay));
				else
					// иначе обновляем
					reuse.setEndTime(System.currentTimeMillis() + reuseDelay);

				// обновляем отобраение отката
				updateReuse(skill, reuseDelay);
			}
		}

		return true;
	}

	/**
	 * Метод обработки каста скила.
	 * 
	 * @param startX стартовая координата.
	 * @param startY стартовая координата.
	 * @param startZ стартовая координата.
	 * @param skill кастуемый скилл.
	 * @param state состояние каста скила.
	 * @param heading разворот персонажа.
	 * @param targetX точка х куда бьем.
	 * @param targetY точка у куда бьем.
	 * @param targetZ точка z куда бьем.
	 */
	public void doCast(float startX, float startY, float startZ, Skill skill, int state, int heading, float targetX, float targetY, float targetZ) {
		doCast(startX, startY, startZ, updateResultSkill(skill), state, heading, targetX, targetY, targetZ, null);
	}

	/**
	 * Обновление итогового кастуемого скила.
	 * 
	 * @param skill обновляемый скил.
	 * @return итоговый скил.
	 */
	protected Skill updateResultSkill(Skill skill) {
		// если скил имеет быстрый режим, и последний каст был недавно и был
		// скастован нуэный скил
		if(skill.isHasFast() && System.currentTimeMillis() - lastCast < 1500 && skill.hasPrevSkillName(lastSkillName)) {
			// получаем ускоренный вариант
			Skill fast = getSkill(skill.getId() + 1);

			// если такой находится, применяем
			if(fast != null)
				skill = fast;
		}

		return skill;
	}

	/**
	 * Метод обработки каста скила.
	 * 
	 * @param startX стартовая координата.
	 * @param startY стартовая координата.
	 * @param startZ стартовая координата.
	 * @param skill кастуемый скилл.
	 * @param state состояние каста скила.
	 * @param heading разворот персонажа.
	 * @param targetX точка х куда бьем.
	 * @param targetY точка у куда бьем.
	 * @param targetZ точка z куда бьем.
	 * @param item кастующий скил итем.
	 */
	public void doCast(float startX, float startY, float startZ, Skill skill, int state, int heading, final float targetX, final float targetY, final float targetZ, ItemInstance item) {
		// если пассивный, то не кастим его
		if(skill.isPassive())
			return;

		if(Config.DEVELOPER_DEBUG_CASTING_SKILL)
			sendMessage(" skill id " + skill.getId() + ", delay = " + skill.getDelay());

		// получаем тип приминения скила
		OperateType operateType = skill.getOperateType();

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();
		switch(operateType) {
			case LOCK_ON:
			case ACTIVE: {
				// проверка условий выполнения
				if(!skill.checkCondition(this, targetX, targetY, targetZ))
					return;

				// получаем кастуемый сейчас скил
				Skill castingSkill = getCastingSkill();

				// если скил такой есть
				if(castingSkill != null) {
					// если это скил выделения целей
					if(castingSkill.getOperateType() == OperateType.LOCK_ON)
						// завершаем его
						abortCast(false);
					// если новый скил имеет принудительный каст
					else if(skill.isForceCast()) {
						// обрываем текущий
						abortCast(true);

						// обновляем кастуемый скил
						skill = updateResultSkill(skill);
					}
					// иначе новый нельзя использовать
					else
						return;
				}

				// получаем активационный скил
				Skill activateSkill = getActivateSkill();

				if(activateSkill != null) {
					if(skill.isForceCast())
						abortCast(false);
					else
						return;
				}

				// если с этим скилам нельзя двигаться
				if(skill.isBlockingMove())
					// останавливаем движение
					stopMove();

				// если это лок он скил
				if(operateType == OperateType.LOCK_ON) {
					// получаем текущий список целей
					Array<Character> targets = getLockOnTargets();

					// если он есть
					if(targets != null)
						// очищаем
						targets.clear();
				}

				// применяем новое положение
				setXYZ(startX, startY, startZ);
				setHeading(heading + skill.getCastHeading());

				// запускаем обработку скила
				skill.startSkill(this, targetX, targetY, targetZ);

				// запускаем движение во время каста
				skillMoveTask.nextTask(skill, targetX, targetY, targetZ);

				// уведомляем о старте каста
				eventManager.notifyStartCasting(this, skill);

				// запускаем откат скила
				disableSkill(skill);

				// если скил применяется при столкновении
				if(skill.isCastToMove())
					// запоминаем цель
					skillUseTask.setTarget(targetX, targetY, targetZ);
				else
					// запускаем задание приминения
					skillUseTask.nextUse(skill, targetX, targetY, targetZ);

				// есть ли мп потребление у скила
				if(skill.getMpConsume() > 0) {
					// рассчитываем оставшиеся мп
					int resultMp = getCurrentMp() - skill.getMpConsume();

					// применяем
					setCurrentMp(resultMp);

					// обновляем мп игроку
					eventManager.notifyMpChanged(this);
				}

				// есть ли хп потребление у скила
				if(skill.getHpConsume() > 0) {
					// рассчитываем оставшиеся хп
					int resultHp = getCurrentHp() - skill.getHpConsume();

					// применяем
					setCurrentHp(resultHp);

					// обновляем хп игроку
					eventManager.notifyHpChanged(this);
				}

				// если потребляется итемы
				if(skill.getItemIdConsume() != 0) {
					// удаляем из инвенторя
					getInventory().removeItem(skill.getItemIdConsume(), skill.getItemCountConsume());

					// обновляем инвентарь
					eventManager.notifyInventoryChanged(this);

					// отображаем сообщение об использовании итема
					PacketManager.showUseItem(this, skill.getItemIdConsume(), (int) skill.getItemCountConsume());
				}

				// запускаем задание каста
				skillCastTask.nextTask(skill, targetX, targetY, targetZ);

				int chargetLevel = getChargeLevel();

				if(chargetLevel > 0) {
					setChargeLevel(0);
					setChargeSkill(null);
				}

				break;
			}
			case ACTIVATE: {
				// получаем активируемый скил
				Skill activateSkill = getActivateSkill();

				// если активируемого скила есть и состояние 0
				if(activateSkill != null && state == 0 && skill.isCanceable()) {
					// отменяем задание юза
					skillUseTask.cancel(false);

					// если запущен каст
					if(skillCastTask.isRunning())
						// отменяем каст
						skillCastTask.cancel(false);
					else {
						// запускаем завершение каста
						activateSkill.endSkill(this, x, y, z, true);

						// уведомляем о завершении каста
						eventManager.notifyFinishCasting(this, activateSkill);
					}

					// зануляем активируемый скил
					setActivateSkill(null);
				}
				// если активируемого скила нет и состояние 1
				else if(activateSkill == null && state == 1) {
					// проверка условий выполнения
					if(!skill.checkCondition(this, targetX, targetY, targetZ))
						return;

					// если сейчас идет каст
					if(isCastingNow())
						// прерываем его
						abortCast(true);

					// запоминаем скил как активируемый
					setActivateSkill(skill);

					// если скил блокитрует движение
					if(skill.isBlockingMove())
						// останавливаем персонажа
						stopMove();

					// применяем стартовые координаты
					setXYZ(startX, startY, startZ);
					setHeading(heading);

					// уведомляем о старте каста
					eventManager.notifyStartCasting(this, skill);

					// ставим на откат скил
					disableSkill(skill);

					// запускаем обработку скила
					skill.startSkill(this, targetX, targetY, targetZ);

					// если есть время каста
					if(skill.getHitTime() != 0) {
						// запускаем задание использования
						skillUseTask.nextUse(skill, targetX, targetY, targetZ);

						// запускаем задание каста
						skillCastTask.nextTask(skill, targetX, targetY, targetZ);
					}

					// есть ли мп потребление у скила
					if(skill.getMpConsume() > 0) {
						// рассчитываем оставшиеся мп
						int resultMp = getCurrentMp() - skill.getMpConsume();

						// применяем
						setCurrentMp(resultMp);

						// обновляем мп игроку
						eventManager.notifyMpChanged(this);
					}

					// есть ли хп потребление у скила
					if(skill.getHpConsume() > 0) {
						// рассчитываем оставшиеся хп
						int resultHp = getCurrentHp() - skill.getHpConsume();

						// применяем
						setCurrentHp(resultHp);

						// обновляем хп игроку
						eventManager.notifyHpChanged(this);
					}

					// если потребляется итемы
					if(skill.getItemIdConsume() != 0) {
						// удаляем из инвенторя
						getInventory().removeItem(skill.getItemIdConsume(), skill.getItemCountConsume());

						// обновляем инвентарь
						eventManager.notifyInventoryChanged(this);

						// отображаем сообщение об использовании итема
						PacketManager.showUseItem(this, skill.getItemIdConsume(), (int) skill.getItemCountConsume());
					}
				}

				int chargetLevel = getChargeLevel();

				if(chargetLevel > 0) {
					setChargeLevel(0);
					setChargeSkill(null);
				}

				break;
			}
			case CAST_ITEM: {
				// проверка условий выполнения
				if(!skill.checkCondition(this, targetX, targetY, targetZ))
					return;

				if(isCastingNow() && !skill.isAltCast())
					return;

				if(skill.isBlockingMove())
					stopMove();

				setHeading(heading);

				// запускаем обработку скила
				if(!skill.isAltCast())
					skill.startSkill(this, targetX, targetY, targetZ);

				disableItem(skill, item);

				if(skill.isAltCast())
					skill.useSkill(Character.this, targetX, targetY, targetZ);
				else
					skillUseTask.nextUse(skill, targetX, targetY, targetZ);

				// есть ли мп потребление у скила
				if(skill.getMpConsume() > 0) {
					// потребляем мп
					int resultMp = getCurrentMp() - skill.getMpConsume();

					setCurrentMp(resultMp);

					// обновляем мп игроку
					eventManager.notifyMpChanged(this);
				}

				// есть ли хп потребление у скила
				if(skill.getHpConsume() > 0) {
					// потребляем хп
					int resultHp = getCurrentHp() - skill.getHpConsume();

					setCurrentHp(resultHp);

					// обновляем хп игроку
					eventManager.notifyHpChanged(this);
				}

				// если потребляется итемы
				if(skill.getItemIdConsume() != 0) {
					// удаляем из инвенторя
					getInventory().removeItem(skill.getItemIdConsume(), skill.getItemCountConsume());

					// обновляем инвентарь
					eventManager.notifyInventoryChanged(this);

					// создаем пакет сообщение
					SystemMessage packet = SystemMessage.getInstance(MessageType.ITEM_USE).addItem(skill.getItemIdConsume(), (int) skill.getItemCountConsume());

					// отправляем
					sendPacket(packet, true);
				}

				if(!skill.isAltCast())
					skillCastTask.nextTask(skill, targetX, targetY, targetZ);
				break;
			}
			case CHARGE: {
				// получаем кастуемый скил
				Skill castingSkill = getCastingSkill();

				// если идет активация скила
				if(state == 1) {
					// проверка условий выполнения
					if(!skill.checkCondition(this, targetX, targetY, targetZ))
						return;

					// если tcnm rfcnetvsq crbk
					if(castingSkill != null) {
						// и если это ЛОК ОН скил
						if(castingSkill.getOperateType() == OperateType.LOCK_ON)
							// обрываем его
							abortCast(false);
						// если этому скилу разрешено обрывать рдугие
						else if(skill.isForceCast())
							// обрываем кастуемый
							abortCast(true);
						// иначе выходим
						else
							return;
					}

					// останавливаем движение
					stopMove();
					// обновляем позицию
					setXYZ(startX, startY, startZ);
					// обновляем направление
					setHeading(heading);

					// запускаем обработку скила
					skill.startSkill(this, targetX, targetY, targetZ);

					// обрабатываем движение скила
					skillMoveTask.nextTask(skill, targetX, targetY, targetZ);

					// уведомляем о старте каста скила
					eventManager.notifyStartCasting(this, skill);

					// если скил юзается при столкновении
					if(skill.isCastToMove())
						// запоминаем точку удара
						skillUseTask.setTarget(targetX, targetY, targetZ);
					else
						// запускаем обрабтку юза
						skillUseTask.nextUse(skill, targetX, targetY, targetZ);

					// потребляем необходимые ресурсы
					skillConsume(skill);

					// запускаем каст
					skillCastTask.nextTask(skill, targetX, targetY, targetZ);

					break;
				}

				// если есть кастуемый скил и он заряжающийся
				if(castingSkill != null && castingSkill.getSkillType() == SkillType.CHARGE)
					abortCast(false);

				break;
			}
			default:
				break;
		}
	}

	/**
	 * Обрабатываем потребление скила.
	 * 
	 * @param skill обрабатываемый скил.
	 */
	protected void skillConsume(Skill skill) {
		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// есть ли мп потребление у скила
		if(skill.getMpConsume() > 0) {
			// потребляем мп
			int resultMp = getCurrentMp() - skill.getMpConsume();

			setCurrentMp(resultMp);

			// обновляем мп игроку
			eventManager.notifyMpChanged(this);
		}

		// есть ли хп потребление у скила
		if(skill.getHpConsume() > 0) {
			// потребляем хп
			int resultHp = getCurrentHp() - skill.getHpConsume();

			setCurrentHp(resultHp);

			// обновляем хп игроку
			eventManager.notifyHpChanged(this);
		}

		// если потребляется итемы
		if(skill.getItemIdConsume() != 0) {
			// удаляем из инвенторя
			getInventory().removeItem(skill.getItemIdConsume(), skill.getItemCountConsume());

			// обновляем инвентарь
			eventManager.notifyInventoryChanged(this);

			// создаем пакет сообщение
			SystemMessage packet = SystemMessage.getInstance(MessageType.ITEM_USE).addItem(skill.getItemIdConsume(), (int) skill.getItemCountConsume());

			// отправляем
			sendPacket(packet, true);
		}
	}

	/**
	 * Обработка каста скила от итема.
	 * 
	 * @param skill скил используемого итема.
	 * @heading направление каста скила.
	 * @param item используемый итем.
	 */
	public void doCast(Skill skill, int heading, ItemInstance item) {
		doCast(x, y, z, skill, 0, heading, x, y, z, item);
	}

	/**
	 * Старт сбора ресурса.
	 * 
	 * @param resourse собираемый ресурс.
	 */
	public void doCollect(ResourseInstance resourse) {
		log.warning(this, new Exception("unsupperted method."));
	}

	/**
	 * Обработка смерти.
	 * 
	 * @param killer убийца персонажа.
	 */
	public void doDie(Character killer) {
		// обраваем каст скила
		abortCast(true);
		// останавливаем движение
		stopMove();

		// отключаем боевую стойку
		stopBattleStance();

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем о убийстве
		eventManager.notifyDead(this, killer);

		// если есть подписчики на убийства
		if(!dieListeners.isEmpty()) {
			dieListeners.readLock();
			try {
				// получаем их массив
				DieListener[] array = dieListeners.array();

				// перебираем
				for(int i = 0, length = dieListeners.size(); i < length; i++)
					// уведомляем их
					array[i].onDie(killer, this);
			} finally {
				dieListeners.readUnlock();
			}
		}
	}

	/**
	 * Обработка падения персонажа.
	 * 
	 * @param startZ высота, с которой упали.
	 * @param endZ высота, на которую упали.
	 */
	public int doFall(float startZ, float endZ) {
		// расчитываем урон
		int damage = (int) Math.abs(startZ - endZ) * 7;

		// если он есть
		if(damage > 0) {
			// применяем
			setCurrentHp(Math.max(2, currentHp - damage));

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// обновляем хп
			eventManager.notifyHpChanged(this);
		}

		// возвращаем урон
		return damage;
	}

	/**
	 * Обработка опрокидывания.
	 */
	public void doOwerturn(Character attacker) {
		// отменяем эффекты, которые спадают при опрокидывании
		effectList.exitNoOwerturnEffects();

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем о опрокидывании
		eventManager.notifyOwerturn(attacker, this, null);
		eventManager.notifyOwerturned(this, attacker, null);

		// ставим флаг опрокинутости
		setOwerturned(true);

		// обрываем каст
		abortCast(true);

		// останавливаем персонажа
		stopMove();

		// обновляем направление персонажа
		setHeading(calcHeading(attacker.getX(), attacker.getY()));
	}

	/**
	 * Обработка тика регена.
	 */
	public void doRegen() {
		// если мертвый, не регеним
		if(isDead())
			return;

		// если условия для регена хп выполняются
		if(regenHp.checkCondition())
			// регеним
			regenHp.doRegen();

		// если условия для регена мп выполняются
		if(regenMp.checkCondition())
			// регеним
			regenMp.doRegen();
	}

	/**
	 * Обработка хила c эффекта.
	 * 
	 * @param heal сколько отхилили.
	 * @param healer хилящий персонаж.
	 */
	public void effectHealHp(int heal, Character healer) {
		// если персонаж мертв или хила нету, выходим
		if(isDead() || heal < 1)
			return;

		charLock.lock();
		try {
			// устанавливаем новое хп
			setCurrentHp(getCurrentHp() + heal);
		} finally {
			charLock.unlock();
		}
	}

	/**
	 * Обработка хила c эффекта.
	 * 
	 * @param heal сколько отхилили.
	 * @param healer хилящий персонаж.
	 */
	public void effectHealMp(int heal, Character healer) {
		// если персонаж мертв или хила мп нету, выходим
		if(isDead() || heal < 1)
			return;

		charLock.lock();
		try {
			// устанавливаем новое мп
			setCurrentMp(getCurrentMp() + heal);
		} finally {
			charLock.unlock();
		}
	}

	/**
	 * Остановка отката скила.
	 * 
	 * @param skill откатываемый скил.
	 */
	public boolean enableSkill(Skill skill) {
		if(skill == null)
			return false;

		// получаем таблицу откатов скилов
		Table<IntKey, ReuseSkill> reuseSkills = getReuseSkills();

		// получаем контейнер отката
		ReuseSkill reuse = reuseSkills.get(skill.getReuseId());

		// если его нашли
		if(reuse != null)
			// обновляем время завершения
			reuse.setEndTime(0);

		// обновляем отображение отката скила
		updateReuse(skill, 0);

		return true;
	}

	/**
	 * @return текущий активированный тогл скил.
	 */
	public final Skill getActivateSkill() {
		return activateSkill;
	}

	@Override
	public AI getAI() {
		return ai;
	}

	/**
	 * @return атак спид.
	 */
	public final int getAtkSpd() {
		if(Config.DEVELOPER_FORCE_ATTACK_SPEED > 0)
			return Config.DEVELOPER_FORCE_ATTACK_SPEED;

		return (int) calcStat(StatType.ATTACK_SPEED, template.getAtkSpd(), null, null);
	}

	/**
	 * @return сила атаки.
	 */
	public int getAttack(Character attacked, Skill skill) {
		return (int) calcStat(StatType.ATTACK, 0, attacked, skill);
	}

	/**
	 * @return набор авто эмоций.
	 */
	protected EmotionType[] getAutoEmotions() {
		return Arrays.toGenericArray();
	}

	/**
	 * @return защита от опрокидывания.
	 */
	public int getBalance(Character attacker, Skill skill) {
		return (int) calcStat(StatType.BALANCE, 0, attacker, skill);
	}

	/**
	 * @return базовый модификатор защиты от опрокидывания.
	 */
	public final int getBalanceFactor() {
		return (int) calcStat(StatType.BALANCE_FACTOR, template.getBalanceFactor(), this, null);
	}

	/**
	 * @return банк персонажа.
	 */
	public Bank getBank() {
		return null;
	}

	/**
	 * @return базовое максимальное хп.
	 */
	public final int getBaseMaxHp() {
		return (int) calcStat(StatType.MAX_HP, template.getMaxHp(), 0x20, null, null);
	}

	/**
	 * @return базовое максимальное мп.
	 */
	public final int getBaseMaxMp() {
		return (int) calcStat(StatType.MAX_MP, template.getMaxMp(), 0x40, null, null);
	}

	/**
	 * @return список калькуляторов.
	 */
	public final Calculator[] getCalcs() {
		return calcs;
	}

	/**
	 * @return ид каста ида.
	 */
	public final int getCastId() {
		return castId;
	}

	/**
	 * @return текущий кастуемый скил.
	 */
	public final Skill getCastingSkill() {
		return castingSkill;
	}

	@Override
	public final Character getCharacter() {
		return this;
	}

	/**
	 * @return уровень заряда.
	 */
	public final int getChargeLevel() {
		return chargeLevel;
	}

	/**
	 * @return ид класса персонажа.
	 */
	public int getClassId() {
		return -1;
	}

	/**
	 * @return шанс нанесения крит удара.
	 */
	public final float getCritDamage(Character attacker, Skill skill) {
		return calcStat(StatType.CRITICAL_DAMAGE, 2, attacker, skill);
	}

	/**
	 * @return шанс нанесения крит удара.
	 */
	public final float getCritRate(Character attacker, Skill skill) {
		return calcStat(StatType.CRITICAL_RATE, template.getCritRate(), attacker, skill);
	}

	/**
	 * @return защита от шанса нанесения крит удара.
	 */
	public final float getCritRateRcpt(Character attacker, Skill skill) {
		return calcStat(StatType.CRIT_CHANCE_RECEPTIVE, template.getCritRcpt(), attacker, skill);
	}

	/**
	 * @return текущий уровень хп.
	 */
	public final int getCurrentHp() {
		return currentHp;
	}

	/**
	 * @return текущий % уровень хп.
	 */
	public final int getCurrentHpPercent() {
		return Math.max(Math.min(currentHp * 100 / getMaxHp(), 100), 0);
	}

	/**
	 * @return текущий уровень мп.
	 */
	public final int getCurrentMp() {
		return currentMp;
	}

	/**
	 * @return текущий % уровень мп.
	 */
	public final int getCurrentMpPercent() {
		return Math.max(Math.min(currentMp * 100 / getMaxMp(), 100), 0);
	}

	/**
	 * @return защита.
	 */
	public int getDefense(Character attacker, Skill skill) {
		return (int) calcStat(StatType.DEFENSE, 0, attacker, skill);
	}

	/**
	 * @return модификатор защиты.
	 */
	public final int getDefenseFactor() {
		return (int) calcStat(StatType.DEFENSE_FACTOR, template.getDefenseFactor(), this, null);
	}

	/**
	 * @return текущий дкэль.
	 */
	public Duel getDuel() {
		return null;
	}

	/**
	 * @return список эффектов.
	 */
	public final EffectList getEffectList() {
		return effectList;
	}

	/**
	 * @return цель.
	 */
	public final TObject getEnemy() {
		return enemy;
	}

	/**
	 * @return уровень сбора кристалов.
	 */
	public int getEnergyLevel() {
		return 0;
	}

	/**
	 * @return экиперовка.
	 */
	public Equipment getEquipment() {
		return null;
	}

	/**
	 * @return геометрия модели.
	 */
	public final Geom getGeom() {
		return geom;
	}

	/**
	 * Дистанция между моделями.
	 * 
	 * @param target целевая модель.
	 * @return дистанция.
	 */
	public float getGeomDistance(Character target) {
		return target.getGeomDistance(x, y) - geom.getRadius();
	}

	@Override
	public float getGeomDistance(float x, float y) {
		return getDistance(x, y) - geom.getRadius();
	}

	/**
	 * @return высота модели персонажа.
	 */
	public float getGeomHeight() {
		return geom.getHeight();
	}

	/**
	 * @return ширина модели персонажа.
	 */
	public float getGeomRadius() {
		return geom.getRadius();
	}

	/**
	 * @return клан игрока.
	 */
	public Guild getGuild() {
		return null;
	}

	/**
	 * @return список сагренных НПС на персонажа.
	 */
	public final Array<Npc> getHateList() {
		return hateList;
	}

	/**
	 * @return сила опрокидывания.
	 */
	public int getImpact(Character attacked, Skill skill) {
		return (int) calcStat(StatType.IMPACT, 0, attacked, skill);
	}

	/**
	 * @return базовый модификатор силы опрокидывания.
	 */
	public final int getImpactFactor() {
		return (int) calcStat(StatType.IMPACT_FACTOR, template.getImpactFactor(), this, null);
	}

	/**
	 * @return инвентарь.
	 */
	public Inventory getInventory() {
		return null;
	}

	/**
	 * @return карма игрока.
	 */
	public int getKarma() {
		return 0;
	}

	/**
	 * @return время последнего каста скила.
	 */
	public long getLastCast() {
		return lastCast;
	}

	/**
	 * @return имя последнего скастанувшегося скила.
	 */
	public SkillName getLastSkillName() {
		return lastSkillName;
	}

	/**
	 * @return уровень персонажа.
	 */
	public int getLevel() {
		return 0;
	}

	/**
	 * @return локальная копия списока сагренных НПС на персонажа.
	 */
	public final Array<Npc> getLocalHateList() {

		LocalObjects local = LocalObjects.get();

		Array<Npc> npcs = local.getNextNpcList();
		Array<Npc> hateList = getHateList();

		hateList.readLock();
		try {
			npcs.addAll(hateList);
		} finally {
			hateList.readUnlock();
		}

		return npcs;
	}

	/**
	 * @return локальная копия списока сагренных НПС на персонажа.
	 */
	public final Array<Npc> getLocalHateList(Array<Npc> container) {

		Array<Npc> hateList = getHateList();

		hateList.readLock();
		try {
			container.addAll(hateList);
		} finally {
			hateList.readUnlock();
		}

		return container;
	}

	/**
	 * @return активный лок он скил.
	 */
	public Skill getLockOnSkill() {
		return lockOnSkill;
	}

	/**
	 * @return набор лок он таргетов.
	 */
	public Array<Character> getLockOnTargets() {
		return null;
	}

	/**
	 * @return текущий максимальный хп.
	 */
	public final int getMaxHp() {
		return (int) calcStat(StatType.MAX_HP, template.getMaxHp(), null, null);
	}

	/**
	 * @returnтекущий максимальный мп.
	 */
	public final int getMaxMp() {
		return (int) calcStat(StatType.MAX_MP, template.getMaxMp(), null, null);
	}

	/**
	 * @return уровень сбора камней.
	 */
	public int getMiningLevel() {
		return 0;
	}

	/**
	 * @return ид модели.
	 */
	public int getModelId() {
		return template.getModelId();
	}

	/**
	 * Получение пакета перемещения персонажа с указанной точки в указанную
	 * точку.
	 * 
	 * @param x стартовая точка.
	 * @param y стартовая точка.
	 * @param z стартовая точка.
	 * @param heading направление.
	 * @param type тип перемещения.
	 * @param targetX конечная точка.
	 * @param targetY конечная точка.
	 * @param targetZ конечная точка.
	 * @return новый пакет.
	 */
	public ServerPacket getMovePacket(float x, float y, float z, int heading, MoveType type, float targetX, float targetY, float targetZ) {
		return CharMove.getInstance(this, type, x, y, z, heading, targetX, targetY, targetZ);
	}

	/**
	 * Получение пакета перемещения персонажа с текущей точки в указанную точку.
	 * 
	 * @param type тип перемещения.
	 * @param targetX конечная точка.
	 * @param targetY конечная точка.
	 * @param targetZ конечная точка.
	 * @return новый пакет.
	 */
	public ServerPacket getMovePacket(MoveType type, float targetX, float targetY, float targetZ) {
		return getMovePacket(x, y, z, heading, type, targetX, targetY, targetZ);
	}

	/**
	 * @return частота обновления позиций.
	 */
	public final int getMoveTickInterval() {
		return 100;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Слезть с маунта.
	 */
	public void getOffMount() {
	}

	/**
	 * @return ид опрокидывания.
	 */
	public int getOwerturnId() {
		return 0;
	}

	/**
	 * @return время, на которое опрокидывается персонаж.
	 */
	public int getOwerturnTime() {
		return 3000;
	}

	/**
	 * @return owner владелец самона.
	 */
	public Character getOwner() {
		return null;
	}

	/**
	 * @return группа игрока, в которой он состоит.
	 */
	public Party getParty() {
		return null;
	}

	/**
	 * @return уровень сбора растений.
	 */
	public int getPlantLevel() {
		return 0;
	}

	/**
	 * @return модификатор атаки.
	 */
	public int getPowerFactor() {
		return (int) calcStat(StatType.POWER_FACTOR, template.getPowerFactor(), this, null);
	}

	/**
	 * @return список квестов.
	 */
	public QuestList getQuestList() {
		return null;
	}

	/**
	 * @return кол-во регенирируемого хп.
	 */
	public final int getRegenHp() {
		return (int) calcStat(StatType.REGEN_HP, template.getRegHp(), this, null);
	}

	/**
	 * @return кол-во регенирируемого мп.
	 */
	public final int getRegenMp() {
		return (int) calcStat(StatType.REGEN_MP, template.getRegMp(), this, null);
	}

	/**
	 * Получение отката скила по скилл ид.
	 * 
	 * @param id сеилл ид.
	 * @return откат скила.
	 */
	public final ReuseSkill getReuseSkill(int id) {
		if(reuseSkills.isEmpty())
			return null;

		return reuseSkills.get(id);
	}

	/**
	 * @return таблицв откатов скилов.
	 */
	public final Table<IntKey, ReuseSkill> getReuseSkills() {
		return reuseSkills;
	}

	/**
	 * @return скорость перемещения.
	 */
	public final int getRunSpeed() {
		return Math.min((int) calcStat(StatType.RUN_SPEED, template.getRunSpd(), this, null), 500);
	}

	/**
	 * Получение скила персонажа по скилл ид.
	 * 
	 * @param skillId ид скила.
	 * @return скилл персонажа.
	 */
	public final Skill getSkill(int skillId) {
		if(skills.isEmpty())
			return null;

		return skills.get(skillId);
	}

	/**
	 * @return таблица скилов персонажа.
	 */
	public final Table<IntKey, Skill> getSkills() {
		return skills;
	}

	/**
	 * @return переменные для скилов.
	 */
	public Table<IntKey, Wrap> getSkillVariables() {
		// если таблицы нету
		if(skillVariables == null) {
			synchronized(this) {
				// и если ее опять нету
				if(skillVariables == null)
					// создаем
					skillVariables = Tables.newConcurrentIntegerTable();
			}
		}

		return skillVariables;
	}

	/**
	 * @return вызванный суммон.
	 */
	public Summon getSummon() {
		return summon;
	}

	/**
	 * @return текущая цель.
	 */
	public Character getTarget() {
		return target;
	}

	/**
	 * @return темплейт персонажа.
	 */
	public CharTemplate getTemplate() {
		return template;
	}

	@Override
	public int getTemplateId() {
		return template.getTemplateId();
	}

	@Override
	public int getTemplateType() {
		return template.getTemplateType();
	}

	/**
	 * @return title титул персонажа.
	 */
	public String getTitle() {
		return title;
	}

	@Override
	public final boolean hasAI() {
		return ai != null;
	}

	/**
	 * @return есть ли на персонаже эффекты.
	 */
	public boolean hasEffects() {
		return effectList.size() > 0;
	}

	/**
	 * @return состоит ли персонаж в гильдии.
	 */
	public boolean hasGuild() {
		return false;
	}

	/**
	 * @return состоит ли игрок в пати.
	 */
	public boolean hasParty() {
		return false;
	}

	/**
	 * @return имеются ли активные квесты.
	 */
	public boolean hasQuests() {
		return false;
	}

	/**
	 * @return заблокированы ли все действия.
	 */
	public final boolean isAllBlocking() {
		return stuned || owerturned || flyingPegas;
	}

	/**
	 * @return заблокирована ли атака.
	 */
	public final boolean isAttackBlocking() {
		return stuned || skillBlocking;
	}

	/**
	 * @return была ли уже нанесена атака во время каста.
	 */
	public boolean isAttacking() {
		return false;
	}

	/**
	 * @return находится ли в боевой стойке.
	 */
	public final boolean isBattleStanced() {
		return battleStanced;
	}

	/**
	 * Определяет, находится ли за списной у цели.
	 * 
	 * @param target целевой.
	 * @return находится ли за спиной.
	 */
	public final boolean isBehindTarget(TObject target) {
		if(target == null)
			return false;

		if(target.isCharacter()) {
			int head = getHeadingTo(target, true);// head != -1 && (head <=
													// 22337 || head >= 43197);
			return head != -1 && (head <= 10430 || head >= 55105);
		}

		return false;
	}

	/**
	 * Расчет блокировки удара по персонажу.
	 * 
	 * @param attacker атакующий персонаж.
	 * @param impactX координата удара.
	 * @param impactY координата удара.
	 * @param skill атакуемый скил.
	 * @return заблокирован ли.
	 */
	public final boolean isBlocked(Character attacker, float impactX, float impactY, Skill skill) {
		if(!isDefenseStance() || skill.isShieldIgnore())
			return false;

		return isInFront(attacker);
	}

	/**
	 * @return кастуется ли сейчас скил.
	 */
	public final boolean isCastingNow() {
		return castingSkill != null;
	}

	@Override
	public final boolean isCharacter() {
		return true;
	}

	/**
	 * @return собирает ли сейчас
	 */
	public boolean isCollecting() {
		return false;
	}

	/**
	 * @return мертв ли персонаж.
	 */
	public final boolean isDead() {
		return currentHp < 1;
	}

	/**
	 * @return в оборонительной ли стойке персонаж.
	 */
	public final boolean isDefenseStance() {
		return defenseStance;
	}

	/**
	 * @return уклоняем ли сейчас персонаж для скилов.
	 */
	public final boolean isEvasioned() {
		Skill skill = getCastingSkill();

		if(skill == null)
			return false;

		return skill.isEvasion();
	}

	/**
	 * @return летит ли на пегасе.
	 */
	public final boolean isFlyingPegas() {
		return flyingPegas;
	}

	/**
	 * @return ГМ ли персонаж.
	 */
	public boolean isGM() {
		return false;
	}

	/**
	 * Поражает ли цель.
	 * 
	 * @param startX координата.
	 * @param startY координата.
	 * @param startZ координата.
	 * @param radius радиус поражения.
	 * @return задевает ли персонажа.
	 */
	public boolean isHit(float startX, float startY, float startZ, float height, float radius) {
		if(Config.DEVELOPER_DEBUG_TARGET_TYPE) {
			Location[] locs = Coords.circularCoords(Location.class, x, y, z, (int) geom.getRadius(), 10);

			// получаем таблицу итемов
			ItemTable itemTable = ItemTable.getInstance();

			ItemTemplate template = itemTable.getItem(125);

			for(int i = 0; i < 10; i++) {
				ItemInstance item = template.newInstance();

				item.setItemCount(1);
				item.setTempOwner(this);

				Location loc = locs[i];

				loc.setContinentId(continentId);

				item.spawnMe(loc);
			}
		}

		return geom.isHit(startX, startY, startZ, height, radius);
	}

	@Override
	public boolean isHit(float startX, float startY, float startZ, float endX, float endY, float endZ, float radius, boolean checkHeight) {
		if(Config.DEVELOPER_DEBUG_TARGET_TYPE) {
			Location[] locs = Coords.circularCoords(Location.class, x, y, z, (int) geom.getRadius(), 10);

			// получаем таблицу итемов
			ItemTable itemTable = ItemTable.getInstance();

			ItemTemplate template = itemTable.getItem(125);

			for(int i = 0; i < 10; i++) {
				ItemInstance item = template.newInstance();

				item.setItemCount(1);
				item.setTempOwner(this);

				Location loc = locs[i];

				loc.setContinentId(continentId);

				item.spawnMe(loc);
			}
		}

		return geom.isHit(startX, startY, startZ, endX, endY, endZ, radius, checkHeight);
	}

	/**
	 * @return находится ли персонаж в зоне костра.
	 */
	public boolean isInBattleTerritory() {
		return false;
	}

	/**
	 * Находится ли цель за спиной.
	 * 
	 * @param target цель.
	 */
	public final boolean isInBehind(Character target) {
		if(target == null)
			return false;

		int head = getHeadingTo(target, false);

		return head != -1 && head >= 24576 && head <= 40960;
	}

	/**
	 * @return находится ли персонаж в зоне костра.
	 */
	public boolean isInBonfireTerritory() {
		return false;
	}

	/**
	 * Находится ли цель в указанном диапозоне градусов перед персонажем.
	 * 
	 * @param target цель.
	 * @param degree точка градусов.
	 * @param width ширина градусов.
	 * @return находится ли в деапозоне.
	 */
	public final boolean isInDegree(Character target, int degree, int width) {
		int angle = (int) Angles.headingToDegree(getHeadingTo(target, false));

		int min = degree - width;
		int max = degree + width;

		if(min < 0)
			min += 360;

		if(max < 0)
			max += 360;

		boolean flag = angle - degree > 180;

		if(flag)
			angle -= 360;

		if(angle > max)
			return false;

		angle += 360;

		return angle > min;
	}

	/**
	 * Находится ли цель в указанном диапозоне градусов перед персонажем.
	 * 
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param degree точка градусов.
	 * @param width ширина градусов.
	 * @return находится ли в деапозоне.
	 */
	public final boolean isInDegree(float targetX, float targetY, int degree, int width) {
		int angle = (int) Angles.headingToDegree(getHeadingTo(targetX, targetY));

		int min = degree - width;
		int max = degree + width;

		if(min < 0)
			min += 360;

		if(max < 0)
			max += 360;

		boolean flag = angle - degree > 180;

		if(flag)
			angle -= 360;

		if(angle > max)
			return false;

		angle += 360;

		return angle > min;
	}

	/**
	 * Находится ли цель перед лицом
	 * 
	 * @param target цель.
	 */
	public final boolean isInFront(Character target) {
		if(target == null)
			return false;

		int head = getHeadingTo(target, false);

		return head != -1 && head <= 8192 || head >= 57344;
	}

	/**
	 * @return находится ли персонаж в мирной зоне.
	 */
	public boolean isInPeaceTerritory() {
		return false;
	}

	@Override
	public boolean isInRange(float x, float y, float z, float range) {
		return getDistance(x, y, z) - geom.getRadius() <= range;
	}

	/**
	 * Находится ли цель с боку
	 * 
	 * @param target цель.
	 */
	public final boolean isInSide(Character target) {
		if(target == null)
			return false;

		int head = getHeadingTo(target, false);

		return head != -1 && (head >= 8192 && head <= 24576 || head >= 40960 && head <= 57344);
	}

	/**
	 * @return не уязвим липерсонаж.
	 */
	public boolean isInvul() {
		return invul;
	}

	/**
	 * @return блок движения.
	 */
	public final boolean isMoveBlocked() {
		return stuned || rooted;
	}

	/**
	 * @return заблокиравано ли перемещение персонажа.
	 */
	public final boolean isMovementDisabled() {
		if(isDead() || defenseStance || rooted || owerturned || skillMoved || stuned || getRunSpeed() < 1)
			return true;

		Skill castingSkill = getCastingSkill();

		if(castingSkill != null && castingSkill.isBlockingMove())
			return true;

		return false;
	}

	/**
	 * @return находится ли в движении персонаж.
	 */
	public final boolean isMoving() {
		return moving;
	}

	/**
	 * @return находится ли перс на маунте.
	 */
	public boolean isOnMount() {
		return false;
	}

	/**
	 * @return опрокинут ли персонаж.
	 */
	public final boolean isOwerturned() {
		return owerturned;
	}

	/**
	 * @return не восприимчив к опрокидыванию ли.
	 */
	public boolean isOwerturnImmunity() {
		return false;
	}

	/**
	 * @return не восприимчив ли к удочке.
	 */
	public boolean isLeashImmunity() {
		return false;
	}

	/**
	 * @return пвп режим игрока.
	 */
	public boolean isPvPMode() {
		return false;
	}

	/**
	 * Определяет, находится ли персонаж сбоку от цели.
	 * 
	 * @param target цель.
	 * @return находится ли сбоку.
	 */
	public final boolean isSideTarget(TObject target) {
		if(target == null)
			return false;

		if(target.isCharacter()) {
			int head = getHeadingTo(target, true);
			return head != -1 && (head <= 22337 || head >= 43197);
		}

		return false;
	}

	/**
	 * @return заблокировано ли использование скилов.
	 */
	public boolean isSkillBlocking() {
		return skillBlocking;
	}

	/**
	 * Находится ли скил в откате.
	 * 
	 * @param skill проверяемый скил.
	 * @return находится ли в откате.
	 */
	public boolean isSkillDisabled(Skill skill) {
		ReuseSkill reuse = reuseSkills.get(skill.getReuseId());

		if(reuse == null)
			return false;

		return System.currentTimeMillis() < reuse.getEndTime();
	}

	/**
	 * @return находится ли персонаж в перемещении скилом.
	 */
	public final boolean isSkillMoved() {
		return skillMoved;
	}

	/**
	 * @return не восприимчив к усыплениюли.
	 */
	public boolean isSleepImmunity() {
		return false;
	}

	/**
	 * @return отспавнен ли.
	 */
	public boolean isSpawned() {
		return spawned;
	}

	/**
	 * @return оглушен ли персонаж.
	 */
	public final boolean isStuned() {
		return stuned;
	}

	/**
	 * @return невосприимчив к оглушению ли.
	 */
	public boolean isStunImmunity() {
		return false;
	}

	@Override
	public void lock() {
		Locks.lock(effectList, charLock);
	}

	/**
	 * Указание откуда и куда двигаться.
	 * 
	 * @param startX стартовая координата.
	 * @param startY стартовая координата.
	 * @param startZ стартовая координата.
	 * @param heading разворот.
	 * @param type тип перемещения.
	 * @param targetX конечная координата.
	 * @param targetY конечная координата.
	 * @param targetZ конечная координата.
	 * @param broadCastMove отправлять ли пакет окружающим.
	 * @param sendSelfPacket отправлять ли пакет персонажу.
	 */
	public void moveToLocation(float startX, float startY, float startZ, int heading, MoveType type, float targetX, float targetY, float targetZ, boolean broadCastMove, boolean sendSelfPacket) {
		// если это падение, обрабатываем дмг
		if(type == MoveType.JUMP_FALL || type == MoveType.RUN_FALL)
			// обрабатываем урон с падения
			doFall(z, targetZ);
		// если перс на маунте и он залез в воду
		else if(isOnMount() && (type == MoveType.SWIM_RUN || type == MoveType.SWIM_STOP))
			// слазим
			getOffMount();
		else if(type == MoveType.RUN)
			updateCoords();

		// обнавдяем направление
		setHeading(heading);

		// запускаем
		moveNextTask.nextTask(startX, startY, startZ, type, targetX, targetY, targetZ);

		// если нужно окружающим отправляеть пакет, отправляем
		if(broadCastMove)
			broadcastMove(x, y, z, heading, type, targetX, targetY, targetZ, false);
	}

	/**
	 * Создание новой геометрии персонажа.
	 * 
	 * @return новая геометрия.
	 */
	protected Geom newGeomCharacter() {
		throw new IllegalArgumentException("unsupported method.");
	}

	/**
	 * Создание нового обработчика регена ХП.
	 * 
	 * @return новый обработчик.
	 */
	protected Regen newRegenHp() {
		throw new IllegalArgumentException("unsupported method.");
	}

	/**
	 * Создание нового обработчика регена МП.
	 * 
	 * @return новый обработчик.
	 */
	protected Regen newRegenMp() {
		throw new IllegalArgumentException("unsupported method.");
	}

	/**
	 * @return ид следующего каста скила.
	 */
	public int nextCastId() {
		return 0;
	}

	/**
	 * Применяет заготовленный скил.
	 * 
	 * @param skill применяемый скил.
	 */
	public final void nextUse(Skill skill) {
		skillUseTask.nextUse(skill);
	}

	/**
	 * Пропуск урона по слушателям.
	 * 
	 * @param attacker атакующий.
	 * @param skill скил.
	 * @param info информация об атаке.
	 */
	public void onDamage(Character attacker, Skill skill, AttackInfo info) {

		Array<DamageListener> listeners = getDamageListeners();

		if(listeners.isEmpty()) {
			return;
		}

		listeners.readLock();
		try {

			for(DamageListener listener : listeners.array()) {

				if(listener == null) {
					break;
				}

				listener.onDamage(attacker, this, info, skill);
			}

		} finally {
			listeners.readUnlock();
		}
	}

	public Array<DamageListener> getDamageListeners() {
		return damageListeners;
	}

	/**
	 * Обработка блоком щитом.
	 * 
	 * @param attacker атакующий.
	 * @param skill скил.
	 * @param info информация об атаке.
	 */
	public void onShield(Character attacker, Skill skill, AttackInfo info) {
		if(isBlocked(attacker, skill.getImpactX(), skill.getImpactY(), skill)) {
			int limit = (int) calcStat(StatType.MAX_DAMAGE_DEFENSE, 0, attacker, skill);

			int mpConsume = 0;

			Skill defense = getActivateSkill();

			if(defense != null)
				mpConsume = defense.blockMpConsume(info.getDamage());

			if(mpConsume > 0) {
				setCurrentMp(getCurrentMp() - mpConsume);

				// получаем менеджера событий
				ObjectEventManager eventManager = ObjectEventManager.getInstance();

				// обновляем мп персонажу
				eventManager.notifyMpChanged(this);
			}

			info.setDamage(Math.max(0, info.getDamage() - limit));
			info.setBlocked(info.isNoDamage());
		}
	}

	/**
	 * @param func удаляемая шансовая функция.
	 */
	public void removeChanceFunc(ChanceFunc func) {
		chanceFuncs.fastRemove(func);
	}

	/**
	 * @param listener слушатель урона.
	 */
	public void removeDamageListener(DamageListener listener) {
		damageListeners.fastRemove(listener);
	}

	/**
	 * @param listener слушатель смерти.
	 */
	public void removeDieListener(DieListener listener) {
		dieListeners.fastRemove(listener);
	}

	/**
	 * Удаление эффекта у персонажа.
	 * 
	 * @param effect удаляемый эффект.
	 */
	public final void removeEffect(Effect effect) {
		effectList.removeEffect(effect);
	}

	/**
	 * Удаляет из агр листа персонажа для указаного нпс.
	 * 
	 * @param hated персонаж, имеющий хейт на этого персонажа.
	 */
	public final void removeHate(Npc npc) {
		if(npc == null || hateList.isEmpty())
			return;

		// TODO
		hateList.fastRemove(npc);
	}

	/**
	 * Удаление скила.
	 * 
	 * @param skillId удаляемый скил.
	 * @param sendPacket отправлять ли пакет.
	 */
	public void removeSkill(int skillId, boolean sendPacket) {
		Table<IntKey, Skill> current = getSkills();

		Skill old = current.remove(skillId);

		if(old != null) {
			old.getTemplate().removePassiveFuncs(this);
			old.fold();
		}
	}

	/**
	 * Удаление скила.
	 * 
	 * @param skill удаляемый скил.
	 * @param sendPacket отправлять ли пакет.
	 */
	public void removeSkill(Skill skill, boolean sendPacket) {
		Table<IntKey, Skill> current = getSkills();

		Skill old = current.remove(skill.getId());

		if(old != null) {
			old.getTemplate().removePassiveFuncs(this);
			old.fold();
		}
	}

	/**
	 * Удаление скила.
	 * 
	 * @param template удаляемый скил.
	 * @param sendPacket отправлять ли пакет.
	 */
	public void removeSkill(SkillTemplate template, boolean sendPacket) {
		Table<IntKey, Skill> current = getSkills();

		Skill old = current.remove(template.getId());

		if(old != null) {
			old.getTemplate().removePassiveFuncs(this);
			old.fold();
		}
	}

	/**
	 * Удаление скилов.
	 * 
	 * @param templates список скилов.
	 * @param sendPacket отправлять ли пакет.
	 */
	public void removeSkills(SkillTemplate[] templates, boolean sendPacket) {
		Table<IntKey, Skill> current = getSkills();

		DataBaseManager dbManager = DataBaseManager.getInstance();

		for(int i = 0, length = templates.length; i < length; i++) {
			Skill skill = current.remove(templates[i].getId());

			if(skill == null)
				continue;

			dbManager.deleteSkill(this, skill);

			skill.getTemplate().removePassiveFuncs(this);
			skill.fold();
		}
	}

	/**
	 * Удаление функции.
	 * 
	 * @param func удаляемая функция.
	 */
	public final void removeStatFunc(StatFunc func) {
		// если функции нету, выходим
		if(func == null)
			return;

		// получаем индекс в массиве калькулятора
		int ordinal = func.getStat().ordinal();

		writeStatLock.lock();
		try {
			// если калькулятора нету, выходим
			if(calcs[ordinal] == null)
				return;

			// удаляем функцию с калькулятора
			calcs[ordinal].removeFunc(func);
		} finally {
			writeStatLock.unlock();
		}
	}

	/**
	 * Удаление списка функций.
	 * 
	 * @param funcs список функций.
	 */
	public final void removeStatFuncs(StatFunc[] funcs) {
		charLock.lock();
		try {
			for(int i = 0, length = funcs.length; i < length; i++) {
				StatFunc func = funcs[i];

				// если функции нету, пропускаем
				if(func == null)
					continue;

				// получаем индекс в массиве калькулятора
				int ordinal = func.getStat().ordinal();
				// калькулятор статов
				Calculator calc = calcs[ordinal];

				// если калькулятора нету, пропускаем
				if(calc == null)
					continue;

				// удаляем функцию с калькулятора
				calc.removeFunc(func);
			}
		} finally {
			charLock.unlock();
		}
	}

	/**
	 * Удаление с видимости старого объекта.
	 * 
	 * @param object удаляемый объект.
	 * @param type тип удаления.
	 */
	public void removeVisibleObject(TObject object, int type) {
	}

	/**
	 * Отправка сообщения в чат окружающим.
	 * 
	 * @param message отправляемое сообщение.
	 */
	public void sayMessage(String message) {
		sayMessage(getName(), message);
	}

	/**
	 * Отправка сообщения в чат окружающим.
	 * 
	 * @param name имя, от кого отправить.
	 * @param message содержание сообщения.
	 */
	public final void sayMessage(String name, String message) {
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем окружащиз игроков
		Array<Player> players = World.getAround(Player.class, local.getNextPlayerList(), this, 300);

		// если игроков нет, выходим
		if(players.isEmpty())
			return;

		// получаем массив игроков
		Player[] array = players.array();

		// создаем пакет сообщения
		CharSay packet = CharSay.getInstance(name, message, SayType.MAIN_CHAT, objectId, getSubId());

		// увеличиваем счетчик отправок пакетов
		for(int i = 0, length = players.size(); i < length; i++)
			packet.increaseSends();

		// отправляем пакет себе
		sendPacket(packet, true);

		// отправляем пакет окружающим игрокам
		for(int i = 0, length = players.size(); i < length; i++)
			array[i].sendPacket(packet, false);
	}

	/**
	 * Отправка системного сообщения персонажу.
	 * 
	 * @param type тип сообщения.
	 */
	public void sendMessage(MessageType type) {
	}

	/**
	 * Отправка в систем чат сообщения.
	 * 
	 * @param message содержание сообщения.
	 */
	public void sendMessage(String message) {
	}

	/**
	 * Отправить пакет персонажу.
	 * 
	 * @param packet отправляемый пакет.
	 * @param increaseSends увеличивать ли счетчик.
	 */
	public void sendPacket(ServerPacket packet, boolean increaseSends) {
	}

	/**
	 * @param activateSkill активный тогл скил.
	 */
	public final void setActivateSkill(Skill activateSkill) {
		this.activateSkill = activateSkill;
	}

	/**
	 * @param ai АИ персонажа.
	 */
	public void setAi(AI ai) {
		this.ai = ai;
	}

	/**
	 * @param attacking была ли уже успешная атака за каст скила.
	 */
	public void setAttacking(boolean attacking) {
	}

	/**
	 * @param battleStanced находится ли персонаж в боевой стойке.
	 */
	public final void setBattleStanced(boolean battleStanced) {
		this.battleStanced = battleStanced;
	}

	/**
	 * @param castId ид каста скила.
	 */
	public final void setCastId(int castId) {
		this.castId = castId;
	}

	/**
	 * @param castingSkill текущий кастуемый кил.
	 */
	public final void setCastingSkill(Skill castingSkill) {
		this.castingSkill = castingSkill;
	}

	/**
	 * @param chargeLevel уровень заряда.
	 */
	public final void setChargeLevel(int chargeLevel) {
		this.chargeLevel = chargeLevel;
	}

	/**
	 * @param currentHp текущий уровень хп.
	 */
	public void setCurrentHp(int currentHp) {
		if(currentHp > getMaxHp())
			currentHp = getMaxHp();

		if(currentHp < 0)
			currentHp = 0;

		this.currentHp = currentHp;
	}

	/**
	 * @param currentMp текущий уровень мп.
	 */
	public void setCurrentMp(int currentMp) {
		if(currentMp > getMaxMp())
			currentMp = getMaxMp();

		if(currentMp < 0)
			currentMp = 0;

		this.currentMp = currentMp;
	}

	/**
	 * @param defenseStance находился ли в боевой стойке.
	 */
	public final void setDefenseStance(boolean defenseStance) {
		this.defenseStance = defenseStance;
	}

	/**
	 * @param enemy цель.
	 */
	public final void setEnemy(TObject enemy) {
		this.enemy = enemy;
	}

	/**
	 * @param equipment экиперовка.
	 */
	public void setEquipment(Equipment equipment) {
	}

	/**
	 * @param flyingPegas летит ли на пегасе.
	 */
	public final void setFlyingPegas(boolean flyingPegas) {
		this.flyingPegas = flyingPegas;
	}

	/**
	 * @param inventory инвентарь.
	 */
	public void setInventory(Inventory inventory) {
	}

	/**
	 * @param invul неуязвим ли персонаж.
	 */
	public final void setInvul(boolean invul) {
		this.invul = invul;
	}

	/**
	 * @param karma карма.
	 */
	public void setKarma(int karma) {
	}

	/**
	 * @param lastCast время последнего каста скила.
	 */
	public void setLastCast(long lastCast) {
		this.lastCast = lastCast;
	}

	/**
	 * @param lastSkillName имя последнего скастанувшегося скила.
	 */
	public void setLastSkillName(SkillName lastSkillName) {
		this.lastSkillName = lastSkillName;
	}

	/**
	 * @param lockOnSkill активный лок он скил.
	 */
	public void setLockOnSkill(Skill lockOnSkill) {
		this.lockOnSkill = lockOnSkill;
	}

	/**
	 * @param moving находится ли в движении персонаж.
	 */
	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	/**
	 * @param name имя персонажа.
	 */
	public final void setName(String name) {
		if(name == null)
			return;

		this.name = name;
	}

	/**
	 * @param owerturned опрокинут ли персонаж.
	 */
	public final void setOwerturned(boolean owerturned) {
		this.owerturned = owerturned;
	}

	/**
	 * @param owner владелец самона.
	 */
	public void setOwner(Character owner) {
		log.warning(getClass(), "unsupported invoke \"setOwner\".");
	}

	/**
	 * @param pvpMode пвп режим игрока.
	 */
	public void setPvPMode(boolean pvpMode) {
	}

	/**
	 * @param rooted заблокировано ли движение персонажа.
	 */
	public final void setRooted(boolean rooted) {
		if(rooted)
			stopMove();

		this.rooted = rooted;
	}

	/**
	 * @param skillBlocking заблокировано ли использование скилов.
	 */
	public void setSkillBlocking(boolean skillBlocking) {
		this.skillBlocking = skillBlocking;
	}

	/**
	 * @param skillMoved перемещается ли средствами скила.
	 */
	public final void setSkillMoved(boolean skillMoved) {
		this.skillMoved = skillMoved;
	}

	/**
	 * @param sks список скилов.
	 */
	public final void setSkills(Skill[] sks) {
		for(int i = 0, length = sks.length; i < length; i++) {
			Skill skill = sks[i];

			if(!skills.containsKey(skill.getId()))
				skills.put(skill.getId(), skill);
		}
	}

	/**
	 * @param stuned оглушен ли персонаж.
	 */
	public final void setStuned(boolean stuned) {
		if(stuned) {
			abortCast(true);
			stopMove();
		}

		this.stuned = stuned;
	}

	/**
	 * @param target текущая цель.
	 */
	public void setTarget(Character target) {
		this.target = target;
	}

	/**
	 * @param title титул персонажа.
	 */
	public final void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Обработка хила от скила.
	 * 
	 * @param damageId ид хила.
	 * @param heal сила хила.
	 * @param healer хилящий персонаж.
	 */
	public void skillHealHp(int damageId, int heal, Character healer) {
		// если мертв или хила нет, выходим
		if(isDead() || heal < 1)
			return;

		charLock.lock();
		try {
			// применяем
			setCurrentHp(getCurrentHp() + heal);
		} finally {
			charLock.unlock();
		}

		// отрбажаем анимацию хила
		PacketManager.showDamage(healer, this, damageId, heal, false, false, Damage.HEAL);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем хп
		eventManager.notifyHpChanged(this);
	}

	/**
	 * Обработка хила от скила.
	 * 
	 * @param damageId ид хила.
	 * @param heal сила хила.
	 * @param healer хилящий персонаж.
	 */
	public void skillHealMp(int damageId, int heal, Character healer) {
		// если перс мертв или хила нет, выходмим
		if(isDead() || heal < 1)
			return;

		charLock.lock();
		try {
			// применяем хил
			setCurrentMp(getCurrentMp() + heal);
		} finally {
			charLock.unlock();
		}

		// отборжаем анимацию хила
		PacketManager.showDamage(healer, this, damageId, heal, false, false, Damage.MANAHEAL);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем состояние мп
		eventManager.notifyMpChanged(this);
	}

	@Override
	public void spawnMe() {
		super.spawnMe();

		// запускаем обработку движения
		moveNextTask.startTask();

		spawned = true;
	}

	/**
	 * Запуск боевой стойки.
	 * 
	 * @param enemy запустивший боевую стойку персонаж.
	 */
	public boolean startBattleStance(Character enemy) {
		return true;
	}

	/**
	 * Запуск эмоций.
	 */
	public void startEmotions() {
		emotionTask.start();
	}

	/**
	 * Остановка боевой стойки.
	 */
	public void stopBattleStance() {
	}

	/**
	 * Стоп эмоции.
	 */
	public void stopEmotions() {
		emotionTask.stop();
	}

	/**
	 * Останавливает движение.
	 */
	public final void stopMove() {
		// определяем, был ли в движении ерсонаж
		boolean moving = (isMoving() || isCastingNow()) && !owerturned;

		// останавливаем таск
		moveNextTask.stopMove();

		// если персонаж был в движении
		if(moving)
			// отсылаем пакет его остановки
			broadcastMove(x, y, z, heading, MoveType.STOP, x, y, z, true);
	}

	/**
	 * Остановка движения средствами скила.
	 */
	public void stopSkillMove() {
		skillMoveTask.cancel(true);
	}

	/**
	 * Телепорт объекта в указаные координаты.
	 * 
	 * @param continentId ид континента.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 */
	public void teleToLocation(int continentId, float x, float y, float z) {
		teleToLocation(continentId, x, y, z, heading);
	}

	/**
	 * Телепорт объекта в указаные координаты.
	 * 
	 * @param continentId ид континента.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 * @param heading разворот.
	 */
	public void teleToLocation(int continentId, float x, float y, float z, int heading) {
		if(isOnMount())
			getOffMount();

		stopMove();
		setContinentId(continentId);
		setXYZ(x, y, z);
		setHeading(heading);
	}

	/**
	 * Телепорт объекта в указанную локу.
	 * 
	 * @param location точка.
	 */
	public void teleToLocation(Location location) {
		teleToLocation(location.getContinentId(), location.getX(), location.getY(), location.getZ(), location.getHeading() != 0 ? location.getHeading() : heading);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void unlock() {
		Locks.unlock(effectList, charLock);
	}

	/**
	 * Обновление координат.
	 */
	public void updateCoords() {
	}

	/**
	 * Проверяет, может ли щит продолжать удерживаться.
	 */
	public void updateDefense() {

		if(!isDefenseStance() || activateSkill == null || activateSkill.getSkillType() != SkillType.DEFENSE)
			return;

		if(getCurrentMp() < 1) {
			abortCast(true);
		}
	}

	/**
	 * Обновление баф иконок.
	 */
	public void updateEffects() {
	}

	/**
	 * Обновление состояния хп.
	 */
	public void updateHp() {
	}

	/**
	 * Обновление информации о персонаже.
	 */
	public void updateInfo() {
		moveNextTask.update();
	}

	/**
	 * Обновлеие состояния хп.
	 */
	public void updateMp() {
	}

	/**
	 * Обновление отображения отката скила.
	 * 
	 * @param skill обновляемый скил.
	 * @param reuseDelay время отката.
	 */
	public void updateReuse(Skill skill, int reuseDelay) {
	}

	/**
	 * Обновление состояния цсталости.
	 */
	public void updateStamina() {
	}

	/**
	 * @return находится ли в корнях.
	 */
	public boolean isRooted() {
		return rooted;
	}

	/**
	 * @param summon вызванный суммон.
	 */
	public void setSummon(Summon summon) {
		this.summon = summon;
	}

	/**
	 * @return текущий заряжаемый скил.
	 */
	public Skill getChargeSkill() {
		return chargeSkill;
	}

	/**
	 * @param chargeSkill текущий заряжаемый скил.
	 */
	public void setChargeSkill(Skill chargeSkill) {
		this.chargeSkill = chargeSkill;
	}

	/**
	 * @return явлеятся ли персонаж дальним классом.
	 */
	public boolean isRangeClass() {
		return false;
	}

	/**
	 * @return отправляеть пакет завершения каста при коллизии.
	 */
	public boolean isBroadcastEndSkillForCollision() {
		return false;
	}

	/**
	 * @param skill проверяемый скил.
	 * @return содержит ли персонаж эффекты от скила.
	 */
	public boolean containsEffect(Skill skill) {

		EffectList effectList = getEffectList();

		if(effectList == null) {
			return false;
		}

		return effectList.contains(skill);
	}

	/**
	 * @return является ли персонаж убийцей игроков.
	 */
	public boolean isPK() {
		return false;
	}
}

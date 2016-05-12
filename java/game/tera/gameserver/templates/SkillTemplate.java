package tera.gameserver.templates;

import rlib.util.Objects;
import rlib.util.Reloadable;
import rlib.util.Strings;
import rlib.util.VarTable;
import rlib.util.array.Arrays;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.OperateType;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.gameserver.model.skillengine.SkillName;
import tera.gameserver.model.skillengine.SkillRangeType;
import tera.gameserver.model.skillengine.SkillType;
import tera.gameserver.model.skillengine.TargetType;
import tera.gameserver.model.skillengine.funcs.Func;

/**
 * Темплейт скилов.
 *
 * @author Ronn
 */
public final class SkillTemplate implements Reloadable<SkillTemplate>
{
	public static final Skill[] EMPTY_SKILLS = new Skill[0];

	/** ид скила */
	private int id;
	/** ид для пакета */
	private int iconId;
	/** ид для демаг пакета */
	private int damageId;
	/** отступ ид */
	private int offsetId;
	/** откат */
	private int reuseDelay;
	/** ид скила, на чей откат влияет */
	private int reuseId;
	/** задержка */
	private int delay;
	/** задержка при начале перемещения при касте скила */
	private int moveDelay;
	/** ид итема */
	private int itemId;
	/** кол-во итемов */
	private int itemCount;
	/** шанс срабатывания шансового скила */
	private int chance;
	/** ид трансформации */
	private int transformId;
	/** мп потребление */
	private int mpConsume;
	/** хп потребление */
	private int hpConsume;
	/** ид потребляемого итема */
	private int itemIdConsume;
	/** кол-во потребляемого итема */
	private int itemCountConsume;
	/** минимальный ренж для каста скила */
	private int minRange;
	/** время каста */
	private int hitTime;
	/** время отведенное на выполнение перемещения */
	private int moveTime;
	/** скорость выстрела */
	private int speed;
	/** отступ в скорости */
	private int speedOffset;
	/** насколько передвигать персонажа во время каста */
	private int moveDistance;
	/** уровень */
	private int level;
	/** класс ид */
	private int classId;
	/** кол-во кастов */
	private int castCount;
	/** максимальное кол-во целей */
	private int maxTargets;
	/** время жизни */
	private int lifeTime;
	/** стартовое состояние */
	private int startState;
	/** окончательный стейт */
	private int endState;
	/** ид сумона */
	private int summonId;
	/** тип сумона */
	private int summonType;
	/** модификатор стейта */
	private int stateMod;
	/** ид маунта */
	private int mountId;
	/** агр поинты скила */
	private int aggroPoint;
	/** максимальная дистанция для каста */
	private int castMaxRange;
	/** минимальная дистанция для каста */
	private int castMinRange;
	/** направление движения */
	private int moveHeding;

	/** модификатор опрокидывания */
	private float owerturnMod;
	/** мощность регена */
	private float regenPower;
	/** стартовая мощность скила */
	private float startPower;
	/** модификатор урона от заряда */
	private float chargeMod;

	/** игнорирует ли щит */
	private boolean shieldIgnore;
	/** может ли скил опрокинуть */
	private boolean canOwerturn;
	/** отображать ли в скил листе */
	private boolean visibleOnSkillList;
	/** игнорировать проверку фронта для перемещения во время каста скила */
	private boolean ignoreBarrier;
	/** кастуется только во время движения */
	private boolean castToMove;
	/** скил является реализованным */
	private boolean implemented;
	/** является ли скил триггером */
	private boolean trigger;
	/** является ли скил тогглом */
	private boolean toggle;
	/** блокировать ли перемезение игрока на воремя каста скила */
	private boolean blockingMove;
	/** альтернативный ли каст у скила */
	private boolean altCast;
	/** принудительный каст */
	private boolean forceCast;
	/** дается ли уклон на время каста скила */
	private boolean evasion;
	/** кроме кастера */
	private boolean noCaster;
	/** принцип движения */
	private boolean rush;
	/** есть ли ускоренный режим каста */
	private boolean hasFast;
	/** является ли скил мили скилом */
	private boolean shortSkill;
	/** статичное ли время каста скила */
	private boolean staticCast;
	/** статичное ли время интервала */
	private boolean staticInterval;
	/** статичный откат скила */
	private boolean staticReuseDelay;
	/** корректировать ли наведение в цель */
	private boolean correctableTarget;

	/** название */
	private String name;
	/** группа скилов */
	private String group;

	/** название скила */
	private SkillName skillName;
	/** группа скила */
	private SkillGroup skillGroup;
	/** тип скила */
	private SkillType skillType;
	/** тип работы */
	private OperateType operateType;
	/** рендж тип скила */
	private SkillRangeType rangeType;

	/** тип таргета */
	private TargetType[] targetType;
	/** название скилов, после которых идет ускоренный каст */
	private SkillName[] prevSkillNames;

	/** набор эффектов */
	private EffectTemplate[] effectTemplates;

	/** набор условий */
	private Condition condition;

	/** набор функций */
	private Func[] passiveFuncs;
	private Func[] castFuncs;

	/** интервал */
	private int[] interval;
	/** мощность */
	private int[] power;
	/** дальность */
	private int[] range;
	/** ширина дуги */
	private int[] width;
	/** центр дуги */
	private int[] degree;
	/** радиус */
	private int[] radius;
	/** дополнительные скилы, которые уйдут в откат */
	private int[] reuseIds;
	/** направление каста скила */
	private int[] heading;
	/** направление каста скила */
	private int[] castHeading;
	/** стадия скила */
	private int[] stage;

	/** применять ли скил */
	private boolean[] apply;

	/** таблица всех параметров */
	private VarTable vars;

	/** пул скилов этого типа */
	private FoldablePool<Skill> pool;

	/**
	 * @param vars набор параметров скила.
	 * @param effectTemplates набор эффектов скила.
	 * @param condition условие использования скила.
	 * @param passiveFuncs набор функций скила.
	 * @param castFuncs набор функций скила.
	 */
	public SkillTemplate(VarTable vars, EffectTemplate[] effectTemplates, Condition condition, Func[] passiveFuncs, Func[] castFuncs)
	{
		this.id = vars.getInteger("id", 1);
		this.iconId = vars.getInteger("iconId", id);
		this.level = vars.getInteger("level", 1);
		this.mpConsume = vars.getInteger("mpConsume", 0);
		this.hpConsume = vars.getInteger("hpConsume", 0);
		this.itemIdConsume = vars.getInteger("itemIdConsume", 0);
		this.itemCountConsume = vars.getInteger("itemCountConsume", 1);
		this.hitTime = vars.getInteger("hitTime", 0);
		this.reuseDelay = vars.getInteger("reuseDelay", 1);
		this.classId = vars.getInteger("classId");
		this.moveDistance = vars.getInteger("moveDistance", 0);
		this.delay = vars.getInteger("delay", 0);
		this.castCount = vars.getInteger("castCount", 1);
		this.speed = vars.getInteger("speed", 1);
		this.speedOffset = vars.getInteger("speedOffset", 0);
		this.itemId = vars.getInteger("itemId", 0);
		this.itemCount = vars.getInteger("itemCount", 0);
		this.damageId = vars.getInteger("damageId", iconId);
		this.chance = vars.getInteger("chance", 0);
		this.moveDelay = vars.getInteger("moveDelay", 0);
		this.moveTime = vars.getInteger("moveTime", hitTime);
		this.transformId = vars.getInteger("transformId", id);
		this.minRange = vars.getInteger("minRange", 0);
		this.maxTargets = vars.getInteger("maxTargets", -1);
		this.lifeTime = vars.getInteger("lifeTime", 0);
		this.reuseId = vars.getInteger("reuseId", id);
		this.offsetId = vars.getInteger("offsetId", 0);
		this.startState = vars.getInteger("startState", 0);
		this.endState = vars.getInteger("endState", startState);
		this.summonId = vars.getInteger("summonId", 0);
		this.summonType = vars.getInteger("summonType", 0);
		this.stateMod = vars.getInteger("stateMod", 1);
		this.mountId = vars.getInteger("mountId", 0);
		this.aggroPoint = vars.getInteger("aggroPoint", 0);
		this.castMaxRange = vars.getInteger("castMaxRange", getMoveDistance());
		this.castMinRange = vars.getInteger("castMinRange", 0);

		this.regenPower = vars.getFloat("regenPower", 1F);
		this.owerturnMod = vars.getFloat("owerturnMod", 1F);
		this.startPower = vars.getFloat("startPower", 1F);
		this.chargeMod = vars.getFloat("chargeMod", 0.1F);

		this.name = vars.getString("name", "none");
		this.group = vars.getString("group", Strings.EMPTY);

		this.shieldIgnore = vars.getBoolean("shieldIgnore", false);
		this.canOwerturn = vars.getBoolean("canOwerturn", true);
		this.visibleOnSkillList = vars.getBoolean("visibleOnSkillList", true);
		this.ignoreBarrier = vars.getBoolean("ignoreBarrier", false);
		this.implemented = vars.getBoolean("implemented", false);
		this.castToMove = vars.getBoolean("castToMove", false);
		this.blockingMove = vars.getBoolean("blockingMove", true);
		this.altCast = vars.getBoolean("altCast", false);
		this.forceCast = vars.getBoolean("forceCast", false);
		this.evasion = vars.getBoolean("evasion", false);
		this.noCaster = vars.getBoolean("noCaster", false);
		this.rush = vars.getBoolean("rush", false);
		this.hasFast = vars.getBoolean("hasFast", false);
		this.correctableTarget = vars.getBoolean("correctableTarget", false);
		this.shortSkill = vars.getBoolean("shortSkill", false);
		this.staticCast = vars.getBoolean("staticCast", false);
		this.staticInterval = vars.getBoolean("staticInterval", staticCast);
		this.staticReuseDelay = vars.getBoolean("staticReuseDelay", operateType == OperateType.CAST_ITEM || operateType == OperateType.NO_CAST_ITEM);

		this.skillType = SkillType.valueOf(vars.getString("skillType", "DEFAULT"));
		this.skillName = SkillName.valueOf(vars.getString("skillName", "UNKNOWN"));
		this.operateType = OperateType.valueOf(vars.getString("operateType", "ACTIVE"));
		this.skillGroup = vars.getEnum("skillGroup", SkillGroup.class, SkillGroup.NONE);
		this.rangeType = vars.getEnum("rangeType", SkillRangeType.class, SkillRangeType.SHORT_SKILL);

		this.power = vars.getIntegerArray("power", ";", 0);
		this.interval = vars.getIntegerArray("interval", ";", 0);
		this.range = vars.getIntegerArray("range", ";", 0);
		this.radius = vars.getIntegerArray("radius", ";", 0);
		this.width = vars.getIntegerArray("width", ";", 0);
		this.degree = vars.getIntegerArray("degree", ";", 0);
		this.heading = vars.getIntegerArray("heading", ";", 0);
		this.stage = vars.getIntegerArray("stage", ";", -1);
		this.castHeading = vars.getIntegerArray("castHeading", ";", 0);

		this.moveHeding = vars.getInteger("moveHeading", heading[0]);

		this.apply = vars.getBooleanArray("apply", ";", true);

		this.targetType = vars.getEnumArray("targetType", TargetType.class, ";", TargetType.TARGET_NONE);
		this.prevSkillNames = vars.getEnumArray("prevSkillNames", SkillName.class, ";", SkillName.UNKNOWN);

		this.effectTemplates = effectTemplates;
		this.condition = condition;

		this.passiveFuncs = passiveFuncs;
		this.castFuncs = castFuncs;

		String line = vars.getString("reuseIds", Strings.EMPTY);

		if(line != Strings.EMPTY && line.length() > 1)
		{
			String[] ids = line.split(",");

			reuseIds = new int[ids.length];

			for(int i = 0; i < ids.length; i++)
				reuseIds[i] = Integer.parseInt(ids[i]);
		}

		this.toggle = skillType == SkillType.DEFENSE || skillType == SkillType.MOUNT;
		this.pool = Pools.newConcurrentFoldablePool(Skill.class);
		this.vars = vars;

		if(!shortSkill)
			shortSkill = range[0] < 200;
	}

	/**
	 * Выдача функций на время каста скила.
	 *
	 * @param owner персонаж.
	 */
	public void addCastFuncs(Character owner)
	{
		if(castFuncs.length < 1)
			return;

		for(int i = 0, length = castFuncs.length; i < length; i++)
			castFuncs[i].addFuncTo(owner);

		owner.updateInfo();
	}

	/**
	 * Выдача пассивных функций.
	 *
	 * @param owner персонаж.
	 */
	public void addPassiveFuncs(Character owner)
	{
		if(passiveFuncs.length < 1)
			return;

		for(int i = 0, length = passiveFuncs.length; i < length; i++)
			passiveFuncs[i].addFuncTo(owner);
	}

	/**
	 * @return агр поинты от скила.
	 */
	public final int getAggroPoint()
	{
		return aggroPoint;
	}

	/**
	 * @return кол-во кастов за каст.
	 */
	public final int getCastCount()
	{
		return castCount;
	}

	/**
	 * @return смещение направления каста.
	 */
	public final int[] getCastHeading()
	{
		return castHeading;
	}

	/**
	 * @return максимальная дистанция для каста.
	 */
	public final int getCastMaxRange()
	{
		return castMaxRange;
	}

	/**
	 * @return минимальная дистанция для каста.
	 */
	public final int getCastMinRange()
	{
		return castMinRange;
	}

	/**
	 * @return шасн срабатывания тригера.
	 */
	public final int getChance()
	{
		return chance;
	}

	/**
	 * @return модификатор урона от зарядки.
	 */
	public final float getChargeMod()
	{
		return chargeMod;
	}

	/**
	 * @return класс ид скила.
	 */
	public final int getClassId()
	{
		return classId;
	}

	/**
	 * @return условие запуска скила.
	 */
	public final Condition getCondition()
	{
		return condition;
	}

	/**
	 * @return ид урона от скила.
	 */
	public final int getDamageId()
	{
		return damageId;
	}

	/**
	 * @return градус поражения скилом.
	 */
	public final int[] getDegree()
	{
		return degree;
	}

	/**
	 * @return задержка перед ударом скила.
	 */
	public final int getDelay()
	{
		return delay;
	}

	/**
	 * @return набор эффектов.
	 */
	public final EffectTemplate[] getEffectTemplates()
	{
		return effectTemplates;
	}

	/**
	 * @return окончательное состояние скила.
	 */
	public final int getEndState()
	{
		return endState;
	}

	/**
	 * @return группа скила.
	 */
	public final String getGroup()
	{
		return group;
	}

	/**
	 * @return смещение направления.
	 */
	public final int[] getHeading()
	{
		return heading;
	}

	/**
	 * @return время каст скила.
	 */
	public final int getHitTime()
	{
		return hitTime;
	}

	/**
	 * @return кол-во потребляемого хп.
	 */
	public final int getHpConsume()
	{
		return hpConsume;
	}

	/**
	 * @return ид скила для пакета.
	 */
	public final int getIconId()
	{
		return iconId;
	}

	/**
	 * @return ид скила.
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * @return интервал между мультикастом.
	 */
	public final int[] getInterval()
	{
		return interval;
	}

	/**
	 * @return кол-во спавнящегося итемов.
	 */
	public final int getItemCount()
	{
		return itemCount;
	}

	/**
	 * @return кол-во потреблемых итемов.
	 */
	public final int getItemCountConsume()
	{
		return itemCountConsume;
	}

	/**
	 * @return ид спавнящегося итема.
	 */
	public final int getItemId()
	{
		return itemId;
	}

	/**
	 * @return ид потребляемого итема.
	 */
	public final int getItemIdConsume()
	{
		return itemIdConsume;
	}

	/**
	 * @return уровень скила.
	 */
	public final int getLevel()
	{
		return level;
	}

	/**
	 * @return время жизни.
	 */
	public final int getLifeTime()
	{
		return lifeTime;
	}

	/**
	 * @return максимальное кол-во порааемых целей.
	 */
	public final int getMaxTargets()
	{
		return maxTargets;
	}

	/**
	 * @return минимальная дистанцияд ля каста скила.
	 */
	public final int getMinRange()
	{
		return minRange;
	}

	/**
	 * @return ид маунта.
	 */
	public final int getMountId()
	{
		return mountId;
	}

	/**
	 * @return задержка перед началом перемезения скила.
	 */
	public final int getMoveDelay()
	{
		return moveDelay;
	}

	/**
	 * @return дистанция перемещение во время каста скила.
	 */
	public final int getMoveDistance()
	{
		return moveDistance;
	}

	/**
	 * @return направление разворота.
	 */
	public final int getMoveHeding()
	{
		return moveHeding;
	}

	/**
	 * @return время отведенное на выполнение перемещения.
	 */
	public final int getMoveTime()
	{
		return moveTime;
	}

	/**
	 * @return кол-во потребляемого мп.
	 */
	public final int getMpConsume()
	{
		return mpConsume;
	}

	/**
	 * @return имя скила.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return отступ по ид.
	 */
	public final int getOffsetId()
	{
		return offsetId;
	}

	/**
	 * @return тип выполнения.
	 */
	public final OperateType getOperateType()
	{
		return operateType;
	}

	/**
	 * @return модификатор опрокидывания.
	 */
	public final float getOwerturnMod()
	{
		return owerturnMod;
	}

	/**
	 * @return мощность скила.
	 */
	public final int[] getPower()
	{
		return power;
	}

	/**
	 * @return имя скилов, после которых идет ускоренный каст.
	 */
	public final SkillName[] getPrevSkillNames()
	{
		return prevSkillNames;
	}

	/**
	 * @return радиус скила.
	 */
	public final int[] getRadius()
	{
		return radius;
	}

	/**
	 * @return дальность каста.
	 */
	public final int[] getRange()
	{
		return range;
	}

	/**
	 * @return тип скила по дальности приминения.
	 */
	public final SkillRangeType getRangeType()
	{
		return rangeType;
	}

	/**
	 * @return сила регена.
	 */
	public final float getRegenPower()
	{
		return regenPower;
	}

	/**
	 * @return время отката скила.
	 */
	public final int getReuseDelay()
	{
		return reuseDelay;
	}

	/**
	 * @return ид скила, которого откат запустит.
	 */
	public final int getReuseId()
	{
		return reuseId;
	}

	/**
	 * @return набор дополнительных скилов, которые уходят в откат.
	 */
	public final int[] getReuseIds()
	{
		return reuseIds;
	}

	/**
	 * @return группа скилов.
	 */
	public final SkillGroup getSkillGroup()
	{
		return skillGroup;
	}

	/**
	 * @return имя скила.
	 */
	public final SkillName getSkillName()
	{
		return skillName;
	}

	/**
	 * @return тип скила.
	 */
	public final SkillType getSkillType()
	{
		return skillType;
	}

	/**
	 * @return скорость выстрела.
	 */
	public final int getSpeed()
	{
		return speed;
	}

	/**
	 * @return набор стадий скила.
	 */
	public final int[] getStage()
	{
		return stage;
	}

	/**
	 * @return стартовая мощность скила.
	 */
	public final float getStartPower()
	{
		return startPower;
	}

	/**
	 * @return стартовое состояние.
	 */
	public final int getStartState()
	{
		return startState;
	}

	/**
	 * @return мод стейта.
	 */
	public int getStateMod()
	{
		return stateMod;
	}

	/**
	 * @return ид сумона.
	 */
	public int getSummonId()
	{
		return summonId;
	}

	/**
	 * @return тип сумона.
	 */
	public int getSummonType()
	{
		return summonType;
	}

	/**
	 * @return тип цели.
	 */
	public final TargetType[] getTargetType()
	{
		return targetType;
	}

	/**
	 * @return ид трансформации.
	 */
	public final int getTransformId()
	{
		return transformId;
	}

	/**
	 * @return все параметры скила.
	 */
	public final VarTable getVars()
	{
		return vars;
	}

	/**
	 * @return ширина градуса.
	 */
	public final int[] getWidth()
	{
		return width;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;

		int result = 1;

		result = prime * result + classId;
		result = prime * result + id;

		return result;
	}

	/**
	 * @return альтернативный ли каст.
	 */
	public final boolean isAltCast()
	{
		return altCast;
	}

	/**
	 * @return применять ли скил.
	 */
	public final boolean[] isApply()
	{
		return apply;
	}

	/**
	 * @return блоуирует ли каст движение.
	 */
	public final boolean isBlockingMove()
	{
		return blockingMove;
	}

	/**
	 * @return может ли скил опрокинуть.
	 */
	public final boolean isCanOwerturn()
	{
		return canOwerturn;
	}

	/**
	 * @return кастуется ли только во время движения скила.
	 */
	public final boolean isCastToMove()
	{
		return castToMove;
	}

	/**
	 * @return присутствует ли уклонение во время каста скила.
	 */
	public final boolean isEvasion()
	{
		return evasion;
	}

	/**
	 * @return принудительный ли каст.
	 */
	public final boolean isForceCast()
	{
		return forceCast;
	}

	/**
	 * @return есть ли ускоренный каст.
	 */
	public final boolean isHasFast()
	{
		return hasFast;
	}

	/**
	 * @return игнорирует ли препядствия.
	 */
	public final boolean isIgnoreBarrier()
	{
		return ignoreBarrier;
	}

	/**
	 * @return реализован ли скил.
	 */
	public final boolean isImplemented()
	{
		return implemented;
	}

	/**
	 * @return не применять ли на кастера.
	 */
	public final boolean isNoCaster()
	{
		return noCaster;
	}

	/**
	 * @return раш движение.
	 */
	public boolean isRush()
	{
		return rush;
	}

	/**
	 * @return игнорирует ли щит.
	 */
	public final boolean isShieldIgnore()
	{
		return shieldIgnore;
	}

	/**
	 * @return является ли скил мили скилом.
	 */
	public final boolean isShortSkill()
	{
		return shortSkill;
	}

	/**
	 * @return статичное ли время каста.
	 */
	public final boolean isStaticCast()
	{
		return staticCast;
	}

	/**
	 * @return статичен ли интервал.
	 */
	public final boolean isStaticInterval()
	{
		return staticInterval;
	}

	/**
	 * @return является ли скил тоглом.
	 */
	public final boolean isToggle()
	{
		return toggle;
	}

	/**
	 * @return отступ в скорости.
	 */
	public int getSpeedOffset()
	{
		return speedOffset;
	}

	/**
	 * @return является ли скил тригером.
	 */
	public final boolean isTrigger()
	{
		return trigger;
	}

	/**
	 * @return отображать ли скил в скил листе.
	 */
	public final boolean isVisibleOnSkillList()
	{
		return visibleOnSkillList;
	}

	/**
	 * @return новый экземпляр скила.
	 */
	public Skill newInstance()
	{
		Skill skill = pool.take();

		if(skill == null)
			return skillType.newInstance(this);

		return skill;
	}

	/**
	 * Положить скил в пул.
	 *
	 * @param skill уже не используемый скил.
	 */
	public void put(Skill skill)
	{
		pool.put(skill);
	}

	@Override
	public void reload(SkillTemplate update)
	{
		Objects.reload(this, update);
	}

	/**
	 * @return постоянен ли откат скила.
	 */
	public boolean isStaticReuseDelay()
	{
		return staticReuseDelay;
	}

	/**
	 * Выдача функций на время каста скила.
	 *
	 * @param owner персонаж.
	 */
	public void removeCastFuncs(Character owner)
	{
		if(castFuncs.length < 1)
			return;

		for(int i = 0, length = castFuncs.length; i < length; i++)
			castFuncs[i].removeFuncTo(owner);

		owner.updateInfo();
	}

	/**
	 * Выдача пассивных функций.
	 *
	 * @param owner персонаж.
	 */
	public void removePassiveFuncs(Character owner)
	{
		if(passiveFuncs.length < 1)
			return;

		for(int i = 0, length = passiveFuncs.length; i < length; i++)
			passiveFuncs[i].removeFuncTo(owner);
	}

	@Override
	public String toString()
	{
		return "id = " + id + ", classId = " + classId + ", level = " + level + ", name = " + name + ", funcs = " + Arrays.toString(passiveFuncs);
	}

	/**
	 * @return корректировать ли наведение в цель.
	 */
	public boolean isCorrectableTarget()
	{
		return correctableTarget;
	}
}

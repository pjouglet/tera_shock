package tera.gameserver.model.skillengine;

import rlib.util.array.Array;
import rlib.util.pools.Foldable;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Интерфейс для реализации скила
 *
 * @author Ronn
 * @created 12.04.2012
 */
public interface Skill extends Foldable
{
	/** ид пвп скила */
	public static final int PVP_SKILL_ID = 67308865;
	/** класс пвп скила */
	public static final int PVP_SKILL_CLASS = -1;

	/**
	 * Рассчитывает таргеты
	 *
	 * @param targets список целей.
	 * @param attacker атакующий персонаж.
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 */
	public void addTargets(Array<Character> targets, Character attacker, float targetX, float targetY, float targetZ);

	/**
	 * Применение скила на цель
	 *
	 * @param target цель.
	 */
	public AttackInfo applySkill(Character attacker, Character target);

	/**
	 * Конвектирование урона в блок.
	 *
	 * @param info инфа об атаке.
	 */
	public int blockMpConsume(int damage);

	/**
	 * Проверка на соответствие условий для каста скила.
	 *
	 * @param attacker атакующий персонаж.
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 * @return все ли нормально.
	 */
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ);

	/**
	 * Завершение каста скила.
	 *
	 * @param attacker атакующий персонаж.
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 * @param force принудительное ли.
	 */
	public void endSkill(Character attacker, float targetX, float targetY, float targetZ, boolean force);

	/**
	 * Положить скил в пул.
	 */
	public void fold();

	/**
	 * @return агро поинты скила.
	 */
	public int getAggroPoint();

	/**
	 * @return кол-во юза скила за каст.
	 */
	public int getCastCount();

	/**
	 * @return смещение направления каста.
	 */
	public int getCastHeading();

	/**
	 * @return текущий ид каста скила.
	 */
	public int getCastId();

	/**
	 * @return максимальная дистанция для каста.
	 */
	public int getCastMaxRange();

	/**
	 * @return минимальная дистанция для каста.
	 */
	public int getCastMinRange();

	/**
	 * @return шанс срабатывания шансового скила.
	 */
	public int getChance();

	/**
	 * @return класс ид скила.
	 */
	public int getClassId();

	/**
	 * @return условие приминения скила..
	 */
	public Condition getCondition();

	/**
	 * @return ид скила для пакета демеджа.
	 */
	public int getDamageId();

	/**
	 * @return стартовый градус охвата.
	 */
	public int getDegree();

	/**
	 * @return задержка перед юзом скила после начала каста.
	 */
	public int getDelay();

	/**
	 * @return массив эффектов скила.
	 */
	public EffectTemplate[] getEffectTemplates();

	/**
	 * @return группа скила для АИ.
	 */
	public String getGroup();

	/**
	 * @return направление каста скила.
	 */
	public int getHeading();

	/**
	 * @return время полного каста скила.
	 */
	public int getHitTime();

	/**
	 * @return потребление хп скилом.
	 */
	public int getHpConsume();

	/**
	 * @return ид скила для пакета.
	 */
	public int getIconId();

	/**
	 * @return ид скила.
	 */
	public int getId();

	/**
	 * @return целевая координата.
	 */
	public float getImpactX();

	/**
	 * @return целевая координата.
	 */
	public float getImpactY();

	/**
	 * @return целевая координата.
	 */
	public float getImpactZ();

	/**
	 * @return интервал между юзами.
	 */
	public int getInterval();

	/**
	 * @return кол-во итемов.
	 */
	public int getItemCount();

	/**
	 * @return кол-во потребяемых итемов.
	 */
	public long getItemCountConsume();

	/**
	 * @return ид итема.
	 */
	public int getItemId();

	/**
	 * @return ид потребляемго итема.
	 */
	public int getItemIdConsume();

	/**
	 * @return уровень скила.
	 */
	public int getLevel();

	/**
	 * @return максимальное кол-во целей.
	 */
	public int getMaxTargets();

	/**
	 * @return минимальная дистанция для каста скила.
	 */
	public int getMinRange();

	/**
	 * @return задержка перед перемещением.
	 */
	public int getMoveDelay();

	/**
	 * @return дистанция, на которую смещается персонаж во время каста.
	 */
	public int getMoveDistance();

	/**
	 * @return направление движения.
	 */
	public int getMoveHeading();

	/**
	 * @return время необходимое для перемещения.
	 */
	public int getMoveTime();

	/**
	 * @return потребление мп скилом.
	 */
	public int getMpConsume();

	/**
	 * @return название скила.
	 */
	public String getName();

	/**
	 * @return тип каста скила.
	 */
	public OperateType getOperateType();

	/**
	 * @return модификатор шанса опрокидывания.
	 */
	public float getOwerturnMod();

	/**
	 * @return мощность скила.
	 */
	public int getPower();

	/**
	 * @return радиус скила
	 */
	public int getRadius();

	/**
	 * @return дальность скила.
	 */
	public int getRange();

	/**
	 * @return тип скила по дальности приминения.
	 */
	public SkillRangeType getRangeType();

	/**
	 * @return откат скила.
	 */
	public int getReuseDelay(Character caster);

	/**
	 * @return ид скила, которого откат запустит.
	 */
	public int getReuseId();

	/**
	 * @return набор дополнительных скилов, которые уходят в откат.
	 */
	public int[] getReuseIds();

	/**
	 * @return название скила.
	 */
	public SkillName getSkillName();

	/**
	 * @return тип скила.
	 */
	public SkillType getSkillType();

	/**
	 * @return скорость скила.
	 */
	public int getSpeed();

	/**
	 * @return текущая стадия скила.
	 */
	public int getStage();

	/**
	 * @return тип таргета.
	 */
	public TargetType getTargetType();

	/**
	 * @return темплейт скила.
	 */
	public SkillTemplate getTemplate();

	/**
	 * @return ид трансформации.
	 */
	public int getTransformId();

	/**
	 * @return ширинаохвата скила.
	 */
	public int getWidth();

	/**
	 * Определяет, есть ли из пред скилов скил с указанным именем.
	 *
	 * @param skillName имя скила.
	 * @return есть ли с таким.
	 */
	public boolean hasPrevSkillName(SkillName skillName);

	/**
	 * @return активный ли каст у скила
	 */
	public boolean isActive();

	/**
	 * @return альтернативный каст.
	 */
	public boolean isAltCast();

	/**
	 * @return применять ли скил.
	 */
	public boolean isApply();

	/**
	 * @return блокировать ли движение игрока во время каста скила.
	 */
	public boolean isBlockingMove();

	/**
	 * @return можно ли отменить каст.
	 */
	public boolean isCanceable();

	/**
	 * @return может ли скил опрокинуть.
	 */
	public boolean isCanOwerturn();

	/**
	 * @return кастовать только до остановки.
	 */
	public boolean isCastToMove();

	/**
	 * @return находится ли в уклонении персонаж на время каста.
	 */
	public boolean isEvasion();

	/**
	 * @return принудительно ли кастуется.
	 */
	public boolean isForceCast();

	/**
	 * @return есть ли ускоренный режим.
	 */
	public boolean isHasFast();

	/**
	 * @return игнорирует ли он барьеры для перемещения.
	 */
	public boolean isIgnoreBarrier();

	/**
	 * @return реализован ли скил.
	 */
	public boolean isImplemented();

	/**
	 * @return не применять ли на кастера.
	 */
	public boolean isNoCaster();

	/**
	 * @return агрессивный ли скил.
	 */
	public boolean isOffensive();

	/**
	 * @return направленный ли скил.
	 */
	public boolean isOneTarget();

	/**
	 * @return пасивный ли скил.
	 */
	public boolean isPassive();

	/**
	 * @return обрабатывать движение как у раша.
	 */
	public boolean isRush();

	/**
	 * @return игнорирует ли скилл щит.
	 */
	public boolean isShieldIgnore();

	/**
	 * @return является ли скил мили скилом.
	 */
	public boolean isShortSkill();

	/**
	 * @return статичное ли время каста.
	 */
	public boolean isStaticCast();

	/**
	 * @return статичен ли интервал.
	 */
	public boolean isStaticInterval();

	/**
	 * @return является ли скил тоглом.
	 */
	public boolean isToggle();

	/**
	 * @return true если является тригером.
	 */
	public boolean isTrigger();

	/**
	 * @return отображать ли в списке скилов.
	 */
	public boolean isVisibleOnSkillList();

	/**
	 * @return может ли скил быть помещен в ожидающие.
	 */
	public boolean isWaitable();

	/**
	 * @param targetX целевая координата.
	 */
	public void setImpactX(float targetX);

	/**
	 * @param targetY целевая координата.
	 */
	public void setImpactY(float targetY);

	/**
	 * @param targeZ целевая координата.
	 */
	public void setImpactZ(float targetZ);

	/**
	 * Отправка всех необходимых пакетов для соответсвующего скила
	 *
	 * @param attacker атакующий персонаж.
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 */
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ);

	/**
	 * Использование скила на цели.
	 *
	 * @param character кастующий персонаж.
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 */
	public void useSkill(Character character, float targetX, float targetY, float targetZ);

	/**
	 * @return постоянен ли откат скила.
	 */
	public boolean isStaticReuseDelay();

	/**
	 * @return отступ в скорости.
	 */
	public int getSpeedOffset();

	/**
	 * @return корректировать ли наведение в цель.
	 */
	public boolean isCorrectableTarget();

	/**
	 * @return направлен ли скил только на себя.
	 */
	public boolean isTargetSelf();
}

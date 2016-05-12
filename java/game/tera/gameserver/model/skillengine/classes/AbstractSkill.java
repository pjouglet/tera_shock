package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.skillengine.OperateType;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillName;
import tera.gameserver.model.skillengine.SkillRangeType;
import tera.gameserver.model.skillengine.SkillType;
import tera.gameserver.model.skillengine.TargetType;
import tera.gameserver.network.serverpackets.MoveSkill;
import tera.gameserver.network.serverpackets.SkillEnd;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Базовая модель скила.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public abstract class AbstractSkill implements Skill
{
	public static final Skill[] EMPTY_SKILLS = new Skill[0];

	/** ид каста скила */
	protected int castId;
	/** номер приминения в течении каста */
	protected int applyOrder;

	/** целевые координаты */
	protected float impactX;
	protected float impactY;
	protected float impactZ;

	/** темплейт скила */
	protected SkillTemplate template;

	/**
	 * @param set набор параметров скила.
	 * @param effectTemplates набор эффектов скила.
	 * @param condition условие использования скила.
	 * @param funcs набор функций скила.
	 */
	public AbstractSkill(SkillTemplate template)
	{
		this.template = template;
	}

	/**
	 * Добавление агр поинты кастующему для нпс, которые в агр листе у таргета.
	 *
	 * @param caster кастующий.
	 * @param target цель кастующего.
	 * @param aggro кол-во агр поинтов.
	 */
	protected void addAggroTo(Character caster, Character target, int aggro)
	{
		// список НПС, сагренных на персонажа
		Array<Npc> hateList = target.getLocalHateList();

		// если не пуст
		if(!hateList.isEmpty())
		{
			Npc[] array = hateList.array();

			// перебираем сагренные НПС
			for(int i = 0, length = hateList.size(); i < length; i++)
				array[i].addAggro(caster, aggro, false);
		}
	}

	/**
	 * Применение эффектов скила.
	 *
	 * @param caster тот, кто кастует скил.
	 */
	protected void addEffects(Character caster)
	{
		// список возможных эффектов от скила
		EffectTemplate[] effectTemplates = template.getEffectTemplates();

		// если таких нет
		if(effectTemplates == null || effectTemplates.length == 0)
			return;

		// получаем формулы
		Formulas formulas = Formulas.getInstance();

		// перебор
		for(int i = 0, length = effectTemplates.length; i < length; i++)
		{
			EffectTemplate temp = effectTemplates[i];

			// если эффект не только на кастующего или шанс не сработал
			if(!temp.isOnCaster() || formulas.calcEffect(caster, caster, temp, this) < 0)
				continue;

			// активируем эффект
			runEffect(temp.newInstance(caster, caster, template), caster);
		}

		return;
	}

	/**
	 * Применение эффектов скила.
	 *
	 * @param effector тот, кто накладывает эффекты.
	 * @param effected тот, на кого накладывают эффекты.
	 */
	protected void addEffects(Character effector, Character effected)
	{
		// список возможных эффектов от скила
		EffectTemplate[] effectTemplates = template.getEffectTemplates();

		// если таких нет
		if(effectTemplates == null || effectTemplates.length == 0)
			return;

		// получаем формулы
		Formulas formulas = Formulas.getInstance();

		// перебираем темплейты
		for(int i = 0, length = effectTemplates.length; i < length; i++)
		{
			// получаем темплейт
			EffectTemplate temp = effectTemplates[i];

			// если эффект только на кастующего, пропускаем
			if(temp.isOnCaster())
				continue;

			// получаем модификатор шанса
			float mod = formulas.calcEffect(effector, effected, temp, this);

			// если эффект не прошел, пропускаем
			if(mod < 0)
				continue;

			// создаем эффект
			Effect effect = temp.newInstance(effector, effected, template);

			// если у него динамическое время
			if(effect.isDynamicTime())
				// применяем модификатор времени
				effect.setPeriod((int) Math.max(temp.getTime() * Math.min(mod, 1), 1));

			// если динамический каунтер
			if(effect.isDynamicCount())
				// применяем модификатор
				effect.setCount((int) Math.max(temp.getCount() * Math.min(mod, 1), 1));

			// активируем эффект
			runEffect(effect, effected);
		}
	}

	@Override
	public void addTargets(Array<Character> targets, Character attacker, float targetX, float targetY, float targetZ)
	{
		getTargetType().getTargets(targets, this, targetX, targetY, targetZ, attacker);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		return null;
	}

	@Override
	public int blockMpConsume(int damage)
	{
		return 0;
	}

	@Override
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ)
	{
		// если скил в откате, то нельзя его юзать
		if(attacker.isSkillDisabled(this))
			return false;

		// если атакующий в корнях
		if(attacker.isRooted() && (isEvasion() || getMoveDistance() != 0))
		{
			//attacker.sendMessage("Нельзя использовать в обездвиженном состоянии.");
			attacker.sendMessage(MessageType.YOU_CANNOT_USE_THAT_SKILL_AT_THE_MOMENT);
			return false;
		}

		Skill activate = attacker.getActivateSkill();

		// если есть активированный скил, и этот скил не имеет принудительного каста
		if(!isForceCast() && activate != null && activate != this)
		{
			attacker.sendMessage(MessageType.YOU_CANNOT_USE_THAT_SKILL_AT_THE_MOMENT);
			return false;
		}

		// если недостаточно мп
		if(attacker.getCurrentMp() < template.getMpConsume())
		{
			attacker.sendMessage(MessageType.NOT_ENOUGH_MP);
			return false;
		}

		// если недостаточно хп
		if(attacker.getCurrentHp() <= template.getHpConsume() + 1)
		{
			attacker.sendMessage("Not enought HP");
			return false;
		}

		// если есть потребляемые итемы
		if(template.getItemIdConsume() != 0)
		{
			Inventory inventory = attacker.getInventory();

			// если ет инвенторя
			if(inventory == null)
				return false;

			// кол-во необходимых итемов
			int count = inventory.getItemCount(template.getItemIdConsume());

			// если итемов не хватает
			if(template.getItemCountConsume() > count)
			{
				attacker.sendMessage("У вас недостаточно необходимых вещей в инвенторе.");
				return false;
			}
		}

		// условие выполнения скила
		Condition condition = template.getCondition();

		// если оно не выполняется
		if(condition != null && !condition.test(attacker, null, this, 0))
			return false;

		return true;
	}

	@Override
	public void endSkill(Character attacker, float targetX, float targetY, float targetZ, boolean force)
	{
		// отображаем анимацию завершения каста скила
		attacker.broadcastPacket(SkillEnd.getInstance(attacker, castId, template.getIconId()));
		// удаляем функции, которые добавлялись на время каста
		template.removeCastFuncs(attacker);
		// добавляем эффекты на кастера
		addEffects(attacker);
	}

	@Override
	public void finalyze(){}

	@Override
	public void fold()
	{
		template.put(this);
	}

	@Override
	public int getAggroPoint()
	{
		return template.getAggroPoint();
	}

	@Override
	public int getCastCount()
	{
		return template.getCastCount();
	}

	@Override
	public int getCastHeading()
	{
		int[] heading = template.getCastHeading();

		return heading[Math.min(applyOrder, heading.length - 1)];
	}

	@Override
	public int getCastId()
	{
		return castId;
	}

	@Override
	public int getCastMaxRange()
	{
		return template.getCastMaxRange();
	}

	@Override
	public int getCastMinRange()
	{
		return template.getCastMinRange();
	}

	@Override
	public int getChance()
	{
		return template.getChance();
	}

	@Override
	public int getClassId()
	{
		return template.getClassId();
	}

	@Override
	public Condition getCondition()
	{
		return template.getCondition();
	}

	@Override
	public int getDamageId()
	{
		return template.getDamageId();
	}

	@Override
	public int getDegree()
	{
		int[] degrees = template.getDegree();

		return degrees[Math.min(applyOrder, degrees.length - 1)];
	}

	@Override
	public int getDelay()
	{
		return template.getDelay();
	}

	@Override
	public EffectTemplate[] getEffectTemplates()
	{
		return template.getEffectTemplates();
	}

	@Override
	public String getGroup()
	{
		return template.getGroup();
	}

	@Override
	public int getHeading()
	{
		int[] heading = template.getHeading();

		return heading[Math.min(applyOrder, heading.length - 1)];
	}

	@Override
	public int getHitTime()
	{
		return template.getHitTime();
	}

	@Override
	public int getHpConsume()
	{
		return template.getHpConsume();
	}

	@Override
	public int getIconId()
	{
		return template.getIconId();
	}

	@Override
	public int getId()
	{
		return template.getId();
	}

	/**
	 * @return целевая координата.
	 */
	public final float getImpactX()
	{
		return impactX;
	}

	/**
	 * @return целевая координата.
	 */
	public final float getImpactY()
	{
		return impactY;
	}

	/**
	 * @return целевая координата.
	 */
	public final float getImpactZ()
	{
		return impactZ;
	}

	@Override
	public int getInterval()
	{
		int[] intervals = template.getInterval();

		return intervals[Math.min(applyOrder, intervals.length - 1)];
	}

	@Override
	public int getItemCount()
	{
		return template.getItemCount();
	}

	@Override
	public long getItemCountConsume()
	{
		return template.getItemCountConsume();
	}

	@Override
	public int getItemId()
	{
		return template.getItemId();
	}

	@Override
	public int getItemIdConsume()
	{
		return template.getItemIdConsume();
	}

	@Override
	public int getLevel()
	{
		return template.getLevel();
	}

	@Override
	public int getMaxTargets()
	{
		return template.getMaxTargets();
	}

	@Override
	public int getMinRange()
	{
		return template.getMinRange();
	}

	@Override
	public int getMoveDelay()
	{
		return template.getMoveDelay();
	}

	@Override
	public int getMoveDistance()
	{
		return template.getMoveDistance();
	}

	@Override
	public int getMoveHeading()
	{
		return template.getMoveHeding();
	}

	@Override
	public int getMoveTime()
	{
		return template.getMoveTime();
	}

	@Override
	public int getMpConsume()
	{
		return template.getMpConsume();
	}

	@Override
	public String getName()
	{
		return template.getName();
	}

	@Override
	public OperateType getOperateType()
	{
		return template.getOperateType();
	}

	@Override
	public float getOwerturnMod()
	{
		return template.getOwerturnMod();
	}

	@Override
	public int getPower()
	{
		int[] powers = template.getPower();

		return powers[Math.min(applyOrder, powers.length - 1)];
	}

	@Override
	public int getRadius()
	{
		int[] redius = template.getRadius();

		return redius[Math.min(applyOrder, redius.length - 1)];
	}

	@Override
	public int getRange()
	{
		int[] ranges = template.getRange();

		return ranges[Math.min(applyOrder, ranges.length - 1)];
	}

	@Override
	public SkillRangeType getRangeType()
	{
		return template.getRangeType();
	}

	@Override
	public int getReuseDelay(Character caster)
	{
		if(isStaticReuseDelay())
			return template.getReuseDelay();

		// получаем тип скила по дальности приминения
		SkillRangeType rangeType = getRangeType();

		return (int) (template.getReuseDelay() * caster.calcStat(rangeType.getReuseStat(), 1, null, null) * rangeType.getReuseMod());
	}

	@Override
	public int getReuseId()
	{
		return template.getReuseId();
	}

	@Override
	public int[] getReuseIds()
	{
		return template.getReuseIds();
	}

	@Override
	public SkillName getSkillName()
	{
		return template.getSkillName();
	}

	@Override
	public SkillType getSkillType()
	{
		return template.getSkillType();
	}

	@Override
	public int getSpeed()
	{
		return template.getSpeed();
	}

	@Override
	public int getStage()
	{
		int[] stages = template.getStage();

		return stages[Math.min(applyOrder, stages.length - 1)];
	}

	@Override
	public TargetType getTargetType()
	{
		TargetType[] types = template.getTargetType();

		return types[Math.min(applyOrder, types.length - 1)];
	}

	@Override
	public final SkillTemplate getTemplate()
	{
		return template;
	}

	@Override
	public int getTransformId()
	{
		return template.getTransformId();
	}

	@Override
	public int getWidth()
	{
		int[] widths = template.getWidth();

		return widths[Math.min(applyOrder, widths.length - 1)];
	}

	@Override
	public int hashCode()
	{
		return template.getId();
	}

	@Override
	public boolean hasPrevSkillName(SkillName skillName)
	{
		return Arrays.contains(template.getPrevSkillNames(), skillName);
	}

	@Override
	public boolean isActive()
	{
		OperateType operateType = template.getOperateType();

		return operateType == OperateType.ACTIVE || operateType == OperateType.ACTIVATE || operateType == OperateType.CHARGE || operateType == OperateType.LOCK_ON;
	}

	@Override
	public boolean isAltCast()
	{
		return template.isAltCast();
	}

	@Override
	public boolean isApply()
	{
		boolean[] apply = template.isApply();

		return apply[Math.min(applyOrder, apply.length - 1)];
	}

	@Override
	public boolean isBlockingMove()
	{
		return template.isBlockingMove();
	}

	@Override
	public boolean isCanceable()
	{
		return true;
	}

	@Override
	public boolean isCanOwerturn()
	{
		return template.isCanOwerturn();
	}

	@Override
	public boolean isCastToMove()
	{
		return template.isCastToMove();
	}

	@Override
	public boolean isEvasion()
	{
		return template.isEvasion();
	}

	@Override
	public boolean isForceCast()
	{
		return template.isForceCast();
	}

	@Override
	public boolean isHasFast()
	{
		return template.isHasFast();
	}

	@Override
	public boolean isIgnoreBarrier()
	{
		return template.isIgnoreBarrier();
	}

	@Override
	public boolean isImplemented()
	{
		return template.isImplemented();
	}

	@Override
	public boolean isNoCaster()
	{
		return template.isNoCaster();
	}

	@Override
	public boolean isOffensive()
	{
		switch(template.getSkillType())
		{
			case TRANSFORM:
			case TELEPORT_JUMP:
			case SPAWN_ITEM:
			case SPAWN_BONFIRE:
			case RESURRECT:
			case RESTART_BONFIRE:
			case PREPARE_MANAHEAL:
			case PARTY_SUMMON:
			case MANAHEAL_PERCENT:
			case HEAL_PERCENT:
			case HEAL:
			case ITEM_BUFF:
			case JUMP:
			case DEFENSE:
			case LANCER_DEFENSE:
			case MANAHEAL:
			case CLEAR_DEBUFF:
			case CANCEL_OWERTURN:
			case CHARGE_MANA_HEAL:
				return false;
			default:
				break;
		}

		return true;
	}

	@Override
	public boolean isOneTarget()
	{
		switch(getTargetType())
		{
			case TARGET_BACK_RAIL:
			case TARGET_RAIL:
			case TARGET_ONE:
			case TARGET_AREA:
				return true;
			default : return false;
		}
	}

	@Override
	public boolean isPassive()
	{
		return template.getOperateType() == OperateType.PASSIVE;
	}

	@Override
	public boolean isRush()
	{
		return template.isRush();
	}

	@Override
	public boolean isShieldIgnore()
	{
		return template.isShieldIgnore();
	}

	@Override
	public boolean isShortSkill()
	{
		return template.isShortSkill();
	}

	@Override
	public boolean isStaticCast()
	{
		return template.isStaticCast();
	}

	@Override
	public boolean isStaticInterval()
	{
		return template.isStaticInterval();
	}

	@Override
	public boolean isToggle()
	{
		return template.isToggle();
	}

	@Override
	public boolean isTrigger()
	{
		return template.isTrigger();
	}

	@Override
	public boolean isVisibleOnSkillList()
	{
		return template.isVisibleOnSkillList();
	}

	@Override
	public boolean isWaitable()
	{
		return true;
	}

	@Override
	public void reinit(){}

	/**
	 * Метод запускающий эффект.
	 *
	 * @param effect запускаемый эффект.
	 * @param effected тот, на кого будет наложен эффект.
	 */
	protected void runEffect(Effect effect, Character effected)
	{
		// если эффект не мгновеный, добавляем в эффект лист
		if(effect.getPeriod() != 0)
			effected.addEffect(effect);
		// иначе тупо сразу применяем
		else
		{
			// метод старта
			effect.onStart();
			// приминение эффекта
			effect.onActionTime();
			// метод завершения
			effect.onExit();
			// складываем в пул
			effect.fold();
		}
	}

	/**
	 * @param impactX целевая координата.
	 */
	public final void setImpactX(float impactX)
	{
		this.impactX = impactX;
	}

	/**
	 * @param impactY целевая координата.
	 */
	public final void setImpactY(float impactY)
	{
		this.impactY = impactY;
	}

	/**
	 * @param impactZ целевая координата.
	 */
	public final void setImpactZ(float impactZ)
	{
		this.impactZ = impactZ;
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		// выдаем функции, которые работают в течении каста скила
		template.addCastFuncs(attacker);

		// обнуляем порядок каста
		applyOrder = 0;

		// получаем ид каста
		castId = attacker.nextCastId();

		// отображаем начало каста
		attacker.broadcastPacket(SkillStart.getInstance(attacker, template.getIconId(), castId, 0));

		// если скил раш, отображаем рывок
		if(isRush())
		{
			// цель рывка
			Character target = attacker.getTarget();

			// если цель есть, отображаем рывок за целью
			if(target != null)
				attacker.broadcastPacket(MoveSkill.getInstance(attacker, target));
			else
				// иначе рывок в точку
				attacker.broadcastPacket(MoveSkill.getInstance(attacker, targetX, targetY, targetZ));
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " template = " + template;
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		applyOrder++;
	}

	@Override
	public boolean isStaticReuseDelay()
	{
		return template.isStaticReuseDelay();
	}

	@Override
	public int getSpeedOffset()
	{
		return template.getSpeedOffset();
	}

	@Override
	public boolean isCorrectableTarget()
	{
		return template.isCorrectableTarget();
	}

	@Override
	public boolean isTargetSelf()
	{
		return getTargetType() == TargetType.TARGET_SELF;
	}
}

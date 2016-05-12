package tera.gameserver.templates;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Strings;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.EffectType;
import tera.gameserver.model.skillengine.ResistType;
import tera.gameserver.model.skillengine.funcs.Func;

/**
 * Темплейт, описывающий базовые параметры эффекта.
 *
 * @author Ronn
 */
public final class EffectTemplate
{
	private static final Logger log = Loggers.getLogger(EffectTemplate.class);

	public static final EffectTemplate[] EMPTY_TEMPLATES = new EffectTemplate[0];

	/**
	 * @return пустой массив темплейтов.
	 */
	public static final EffectTemplate[] getEmptyTemplates()
	{
		return EMPTY_TEMPLATES;
	}

	/** пул эффектов с этим темплейтом */
	protected final FoldablePool<Effect> effectPool;

	/** контейнер активно использующихся эффектов */
	protected final Array<Effect> activeEffect;

	/** количество повторов эффекта */
	private int count;
	/** время эффекта */
	private int time;
	/** мощность эффекта */
	private int power;
	/** ид эффекта */
	private int id;
	/** шанс наложения */
	private int chance;
	/** предел чего-нибудь */
	private int limit;

	/** нейкое значение эффекта */
	private float value;

	/** является ли дебаффом */
	private boolean debuff;
	/** применяемый эффект на кастующего скил */
	private boolean onCaster;
	/** снимается при атаке */
	private boolean noAttack;
	/** снимается при получении урона */
	private boolean noAttacked;
	/** снимается при опрокидывании */
	private boolean noOwerturn;
	/** динамическое время эффекта */
	private boolean dynamicTime;
	/** динасмический каунтер эффекта */
	private boolean dynamicCount;

	/** конструктор эффектов */
	private EffectType constructor;
	/** тип ресиста к эффекту */
	private ResistType resistType;

	/** набор функций эффекта */
	private Func[] funcs;

	/** стак тип эффекта */
	private String stackType;
	/** опции эффекта */
	private String options;

	/**
	 * @param vars таблица параметров.
	 * @param funcs список функции эффектов.
	 */
	public EffectTemplate(VarTable vars, Func[] funcs)
	{
		this.count = vars.getInteger("count", 1);
		this.time = vars.getInteger("time", 0);
		this.power = vars.getInteger("power", 0);
		this.id = vars.getInteger("id", -1);
		this.chance = vars.getInteger("chance", -1);
		this.limit = vars.getInteger("limit", -1);

		this.value = vars.getFloat("value", 0F);

		this.debuff = vars.getBoolean("debuff", false);
		this.onCaster = vars.getBoolean("onCaster", false);
		this.noAttack = vars.getBoolean("noAttack", false);
		this.noAttacked = vars.getBoolean("noAttacked", false);
		this.noOwerturn = vars.getBoolean("noOwerturn", false);
		this.dynamicCount = vars.getBoolean("dynamicCount", false);
		this.dynamicTime = vars.getBoolean("dynamicTime", false);

		this.stackType = vars.getString("stackType", Strings.EMPTY);
		this.options = vars.getString("options", Strings.EMPTY);

		this.effectPool = Pools.newConcurrentFoldablePool(Effect.class);
		this.activeEffect = Arrays.toConcurrentArray(Effect.class);

		this.constructor = vars.getEnum("type", EffectType.class);
		this.resistType = vars.getEnum("resistType", ResistType.class, ResistType.noneResist);

		this.funcs = funcs;
	}

	/**
	 * @return шанс наложения эффекта.
	 */
	public final int getChance()
	{
		return chance;
	}

	/**
	 * @return конструктор эффекта.
	 */
	public final EffectType getConstructor()
	{
		return constructor;
	}

	/**
	 * @return кол-во повторов эффекта.
	 */
	public final int getCount()
	{
		return count;
	}

	/**
	 * @return список функций эффекта.
	 */
	public final Func[] getFuncs()
	{
		return funcs;
	}

	/**
	 * @return ид эффекта.
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * @return лимит чего-то.
	 */
	public final int getLimit()
	{
		return limit;
	}

	/**
	 * @return опции эффекта.
	 */
	public final String getOptions()
	{
		return options;
	}

	/**
	 * @return сила эффекта.
	 */
	public final int getPower()
	{
		return power;
	}

	/**
	 * @return тип ресиста к эффекту.
	 */
	public final ResistType getResistType()
	{
		return resistType;
	}

	/**
	 * @return стак тип эффекта.
	 */
	public final String getStackType()
	{
		return stackType;
	}

	/**
	 * @return время эффекта.
	 */
	public final int getTime()
	{
		return time;
	}

	/**
	 * @return значение эффекта.
	 */
	public final float getValue()
	{
		return value;
	}

	/**
	 * @return является ли эффект дебаффом.
	 */
	public final boolean isDebuff()
	{
		return debuff;
	}

	/**
	 * @return снимается ли при атаке.
	 */
	public boolean isNoAttack()
	{
		return noAttack;
	}

	/**
	 * @return снимается ли при получении уроеа.
	 */
	public boolean isNoAttacked()
	{
		return noAttacked;
	}

	/**
	 * @return снимается ли при опрокидывании.
	 */
	public boolean isNoOwerturn()
	{
		return noOwerturn;
	}

	/**
	 * @return применяется ли эффект на кастера.
	 */
	public final boolean isOnCaster()
	{
		return onCaster;
	}

	/**
	 * Создает экземпляр эффекта.
	 *
	 * @param effector тот кто наложил эффект.
	 * @param effected тот на кого наложили эффект.
	 * @param skill скилл, который наложил эффект.
	 * @return новый экземпляр эффекта.
	 */
	public final Effect newInstance(Character effector, Character effected, SkillTemplate skill)
	{
		// извлекаем эффект из пула
		Effect effect = effectPool.take();

		// если его нет
		if(effect == null)
			// создаем и возвращаем новый
			return constructor.newInstance(this, effector, effected, skill);

		// если эффект уже используется о_О
		if(activeEffect.contains(effect))
		{
			// сообщаем
			log.warning(new Exception("found duplicate active effect"));
			// возвращаем новый
			return constructor.newInstance(this, effector, effected, skill);
		}

		// добавлям в список используемых
		activeEffect.add(effect);

		// вносим новго эффектора
		effect.setEffector(effector);
		// вносим нового эффектида
		effect.setEffected(effected);

		// возвращаем эффект
		return effect;
	}

	/**
	 * @param effect сохраняемый эффект.
	 */
	public void put(Effect effect)
	{
		// удаляем из активных
		activeEffect.fastRemove(effect);
		// складируем в пул
		effectPool.put(effect);
	}

	/**
	 * @param isDebuff является ли эффект дебафом.
	 */
	public void setDebuff(boolean isDebuff)
	{
		this.debuff = isDebuff;
	}

	/**
	 * @return динамический	ли каутер эффекта.
	 */
	public boolean isDynamicCount()
	{
		return dynamicCount;
	}

	/**
	 * @return динамическое ли время эффекта.
	 */
	public boolean isDynamicTime()
	{
		return dynamicTime;
	}

	@Override
	public String toString()
	{
		return "EffectTemplate effectPool = " + effectPool + ", count = " + count + ", time = " + time + ", power = " + power + ", id = " + id + ", chance = " + chance + ", debuff = " + debuff + ", constructor = " + constructor + ", funcs = " + Arrays.toString(funcs) + ", stackType = " + stackType;
	}
}

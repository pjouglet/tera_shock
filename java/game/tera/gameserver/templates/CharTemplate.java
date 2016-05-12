package tera.gameserver.templates;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Objects;
import rlib.util.Reloadable;
import rlib.util.VarTable;
import tera.gameserver.model.skillengine.funcs.Func;

/**
 * Базовая модель шаблона персонажей.
 *
 * @author Ronn
 */
public abstract class CharTemplate implements Reloadable<CharTemplate>
{
	protected static final Logger log = Loggers.getLogger(CharTemplate.class);

	/** контейнер всех переменных */
	protected final VarTable vars;

	/** набор функций */
	protected Func[] funcs;

	/** базовое макс хп */
	protected int maxHp;
	/** базовое макс мп */
	protected int maxMp;
	/** базовый реген хп */
	protected int regHp;
	/** базовый реген мп */
	protected int regMp;
	/** базовая атака */
	protected int powerFactor;
	/** базовая защита */
	protected int defenseFactor;
	/** базавая мощность опрокидывания */
	protected int impactFactor;
	/** базавая защита от опрокидывания */
	protected int balanceFactor;
	/** базавая скорость атаки */
	protected int atkSpd;
	/** базавая скорость бега */
	protected int runSpd;
	/** базовый шанс крита */
	protected int critRate;
	/** базовая защита от крита */
	protected int critRcpt;
	/** скорость разворота */
	protected int turnSpeed;

	public CharTemplate(VarTable vars, Func[] funcs)
	{
		this.maxHp = vars.getInteger("maxHp");
		this.maxMp = vars.getInteger("maxMp");
		this.regHp = vars.getInteger("regHp", 9);
		this.regMp = vars.getInteger("regMp", 9);
		this.powerFactor = vars.getInteger("powerFactor", 0);
		this.defenseFactor = vars.getInteger("defenseFactor", 0);
		this.impactFactor = vars.getInteger("impactFactor", 50);
		this.balanceFactor = vars.getInteger("balanceFactor", 50);
		this.atkSpd = vars.getInteger("atkSpd");
		this.runSpd = vars.getInteger("runSpd");
		this.critRate = vars.getInteger("critRate", 50);
		this.critRcpt = vars.getInteger("critRcpt", 50);
		this.turnSpeed = vars.getInteger("turnSpeed", 6000);
		this.funcs = funcs;
		this.vars = VarTable.newInstance().set(vars);
	}

	/**
	 * @return базовая скорость атаки.
	 */
	public final int getAtkSpd()
	{
		return atkSpd;
	}

	/**
	 * @return базовая защита от опракидывания.
	 */
	public final int getBalanceFactor()
	{
		return balanceFactor;
	}

	/**
	 * @return базовый шанс крита.
	 */
	public final int getCritRate()
	{
		return critRate;
	}

	/**
	 * @return базовая защита от крита.
	 */
	public final int getCritRcpt()
	{
		return critRcpt;
	}

	/**
	 * @return базовая защита.
	 */
	public final int getDefenseFactor()
	{
		return defenseFactor;
	}

	/**
	 * @return набор функций.
	 */
	public final Func[] getFuncs()
	{
		return funcs;
	}

	/**
	 * @return базовая сила опракидывания.
	 */
	public final int getImpactFactor()
	{
		return impactFactor;
	}

	/**
	 * @return базовый макс хп персонажа.
	 */
	public final int getMaxHp()
	{
		return maxHp;
	}

	/**
	 * @return базовый макс мп персонажа.
	 */
	public final int getMaxMp()
	{
		return maxMp;
	}

	/**
	 * @return ид модели персонажа.
	 */
	public int getModelId()
	{
		return 0;
	}

	/**
	 * @return базовая сила.
	 */
	public final int getPowerFactor()
	{
		return powerFactor;
	}

	/**
	 * @return базовый реген хп.
	 */
	public final int getRegHp()
	{
		return regHp;
	}

	/**
	 * @return базовый реген мп.
	 */
	public final int getRegMp()
	{
		return regMp;
	}

	/**
	 * @return базовая скорость бега.
	 */
	public final int getRunSpd()
	{
		return runSpd;
	}

	/**
	 * @return ид шаблона.
	 */
	public int getTemplateId()
	{
		return 0;
	}

	/**
	 * @return тип шаблона.
	 */
	public int getTemplateType()
	{
		return 0;
	}

	/**
	 * @return скорость разворота персонажа.
	 */
	public int getTurnSpeed()
	{
		return turnSpeed;
	}

	/**
	 * @return таблица всех переменных.
	 */
	public VarTable getVars()
	{
		return vars;
	}

	@Override
	public void reload(CharTemplate update)
	{
		if(getClass() != update.getClass())
			return;

		Objects.reload(this, update);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " maxHp = " + maxHp + ", maxMp = " + maxMp + ", regHp = " + regHp + ", regMp = " + regMp + ", powerFactor = " + powerFactor + ", defenseFactor = " + defenseFactor + ", impactFactor = " + impactFactor + ", balanceFactor = " + balanceFactor + ", atkSpd = " + atkSpd + ", runSpd = " + runSpd + ", critRate = " + critRate + ", critRcpt = " + critRcpt;
	}
}
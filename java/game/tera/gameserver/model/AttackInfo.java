package tera.gameserver.model;

/**
 * Контейнер с информацией об атаке персонажа на персонажа.
 * 
 * @author Ronn
 */
public final class AttackInfo
{
	/** урон */
	private int damage;
	
	/** крит ли */
	private boolean crit;
	/** блок ли */
	private boolean blocked;
	/** опрокидование ли */
	private boolean owerturn;
	
	public AttackInfo()
	{
		super();
	}

	/**
	 * @param damage нанесенный урон.
	 */
	public AttackInfo(int damage)
	{
		this.damage = damage;
	}

	/**
	 * Добавить урона.
	 * 
	 * @param damage кол-во добавления к урону.
	 */
	public void addDamage(int damage)
	{
		this.damage += damage;
	}
	
	/**
	 * Очистка информации.
	 */
	public AttackInfo clear()
	{
		this.damage = 0;
		this.owerturn = false;
		this.crit = false;
		this.blocked = false;
		
		return this;
	}
	
	/**
	 * Поделить урон.
	 * 
	 * @param mod на сколько делить урон.
	 */
	public void divDamage(float mod)
	{
		damage /= mod;
	}
	
	/**
	 * Поделить урон.
	 * 
	 * @param mod на сколько делить урон.
	 */
	public void divDamage(int mod)
	{
		damage /= mod;
	}
	
	/**
	 * @return кол-во нанесенного урона.
	 */
	public int getDamage()
	{
		return damage;
	}
	
	/**
	 * @return урон конвектированный в урон по мп при блокировке удара.
	 */
	public int getDamageMp()
	{
		return damage / 3;
	}
	
	/**
	 * @return заблокирована ли атака.
	 */
	public boolean isBlocked()
	{
		return blocked;
	}
	
	/**
	 * @return критическая ли атака.
	 */
	public boolean isCrit()
	{
		return crit;
	}
	
	/**
	 * @return отсутствует ли урон у атаки.
	 */
	public boolean isNoDamage()
	{
		return damage < 1;
	}
	
	/**
	 * @return опрокидывающая ли атака.
	 */
	public boolean isOwerturn()
	{
		return owerturn;
	}
	
	/**
	 * Умножение урона.
	 * 
	 * @param mod во сколько раз умножить урон.
	 */
	public void mulDamage(float mod)
	{
		damage *= mod;
	}
	
	/**
	 * Умножение урона.
	 * 
	 * @param mod во сколько раз умножить урон.
	 */
	public void mulDamage(int mod)
	{
		damage *= mod;
	}
	
	/**
	 * @param blocked заблокирован ли удар.
	 */
	public void setBlocked(boolean blocked)
	{
		this.blocked = blocked;
	}
	
	/**
	 * @param crit критичиский ли удар.
	 */
	public void setCrit(boolean crit)
	{
		this.crit = crit;
	}
	
	/**
	 * @param damage сколько урона нанесено.
	 */
	public void setDamage(int damage)
	{
		this.damage = damage;
	}

	/**
	 * @param owerturn опракидывающий ли удар.
	 */
	public void setOwerturn(boolean owerturn)
	{
		this.owerturn = owerturn;
	}

	@Override
	public String toString()
	{
		return "AttackInfo damage = " + damage + ", crit = " + crit + ", blocked = " + blocked + ", owerturn = " + owerturn;
	}
}

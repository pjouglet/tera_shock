package tera.gameserver.model.skillengine;

import tera.gameserver.model.Character;

/**
 * Перечисление видов ресистов.
 * 
 * @author Ronn
 */
public enum ResistType
{
	noneResist(null, null),
	owerturnResist(StatType.OWERTURN_POWER, StatType.OWERTURN_RECEPTIVE),
	stunResist(StatType.STUN_POWER, StatType.STUN_RECEPTIVE),
	damageResist(StatType.DAMAGE_POWER, StatType.DAMAGE_RECEPTIVE),
	weakResist(StatType.WEAK_POWER, StatType.WEAK_RECEPTIVE);
	
	/** стат, увеличивающий шанс наложения дебафа */
	private StatType powerStat;
	/** стат, уменьшающий шанс наложения дебафа */
	private StatType rcptStat;
	
	/**
	 * @param powerStat
	 * @param rcptStat
	 */
	private ResistType(StatType powerStat, StatType rcptStat)
	{
		this.powerStat = powerStat;
		this.rcptStat = rcptStat;
	}
	
	/**
	 * Проверка, нужно ли рассчитывать шанс.
	 * 
	 * @param attacker атакующий.
	 * @param attacked атакуемый.
	 * @return считать ли прохождение.
	 */
	public boolean checkCondition(Character attacker, Character attacked)
	{
		switch(this)
		{
			case stunResist: return !attacked.isStunImmunity();
			case owerturnResist: return !(attacked.isOwerturnImmunity() || attacked.isOwerturned());
			default:
				break;
		}
		
		return true;
	}

	/**
	 * @return тип стата, увеличивающий шанс дебафа.
	 */
	public final StatType getPowerStat()
	{
		return powerStat;
	}

	/**
	 * @return тип стата, уменьшающий тип ресиста.
	 */
	public final StatType getRcptStat()
	{
		return rcptStat;
	}
}

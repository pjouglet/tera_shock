package tera.gameserver.model.skillengine;

import java.util.NoSuchElementException;

/**
 * Перечисление видов параметров персонажей.
 *
 * @author Ronn
 */
public enum StatType
{
	/**-----------------------------основные в статусе-----------------------------------*/

	MAX_HP("maxHp"),
	MAX_MP("maxMp"),

	ATTACK("atk"),
	DEFENSE("def"),

	IMPACT("impact"),
	BALANCE("balance"),

	CRITICAL_DAMAGE("cAtk"),
	CRITICAL_RATE("rCrit"),

	CRIT_CHANCE_RECEPTIVE("critChanceRcpt"),

	ATTACK_SPEED("atkSpd"),
	RUN_SPEED("runSpd"),

	BASE_HEART("heart"),

	/**----------------------------------------------------------------------------------------- */

	/**------------------------------------базовые------------------------------------------*/

	POWER_FACTOR("powerFactor"),
	DEFENSE_FACTOR("defenseFactor"),

	IMPACT_FACTOR("impactFactor"),
	BALANCE_FACTOR("balanceFactor"),

	/**-------------------------------------------------------------------------------------------*/

	/** -------------типы воздействий мощность/сопротивление-------------- */

	WEAK_RECEPTIVE("weakRcpt"),
	DAMAGE_RECEPTIVE("dmgRcpt"),
	STUN_RECEPTIVE("stunRcpt"),
	OWERTURN_RECEPTIVE("owerturnRcpt"),

	WEAK_POWER("weakPower"),
	DAMAGE_POWER("dmgPower"),
	STUN_POWER("stunPower"),
	OWERTURN_POWER("owerturnPower"),

	/**--------------------------------------------------------------------------------------------*/


	/**-------------------------------------статы сбора--------------------------------------*/

	COLLECTING_ORE("collectingOre"),
	COLLECTING_PLANT("collectingPlant"),
	COLLECTING_OTHER("collectingOther"),

	/**--------------------------------------------------------------------------------------------*/

	/**-------------------------------------остальные----------------------------------------*/

	AGGRO_MOD("agrMod"),

	SHORT_SKILL_REUSE("shortReuse"),
	RANGE_SKILL_REUSE("rangeReuse"),
	OTHER_SKILL_REUSE("otherReuse"),

	SHORT_SKILL_POWER("shortPower"),
	RANGE_SKILL_POWER("rangePower"),
	OTHER_SKILL_POWER("otherPower"),

	SHORT_SKILL_RECEPTIVE("shortRcpt"),
	RANGE_SKILL_RECEPTIVE("rangeRcpt"),
	OTHER_SKILL_RECEPTIVE("otherReuse"),

	REGEN_HP("regHp"),
	REGEN_MP("regMp"),

	MIN_HEART("minHeart"),
	MIN_HEART_PERCENT("minHeartPercent"),

	ABSORPTION_HP("absHp"),
	ABSORPTION_MP("absMp"),

	GAIN_MP("gainMp"),

	ATTACK_ABSORPTION_MP("atkAbsMp"),
	DEFENSE_ABSORPTION_MP("defAbsMp"),

	ABSORPTION_HP_POWER("absHpPower"),
	ABSORPTION_MP_POWER("absMpPower"),

	ABSORPTION_MP_ON_MAX("absMpOnMax"),

	HEAL_POWER_PERCENT("healPowerPercent"),
	HEAL_POWER_STATIC("healPowerStatic"),

	MAX_DAMAGE_DEFENSE("maxDamDef"),

	FALLING_DAMAGE("fallDam");

	/** ------------------------------------------------------------------------------------*/

	/** кол-во видов параметров */
	public static final int SIZE = values().length;

	/**
	 * Получить тип параметра по названию в хмл.
	 *
	 * @param name название в хмл.
	 * @return тип параметра.
	 */
	public static StatType valueOfXml(String name)
	{
		for(StatType stat : values())
			if(stat.xmlName.equals(name))
				return stat;

		throw new NoSuchElementException("Unknown name '" + name + "' for enum Stats");
	}

	/** название в хмл */
	private String xmlName;

	/**
	 * @param xmlName название параметра в хмл.
	 */
	private StatType(String xmlName)
	{
		this.xmlName = xmlName;
	}

	/**
	 * @return название параметра в хмл.
	 */
	public String getValue()
	{
		return xmlName;
	}
}

package tera.gameserver.model.skillengine;

import tera.Config;

/**
 * Перечисление типов скилов по дальности атаки.
 *
 * @author Ronn
 */
public enum SkillRangeType
{
	SHORT_SKILL(StatType.SHORT_SKILL_POWER, 
			StatType.SHORT_SKILL_RECEPTIVE, 
			StatType.SHORT_SKILL_REUSE, 
			Config.WORLD_SHORT_SKILL_REUSE_MOD),
	
	RANGE_SKILL(StatType.RANGE_SKILL_POWER,
			StatType.RANGE_SKILL_RECEPTIVE, 
			StatType.RANGE_SKILL_REUSE, 
			Config.WORLD_RANGE_SKILL_REUSE_MOD),
	
	OTHER_SKILL(StatType.OTHER_SKILL_POWER, 
			StatType.OTHER_SKILL_RECEPTIVE, 
			StatType.OTHER_SKILL_REUSE, 
			Config.WORLD_OTHER_SKILL_REUSE_MOD);

	/** стат отвечающий за увеличение урона */
	private StatType powerStat;
	/** стат отвечающий за уменьшение урона */
	private StatType rcptStat;
	/** стат отвечающий за откат */
	private StatType reuseStat;
	
	/** модификатор отката */
	private float reuseMod;

	private SkillRangeType(StatType powerStat, StatType rcptStat, StatType reuseStat, float reuseMod)
	{
		this.powerStat = powerStat;
		this.rcptStat = rcptStat;
		this.reuseStat = reuseStat;
		this.reuseMod = reuseMod;
	}

	/**
	 * @return стат отвечающий за урон этого типа скила.
	 */
	public final StatType getPowerStat()
	{
		return powerStat;
	}

	/**
	 * @return стат отвечающий за урон этого типа скила.
	 */
	public final StatType getRcptStat()
	{
		return rcptStat;
	}

	/**
	 * @return стат отвечающий за откат этого типа скила.
	 */
	public final StatType getReuseStat()
	{
		return reuseStat;
	}
	
	/**
	 * @return модификатор отката.
	 */
	public final float getReuseMod()
	{
		return reuseMod;
	}
}

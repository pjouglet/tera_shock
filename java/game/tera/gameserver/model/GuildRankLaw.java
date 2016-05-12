package tera.gameserver.model;

/**
 * Перечисление наборов прав рангов гильдий.
 * 
 * @author Ronn
 */
public enum GuildRankLaw
{
	/** 0 простой мембер */
	MEMBER,
	/** 1 возможность изменять состав гильдии */
	LINE_UP,
	/** 2 возможность лазить в банк */
	BANK,
	/** 3 менять состав + банк */
	LINE_UP_BANK,
	/** 4 трогать титулы */
	TITLE,
	/** 5 менять состав гильдий и титулы */
	LINE_UP_TITLE,
	/** 6 лазить в банк и менять титулы */
	BANK_TITLE,
	/** 7 менять состав гильдии, титулы и лазить в банк */
	LINE_UP_BANK_TITLE,
	/** 8 */
	UNKNOW1,
	/** 9 */
	UNKNOW2,
	/** 10 */
	UNKNOW3,
	/** 11 */
	UNKNOW4,
	/** 12 */
	UNKNOW5,
	/** 13 */
	UNKNOW6,
	/** 14 */
	UNKNOW7,
	/** 15 */
	UNKNOW8,
	/** 16 начинать войну */
	GVG,
	/** 17 менять состав, начинать войну */
	LINE_UP_GVG,
	/** 18 */
	UNKNOW9,
	/** 19 менять состав и ГвГ */
	LINE_UP_BANK_GVG,
	/** 20 титул и ГвГ */
	TITLE_GVG,
	/** 21 изменять состав, титулы и ГвГ */
	LINE_UP_TITLE_GVG,
	/** 22 лазить в банк, титулы и ГвГ */
	BANK_TITLE_GVG,
	/** 23 бан, титулы и ГвГ */
	LINE_UP_BANK_TITLE_GVG,
	/** 24 все можно */
	GUILD_MASTER;
	
	/** массив всех наборов */
	public static final GuildRankLaw[] VALUES = values();
	
	public static GuildRankLaw valueOf(int index)
	{
		if(index < 0 || index >= VALUES.length)
			return MEMBER;
		
		GuildRankLaw rank = VALUES[index];
		
		if(rank.name().contains("UNKNOW"))
			return MEMBER;
		
		return rank;
	}
}

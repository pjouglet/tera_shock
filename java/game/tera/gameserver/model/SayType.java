package tera.gameserver.model;

/**
 * Перечисление типов сообщений в чат.
 *
 * @author Ronn
 */
public enum SayType
{
	/** основной чат */
	MAIN_CHAT,
	/** пати чат */
	PARTY_CHAT,
	/** чат гильдии */
	GUILD_CHAT,
	/** крик */
	SHAUT_CHAT,
	/** трейд чат */
	TRADE_CHAT,
	/** чат группы */
	GROUP_CHAT,
	CLUB_CHAT,
	/** приватный чат */
	PRIVATE_CHAT,
	/** тебе шепчут */
	WHISHPER_CHAT,
	UNKNOWN_1,
	UNKNOWN_2,
	CANAL_1_CHAT,
	CANAL_2_CHAT,
	CANAL_3_CHAT,
	CANAL_4_CHAT,
	CANAL_5_CHAT,
	CANAL_6_CHAT,
	CANAL_7_CHAT,
	CANAL_8_CHAT,
	CANAL_9_CHAT,
	/** поиск группы */
	LOOKING_FOR_GROUP,
	/** обращение внимания */
	NOTICE_CHAT,
	ALARM_CHAT,
	/** анонсы */
	ADMIN_CHAT,
	/** системный чат */
	SYSTEM_CHAT,

	SIMPLE_SYSTEM_CHAT;

	public static final SayType[] VALUES = values();

	/**
	 * Получение типа чата по индексу.
	 *
	 * @param index тип чата.
	 * @return тип чата.
	 */
	public static final SayType valueOf(int index)
	{
		if(index < 0 || index >= VALUES.length)
			return SayType.MAIN_CHAT;

		return VALUES[index];
	}
}

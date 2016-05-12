package tera.gameserver.model.ai.npc;

import rlib.util.Rnd;

/**
 * Пачка сообщений для АИ НПС.
 *
 * @author Ronn
 */
public final class MessagePackage
{
	/** название пакета */
	private String name;

	/** набор сообщений пакета */
	private String[] messages;

	/** лимит массива сообщений */
	private int limit;

	public MessagePackage(String name, String[] messages)
	{
		this.name = name;
		this.messages = messages;
		this.limit = messages.length - 1;
	}

	/**
	 * @return название пакета.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return случайное сообщение.
	 */
	public final String getRandomMessage()
	{
		return messages[Rnd.nextInt(0, limit)];
	}
}

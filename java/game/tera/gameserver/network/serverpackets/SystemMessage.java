package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.MessageType;
import tera.gameserver.network.ServerPacketType;


/**
 * Пакет системного сообщения.
 *
 * @author Ronn
 */
public class SystemMessage extends ServerPacket
{
	private static final SystemMessage instance = new SystemMessage();

	private static final char split = 0x0B;

	public static SystemMessage getInstance(MessageType type)
	{
		SystemMessage packet = (SystemMessage) instance.newInstance();

		packet.builder = new StringBuilder(type.getName());

		return packet;
	}

	public static SystemMessage getInstance(String message)
	{
		SystemMessage packet = (SystemMessage) instance.newInstance();

		packet.builder = new StringBuilder(message);

		return packet;
	}

	/** подготовленная строка */
	private StringBuilder builder;

	public SystemMessage add(String var, String val)
	{
		builder.append(split);
		builder.append(var);
		builder.append(split);
		builder.append(val);

		return this;
	}

	/**
	 * Добавление атакующего.
	 */
	public SystemMessage addAttacker(String name)
	{
		builder.append(split);
		builder.append("attacker");
		builder.append(split);
		builder.append(name);

		return this;
	}

	/**
	 * Добавить итемы в сообщение.
	 */
	public SystemMessage addItem(int id, int count)
	{
		builder.append(split);
		builder.append("ItemName");
		builder.append(split);
		builder.append("@Item:").append(id);
		builder.append(split);
		builder.append("ItemAmount");
		builder.append(split);
		builder.append(count);

		return this;
	}

	/**
	 * Добавить итемы в сообщение.
	 */
	public SystemMessage addItemName(int id)
	{
		builder.append(split);
		builder.append("ItemName");
		builder.append(split);
		builder.append("@Item:").append(id);

		return this;
	}

	public SystemMessage addLoser(String name)
	{
		builder.append(split);
		builder.append("loser");
		builder.append(split);
		builder.append(name);

		return this;
	}

	/**
	 * Добавить кому сколько денег выдано.
	 */
	public SystemMessage addMoney(String name, int count)
	{
		builder.append(split);
		builder.append("UserName");
		builder.append(split);
		builder.append(name);
		builder.append(split);
		builder.append("Money");
		builder.append(split);
		builder.append(count);

		return this;
	}

	public SystemMessage addOpponent(String name)
	{
		builder.append(split);
		builder.append("Opponent");
		builder.append(split);
		builder.append(name);

		return this;
	}

	/**
	 * Добавить сколько денег потрачено.
	 */
	public SystemMessage addPaidMoney(int count)
	{
		builder.append(split);
		builder.append("amount");
		builder.append(split);
		builder.append(count);

		return this;
	}

	/**
	 * Добавить игрока.
	 */
	public SystemMessage addPlayer(String name)
	{
		builder.append(split);
		builder.append("player");
		builder.append(split);
		builder.append(name);

		return this;
	}

	public SystemMessage addProf(int count)
	{
		builder.append(split);
		builder.append("prof");
		builder.append(split);
		builder.append(count);

		return this;
	}

	public SystemMessage addQuestName(int id)
	{
		builder.append(split);
		builder.append("QuestName");
		builder.append(split);
		builder.append("@quest:");
		builder.append(id).append("001");

		return this;
	}

	public SystemMessage addQuestName(String name)
	{
		builder.append(split);
		builder.append("QuestName");
		builder.append(split);
		builder.append(name);

		return this;
	}

	public SystemMessage addRequestor(String name)
	{
		builder.append(split);
		builder.append("requestor");
		builder.append(split);
		builder.append(name);

		return this;
	}

	public SystemMessage addSkillName(String name)
	{
		builder.append(split);
		builder.append("SkillName");
		builder.append(split);
		builder.append(name);

		return this;
	}

	public SystemMessage addTarget(String name)
	{
		builder.append(split);
		builder.append("target");
		builder.append(split);
		builder.append(name);

		return this;
	}

	public SystemMessage addUserName(String name)
	{
		builder.append(split);
		builder.append("UserName");
		builder.append(split);
		builder.append(name);

		return this;
	}

	public SystemMessage addWinner(String name)
	{
		builder.append(split);
		builder.append("winner");
		builder.append(split);
		builder.append(name);

		return this;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SYSTEM_MESSAGE;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeShort(buffer, 6);
		writeStringBuilder(buffer, builder);
	}
}

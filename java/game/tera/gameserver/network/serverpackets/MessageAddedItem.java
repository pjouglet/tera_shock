package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;

/**
 * Пакет системного сообщения.
 *
 * @author Ronn
 */
public class MessageAddedItem extends ServerPacket
{
	private static final MessageAddedItem instance = new MessageAddedItem();

	private static final char split = 0x0B;

	public static MessageAddedItem getInstance(String name, int itemId, int itemCount)
	{
		MessageAddedItem packet = (MessageAddedItem) instance.newInstance();

		StringBuilder builder = new StringBuilder();

		builder.append("@379");
		builder.append(split);
		builder.append("UserName");
		builder.append(split);
		builder.append(name);
		builder.append(split);
		builder.append("ItemAmount");
		builder.append(split);
		builder.append(itemCount);
		builder.append(split);
		builder.append("ItemName");
		builder.append(split);
		builder.append("@item:").append(itemId);

		packet.itemId = itemId;
		packet.builder = builder;

		return packet;
	}

	/** подготовленная строка */
	private StringBuilder builder;

	/** ид итема */
	private int itemId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.MESSAGE_ADD_ITEM;
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
		writeShort(buffer, 32);
		writeInt(buffer, itemId);//ид итема
		writeInt(buffer, 0);
		writeLong(buffer, 1);
		writeLong(buffer, 0);
		writeShort(buffer, 0);
		writeString(buffer, builder.toString());
	}
}

package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;

public class FFStructure extends ServerConstPacket
{
	private static final FFStructure instance = new FFStructure();

	public static FFStructure getInstance()
	{
		return instance;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.FF_STRUCTURE;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);

		writeShort(buffer, 5);
		writeShort(buffer, 22);
		writeInt(buffer, 1);
		writeInt(buffer, 0);

		writeInt(buffer, 60);
		writeShort(buffer, 0);

		writeShort(buffer, 22);
		writeShort(buffer, 34);
		writeInt(buffer, 0);
		writeInt(buffer, 0);

		writeShort(buffer, 34);
		writeShort(buffer, 46);//2E00
		writeInt(buffer, -1);
		writeInt(buffer, 0);

		writeShort(buffer, 46);
		writeShort(buffer, 58);
		writeInt(buffer, -1);
		writeInt(buffer, 0);

		writeShort(buffer, 58); //3A00
		writeShort(buffer, 70);
		writeInt(buffer, -1);
		writeInt(buffer, 0);

		writeShort(buffer, 70); //46000000
		writeShort(buffer, 0); //46000000
		writeInt(buffer, -1);
		writeInt(buffer, 0);
	}
}
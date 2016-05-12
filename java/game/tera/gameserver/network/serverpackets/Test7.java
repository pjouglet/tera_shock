package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;

public class Test7 extends ServerConstPacket
{
	private static final Test7 instance = new Test7();

	public static Test7 getInstance()
	{
		return instance;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.TEST_7;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
        writeShort(buffer, 6);//06 00 5E 00 5F 00 5E 00 00 00
        writeShort(buffer, 94);//
        writeShort(buffer, 95);//
        writeShort(buffer, 94);//
        writeShort(buffer, 0);//
	}
}
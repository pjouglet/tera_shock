package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;

public class Test25 extends ServerConstPacket
{
	private static final Test25 instance = new Test25();

	public static Test25 getInstance()
	{
		return instance;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.TEST_25;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeInt(buffer, 0x00000000);
	}
}
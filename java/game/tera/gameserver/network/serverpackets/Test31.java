package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

public class Test31 extends ServerPacket
{
	private static final ServerPacket instance = new Test31();
	
	public static Test31 getInstance()
	{
		return (Test31) instance.newInstance();
	}
	
	private Test31()
	{
		super();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.TEST_24;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		writeInt(0x00000000);
		writeInt(0x00000000);
	}
}

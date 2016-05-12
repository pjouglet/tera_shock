package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

public class Test26 extends ServerPacket
{
	private static final ServerPacket instance = new Test26();
	
	public static Test26 getInstance()
	{
		return (Test26) instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.TEST_26;
	}

	@Override
	protected final void writeImpl()
	{
		writeShort(0x5415); 
		  writeInt(0);
	}
}
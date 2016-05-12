package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * @author Ronn
 */
public class AuthSuccessful2 extends ServerPacket
{
	private static final ServerPacket instance = new AuthSuccessful2();
	
	public static ServerPacket getInstance()
	{
		return instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.AUTH_SUCCESFUL_2;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeLong(0x0000000200000001L);
		writeShort(0);
		writeByte(1); 
	}
}
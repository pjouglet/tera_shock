package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * @author Ronn
 */
public class AuthFailed extends ServerPacket 
{
	private static final ServerPacket instance = new AuthFailed();
	
	public static AuthFailed getInstance()
	{
		return (AuthFailed) instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.AUTH_FAILED;
	}

	@Override
	protected void writeImpl() 
	{
        writeOpcode();
        writeInt(0x01010000);
        writeInt(0x00008000);
		writeShort(0);
		writeByte(0);
	}
}
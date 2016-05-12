package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

public class CharacterQuitCancelOk extends ServerPacket
{	
	private static final ServerPacket instance = new CharacterQuitCancelOk();
	
	public static CharacterQuitCancelOk getInstance()
	{
		return (CharacterQuitCancelOk) instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_EXIT_CANCEL_OK;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeByte(0x00); // 00 - Successful.
	}
}
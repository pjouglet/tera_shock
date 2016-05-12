package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

public class CharacterRestartCancelOk extends ServerPacket
{
	private static final ServerPacket instance = new CharacterRestartCancelOk();
	
	public static CharacterRestartCancelOk getInstance()
	{
		return (CharacterRestartCancelOk) instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_EXIT_OK;
	}
	
	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeByte(0x00); // 00 - Successful.
	}
}
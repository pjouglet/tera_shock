package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

public class CharacterQuitOk extends ServerPacket
{
	private static final ServerPacket instance = new CharacterQuitOk();
	
	public static CharacterQuitOk getInstance(int state)
	{
		CharacterQuitOk packet = (CharacterQuitOk) instance.newInstance();
		
		packet.state = state;
		
		return packet;
	}
	
	private int state;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_EXIT_OK;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		
		if(state == 1)
			writeInt(0x0A000000);
	}
}
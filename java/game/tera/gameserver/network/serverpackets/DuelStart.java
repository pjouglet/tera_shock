package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет начала дуэли.
 * 
 * @author Ronn
 */
public class DuelStart extends ServerPacket
{
	private static final ServerPacket instance = new DuelStart();
	
	public static DuelStart getInstance()
	{
		return (DuelStart) instance.newInstance();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.DUEL_START;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeShort(0x1388);
	}
}
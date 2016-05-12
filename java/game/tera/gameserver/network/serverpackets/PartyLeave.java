package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, с уведомлением о том, что игрок вышел из пати.
 * 
 * @author Ronn
 */
public class PartyLeave extends ServerPacket
{
	private static final ServerPacket instance = new PartyLeave();
	
	public static PartyLeave getInstance()
	{
		return (PartyLeave) instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PARTY_LEAVE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
	}
}

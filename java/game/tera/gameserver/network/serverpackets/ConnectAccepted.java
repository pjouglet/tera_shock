package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Пакет подтверждающий соединение с сервером
 *
 * @author Ronn
 * @created 31.03.2012
 */
public class ConnectAccepted extends ServerPacket
{
	private static final ServerPacket instance = new ConnectAccepted();
	
	public static ConnectAccepted getInstance()
	{
		return (ConnectAccepted) instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CONNECT_SUCCESSFUL;
	}
	
	@Override
	protected void writeImpl()
	{
		writeInt(1);
	}
}
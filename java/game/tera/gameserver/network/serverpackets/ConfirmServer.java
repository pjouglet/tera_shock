package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;


/**
 * Пакет ответа на запрос проверки сервера
 * 
 * @author Ronn
 * @created 25.03.2012
 */
public class ConfirmServer extends ServerPacket
{
	private static final ServerPacket instance = new ConfirmServer();
	
	public static ConfirmServer getInstance(int index)
	{
		ConfirmServer packet = (ConfirmServer) instance.newInstance();
		
		packet.index = index;
		
		return packet;
	}
	
	/** индекс подтверждения */
	private int index;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.REQUEST_CHECKING_SERVER;
	}
	
	@Override
	public boolean isSynchronized()
	{
		return false;
	}
	
	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeInt(buffer, index);
		writeByte(buffer, 1);
	}
}
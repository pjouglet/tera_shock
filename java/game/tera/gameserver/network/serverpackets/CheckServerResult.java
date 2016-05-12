package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;


/**
 * Пакет ответа на запрос проверки сервера
 * 
 * @author Ronn
 * @created 25.03.2012
 */
public class CheckServerResult extends ServerPacket
{
	private static final ServerPacket instance = new CheckServerResult();
	
	public static CheckServerResult getInstance(int[] vals)
	{
		CheckServerResult packet = (CheckServerResult) instance.newInstance();
		
		packet.vals[0] = vals[0];
		packet.vals[1] = vals[1];
		packet.vals[2] = vals[2];
		
		return packet;
	}
	
	/** индекс подтверждения */
	private int[] vals;
	
	public CheckServerResult()
	{
		this.vals = new int[3];
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHECK_SERVER_RESULT;
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
		writeByte(buffer, 0);//00 
		writeInt(buffer, vals[0]);//01 00 00 00  число 1
		writeInt(buffer, vals[1]);//05 00 00 00  число 2
		writeInt(buffer, vals[2]);//2C 00 00 00  число 3
	}
}
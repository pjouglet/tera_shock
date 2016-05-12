package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;


/**
 * Серверный пакет с прогрессом сбора ресурсов.
 * 
 * @author Ronn
 */
public class ResourseCollectProgress extends ServerPacket
{
	private static final ServerPacket instance = new ResourseCollectProgress();
	
	public static ResourseCollectProgress getInstance(int progress)
	{
		ResourseCollectProgress packet = (ResourseCollectProgress) instance.newInstance();
		
		packet.progress = progress;
		
		return packet;
	}
	
	/** сколько % уже выполнено */
	private int progress;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.RESOURSE_COLLECT_PROGRESS;
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
		writeInt(buffer, progress);//18 00 00 00 ПРогресс сбора в процентах
	}
}

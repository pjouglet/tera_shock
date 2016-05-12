package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.network.ServerPacketType;


/**
 * Серверный пакет со стартом сбора ресурсов.
 * 
 * @author Ronn
 */
public class ResourseStartCollect extends ServerPacket
{
	private static final ServerPacket instance = new ResourseStartCollect();
	
	public static ResourseStartCollect getInstance(Character collector, ResourseInstance resourse)
	{
		ResourseStartCollect packet = (ResourseStartCollect) instance.newInstance();
		
		packet.collectorId = collector.getObjectId();
		packet.collectorSubId = collector.getSubId();
		packet.resourseId = resourse.getObjectId();
		packet.resourseSubId = resourse.getSubId();
		
		return packet;
	}
	
	/** обджект ид сборщика */
	private int collectorId;
	/** саб ид сборщика */
	private int collectorSubId;
	/** обджект ид ресурса */
	private int resourseId;
	/** саб ид ресурса */
	private int resourseSubId;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.RESOURSE_START_COLLECT;
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
		writeInt(buffer, collectorId);//AA 6C 0D 00 //обжект ид кто собирает
		writeInt(buffer, collectorSubId);//00 80 00 01 //саб ид того кто собирает
		writeInt(buffer, resourseId);//3B 95 07 00 //обжект ид растения
		writeInt(buffer, resourseSubId);//00 80 04 00 //саб ид растение
	}
}

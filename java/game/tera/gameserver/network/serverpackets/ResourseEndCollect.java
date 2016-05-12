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
public class ResourseEndCollect extends ServerPacket
{
	private static final ServerPacket instance = new ResourseEndCollect();
	
	/** сбор был успешен */
	public static final int SUCCESSFUL = 3;
	/** сбор был просран */
	public static final int FAILED = 2;
	/** сбор был прерван */
	public static final int INTERRUPTED = 0;
	
	public static ResourseEndCollect getInstance(Character collector, ResourseInstance resourse, int result)
	{
		ResourseEndCollect packet = (ResourseEndCollect) instance.newInstance();
		
		packet.collectorId = collector.getObjectId();
		packet.collectorSubId = collector.getSubId();
		packet.resourseId = resourse.getObjectId();
		packet.resourseSubId = resourse.getSubId();
		packet.result = result;
		
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
	/** результат */
	private int result;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.RESOURSE_END_COLLECT;
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
		writeInt(buffer, result);//03 00 00 00
	}
}

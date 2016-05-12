package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.network.ServerPacketType;
import tera.util.Location;


/**
 * Серверный пакет с информацией об ресурсе.
 * 
 * @author Ronn
 */
public class ResourseInfo extends ServerPacket
{
	private static final ServerPacket instance = new ResourseInfo();
	
	public static ResourseInfo getInstance(ResourseInstance resourse)
	{
		ResourseInfo packet = (ResourseInfo) instance.newInstance();
		
		packet.objectId = resourse.getObjectId();
		packet.subId = resourse.getSubId();
		packet.templateId = resourse.getTemplateId();
		
		resourse.getLoc(packet.loc);
		
		return packet;
	}
	
	/** обджект ид нпс */
	private int objectId;
	/** саб ид нпс */
	private int subId;
	/** ид темплейта */
	private int templateId;
	
	/** координаты нпс */
	private Location loc;
	
	public ResourseInfo()
	{
		super();
		
		this.loc = new Location();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.RESOURSE_INFO;
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
		writeInt(buffer, objectId);//EF F7 04 00 обжект ид
		writeInt(buffer, subId);//00 80 04 00 саб ид
		writeInt(buffer, templateId);//90 01 00 00 ид растения/камня
		writeInt(buffer, 1);//01 00 00 00 //??
		writeFloat(buffer, loc.getX());//D1 6A 13 C6 
		writeFloat(buffer, loc.getY());//F4 99 05 C6 
		writeFloat(buffer, loc.getZ());//20 97 10 44
	}
}

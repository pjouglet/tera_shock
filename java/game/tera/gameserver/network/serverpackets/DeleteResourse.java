package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.network.ServerPacketType;


/**
 * Серверный пакет для удаления объекта.
 * 
 * @author Ronn
 */
public class DeleteResourse extends ServerPacket
{
	/** гибель */
	public static final int DEAD = 5;
	/** исчезает с пылью под ногами */
	public static final int DISAPPEARS_DUST = 4;
	/** просто исчезает */
	public static final int DISAPPEARS = 1;
	
	private static final ServerPacket instance = new DeleteResourse();
	
	public static DeleteResourse getInstance(ResourseInstance resourse, int type)
	{
		DeleteResourse packet = (DeleteResourse) instance.newInstance();
		
		packet.type = type;
		packet.objectId = resourse.getObjectId();
		packet.subId = resourse.getSubId();
		
		return packet;
	}
	
	/** тип удаления */
	private int type;
	/** обджект ид */
	private int objectId;
	/** саб ид */
	private int subId;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.RESOURSE_REMOVE;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}
	
	@Override
	protected final void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeInt(buffer, objectId);//3B 95 07 00 //обжект ид
		writeInt(buffer, subId);//00 80 04 00 //саб ид
		writeByte(buffer, type);//01 //ид анимации
	}
}
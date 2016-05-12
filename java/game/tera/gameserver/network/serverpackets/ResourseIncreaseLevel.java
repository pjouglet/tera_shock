package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.resourse.ResourseType;
import tera.gameserver.network.ServerPacketType;


/**
 * Серверный пакет с увеличением уровня сбора ресурсов.
 * 
 * @author Ronn
 */
public class ResourseIncreaseLevel extends ServerPacket
{
	private static final ServerPacket instance = new ResourseIncreaseLevel();
	
	public static ResourseIncreaseLevel getInstance(ResourseType type, int level)
	{
		ResourseIncreaseLevel packet = (ResourseIncreaseLevel) instance.newInstance();
		
		packet.level = level;
		packet.type = type;
		
		return packet;
	}
	
	/** тип ресурса */
	private ResourseType type;
	
	/** уровень навыка */
	private int level;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.RESOURSE_INCREASE_LEVEL;
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
		writeInt(buffer, type.ordinal());
		writeShort(buffer, level); //06 00 кол-во
	}
}

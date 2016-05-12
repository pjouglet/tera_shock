package tera.gameserver.network.serverpackets;

import tera.gameserver.model.worldobject.WorldObject;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с инфой об удалении объекта.
 * 
 * @author Ronn
 */
public class DeleteWorldObject extends ServerPacket
{
	private static final ServerPacket instance = new DeleteWorldObject();
	
	public static DeleteWorldObject getInstance(WorldObject object)
	{
		DeleteWorldObject packet = (DeleteWorldObject) instance.newInstance();
		
		packet.objectId = object.getObjectId();
		packet.subId = object.getSubId();
		
		return packet;
	}
	
	/** обджект ид объекта */
	private int objectId;
	/** саб ид объекта */
	private int subId;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.BONFIRE_DELETE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(objectId);//4A 50 0C 00 обжект ид
		writeInt(subId);//00 80 0E 00 саб ид  
		writeByte(1);//01    
	}
}

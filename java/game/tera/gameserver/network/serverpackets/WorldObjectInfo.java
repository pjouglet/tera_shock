package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.worldobject.WorldObject;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с минфой об объекте.
 *
 * @author Ronn
 */
public class WorldObjectInfo extends ServerPacket
{
	private static final ServerPacket instance = new WorldObjectInfo();

	public static WorldObjectInfo getInstance(WorldObject object)
	{
		WorldObjectInfo packet = (WorldObjectInfo) instance.newInstance();

		packet.objectId = object.getObjectId();
		packet.subId = object.getSubId();
		packet.x = object.getX();
		packet.y = object.getY();
		packet.z = object.getZ();

		return packet;
	}

	/** обджект ид объекта */
	private int objectId;
	/** саю ид объекта */
	private int subId;

	/** координаты объекта */
	private float x;
	private float y;
	private float z;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.BONFIRE_INFO;
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
		writeInt(buffer, 0);//00 00 00 00
		writeInt(buffer, objectId);//4A 50 0C 00 обжект ид
		writeInt(buffer, subId);//00 80 0E 00 саб ид
		writeInt(buffer, 1);//01 00 00 00
		writeFloat(buffer, x);//66 45 81 47 x
		writeFloat(buffer, y);//83 C5 9E C7 y
		writeFloat(buffer, z);//8C 19 50 C5 z
		writeInt(buffer, 0);//00 00 00 00
	}
}

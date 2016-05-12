package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет о перемещении персонажа с одной точки в другую
 *
 * @author Ronn
 */
public class CharClimb extends ServerPacket
{
	private static final ServerPacket instance = new CharClimb();

	public static CharClimb getInstance(Character actor, int heading, float targetX, float targetY, float targetZ)
	{
		CharClimb packet = (CharClimb) instance.newInstance();

		packet.objectId = actor.getObjectId();
		packet.subId = actor.getSubId();
		packet.x = actor.getX();
		packet.y = actor.getY();
		packet.z = actor.getZ();
		packet.heading = heading;
		packet.targetX = targetX;
		packet.targetY = targetY;
		packet.targetZ = targetZ;

		return packet;
	}

	/** обджект ид */
	private int objectId;
	/** саб ид */
	private int subId;

	private float x;
	private float y;
	private float z;

	/** направление */
	private int heading;

	/** конечная точка */
	private float targetX;
	private float targetY;
	private float targetZ;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHAR_CLIMB;
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
		//ид обьекта
		writeInt(buffer, objectId);
		writeInt(buffer, subId);

		//начальная точка
		writeFloat(buffer, x);
		writeFloat(buffer, y);
		writeFloat(buffer, z);
		//поворот
		writeShort(buffer, heading);
		//конечная точка
		writeFloat(buffer, targetX);
		writeFloat(buffer, targetY);
		writeFloat(buffer, targetZ);

		//тип движения
		writeByte(buffer, 0);
	}
}
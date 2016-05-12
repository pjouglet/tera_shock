package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;


/**
 * Серверный пакет, сообщающий о смерти персонажа.
 *
 * @author Ronn
 */
public class CharDead extends ServerPacket
{
	private static final ServerPacket instance = new CharDead();

	/**
	 * @return новый экземпляр пакета.
	 */
	public static CharDead getInstance(Character character, boolean dead)
	{
		CharDead packet = (CharDead) instance.newInstance();

		packet.objectId = character.getObjectId();
		packet.subId = character.getSubId();

		packet.state = dead? 0 : 1;

		packet.x = character.getX();
		packet.y = character.getY();
		packet.z = character.getZ();

		return packet;
	}

	/** уникальный ид перса */
	private int objectId;
	/** под ид перса */
	private int subId;

	/** состояние персонажа */
	private int state;

	/** координаты перса */
	private float x;
	private float y;
	private float z;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHAR_DEAD;
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
		writeInt(buffer, objectId);
        writeInt(buffer, subId);
        writeFloat(buffer, x);
        writeFloat(buffer, y);
        writeFloat(buffer, z);
        writeShort(buffer, state);
	}
}
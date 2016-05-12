package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет спавна перса.
 *
 * @author Ronn
 */
public class SpawnChar extends ServerPacket
{
	private static final ServerPacket instance = new SpawnChar();

	public static SpawnChar getInstance(Player player)
	{
		SpawnChar packet = (SpawnChar) instance.newInstance();

		packet.objectId = player.getObjectId();
		packet.subId = player.getSubId();
		packet.heading = player.getHeading();
		packet.dead = player.isDead()? 0 : 1;

		packet.x = player.getX();
		packet.y = player.getY();
		packet.z = player.getZ();

		return packet;
	}

	/** уникальный ид игрока */
	private int objectId;
	/** под ид игрока */
	private int subId;
	/** направление разворота */
	private int heading;
	/** мертв ли */
	private int dead;

	/** позиция игрока */
	private float x;
	private float y;
	private float z;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHAR_SPAWN;
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
		writeShort(buffer, heading);
		writeByte(buffer, dead);
	}
}
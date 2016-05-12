package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет телепорта игрока.
 *
 * @author Ronn
 */
public class Tp1 extends ServerPacket
{
	private static final ServerPacket instance = new Tp1();

	public static Tp1 getInstance(Player player)
	{
		Tp1 packet = (Tp1) instance.newInstance();

		packet.objectId = player.getObjectId();
		packet.subId = player.getSubId();

		return packet;
	}

	/** уникальный ид */
    private int objectId;
    /** под ид */
    private int subId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.TP1;
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
        writeInt(buffer, 0x000F4178);
	}
}
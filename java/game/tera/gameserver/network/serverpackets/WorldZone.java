package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, отправляющий ид территории для загрузки клиентом.
 *
 * @author Ronn
 */
public class WorldZone extends ServerPacket
{
	private static final ServerPacket instance = new WorldZone();

	public static WorldZone getInstance(Player player)
	{
		WorldZone packet = (WorldZone) instance.newInstance();

		packet.player = player;
		packet.zoneId = player.getZoneId();

		return packet;
	}

	public static WorldZone getInstance(Player player, int zoneId)
	{
		WorldZone packet = (WorldZone) instance.newInstance();

		packet.player = player;
		packet.zoneId = zoneId;

		return packet;
	}

	/** игрок для которого нужно определить территорию */
	private Player player;

	/** ид зоны */
	private int zoneId;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.WORLD_ZONE;
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
		writeInt(buffer, zoneId);
		writeFloat(buffer, player.getX());
		writeFloat(buffer, player.getY());
		writeFloat(buffer, player.getZ());
		writeByte(buffer, 0);
	}
}

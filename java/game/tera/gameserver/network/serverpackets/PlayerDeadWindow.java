package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, запускающий окно смерти.
 *
 * @author Ronn
 */
public class PlayerDeadWindow extends ServerConstPacket
{
	private static final PlayerDeadWindow instance = new PlayerDeadWindow();

	public static PlayerDeadWindow getInstance()
	{
		return instance;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_DEAD_WINDOW;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeInt(buffer, 0x0000001E);
	    writeInt(buffer, 0x000032E6);
        writeShort(buffer, 0);
	}
}
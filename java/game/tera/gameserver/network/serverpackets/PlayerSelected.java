package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет подтверждающий выбор персонажа для входа.
 *
 * @author Ronn
 * @created 31.03.2012
 */
public class PlayerSelected extends ServerConstPacket
{
	private static final PlayerSelected instance = new PlayerSelected();

	public static PlayerSelected getInstance()
	{
		return instance;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_SELECTED;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeByte(buffer, 1);
		writeInt(buffer, 0);
		writeInt(buffer, 0);
	}
}
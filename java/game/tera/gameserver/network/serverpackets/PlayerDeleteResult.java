package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с результатом попытки удаления персонажа.
 *
 * @author Ronn
 * @created 31.03.2012
 */
public class PlayerDeleteResult extends ServerPacket
{
	/** успешно удален */
	public static final int SUCCESSFUL = 1;
	/** не успешно удален */
	public static final int FAILED = 0;

	private static final ServerPacket instance = new PlayerDeleteResult();

	public static PlayerDeleteResult getInstance(int result)
	{
		PlayerDeleteResult packet = (PlayerDeleteResult) instance.newInstance();

		packet.result = result;

		return packet;
	}

	/** результат удаления */
	private int result;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_DELETE_RESULT;
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
		writeByte(buffer, result);
	}
}
package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с настройками клиента.
 *
 * @author Ronn
 */
public class HotKey extends ServerPacket
{
	private static final ServerPacket instance = new HotKey();

	public static HotKey getInstance(Player player)
	{
		HotKey packet = (HotKey) instance.newInstance();

		packet.hotkey = player.getHotkey();

		return packet;
	}

	/** настройки раскладки */
	private byte[] hotkey;

	@Override
	public void finalyze()
	{
		hotkey = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.HOT_KEY;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		if(hotkey != null)
		{
			writeOpcode(buffer);

			buffer.put(hotkey);
		}
	}
}

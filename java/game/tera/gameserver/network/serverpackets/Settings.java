package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с настройками клиента.
 *
 * @author Ronn
 */
public class Settings extends ServerPacket
{
	private static final ServerPacket instance = new Settings();

	public static Settings getInstance(Player player)
	{
		Settings packet = (Settings) instance.newInstance();

		packet.setting = player.getSettings();

		return packet;
	}

	/** настройки клиента */
	private byte[] setting;

	@Override
	public void finalyze()
	{
		setting = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SETTINGS;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		if(setting != null)
		{
			writeOpcode(buffer);
			buffer.put(setting);
		}
	}
}

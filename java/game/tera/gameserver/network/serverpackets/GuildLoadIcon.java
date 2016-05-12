package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с информацией о клане.
 * 
 * @author Ronn
 */
public class GuildLoadIcon extends ServerPacket
{
	private static final ServerPacket instance = new GuildLoadIcon();
	
	public static GuildLoadIcon getInstance()
	{
		return (GuildLoadIcon) instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.GUILD_LOAD_ICON;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
	}
}

package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Запрос названия клана.
 * 
 * @author Ronn
 */
public class GuildInputName extends ServerPacket
{
	private static final ServerPacket instance = new GuildInputName();
	
	public static GuildInputName getInstance()
	{
		return (GuildInputName) instance.newInstance();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.GUILD_INPUT_NAME;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(5); //05 00 00 00
	}
}

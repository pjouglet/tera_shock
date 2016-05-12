package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Подтверждение правильного ввода имени гильдии.
 * 
 * @author Ronn
 */
public class GuildCheckName extends ServerPacket
{
	private static final ServerPacket instance = new GuildCheckName();
	
	public static GuildCheckName getInstance(String name)
	{
		GuildCheckName packet = (GuildCheckName) instance.newInstance();
		
		packet.name = name;
		
		return packet;
	}
	
	/** название гильдии */
	private String name;

	@Override
	public void finalyze()
	{
		name = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.GUILD_CHECK_NAME;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeShort(1);
		writeShort(8);
		writeInt(8);
		writeShort(22);
		writeShort(2);
		writeInt(0);
		writeByte(0);
		writeS(name);// 61 00 64 00 61 00 64 00 61 00 64 00 00 00
		writeByte(0);
	}
}

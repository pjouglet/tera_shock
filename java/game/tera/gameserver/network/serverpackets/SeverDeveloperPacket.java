package tera.gameserver.network.serverpackets;

import java.util.List;

import tera.gameserver.network.ServerPacketType;


/**
 * @author Ronn
 */
public class SeverDeveloperPacket extends ServerPacket
{
	private static final ServerPacket instance = new SeverDeveloperPacket();

	public static SeverDeveloperPacket getInstance(List<Short> list)
	{
		SeverDeveloperPacket packet = (SeverDeveloperPacket) instance.newInstance();

		packet.list = list;

		return packet;
	}

	private List<Short> list;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.DEVELOPER_PACKET;
	}

	@Override
	protected void writeImpl()
	{
		for(short val : list)
			writeByte(val);
	}
}

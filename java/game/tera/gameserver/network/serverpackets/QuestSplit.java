package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * @author Ronn
 */
public class QuestSplit extends ServerPacket
{
	private static final ServerPacket instance = new QuestSplit();
	
	public static QuestSplit getInstance()
	{
		return (QuestSplit) instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.QUEST_SPLIT_LIST;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeShort(0);
		writeByte(0);
	}
}

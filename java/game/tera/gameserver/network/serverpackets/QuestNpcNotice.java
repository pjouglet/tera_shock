package tera.gameserver.network.serverpackets;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.quests.NpcIconType;
import tera.gameserver.network.ServerPacketType;

/**
 * @author Ronn
 */
public class QuestNpcNotice extends ServerPacket
{
	private static final ServerPacket instance = new QuestNpcNotice();
	
	public static QuestNpcNotice getInstance(Npc npc, NpcIconType type)
	{
		QuestNpcNotice packet = (QuestNpcNotice) instance.newInstance();
		
		packet.id = npc.getObjectId();
		packet.subId = npc.getSubId();
		packet.type = type;
		
		return packet;
	}
	
	/** тип подсветки */
	private NpcIconType type;
	
	/** ид нпс */
	private int id;
	/** саб ид нпс */
	private int subId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.QUEST_NPC_NOTICE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(id);//40 A9 00 00 
		writeInt(subId);//00 80 0B 00 
		writeInt(type.ordinal());//02 00 00 00 
		writeByte(1);//01 
	}
}

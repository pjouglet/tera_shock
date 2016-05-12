package tera.gameserver.network.serverpackets;

import tera.gameserver.model.quests.QuestState;
import tera.gameserver.network.ServerPacketType;

/**
 * @author Ronn
 */
public class QuestMoveToPanel extends ServerPacket
{
	private static final ServerPacket instance = new QuestMoveToPanel();
	
	public static QuestMoveToPanel getInstance(QuestState quest)
	{
		QuestMoveToPanel packet = (QuestMoveToPanel) instance.newInstance();
		
		packet.questId = quest.getQuestId();
		packet.objectId = quest.getObjectId();
		packet.state = quest.getState();
		
		return packet;
	}
	
	/** ид квеста */
	private int questId;
	/** ид состояния квеста */
	private int objectId;
	/** стадия квеста */
	private int state;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.QUEST_MOVE_TO_PANEL;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(questId);   //29 05 00 00 //NPC ID
		writeInt(objectId);  //C4 E0 89 00 //обжект ид
		writeByte(state);   //01//номер квеста
	}
}

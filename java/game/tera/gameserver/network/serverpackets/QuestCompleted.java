package tera.gameserver.network.serverpackets;

import tera.gameserver.model.quests.QuestState;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет выполнения квеста.
 *
 * @author Ronn
 */
public class QuestCompleted extends ServerPacket
{
	private static final ServerPacket instance = new QuestCompleted();

	public static QuestCompleted getInstance(QuestState quest, boolean canceled)
	{
		QuestCompleted packet = (QuestCompleted) instance.newInstance();

		packet.questId = quest.getQuestId();
		packet.objectId = quest.getObjectId();
		packet.canceled = canceled? 1 : 0;

		return packet;
	}

	/** ид квеста */
	private int questId;
	/** ид состояния квеста */
	private int objectId;
	/** отменен ли квест */
	private int canceled;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.QUEST_REMOVE_TO_PANEL;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();

		writeInt(questId);//16 05 00 00
		writeInt(objectId);//FE DF 89 00
		writeShort(canceled);//00 00  0 - выполнен, 1 - отменен
	}
}

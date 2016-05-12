package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;

import tera.gameserver.model.quests.QuestDate;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет со списком выполненных квестов.
 *
 * @author Ronn
 */
public class QuestCompleteList extends ServerPacket
{
	private static final ServerPacket instance = new QuestCompleteList();

	public static QuestCompleteList getInstance(Table<IntKey, QuestDate> completeTable)
	{
		QuestCompleteList packet = (QuestCompleteList) instance.newInstance();

		ByteBuffer buffer = packet.getPrepare();

		try
		{
			Array<QuestDate> completed = packet.getCompleted();

			// вносим и сортируем штамы выполнения
			completeTable.values(completed);

			// получаем массив штампов
			QuestDate[] array = completed.array();

			// получаем последний штамп
			QuestDate last = completed.last();

			int bytes = 8;

			packet.writeShort(buffer, completeTable.size()); //1B 00 кол-во квестов в книге
			packet.writeShort(buffer, bytes); //08 00

			for(int i = 0, length = completed.size(); i < length; i++)
			{
				QuestDate next = array[i];

				packet.writeShort(buffer, bytes);

				if(next != last)
					bytes += 8;
				else
					bytes = 0;

				packet.writeShort(buffer, bytes);
				packet.writeInt(buffer, next.getQuestId());
			}

			return packet;
		}
		finally
		{
			buffer.flip();
		}
	}

	/** список выполненных квестов */
	private Array<QuestDate> completed;

	/** промежуточный буффер */
	private ByteBuffer prepare;

	public QuestCompleteList()
	{
		this.prepare = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
		this.completed = Arrays.toSortedArray(QuestDate.class);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
		completed.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.QUEST_COMPLETE_LIST;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);

		// получаем промежуточный буффер
		ByteBuffer prepare = getPrepare();

		// переносим данные
		buffer.put(prepare.array(), 0, prepare.limit());
	}

	/**
	 * @return подготовленный буфер.
	 */
	public ByteBuffer getPrepare()
	{
		return prepare;
	}

	/**
	 * @return список выполннных квестов.
	 */
	public Array<QuestDate> getCompleted()
	{
		return completed;
	}
}

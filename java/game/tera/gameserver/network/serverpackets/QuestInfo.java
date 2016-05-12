package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.Reward;
import tera.gameserver.model.quests.actions.ActionAddItem;
import tera.gameserver.network.ServerPacketType;


/**
 * Серверный пакет содержащий информацию об диалогово окне нпс
 *
 * @author Ronn
 */
public class QuestInfo extends ServerPacket
{
	private static final ServerPacket instance = new QuestInfo();

	public static QuestInfo getInstance(Npc npc, Player player, Quest quest, int dialogId, int page, String button)
	{
		QuestInfo packet = (QuestInfo) instance.newInstance();

		packet.buffer = packet.prepare;

		packet.writeShort(1);// 01 00
		packet.writeShort(64);// 40 00
		packet.writeShort(2);// 01 00

		int endNameLink = 88 + button.length();// 28;//link.getName().length();

		packet.writeShort(endNameLink);// 60 00
		packet.writeInt(npc.getObjectId()); // Обжэект ид нпц
		packet.writeInt(npc.getSubId()); // саб ид нпц

		packet.writeInt(dialogId);// 02 00 00 00
		packet.writeInt(quest.getId());// npc.getNpcId());//29 05 00 00
		packet.writeInt(4);// npc.getType());//00 00 00 00

		packet.writeInt(0);// 00 00 00 00
		packet.writeInt(page);// 02 00 00 00 // номер диалога в квесте

		packet.writeInt(0x3296B647); // 47 B6 96 32 Ид окна со списком ссылок

		packet.writeInt(0);// 00 00 00 00
		packet.writeByte(0);// 00
		packet.writeByte(1);// 01
		packet.writeLong(0);// 00 00 00 00 00 00 00 00
		packet.writeShort(0);// 00 00 // если 1, то пишет про награду что-то
		packet.writeInt(0xFFFFFFFF);

		packet.writeInt(64);// первый байт описания данной ссылки в пакете
		packet.writeShort(78); // (первый байт описания данной ссылки в пакете) + 8
		packet.writeInt(1);// номер ссылки
		packet.writeInt(4);// link.getType().getId()); // 19 квест, 26 трейд, 28 магазины
		packet.writeString(button);// link.getName());

		Reward reward = quest.getReward();

		ActionAddItem[] items = reward.getItems();

		ActionAddItem last = null;

		if(items != null)
		{
			for(int i = 0, length = items.length; i < length; i++)
			{
				ActionAddItem item = items[i];

				if(item.test(npc, player))
					last = item;
			}
		}

		packet.writeInt(endNameLink);

		// если выдаём итемы
		if(last == null)
			packet.writeInt(0);
		else
		{
			packet.writeShort(1);
			packet.writeShort(140);
		}

		packet.writeInt(0);
		packet.writeInt(0);
		packet.writeInt(reward.getExp()); // Награда экспой
		packet.writeInt(reward.getMoney()); // Награда деньгами
		packet.writeInt(0);
		packet.writeInt(0); // polyci points
		packet.writeInt(0); // reputations
		packet.writeInt(0);
		packet.writeInt(0);

		if(last != null)
		{
			int bytes = 140;

			for(int i = 0, length = items.length; i < length; i++)
			{
				ActionAddItem item = items[i];

				if(item.test(npc, player))
				{
					packet.writeShort(bytes);

					if(item == last)
						bytes = 0;
					else
						bytes += 12;

					packet.writeShort(bytes);
					packet.writeInt(item.getItemId());// 2D 45 00 00 Итем Ид (для нашего класса во всех квестах идёт для класса сли пушки)
					packet.writeInt(item.getItemCount());// 01 00 00 00 кол-во
				}
			}
		}

		return packet;
	}

	/** промежуточный буффер */
	private ByteBuffer prepare;

	public QuestInfo()
	{
		super();

		this.prepare = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.QUEST_INFO;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();

		prepare.flip();

		buffer.put(prepare);
	}
}
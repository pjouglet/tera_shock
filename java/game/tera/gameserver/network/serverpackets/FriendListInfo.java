package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.Strings;
import tera.gameserver.model.FriendInfo;
import tera.gameserver.model.FriendList;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с информацией о списке друзей.
 *
 * @author Ronn
 */
public class FriendListInfo extends ServerPacket
{
	private static final ServerPacket instance = new FriendListInfo();

	public static FriendListInfo getInstance(Player player)
	{
		FriendListInfo packet = (FriendListInfo) instance.newInstance();

		ByteBuffer buffer = packet.getPrepare();

		try
		{
			// получаем список друзей игрока
			FriendList friendList = player.getFriendList();

			packet.writeShort(buffer, 8);// 08 00 кол-во перосов(друзей) или статик
			packet.writeShort(buffer, 8);// 08 00 кол-во перосов(друзей) или статик

			int n = 8;

			synchronized(friendList)
			{
				// получаем список друзей
				FriendInfo[] friends = friendList.getFriends();

				// перебираем друзей
				for(int i = 0, length = friendList.size(); i < length; i++)
				{
					// получаем информацию о друге
					FriendInfo friend = friends[i];

					// получаем его имя
					String name = friend.getName();

					// получаем длинну имени
					int nameLength = Strings.length(name);

					packet.writeShort(buffer, n);// 08 00 байт начала описания друга

					if(i == length)
						packet.writeShort(buffer, 0);
					else
						packet.writeShort(buffer, n + 42 + nameLength);// 44 00 байта конца описания друга , если друг в списке последний то 0

					packet.writeShort(buffer, n + 40);// 30 00 байт начала ника
					packet.writeShort(buffer, n + 40 + nameLength);// 42 00 байт конец ника

					packet.writeInt(buffer, friend.getObjectId());// 79 12 00 00 обж ид вроде
					packet.writeInt(buffer, friend.getLevel());// 3C 00 00 00 уровень
					packet.writeInt(buffer, friend.getRaceId());// 03 00 00 00 расса
					packet.writeInt(buffer, friend.getClassId());// 04 00 00 00 класс

					packet.writeInt(buffer, 0);// 01 00 00 00 хз потесть вроде онлайн
					packet.writeInt(buffer, 0);// 01 00 00 00 локация
					packet.writeInt(buffer, 0);// 0F 00 00 00 локация
					packet.writeInt(buffer, 0);// B0 00 00 00 локация
					packet.writeString(buffer, name); // 46 00 61 00 72 00 76 00 61 00 74 00 65 00 72 00 00 00 F.a.r.v.a.t.e.r.
					packet.writeShort(buffer, 0);

					n = n + 42 + nameLength;
				}
			}

			return packet;
		}
		finally
		{
			buffer.flip();
		}
	}

	/** подготовленный буффер для отправки данных */
	private ByteBuffer prepare;

	public FriendListInfo()
	{
		this.prepare = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.FRIEND_LIST_INFO;
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
}

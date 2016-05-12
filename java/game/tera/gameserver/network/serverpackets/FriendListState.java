package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.FriendList;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с информацией о списке друзей.
 *
 * @author Ronn
 */
public class FriendListState extends ServerPacket
{
	private static final ServerPacket instance = new FriendListState();

	public static FriendListState getInstance(Player player)
	{
		FriendListState packet = (FriendListState) instance.newInstance();

		ByteBuffer buffer = packet.prepare;

		// получаем список друзей игрока
		FriendList friendList = player.getFriendList();

		int n = 8;

		synchronized(friendList)
		{
			// получаем список онлаин друзей
			Player[] players = friendList.getPlayers();

			packet.writeShort(buffer, friendList.online());//02 00 сколько чел в сети
			packet.writeShort(buffer, 8);//08 00

			// перебираем друзей
			for(int i = 0, length = friendList.online(); i < length; i++)
			{
				// получаем друга
				Player target = players[i];

				packet.writeShort(buffer, n);//08 00

				if(i == length)
					packet.writeShort(buffer, 0);
				else
					packet.writeShort(buffer, n += 30);//26 00 если последний нуллим.

				packet.writeInt(buffer, target.getObjectId());//1D 34 00 00  обежкт ид
				packet.writeInt(buffer, target.getLevel());//3C 00 00 00
				packet.writeInt(buffer, 0);//00 00 00 00
				packet.writeInt(buffer, 0);//0F 27 00 00
				packet.writeInt(buffer, 0);//0F 00 00 00
				packet.writeInt(buffer, 0);//41 23 00 00
				packet.writeShort(buffer, 1);//10 00
			}
		}

		return packet;
	}

	/** подготовленный буффер для отправки данных */
	private ByteBuffer prepare;

	public FriendListState()
	{
		super();

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
		return ServerPacketType.FRIEND_LIST_STATE;
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

		prepare.flip();

		buffer.put(prepare);
	}
}

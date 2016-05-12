package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.Strings;
import rlib.util.array.Array;
import tera.gameserver.model.Party;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет с полной информацией о группе.
 *
 * @author Ronn
 * @created 07.03.2012
 */
public class PartyInfo extends ServerPacket
{
	private static final ServerPacket instance = new PartyInfo();

	public static PartyInfo getInstance(Party party)
	{
		PartyInfo packet = (PartyInfo) instance.newInstance();

		ByteBuffer buffer = packet.getPrepare();

		packet.writeShort(buffer, 2);// 02 00
		packet.writeShort(buffer, 48);// 30 00
		packet.writeShort(buffer, 48);// 30 00
		packet.writeShort(buffer, 0);// 00 00
		packet.writeByte(buffer, 0);// 00
		packet.writeInt(buffer, party.getObjectId());// 06 17 00 00
		packet.writeShort(buffer, 9);// 09 00
		packet.writeShort(buffer, 0);// 0D 00 SERVER ID
		packet.writeInt(buffer, 0);// 0D 00 00 00  SERVER ID
		packet.writeInt(buffer, party.getLeaderId());// DE 2C 0B 00
		packet.writeInt(buffer, party.isRoundLoot()? 1 : 0);// 01 00 00 00 // режим лута 0 кто поднял и того, 1 - всякие условия
		packet.writeInt(buffer, 0); // 03 00 00 00 грейд рола итемов
		packet.writeShort(buffer, 0);// 00 00 апплед то геар онли
		packet.writeInt(buffer, 0);// 01 00 00 00 // режим рола
		packet.writeInt(buffer, 0);// 01 00 00 00 // ноу сулбоунд итемс
		packet.writeByte(buffer, party.isLootInCombat()? 0 : 1);// 00 модно ли в бою поднимать

		// получаем членов группы
		Array<Player> members = party.getMembers();

		members.readLock();
		try
		{
			int byets = 48;

			// получаем список членов группы
			Player[] array = members.array();

			// перебираем
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				packet.writeShort(buffer, byets);// 48 первый байт начало описания

				// расчитываем длинну имени
				int nameLength = Strings.length(member.getName());

				byets += (35 + nameLength); // получаем последний байт

				if(i < length)
					packet.writeShort(buffer, byets);// 99 последний байт
				else
					packet.writeShort(buffer, 0);

				packet.writeShort(buffer, byets - nameLength);// 83 байт начала ника
				packet.writeInt(buffer, 0);// 0D 00 00 00  SERVER ID
				packet.writeInt(buffer, member.getObjectId());// 64 44 00 00 //айди
				packet.writeInt(buffer, member.getLevel());// 2A 00 00 00//уровень
				packet.writeInt(buffer, member.getClassId());// 06 00 00 00//код профы
				packet.writeByte(buffer, 1);// 01
				packet.writeInt(buffer, member.getObjectId());// 32 17 B2 03 ид игрока
				packet.writeInt(buffer, member.getSubId()); // 00 80 00 13 саб ид игрока
				packet.writeShort(buffer, 0); // 00 00
				packet.writeByte(buffer, 0);
				packet.writeByte(buffer, 0);
				packet.writeString(buffer, member.getName()); // 00 61 00 73 00 64 00 61 00 00 00 //имя первого
			}
		}
		finally
		{
			members.readUnlock();
		}

		buffer.flip();

		return packet;
	}

	/** пати */
	private final ByteBuffer prepare;

	public PartyInfo()
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
		return ServerPacketType.PARTY_INFO;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	/**
	 * @return промежуточный буффер.
	 */
	public ByteBuffer getPrepare()
	{
		return prepare;
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
}

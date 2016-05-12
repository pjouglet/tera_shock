package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.Strings;
import rlib.util.array.Array;
import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildMember;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с информацией о клане.
 *
 * @author Ronn
 */
public class GuildMembers extends ServerPacket
{
	private static final ServerPacket instance = new GuildMembers();

	public static GuildMembers getInstance(Player player)
	{
		GuildMembers packet = (GuildMembers) instance.newInstance();

		Guild guild = player.getGuild();

		if(guild == null)
			return packet;

		ByteBuffer buffer = packet.getPrepare();

		packet.writeShort(buffer, 2);// 04 00
		packet.writeShort(buffer, 11);// 0B 00
		packet.writeByte(buffer, 1);// 01
		packet.writeByte(buffer, 1);// 01
		packet.writeByte(buffer, 1);// 01

		int byets = 11;

		Array<GuildMember> members = guild.getMembers();

		members.readLock();
		try
		{
			GuildMember[] array = members.array();

			for(int i = 0, length = members.size(); i < length; i++)
			{
				packet.writeShort(buffer, byets);// 48 первый байт начало описания

				GuildMember member = array[i];

				int nameLength = Strings.length(member.getName());
				int titleLength = Strings.length(member.getNote());

				byets += (57 + nameLength + titleLength); // получаем последний байт 77-16

				if(i != length - 1)
					packet.writeShort(buffer, byets); // 99 последний байт
				else
					packet.writeShort(buffer, 0);

				packet.writeShort(buffer, byets - nameLength - titleLength);
				packet.writeShort(buffer, byets - titleLength);

				packet.writeInt(buffer, member.getObjectId()); // 1A 38 00 00 //ид локации
				packet.writeInt(buffer, 1); // 01 00 00 00 //статик
				packet.writeInt(buffer, 1); // 06 00 00 00 //вроде континент
				packet.writeInt(buffer, 1); // 38 00 00 00 //типа области
				packet.writeInt(buffer, member.getRankId()); // 03 00 00 00 //Лидер 01, простой чмо 03
				packet.writeInt(buffer, member.getLevel()); // 2F 00 00 00 //лвл
				packet.writeInt(buffer, member.getRaceId()); // 00 00 00 00 //Код рассы
				packet.writeInt(buffer, member.getClassId()); // 01 00 00 00 //Код классы
				packet.writeInt(buffer, member.getSex()); // 00 00 00 00 //Пол
				packet.writeInt(buffer, member.isOnline() ? 0 : 2); // 02 00 00 00 //02 не в сети, 00 в сети
				packet.writeInt(buffer, member.getLastOnline()); // 6B 57 AC 4F //время когда был в сети
				packet.writeInt(buffer, 0); // 00 00 00 00
				packet.writeByte(buffer, 0);
				packet.writeString(buffer, member.getName()); // 00 52 00 65 00 64 00 6E 00 65 00 72 00 00 00 00 00 .R.e.d.n.e.r....
				packet.writeString(buffer, member.getNote()); // титул, если пусто то отправляем пробел т.е. 00 00
			}
		}
		finally
		{
			members.readUnlock();
		}

		buffer.flip();

		return packet;
	}

	/** подготовленный буффер для отправки данных */
	private ByteBuffer prepare;

	public GuildMembers()
	{
		this.prepare = ByteBuffer.allocate(204800).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.GUILD_MEMBERS;
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

package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import rlib.util.Strings;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildMember;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с информацией о клане.
 *
 * @author Ronn
 */
public class GuildInfo extends ServerPacket
{
	private static final ServerPacket instance = new GuildInfo();

	public static GuildInfo getInstance(Player player)
	{
		GuildInfo packet = (GuildInfo) instance.newInstance();

		// получаем гильдию игрока
		Guild guild = player.getGuild();

		ByteBuffer buffer = packet.getPrepare();

		try
		{
			if(guild == null)
				return packet;

			GuildMember leader = guild.getLeader();

			if(leader == null)
				return packet;

			GuildRank myrank = player.getGuildRank();
			String advertisment = ""; // обьявление для гильдии

			int guildNameLength = Strings.length(guild.getName());
			int guildTitleLength = Strings.length(guild.getTitle());
			int leaderNameLength = Strings.length(leader.getName());
			int leaderTitleLength = Strings.length(guild.getMessage());
			int advertismentLength = Strings.length(advertisment);
			int myRankLenght = Strings.length(myrank.getName());

			int fb = 79;
			int n = fb + guildNameLength + guildTitleLength + leaderNameLength + leaderTitleLength + myRankLenght + advertismentLength;

			packet.writeShort(buffer, 3);// 03 00
			packet.writeShort(buffer, n);// 82 00 Байт начала выдачи рангов
			packet.writeShort(buffer, fb);// 3E 00 байт названия гильды статик
			packet.writeShort(buffer, fb + guildNameLength);// 54 00 Начала титула гильды
			packet.writeShort(buffer, fb + guildNameLength + guildTitleLength);// 5C 00
			packet.writeShort(buffer, fb + guildNameLength + guildTitleLength + leaderNameLength);// 6E 00
			packet.writeShort(buffer, fb + guildNameLength + guildTitleLength + leaderNameLength + 2);// 70 00
			packet.writeShort(buffer, fb + guildNameLength + guildTitleLength + leaderNameLength + 2 + myRankLenght);// 7A 00

			packet.writeInt(buffer, 2127); // B2 00 00 00
			packet.writeInt(buffer, 0); // 00 00 00 00
			packet.writeInt(buffer, 0); // 00 00 00 00
			packet.writeInt(buffer, leader.getObjectId()); // 1D 34 00 00 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			packet.writeInt(buffer, (int) (System.currentTimeMillis() / 1000)); // FE C4 9A 4F дата созадния гильдии
			packet.writeInt(buffer, 0); // 00 00 00 00
			packet.writeInt(buffer, guild.getLevel()); // 01 00 00 00

			packet.writeInt(buffer, 0); // 00 00 00 00 Level duration
			packet.writeInt(buffer, 0); // 05 00 00 00 Praise
			packet.writeInt(buffer, 0);// FF FF FF FF Polyci Points

			packet.writeByte(buffer, 1);// 01
			packet.writeByte(buffer, 0);
			/*
			 * 01 0F ........ÿÿÿÿ.... 0E 00 00 00 00 00 00
			 */

			packet.writeByte(buffer, 1);// 01
			packet.writeByte(buffer, 15);// 0F
			packet.writeInt(buffer, 14); // 0E 00 00 00

			packet.writeShort(buffer, 0); // 00 00
			packet.writeByte(buffer, 0); // 00

			packet.writeInt(buffer, -1); // FF FF FF FF
			packet.writeInt(buffer, -1); // FF FF FF FF

			packet.writeString(buffer, guild.getName());

			// 00 53 00 77 00 65 00 65 00
			// 74 00 20 00 50 00 61 00 69 00 6E 00 00 00 ........yyyy..S.w.e.e.t...P.a.i.
			packet.writeString(buffer, guild.getTitle());
			packet.writeString(buffer, leader.getName());// 55 00 72 00 61 00 6D 00 65 00 73 00 68 00 69 00 00 00 U.r.a.m.e.s.h.i.....
			packet.writeString(buffer, guild.getMessage());// 00 00 чтото нада вертеть, оставим пока пробел

			// packet.writeH(0);//packet.writeSS(guild.getTitle());// 4E 00 6F 00 6F 00 62 00 00 00 5E 00 5F 00 5E 00 00 00 N.o.o.b...^._.^
			packet.writeString(buffer, myrank.getName());
			packet.writeString(buffer, advertisment);

			Table<IntKey, GuildRank> ranks = guild.getRanks();

			synchronized(guild)
			{
				int k = 0;

				for(Iterator<GuildRank> iterator = ranks.iterator(); iterator.hasNext();)
				{
					GuildRank rank = iterator.next();

					packet.writeShort(buffer, n);// 82 00

					k = Strings.length(rank.getName());

					n += (14 + k);

					if(iterator.hasNext())
						packet.writeShort(buffer, n); // A8 00
					else
						packet.writeShort(buffer, 0);

					packet.writeShort(buffer, n - k);// 90 00

					packet.writeInt(buffer, rank.getIndex());// 01 00 00 00
					packet.writeInt(buffer, rank.getLawId());// 00 00 00 00
					packet.writeString(buffer, rank.getName()); // 00 47 00 75 00 69 00 6C 00 64 00 6D 00 //суда ранг
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

	public GuildInfo()
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
		return ServerPacketType.GUILD_INFO;
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
	 * @return подготавливаемый буффер.
	 */
	public ByteBuffer getPrepare()
	{
		return prepare;
	}
}

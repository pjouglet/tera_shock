package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.array.Array;
import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildLog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с информацией о клане.
 *
 * @author Ronn
 */
public class GuildLogs extends ServerPacket
{
	private static final ServerPacket instance = new GuildLogs();

	public static GuildLogs getInstance(Player player)
	{
		GuildLogs packet = (GuildLogs) instance.newInstance();

		Guild guild = player.getGuild();

		if(guild == null)
			return packet;

		ByteBuffer buffer = packet.getPrepare();

		Array<GuildLog> logs = guild.getLogs();

		int bytes = 16;

		packet.writeShort(buffer, logs.size()); // 07 00 кол-во сообщений
		packet.writeShort(buffer, bytes); // 10 00
		packet.writeInt(buffer, 1); // 01 00 00 00
		packet.writeInt(buffer, 1); // 01 00 00 00

		/*
		 * for(){ writeH(bytes); //если последний нулим bytes=0; writeH(bytes);
		 *
		 * writeH(bytes);//байт описания имени writeH(bytes);//байт окончания описания имени writeQ();//время лога в милисекундах writeD(32); writeSS();//имя того кто виновен writeSS();//ссылка writeSS();//текст лога }
		 */

		buffer.flip();

		return packet;
	}

	/** подготовленный буффер для отправки данных */
	private ByteBuffer prepare;

	public GuildLogs()
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
		return ServerPacketType.GUILD_LOGS;
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

	public ByteBuffer getPrepare()
	{
		return prepare;
	}
}

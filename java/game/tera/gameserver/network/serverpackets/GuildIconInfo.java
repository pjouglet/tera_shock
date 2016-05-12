package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.Strings;
import tera.gameserver.model.GuildIcon;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с информацией о клане.
 *
 * @author Ronn
 */
public class GuildIconInfo extends ServerPacket
{
	private static final ServerPacket instance = new GuildIconInfo();

	public static GuildIconInfo getInstance(GuildIcon icon)
	{
		GuildIconInfo packet = (GuildIconInfo) instance.newInstance();

		if(icon == null || !icon.hasIcon())
			return packet;

		ByteBuffer buffer = packet.getPrepare();

		byte[] bytes = icon.getIcon();

		packet.writeShort(buffer, 10);//0A 00
		packet.writeShort(buffer, Strings.length(icon.getName()) + 10);//32 00 суда длинна названия картинок на сервере * 2 +10
		packet.writeShort(buffer, bytes.length);//E3 11 вес картинки
		packet.writeString(buffer, icon.getName());//имя картинки guildlogo_13_2127_2 по этому имени и будет вызываться картинка у других

		buffer.put(bytes);

		buffer.flip();

		return packet;
	}

	/** подготовленный буффер для отправки данных */
	private ByteBuffer prepare;

	public GuildIconInfo()
	{
		this.prepare = ByteBuffer.allocate(1024000).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.GUILD_ICON_INFO;
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

package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import rlib.util.Strings;
import tera.gameserver.model.SayType;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для передачи текста в чат.
 *
 * @author Ronn
 */
public class CharSay extends ServerPacket
{
	private static final ServerPacket instance = new CharSay();

	public static CharSay getInstance(String name, String text, SayType type, int objectId, int subId)
	{
		CharSay packet = (CharSay) instance.newInstance();

		packet.name = name;
		packet.text = text;
		packet.type = type;
		packet.objectId = objectId;
		packet.subId = subId;

		return packet;
	}

	/** текст */
	private String text;
	/** имя того, от кого текст */
	private String name;
	/** тип чата */
	private SayType type;

	/** обджект ид персонажа */
	private int objectId;
	/** саб ид персонажа */
	private int subId;

	@Override
	public void finalyze()
	{
		text = null;
		name = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SAY;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void write(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeShort(buffer, 22);
		writeShort(buffer, 24 + Strings.length(name));//длинна имени
		writeInt(buffer, type.ordinal());
		writeInt(buffer, objectId);
		writeInt(buffer, subId);
		writeByte(buffer, 0);
		writeByte(buffer, 0);
		writeString(buffer, name);//имя
		writeByte(buffer, 0);

		if(name == null || name.isEmpty())
			writeShort(buffer, 0x2000);

		writeByte(buffer, 0);
		writeString(buffer, text);//текст
		writeByte(buffer, 0);
	}
}
package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с подтверждением корректности имени.
 *
 * @author Ronn
 */
public class ResultCheckName extends ServerPacket
{
	private static final ServerPacket instance = new ResultCheckName();

	public static ResultCheckName getInstance(String name, int type)
	{
		ResultCheckName packet = (ResultCheckName) instance.newInstance();

		packet.name = name;
		packet.type = type;

		return packet;
	}

	/** проверяемое имя */
	private String name;

	/** тип сообщения */
	private int type;

	@Override
	public void finalyze()
	{
		name = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.RESULT_CHECK_NAME;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeShort(1);
		writeShort(8);
		writeInt(8);
		writeShort(22);
		writeInt(type);//индекс
		writeInt(0); //18
		writeString(name);//текст
		writeByte(0);
	}
}

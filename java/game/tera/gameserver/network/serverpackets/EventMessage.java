package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Ивентовое сообщение в чат.
 *
 * @author Ronn
 */
public class EventMessage extends ServerPacket
{
	private static final ServerPacket instance = new EventMessage();

	public static EventMessage getInstance(String head, String message, String info)
	{
		EventMessage packet = (EventMessage) instance.newInstance();

		packet.head = head;
		packet.message = message;
		packet.info = info;

		return packet;
	}

	/** заголовок */
	private String head;
	/** мессадж */
	private String message;
	/** инфо */
	private String info;

	@Override
	public void finalyze()
	{
		head = null;
		info = null;
		message = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.EVENT_MESSAGE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeShort(6);//начало оглавления
		writeString(head);//ссылка оглавления

		buffer.position(buffer.position() - 2);

		writeShort(11);//разделитель
		writeString(message);//само сообщение

		buffer.position(buffer.position() - 2);

		writeShort(11);//разделитель
		writeString(info);//сдесь доп инфо это мб кол-во,число или ссылка
	}
}

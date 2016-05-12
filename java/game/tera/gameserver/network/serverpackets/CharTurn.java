package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакт обновляющий развотрот персонажа для клиентов.
 *
 * @author Ronn
 */
public class CharTurn extends ServerPacket
{
	private static final ServerPacket instance = new CharTurn();

	public static CharTurn getInstance(Character character, int newHeading, int time)
	{
		CharTurn packet = (CharTurn) instance.newInstance();

		if(character == null)
			log.warning(packet, new Exception("not found character"));

		packet.objectId = character.getObjectId();
		packet.subId = character.getSubId();
		packet.time = time;
		packet.newHeading = newHeading;

		return packet;
	}

	/** уникальный ид персонажа */
	private int objectId;
	/** под ид персонажа */
	private int subId;
	/** время, за которое он разворачивается */
	private int time;
	/** новый разворот */
	private int newHeading;


	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHAR_TURN;
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
		writeInt(buffer, objectId);//ожект ид
		writeInt(buffer, subId);//саб ид
		writeShort(buffer, newHeading);//хэдэр
		writeShort(buffer, time);//время
		writeShort(buffer, 0);
	}
}

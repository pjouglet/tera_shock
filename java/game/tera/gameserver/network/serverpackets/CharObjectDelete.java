package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.traps.Trap;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для удаления ловушки.
 *
 * @author Ronn
 */
public class CharObjectDelete extends ServerPacket
{
	private static final ServerPacket instance = new CharObjectDelete();

	public static CharObjectDelete getInstance(int objectId, int subId)
	{
		CharObjectDelete packet = (CharObjectDelete) instance.newInstance();

		packet.objectId = objectId;
		packet.subId = subId;

		return packet;
	}

	public static CharObjectDelete getInstance(Trap trap)
	{
		CharObjectDelete packet = (CharObjectDelete) instance.newInstance();

		packet.objectId = trap.getObjectId();
		packet.subId = trap.getSubId();

		return packet;
	}

	/** обджект ид */
	private int objectId;
	/** саб ид */
	private int subId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.TRAP_DELETE;
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
		writeInt(buffer, objectId);//BC 11 06 00
		writeInt(buffer, subId);//00 80 0D 00
		writeByte(buffer, 1);//01
	}
}

package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.Character;
import tera.gameserver.model.traps.Trap;
import tera.gameserver.network.ServerPacketType;


/**
 * Серверный пакет с информацией об НПС.
 *
 * @author Ronn
 */
public class TrapInfo extends ServerPacket
{
	private static final ServerPacket instance = new TrapInfo();

	public static TrapInfo getInstance(Trap trap)
	{
		TrapInfo packet = (TrapInfo) instance.newInstance();

		ByteBuffer buffer = packet.prepare;

		Character owner = trap.getOwner();

		packet.writeInt(buffer, trap.getObjectId());//BC 11 06 00 ид
		packet.writeInt(buffer, trap.getSubId());//00 80 0D 00 саб ид
		packet.writeInt(buffer, owner.getTemplateType());//00 00 00 00
		packet.writeInt(buffer, trap.getTemplateId());//08 60 01 04
		packet.writeFloat(buffer, trap.getX());//61 5F 05 C7
		packet.writeFloat(buffer, trap.getY());//1D BB 94 C6
		packet.writeFloat(buffer, trap.getZ());//76 7C 40 43
		packet.writeFloat(buffer, trap.getX());//61 5F 05 C7
		packet.writeFloat(buffer, trap.getY());//1D BB 94 C6
		packet.writeFloat(buffer, trap.getZ());//76 7C 40 43
		packet.writeInt(buffer, 0);//00 00 00 00
		packet.writeByte(buffer, 0);//00
		packet.writeInt(buffer, owner.getObjectId());//CD 03 0D 00 ид хозяина
		packet.writeInt(buffer, owner.getSubId());//00 80 00 01 саб ид хозяин
		packet.writeInt(buffer, owner.getModelId());//0A 29 00 00  ид темлейта

		return packet;
	}

	/** промежуточный буффер */
	private ByteBuffer prepare;

	public TrapInfo()
	{
		super();

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
		return ServerPacketType.TRAP_INFO;
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

		prepare.flip();

		buffer.put(prepare);
	}
}

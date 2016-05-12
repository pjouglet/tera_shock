package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет о том, что цель защитилась от эффекта.
 *
 * @author Ronn
 */
public class ResistEffect extends ServerPacket
{
	private static final ServerPacket instance = new ResistEffect();

	/** эффект был заресистен */
	public static final int RESISTED = 2;
	/** от эффекта у цели иммунитет */
	public static final int IMMUNE = 3;

	public static ResistEffect getInstance(Character character, Effect effect, int result)
	{
		ResistEffect packet = (ResistEffect) instance.newInstance();

		packet.objectId = character.getObjectId();
		packet.subId = character.getSubId();
		packet.effectId = effect.getEffectId();
		packet.result = result;

		return packet;
	}

	/** уникальный ид объекта */
	private int objectId;
	/** под ид объекта */
	private int subId;
	/** ид эффекта */
	private int effectId;
	/** результат */
	private int result;


	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.RESIST_EFFECT;
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
        writeInt(buffer, objectId);//3B F8 0C 00 обжект ид
        writeInt(buffer, subId);//00 80 0B 00 саб ид
        writeInt(buffer, effectId);//60 10 03 00 ид скила или эфект хз
        writeShort(buffer, result);//02 00
        writeByte(buffer, 0);//00
	}
}


package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Состояние игрока.
 *
 * @author Ronn
 */
public class NpcState extends ServerPacket
{
	private static final ServerPacket instance = new NpcState();

	public static NpcState getInstance(Character character, Character target, int state)
	{
		NpcState packet = (NpcState) instance.newInstance();

		packet.objectId = character.getObjectId();
		packet.subId = character.getSubId();
		packet.targetId = target.getObjectId();
		packet.targetSubId = target.getSubId();
		packet.state = state;

		return packet;
	}

	/** уникальный ид игрока */
	private int objectId;
	/** под ид игрока */
	private int subId;

	/** целевой ид */
	private int targetId;
	/** целевой саб ид */
	private int targetSubId;

	/** состояние игрока */
	private int state;


	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.NPC_STATE;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeShort(buffer, 0xC2F6);
		writeInt(buffer, objectId);//6D 47 0D 00 обжект ид 1
		writeInt(buffer, subId);//00 80 00 01 саб ид 1

		writeInt(buffer, targetId);//97 F6 0A 00 обжект ид 2
		writeInt(buffer, targetSubId);//00 80 0B 00 саб ид 2

		writeShort(buffer, state);//01 00
	}
}


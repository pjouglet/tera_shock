package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, указывающий на кого смотреть нпс.
 * 
 * @author Ronn
 */
public class NpcNotice extends ServerPacket
{
	private static final ServerPacket instance = new NpcNotice();
	
	public static NpcNotice getInstance(Character character, int targetId, int targetSubId)
	{
		NpcNotice packet = (NpcNotice) instance.newInstance();
		
		packet.character = character;
		packet.targetId = targetId;
		packet.targetSubId = targetSubId;
		
		return packet;
	}
	
	/** нпс, который сомтрит */
	private Character character;
	
	/** персонаж, на которого он смотрит */
	private int targetId;
	private int targetSubId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.NPC_NOTICE;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		writeInt(character.getObjectId());
		writeInt(character.getSubId());
		writeByte(0);
		writeInt(0x00000005);
		writeInt(targetId);
		writeInt(targetSubId);
		writeInt(targetId == 0 ? 0 : 1);
	}
}
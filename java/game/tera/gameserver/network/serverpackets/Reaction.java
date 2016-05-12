package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.model.ReactionType;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет отображения реакции.
 * 
 * @author Ronn
 */
public class Reaction extends ServerPacket
{
	private static final ServerPacket instance = new Reaction();
	
	public static Reaction getInstance(Character actor, ReactionType type)
	{
		Reaction packet = (Reaction) instance.newInstance();
		
		packet.objectId = actor.getObjectId();
		packet.subId = actor.getSubId();
		packet.type = type;
		
		return packet;
	}
	
	/** тип реакции */
	private ReactionType type;
	
	/** ид персонажа */
	private int objectId;
	/** саб ид персонажа */
	private int subId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.REACTION;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		writeInt(objectId);
		writeInt(subId);
		writeInt(type.ordinal());
	}
}
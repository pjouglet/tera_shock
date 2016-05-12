package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.model.EmotionType;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет отображения эмоций.
 * 
 * @author Ronn
 */
public class Emotion extends ServerPacket
{
	private static final ServerPacket instance = new Emotion();
	
	public static Emotion getInstance(Character character, EmotionType type)
	{
		Emotion packet = (Emotion) instance.newInstance();
		
		packet.objectId = character.getObjectId();
		packet.subId = character.getSubId();
		packet.type = type;
		
		return packet;
	}
	
	/** тип эмоции */
	private EmotionType type;
	
	/** ид персонажа */
	private int objectId;
	/** саб ид персонажа */
	private int subId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.EMOTION;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		writeInt(objectId);
		writeInt(subId);
		writeInt(type.ordinal());
		writeInt(0);
		writeByte(0);
	}
}
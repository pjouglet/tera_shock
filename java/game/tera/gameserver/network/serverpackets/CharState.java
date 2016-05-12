package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Состояние игрока.
 * 
 * @author Ronn
 */
public class CharState extends ServerPacket
{
	private static final ServerPacket instance = new CharState();
	
	public static CharState getInstance(int objectId, int subId, int state)
	{
		CharState packet = (CharState) instance.newInstance();
		
		packet.objectId = objectId;
		packet.subId = subId;
		packet.state = state;
		
		return packet;
	}
	
	/** уникальный ид игрока */
	private int objectId;
	/** под ид игрока */
	private int subId;
	/** состояние игрока */
	private int state;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHAR_STATE;
	}

	@Override
	protected final void writeImpl()
	{
        writeOpcode();
        writeInt(objectId);
        writeInt(subId);
        writeByte(state);
	}
}

